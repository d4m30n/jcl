package nz.ac.waikato.orca;

public class ControllerNULL extends ControllerHead {

	@Override
	public boolean evaluate(ParameterInterface<?>[] parameters, double[] measurements, Double[] setpoints, long dtSec) {
		return true;
	}

	@Override
	public double[] get() {
		return null;
	}
}