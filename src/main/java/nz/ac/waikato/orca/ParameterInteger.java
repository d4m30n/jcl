package nz.ac.waikato.orca;

public class ParameterInteger extends Parameter<Integer> {
	// Required veriables to hold the weight and parameter
	private Integer _parameter;
	private int _weight = 1;

	// Allows user to define if the weight can be updated
	private boolean _allowWeightUpdate;

	public ParameterInteger(int initalParameter, Integer maxValue, Integer minValue, String name) {
		super((maxValue != null) ? maxValue.doubleValue() : null, (minValue != null) ? minValue.doubleValue() : null,
				name);
		_parameter = initalParameter;
		_weight = 1;
		_allowWeightUpdate = true;
	}

	public ParameterInteger(int initalParameter, String name) {
		super(name);
		_parameter = initalParameter;
		_weight = 1;
		_allowWeightUpdate = true;
	}

	@Override
	public double getDouble() {
		return _parameter.doubleValue();
	}

	@Override
	public Integer get() {
		return _parameter;
	}

	@Override
	public boolean set(Double newParameter) {
		if (newParameter > Integer.MAX_VALUE) {
			_parameter = Integer.MAX_VALUE;
			return false;
		} else if (newParameter < Integer.MIN_VALUE) {
			_parameter = Integer.MIN_VALUE;
			return false;
		}
		_parameter = (int) Math.round(newParameter);
		return true;
	}

	@Override
	public int getWeight() {
		return _weight;
	}

	@Override
	public boolean setWeight(Weights newWeight) {
		if (!_allowWeightUpdate)
			return false;
		_weight = newWeight.get();
		return true;
	}

	@Override
	public void printValue() {
		System.out.printf("%d", _parameter);
	}
}