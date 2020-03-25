package nz.ac.waikato.orca;

public interface ControllerInterface {

	public enum controllerType {
		NULL("null"), PID("pid"), LQR("lqr");

		private String stringName;

		private controllerType(String stringName) {
			this.stringName = stringName;
		}

		public String get() {
			return stringName;
		}
	}

	/**
	 * 
	 * @param parameters
	 * @param measurements
	 * @param setpoints
	 * @return - True if the controller could be evaluated
	 */
	boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints, long dtSec);

	double[] get();
}