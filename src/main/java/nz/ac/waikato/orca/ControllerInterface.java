package nz.ac.waikato.orca;

public interface ControllerInterface {
	/**
	 * 
	 * @param parameters
	 * @param measurements
	 * @param setpoints
	 * @return - True if the controller could be evaluated
	 */
	boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints);

	double[] get();
}