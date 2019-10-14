package jcl;

public class ParameterInteger extends Parameter {
	// Required veriables to hold the weight and parameter
	private int _parameter;
	private int _weight;

	// Allows user to define if the weight can be updated
	private boolean _allowWeightUpdate;

	public ParameterInteger(int initalParameter, int initalWeight, boolean allowWeightUpdate, int maxValue,
			int minValue, String name) {
		super(maxValue, minValue, name);
		_parameter = initalParameter;
		_weight = initalWeight;
		_allowWeightUpdate = allowWeightUpdate;
	}

	public ParameterInteger(int initalParameter, int initalWeight, boolean allowWeightUpdate, String name) {
		super(name);
		_parameter = initalParameter;
		_weight = initalWeight;
		_allowWeightUpdate = allowWeightUpdate;
	}

	public ParameterInteger(int initalParameter, boolean allowWeightUpdate, int maxValue, int minValue, String name) {
		super(maxValue, minValue, name);
		_parameter = initalParameter;
		_weight = 1;
		_allowWeightUpdate = allowWeightUpdate;
	}

	public ParameterInteger(int initalParameter, boolean allowWeightUpdate, String name) {
		super(name);
		_parameter = initalParameter;
		_weight = 1;
		_allowWeightUpdate = allowWeightUpdate;
	}

	@Override
	public Double get() {
		return (double) _parameter;
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
	protected boolean _setWeight(int newWeight) {
		if (!_allowWeightUpdate)
			return false;
		_weight = newWeight;
		return true;
	}
}