package jcl;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.sun.management.OperatingSystemMXBean;

public class MeasureSystem implements MeasureInterface {

	private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
			.getOperatingSystemMXBean();

	private double _currentCPU = 0;
	private double _currentMemory = 0;
	private Double _setpointCPU = null;
	private Double _setpointMemory = null;
	private long _measureIntervalInMillis;

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
	}

	public MeasureSystem() {
		_measureIntervalInMillis = TimeUnit.SECONDS.toMillis(1);
	}

	public static int NUMBEROFMEASUREMENTVALUES = 2;

	public enum MeasureValues {
		CPU, MEMORY;
	}

	@Override
	public void print(long currentRuntimeInMillis) {
		long currentRuntimeInSec = TimeUnit.MILLISECONDS.toSeconds(currentRuntimeInMillis);
		System.out.printf("%.2f\t%.2f\t%d%n", _currentCPU, _currentMemory, currentRuntimeInSec);
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
		return operatingSystemMXBean.getSystemCpuLoad();
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
		return (double) totalUsedMemory / totalAvalableMemory;
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