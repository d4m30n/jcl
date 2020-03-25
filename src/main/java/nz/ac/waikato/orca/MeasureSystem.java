package nz.ac.waikato.orca;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

public class MeasureSystem implements MeasureInterface {

	private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
			.getOperatingSystemMXBean();
	private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	private double _currentCPU = 0;// Holds the last current CPU measured by the system
	private double _currentMemory = 0;// Holds the last current Memory measured by the system
	private Double _setpointCPU = null;// Holds the desired cpu setpoint
	private Double _setpointMemory = null;// Holds the desired memory setpoint
	private double _cpuChange;
	private double _memoryChange;
	private long _measureIntervalInMillis;// Holds the interval to measure the system at in milliseconds
	private final double _percentageForCores;

	/**
	 * 
	 * @param MeasureInterval
	 * @param timeFormat
	 * @param cpuSetpoint
	 * @param memorySetpoint
	 * @throws Exception
	 */
	public MeasureSystem(long MeasureInterval, TimeUnit timeFormat, Double cpuSetpoint, Double memorySetpoint)
			throws Exception {
		if (timeFormat == null)
			throw new Exception("Invalid time format");
		_measureIntervalInMillis = timeFormat.toMillis(MeasureInterval);
		_setpointCPU = cpuSetpoint;
		_setpointMemory = memorySetpoint;
		_percentageForCores = 100.0 * operatingSystemMXBean.getAvailableProcessors();
	}

	public MeasureSystem(Double cpuSetpoint, Double memorySetpoint) {
		this();
		_setpointCPU = cpuSetpoint;
		if (_setpointCPU == null) {
			_cpuChange = 1d;
		} else {
			_cpuChange = cpuSetpoint;
		}
		_setpointMemory = memorySetpoint;
		if (_setpointMemory == null) {
			_memoryChange = 1d;
		} else {
			_memoryChange = memorySetpoint;
		}
	}

	public MeasureSystem() {
		_measureIntervalInMillis = TimeUnit.SECONDS.toMillis(1);
		_percentageForCores = 100.0 * operatingSystemMXBean.getAvailableProcessors();
	}

	public static int NUMBEROFMEASUREMENTVALUES = 2;

	public enum MeasureValues {
		CPU, MEMORY;
	}

	private boolean firstPrint = true;

	@Override
	public void print(long currentRuntimeInMillis, ParameterInterface<?>[] parameters) {
		if (firstPrint) {
			firstPrint = false;
			System.out.printf("CPU,MEMORY");
			if (parameters != null) {
				for (ParameterInterface<?> p : parameters) {
					System.out.printf(",");
					System.out.printf("%s", p.getName());
				}
			}
			if (_setpointCPU != null) {
				System.out.printf(",set CPU");
				System.out.printf(",Change CPU");
			}
			if (_setpointMemory != null) {
				System.out.printf(",set Memory");
				System.out.printf(", Change Memory");
			}
			System.out.printf(",time%n");
		}
		long currentRuntimeInSec = TimeUnit.MILLISECONDS.toSeconds(currentRuntimeInMillis);
		System.out.printf("%.2f,%.2f", _currentCPU, _currentMemory);
		if (parameters != null) {
			for (ParameterInterface<?> p : parameters) {
				System.out.printf(",");
				p.printValue();
			}
		}
		if (_setpointCPU != null) {
			System.out.printf(",%.2f", _setpointCPU);
			System.out.printf(",%.2f", _cpuChange);
		}
		if (_setpointMemory != null) {
			System.out.printf(",%.2f", _setpointMemory);
			System.out.printf(",%.2f", _memoryChange);
		}
		System.out.printf(",%d%n", currentRuntimeInSec);
	}

	@Override
	public void measure() {
		_currentCPU = measureCPU();
		_currentMemory = measureMemory();
	}

	@Override
	public long getMeasureIntervalInMillis() {
		return _measureIntervalInMillis;
	}

	@Override
	public long getMeasureIntervalInSec() {
		return TimeUnit.MILLISECONDS.toSeconds(_measureIntervalInMillis);
	}

	/**
	 * Measures the amount of CPU used by the JVM
	 * 
	 * @return - The percentage of CPU used by the JVM
	 */
	private double measureCPU() {
		return operatingSystemMXBean.getProcessCpuLoad() * (_percentageForCores);
	}

	/**
	 * Measures the total amount of memory used by the JVM
	 * 
	 * @return - The percentage of memory used by the JVM
	 */
	private double measureMemory() {
		long totalAvalableMemory = operatingSystemMXBean.getTotalPhysicalMemorySize()
				+ operatingSystemMXBean.getTotalSwapSpaceSize();
		long totalFreeMemory = operatingSystemMXBean.getFreePhysicalMemorySize()
				+ operatingSystemMXBean.getFreeSwapSpaceSize();
		long totalUsedMemory = totalAvalableMemory - totalFreeMemory;
		return 100.0 * totalUsedMemory / totalAvalableMemory;
	}

	@Override
	public double[] getMeasurements() {
		double[] returnValues = { _currentCPU, _currentMemory };
		return returnValues;
	}

	double[] perror = new double[2];
	double[] integral = new double[2];
	double[][] kvalue = { { 0.007, 0.007, 0.007 }, { 0.005, 0.005, 0.005 } };

	private double PIDSetpoint(double setpoint, double current, int place, double dt, long timeInSeconds) {
		double kp = kvalue[place][0];// * Math.pow((1 - 0.10), timeInSeconds);
		double ki = kvalue[place][1];// * Math.pow((1 - 0.20), timeInSeconds);
		double kd = kvalue[place][2];// * Math.pow((1 - 0.15), timeInSeconds);
		double error = setpoint - current;
		integral[place] = integral[place] + error * dt;
		double der = (error - perror[place]) / dt;
		double output = (kp * error) + (ki * integral[place]) + (kd * der);
		perror[place] = error;
		return output;
	}

	private boolean RunPID = true;
	private boolean UsePID = true;

	double[] advradge = { 0, 0 };
	double[] numValues = { 0, 0 };

	public Double[] getRawSetpoints() {
		Double[] returnValues = { _setpointCPU, _setpointMemory };
		return returnValues;
	}

	@Override
	public Double[] getSetpoints(int numberOfControlUpdates, long timeInSeconds) {
		if (_setpointCPU == null) {
			_setpointCPU = 1d;
		}
		if (_setpointMemory == null) {
			_setpointMemory = 1d;
		}
		if (UsePID == true) {
			if (numValues[0] >= 5) {
				advradge[0] = 0;
				advradge[1] = 0;
				numValues[0] = 0;
				numValues[1] = 0;
			}
			advradge[0] += _currentCPU;
			advradge[1] += _currentMemory;
			numValues[0]++;
			numValues[1]++;
			double ACPU = advradge[0] / numValues[0];
			double AMEM = advradge[1] / numValues[1];
			// System.out.println("ACPU:" + ACPU + "\tAMEM:" + AMEM);
			if (ACPU >= _setpointCPU || AMEM >= _setpointMemory) {
				RunPID = false;
				_cpuChange -= 1;
				_memoryChange -= 1;
			} else {
				RunPID = true;
			}
			if (RunPID) {
				double dt = TimeUnit.MILLISECONDS.toSeconds(getMeasureIntervalInMillis()) / numberOfControlUpdates;
				_cpuChange = _cpuChange + PIDSetpoint(_setpointCPU, _currentCPU, 0, dt, timeInSeconds);
				_memoryChange = _memoryChange + PIDSetpoint(_setpointMemory, _currentMemory, 1, dt, timeInSeconds);
			}
		}
		if (_cpuChange < 1)
			_cpuChange = 1;
		if (_memoryChange < 1)
			_memoryChange = 1;
		double rcpu = ModelLQR.encodeMeasurement(_cpuChange, ModelLQR.CPU);
		double rmemory = ModelLQR.encodeMeasurement(_memoryChange, ModelLQR.MEMORY);
		Double[] returnValues = { rcpu, rmemory };
		return returnValues;
	}

}
