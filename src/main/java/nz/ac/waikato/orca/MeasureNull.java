package nz.ac.waikato.orca;

import java.util.concurrent.TimeUnit;

/**
 * Used as the null option when no measurement is required for the controller
 */
public class MeasureNull implements MeasureInterface {

	long _measureIntervalInMillis;

	public MeasureNull(long MeasureInterval, TimeUnit timeFormat) throws Exception {
		if (timeFormat == null)
			throw new Exception("Invalid time format");
		_measureIntervalInMillis = timeFormat.toMillis(MeasureInterval);
	}

	public MeasureNull() {
		_measureIntervalInMillis = TimeUnit.SECONDS.toMillis(1);
	}

	@Override
	public void print(long currentRuntime, ParameterInterface<?>[] parameters) {
		System.out.printf("Null Measure");
		return;
	}

	@Override
	public void measure() {
		return;
	}

	@Override
	public long getMeasureIntervalInMillis() {
		return _measureIntervalInMillis;
	}

	@Override
	public double[] getMeasurements() {
		return null;
	}

	@Override
	public Double[] getSetpoints(int numberOfControlUpdates, long timeInSeconds) {
		return null;
	}

}