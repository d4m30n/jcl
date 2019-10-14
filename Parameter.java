package jcl;

public abstract class Parameter {
	private static int nextID = 0;

	private double _maxValue = Double.MAX_VALUE;
	private double _minValue = Double.MIN_VALUE;
	public final String NAME;
	public final int ID;

	/**
	 * Sets the max and the min values this parameter can hold
	 * 
	 * @param maxValue The maximum value this parameter can hold
	 * @param minValue The minimum value this parameter can hold
	 */
	public Parameter(double maxValue, double minValue, String name) {
		this(name);
		_maxValue = maxValue;
		_minValue = minValue;
	}

	/**
	 * Allows setting only one min or max value keeping the other default
	 * 
	 * @param value      The min or max value you want this parameter to hold
	 * @param isMaxValue true if the value is the max or false if its the min
	 */
	public Parameter(double value, boolean isMaxValue, String name) {
		this(name);
		if (isMaxValue) {
			_maxValue = value;
			_minValue = Double.MIN_VALUE;
		} else {
			_maxValue = Double.MAX_VALUE;
			_minValue = Double.MIN_VALUE;
		}
	}

	/**
	 * Keeps the min and max to there default values
	 */
	public Parameter(String name) {
		this.NAME = name;
		this.ID = nextID;
		nextID++;
	}

	public abstract Double get();

	public abstract boolean set(Double newParameter);

	public abstract int getWeight();

	/**
	 * Allows the controller to ignore or invert the value used for this parameter
	 * 
	 * @param newWeight The weight you want to use must be between -1 and 1
	 * @return returns wether or not the value can be set or not
	 * @throws Exception throws exception if the newWeight is greater than 1 or less
	 *                   than -1
	 */
	public boolean setWeight(int newWeight) throws Exception {
		if (newWeight > 1 || newWeight < -1)
			throw new Exception("The weight must be between -1 and 1");
		return _setWeight(newWeight);
	}

	protected abstract boolean _setWeight(int newWeight);

	/**
	 * Returns the maximum value that can be stored
	 * 
	 * @return The maximum value that can be stored
	 */
	public double getMaxValue() {
		return _maxValue;
	}

	/**
	 * Returns the minimum value that can be stored
	 * 
	 * @return The minimum value that can be stored
	 */
	public double getMinValue() {
		return _minValue;
	}
}