package nz.ac.waikato.orca;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import nz.ac.waikato.orca.ml.Values;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;

public class Controller {
	/**
	 * Handles the measure and control objects
	 */
	private ControllerInterface _ControllerInterface;
	private MeasureInterface _MeasureInterface;
	private ParameterInterface<?>[] _parameters;

	/**
	 * Handles the parameters required by the thread
	 */
	// Holds the total runtime in milliseconds
	private long _runtimeInMillisec;
	// Holds the thread the is running the controller
	private Thread _runningThread;
	// Holds an instance to itself
	private final Controller me = this;
	// boolean that indecates weather the controller is running
	// Also used to tell the controller to stop
	private boolean _isRunning = false;
	private boolean _printOutput = false;
	private int _skipPrintOutput = 0;
	private final int _seed;
	// Used to calculate the total running time of controller
	private long _systemStartTime;
	// Holds the thread for the timer
	private Thread _sleepTimer;

	private int numberOfControlUpdates = 1;

	/**
	 * 
	 * @param controllerInterface - The controller that will be used to control the
	 *                            parameters or ControllerNull if no controller is
	 *                            used
	 * @param measureInterface    - The measurement class that is used to measure
	 *                            what is needed for the controller or MeasureNull
	 *                            if no measurement is needed
	 * @param parameters          - An arraylist of parameters that are used to
	 *                            control the program
	 * @param runtime             - The amount of time that the controller will run
	 *                            for or -1 if there is no runtime needed
	 * @param timeMeasurement     - The measurement that the runtime is given in can
	 *                            be null if -1 is used for the runtime
	 * @throws Exception - Throws an exception if any of the conditions for the
	 *                   parameters are not met
	 */
	public Controller(ControllerInterface controllerInterface, MeasureInterface measureInterface,
			ParameterInterface<?>[] parameters, long runtime, TimeUnit timeMeasurement, int seed) throws Exception {
		this._seed = seed;
		// Checks for a null value given for the controller
		if (controllerInterface == null)
			controllerInterface = new ControllerNULL();
		// Validates if null is allowed for timeMeasurement based on if runtime is less
		// than 0
		if (runtime > 0 && timeMeasurement == null)
			throw new Exception("A unit must be provided for the runtime");
		// Validates if null is valid for the parameter list based on if the null
		// controller is used
		if (parameters == null && !(controllerInterface instanceof ControllerNULL)) {
			throw new Exception("Invalid setup parameters can only be null if ControllerNull is used");
		}
		// If null is given for measurement then set it to the null measure class
		if (measureInterface == null)
			measureInterface = new MeasureNull();
		this._ControllerInterface = controllerInterface;
		this._MeasureInterface = measureInterface;
		// If the controller is null then set the rest of the parameters to null as well
		// else use the parameters given
		this._parameters = parameters;
		// The other classes only look for -1 in the runtime so if it is less than 0
		// just set to -1 otherwise use the time measurement to convert the time given
		// into milliseconds
		if (runtime < 0)
			this._runtimeInMillisec = -1;
		else
			this._runtimeInMillisec = timeMeasurement.toMillis(runtime);
	}

	/**
	 * 
	 * @param controllerInterface - The controller that will be used to control the
	 *                            parameters or ControllerNull if no controller is
	 *                            used
	 * @param measureInterface    - The measurement class that is used to measure
	 *                            what is needed for the controller or MeasureNull
	 *                            if no measurement is needed
	 * @param parameters          - An arraylist of parameters that are used to
	 *                            control the program
	 * @throws Exception - Throws an exception if any of the conditions for the
	 *                   parameters are not met
	 */
	public Controller(ControllerInterface controllerInterface, MeasureInterface measureInterface,
			ParameterInterface<?>[] parameters, int seed) throws Exception {
		this(controllerInterface, measureInterface, parameters, -1, null, seed);
	}

	public boolean isRunning() {
		return _isRunning;
	}

	public int getNumberOfControlUpdates() {
		return numberOfControlUpdates;
	}

	public void setNumberOfControlUpdates(int newControlUpdates) {
		if (!_isRunning && newControlUpdates > 0) {
			numberOfControlUpdates = newControlUpdates;
		}
	}

	/**
	 * Creates a thread and starts the controller
	 * 
	 * @param printOutput - Boolean to weather or not the output of measure should
	 *                    be printed
	 * @throws Exception - If the controller was unable to be started
	 */
	public void start(boolean printOutput) throws Exception {
		if (_isRunning)
			throw new Exception("Unable to start thread while one is already running");
		_skipPrintOutput = 0;
		startRunningThread(printOutput);
	}

	private void startRunningThread(boolean printOutput) throws Exception {
		_printOutput = printOutput;
		_isRunning = true;
		_runningThread = new Thread(new Runnable() {

			@Override
			public void run() {
				me.run();
			}
		});
		_runningThread.start();
		if (!_runningThread.isAlive())
			throw new Exception("Unable to start thread");
	}

	/**
	 * Create a thread and starts the controller
	 * 
	 * @param printOutput     - Boolean indecating weather or not the output
	 *                        measured should be printed to terminal
	 * @param skipPrintOutput - Integer indecating the number of measures to skip
	 *                        printing
	 * @throws Exception - Throws an exception if there is a thread already running
	 *                   or was unable to start.
	 */
	public void start(boolean printOutput, int skipPrintOutput) throws Exception {
		if (_isRunning)
			throw new Exception("Unable to start thread while one is already running");
		if (skipPrintOutput < 0)
			skipPrintOutput = 0;
		_skipPrintOutput = skipPrintOutput;
		startRunningThread(printOutput);
	}

	public void stop() {
		_isRunning = false;
		if (_sleepTimer == null)
			return;
		else
			_sleepTimer.interrupt();
	}

	private static final int STABLETIME = 60;
	private static final int VALUECOLLECT = STABLETIME;
	private static final int EVALUEATEB = 10 * VALUECOLLECT;
	private ArrayList<Values> MeasuredValues = new ArrayList<>();

	private int[][] sampleData = { { 0, 0, 0, 0, 0 }, { 4676, 5, 59, 3, 1 }, { 410, 3321, 62, 5, 5 },
			{ 3517, 1732, 70, 5, 4 }, { 1685, 2184, 2, 5, 3 }, { 1798, 1846, 93, 5, 1 }, { 20, 1759, 2, 2, 1 },
			{ 2999, 9804, 14, 2, 2 }, { 2637, 7931, 11, 4, 2 }, { 5614, 2804, 12, 2, 7 }, { 506, 5802, 43, 2, 1 },
			{ 4666, 1021, 94, 1, 3 }, { 251, 3314, 17, 2, 7 }, { 395, 1985, 82, 1, 3 }, { 7300, 4670, 79, 2, 2 },
			{ 7036, 3795, 92, 1, 3 }, { 15, 30, 66, 1, 1 }, { 9423, 346, 19, 3, 5 }, { 501, 1676, 50, 3, 1 },
			{ 800, 4563, 88, 4, 5 }, { 7814, 4286, 68, 3, 6 }, { 4179, 1885, 97, 1, 4 }, { 718, 8669, 52, 4, 3 },
			{ 1198, 8118, 71, 1, 4 }, { 5386, 7899, 58, 5, 4 }, { 899, 100, 39, 1, 7 }, { 3168, 3825, 49, 2, 1 },
			{ 7187, 6281, 69, 5, 4 }, { 9, 5, 67, 2, 3 }, { 8744, 2897, 11, 2, 5 }, { 1949, 8564, 93, 3, 6 },
			{ 9724, 3258, 49, 3, 5 }, { 1672, 681, 58, 3, 4 }, { 8352, 160, 12, 1, 6 }, { 267, 90, 98, 5, 4 },
			{ 13, 187, 93, 2, 3 }, { 4752, 5812, 9, 4, 1 }, { 3215, 9895, 39, 1, 3 }, { 7650, 8193, 74, 3, 6 },
			{ 4268, 10, 70, 3, 5 }, { 7844, 6292, 43, 1, 2 }, { 7236, 5809, 14, 4, 6 }, { 3135, 6305, 54, 5, 2 },
			{ 6876, 5174, 96, 5, 5 }, { 6220, 7416, 2, 5, 5 }, { 7891, 5484, 22, 4, 1 }, { 190, 3585, 80, 3, 3 },
			{ 6569, 632, 46, 5, 5 }, { 9747, 5935, 89, 1, 3 }, { 20, 20, 45, 5, 6 } };

	private int NextCollect = VALUECOLLECT;
	private int NextEvaluate = EVALUEATEB;
	private int NextSample = 0;
	private boolean training = false;

	private void ValuesCollect(long TimeInMilliseconds) {
		long TimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(TimeInMilliseconds);
		if (TimeInSeconds >= NextCollect) {
			NextCollect += VALUECOLLECT;
			double CPU = _MeasureInterface.getMeasurements()[0];
			double Memory = _MeasureInterface.getMeasurements()[1];
			int Hash = (int) _parameters[0].get();
			int Sleep = (int) _parameters[1].get();
			int Buttons = (int) _parameters[2].get();
			int Depth = (int) _parameters[3].get();
			int Breadth = (int) _parameters[4].get();
			Values MValue = new Values(CPU, Memory, Hash, Sleep, Buttons, Depth, Breadth);
			boolean done = false;
			try {
				for (Values values : MeasuredValues) {
					if (values.CompareTo(MValue)) {
						double[] Tmp = { CPU, Memory };
						values.AddToValues(Tmp);
						done = true;
						break;
					}
				}
				if (done == false) {
					MeasuredValues.add(MValue);
				}
			} catch (Exception e) {
				System.out.print(e);
				System.exit(-42);
			}
			if (training) {
				_parameters[0].set((double) sampleData[NextSample][0]);
				_parameters[1].set((double) sampleData[NextSample][1]);
				_parameters[2].set((double) sampleData[NextSample][2]);
				_parameters[3].set((double) sampleData[NextSample][3]);
				_parameters[4].set((double) sampleData[NextSample][4]);
				NextSample++;
				if (NextSample == sampleData.length) {
					training = false;
				}
			}
		}
	}

	private void Evaluate(long TimeInMilliseconds) {
		long TimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(TimeInMilliseconds);
		if (TimeInSeconds >= NextEvaluate) {
			NextEvaluate += EVALUEATEB;
			Instances CPUInstances = new Instances("CPU", Values.GetArrtibutesList(Values.CPUType),
					Values.NumberOfAttributes);
			Instances MemoryInstances = new Instances("Memory", Values.GetArrtibutesList(Values.MemoryType),
					Values.NumberOfAttributes);
			CPUInstances.setClassIndex(0);
			MemoryInstances.setClassIndex(0);
			try {
				for (Values value : MeasuredValues) {
					CPUInstances.add(value.GetInstance(Values.CPUType));
					MemoryInstances.add(value.GetInstance(Values.MemoryType));
				}
				LinearRegression CPULR = new LinearRegression();
				LinearRegression MemoryLR = new LinearRegression();
				CPULR.buildClassifier(CPUInstances);
				MemoryLR.buildClassifier(MemoryInstances);
				double[][] newB = { { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 } };
				double[] CPUValues = CPULR.coefficients();
				double[] MemoryValues = MemoryLR.coefficients();
				for (int i = 1; i < CPUValues.length - 1; i++) {
					newB[0][i - 1] = CPUValues[i];
					newB[1][i - 1] = MemoryValues[i];
				}
				ModelLQR.UpdateIntercept(CPUValues[6], ModelLQR.CPU);
				ModelLQR.UpdateIntercept(MemoryValues[6], ModelLQR.MEMORY);
				((ControllerLQR) _ControllerInterface).UpdateB(newB);
			} catch (Exception e) {
				System.out.printf("Error:%s\n", e.toString());
				System.out.println(e);
				e.printStackTrace();
				System.exit(-42);
			}
		}
	}

	private static final int GCATEACHSECOND = 0;
	private int GCCount = 0;

	/**
	 * Handles all of the code that is used while the controller is running
	 */
	private void run() {
		startTimer();
		_systemStartTime = System.currentTimeMillis();
		long startTime;
		long stopTime;
		while (_isRunning) {
			startTime = System.currentTimeMillis();
			_MeasureInterface.measure();
			long currentRuntime = System.currentTimeMillis() - _systemStartTime;
			double[] measurements = _MeasureInterface.getMeasurements();
			Double[] setpoints;
			if (_ControllerInterface instanceof ControllerLQR) {
				setpoints = _MeasureInterface.getSetpoints(numberOfControlUpdates,
						TimeUnit.MILLISECONDS.toSeconds(currentRuntime));
			} else {
				setpoints = _MeasureInterface.getRawSetpoints();
			}
			if (training != true) {
				for (int i = 0; i < numberOfControlUpdates; i++) {
					_ControllerInterface.evaluate(_parameters, measurements, setpoints,
							_MeasureInterface.getMeasureIntervalInSec());
				}
			}
			if (_ControllerInterface instanceof ControllerLQR && training == true) {
				ValuesCollect(currentRuntime);
				Evaluate(currentRuntime);
			}
			if (_printOutput) {
				if (_skipPrintOutput <= 0) {
					_MeasureInterface.print(currentRuntime, _parameters);
				} else {
					_skipPrintOutput--;
				}
			}
			if (GCCount == GCATEACHSECOND) {
				System.gc();
				GCCount = 0;
			} else {
				GCCount++;
			}
			stopTime = System.currentTimeMillis();
			long runtime = (stopTime - startTime);
			long pauseTime = _MeasureInterface.getMeasureIntervalInMillis() - runtime;
			if (pauseTime > 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(pauseTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println("ERROR:" + e);
				}
			} else if (pauseTime < 0) {
				if (numberOfControlUpdates > 1) {
					numberOfControlUpdates--;
				}
			}
		}
		System.exit(0);
	}

	/**
	 * Starts a timer to end the controller once a set time is done if runtime is -1
	 * no timer is set
	 */
	private void startTimer() {
		// No timer is needed if -1 is set
		if (_runtimeInMillisec == -1)
			return;
		// Create new timer thread
		_sleepTimer = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Sleep time in milliseconds
					TimeUnit.MILLISECONDS.sleep(me._runtimeInMillisec);
					// set is running to false when time is up
					me._isRunning = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		_sleepTimer.start();
	}
}
