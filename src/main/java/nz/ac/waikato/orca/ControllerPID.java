package nz.ac.waikato.orca;

public class ControllerPID extends ControllerHead {

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints) {
		if (areAllSetpointsNull(setpoints))
			return false;
		return false;
	}

	@Override
	public double[] get() {
		// TODO Auto-generated method stub
		return null;
	}

}