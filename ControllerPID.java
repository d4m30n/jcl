package jcl;

public class ControllerPID extends ControllerHead {

	@Override
	public boolean evaluate(Parameter[] parameters, double[] measurements, Double[] setpoints) {
		if (areAllSetpointsNull(setpoints))
			return false;
		return false;
	}

}