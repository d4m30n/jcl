package jcl;

public interface ControllerInterface {
	/**
	 * 
	 * @param parameters
	 * @param measurements
	 * @param setpoints
	 * @return - True if the controller could be evaluated
	 */
	boolean evaluate(Parameter[] parameters, double[] measurements, Double[] setpoints);
}