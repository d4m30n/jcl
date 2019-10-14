package jcl;

import java.util.concurrent.TimeUnit;

public class Controller {
	/**
	 * Handles the measure and control objects
	 */
	private ControllerInterface _ControllerInterface;
	private MeasureInterface _MeasureInterface;
	private Parameter[] _parameters;

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
	// Used to calculate the total running time of controller
	private long _systemStartTime;
	// Holds the thread for the timer
	private Thread _sleepTimer;

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
			Parameter[] parameters, long runtime, TimeUnit timeMeasurement) throws Exception {
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
		if (controllerInterface instanceof ControllerNULL) {
			this._parameters = null;
		} else {
			this._parameters = parameters;
		}
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
			Parameter[] parameters) throws Exception {
		this(controllerInterface, measureInterface, parameters, -1, null);
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
		_printOutput = printOutput;
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

	public void stop() {
		_isRunning = false;
		if (_sleepTimer == null)
			return;
		else
			_sleepTimer.interrupt();
	}

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
			_ControllerInterface.evaluate(_parameters, _MeasureInterface.getMeasurements(),
					_MeasureInterface.getSetpoints());
			if (_printOutput) {
				long currentRuntime = System.currentTimeMillis() - _systemStartTime;
				_MeasureInterface.print(currentRuntime);
			}
			stopTime = System.currentTimeMillis();
			long pauseTime = _MeasureInterface.getMeasureIntervalInMillis() - (stopTime - startTime);
			if (pauseTime > 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(pauseTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Starts a timer to end the controller once a set time is done if runtime is -1
	 * no timer is set
	 */
	private void startTimer() {
		_isRunning = true;
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