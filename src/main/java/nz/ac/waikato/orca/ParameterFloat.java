package nz.ac.waikato.orca;

public class ParameterFloat extends Parameter<Float> {

	private Float _parameter;
	private int _weight;

	public ParameterFloat(float initalParameter, Integer maxValue, Integer minValue, String name) {
		super(maxValue.doubleValue(), minValue.doubleValue(), name);
		_parameter = initalParameter;
		_weight = 1;
	}

	public ParameterFloat(float initalParameter, String name) {
		super(name);
		_parameter = initalParameter;
		_weight = 1;
	}

	@Override
	public double getDouble() {
		return _parameter.doubleValue();
	}

	@Override
	public Float get() {
		return _parameter;
	}

	@Override
	public boolean set(Double newParameter) {
		_parameter = newParameter.floatValue();
		return true;
	}

	@Override
	public int getWeight() {
		return _weight;
	}

	@Override
	public boolean setWeight(Weights newWeight) {
		_weight = newWeight.get();
		return true;
	}

	@Override
	public void printValue() {
		System.out.printf("%f", _parameter);
	}

}