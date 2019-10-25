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

	public MeasureSystem(long threadID) {
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

	@Override
	public Double[] getSetpoints() {
		Double[] returnValues = { _setpointCPU, _setpointMemory };
		return returnValues;
	}

}