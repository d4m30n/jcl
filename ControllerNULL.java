package jcl;

public class ControllerNULL extends ControllerHead {

	@Override
	public boolean evaluate(Parameter[] parameters, double[] measurements, Double[] setpoints) {
		return true;
	}
}