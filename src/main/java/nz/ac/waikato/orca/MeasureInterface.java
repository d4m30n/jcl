package nz.ac.waikato.orca;

interface MeasureInterface {

	/**
	 * Used to print the values to the terminal
	 * 
	 * @param currentRuntime - The current time the system has been running in
	 *                       milliseconds
	 */
	public void print(long currentRuntime, ParameterInterface<?>[] parameters);

	/**
	 * Used to measure the required values
	 */
	public void measure();

	/**
	 * Returns the interval at witch to measure
	 * 
	 * @return - The interval in milliseconds to measure
	 */
	public long getMeasureIntervalInMillis();

	/**
	 * 
	 * @return
	 */
	public double[] getMeasurements();

	/**
	 * 
	 * @return
	 */
	public Double[] getSetpoints();
}