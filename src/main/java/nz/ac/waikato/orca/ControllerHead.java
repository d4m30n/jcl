package nz.ac.waikato.orca;

abstract class ControllerHead implements ControllerInterface {

	/**
	 * Decides if all the setpoints are null or not
	 * 
	 * @param setpoints - A list of all setpoints
	 * @return - True if setpoints are null or false otherwise
	 */
	protected boolean areAllSetpointsNull(Double[] setpoints) {
		int nullSetpoints = 0;
		for (Double s : setpoints) {
			// Stop looping as soon as a setpoint is not null
			if (s != null)
				break;
			nullSetpoints++;
		}
		// If the number of null setpoints are equal to the number of points there all
		// null
		if (nullSetpoints == setpoints.length)
			return true;
		else
			return false;
	}
}