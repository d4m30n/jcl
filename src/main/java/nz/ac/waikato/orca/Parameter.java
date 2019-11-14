package nz.ac.waikato.orca;

public abstract class Parameter<T> implements ParameterInterface<T> {
	private static int nextID = 0;

	protected double _maxValue = Double.MAX_VALUE;
	protected double _minValue = Double.MIN_VALUE;
	public final String NAME;
	public final int ID;

	/**
	 * Sets the max and the min values this parameter can hold
	 * 
	 * @param maxValue The maximum value this parameter can hold
	 * @param minValue The minimum value this parameter can hold
	 */
	protected Parameter(Double maxValue, Double minValue, String name) {
		this(name);
		if (maxValue == null)
			_maxValue = Double.MAX_VALUE;
		else
			_maxValue = maxValue;
		if (minValue == null)
			_minValue = Double.MIN_VALUE;
		else
			_minValue = minValue;
	}

	/**
	 * Keeps the min and max to there default values
	 */
	protected Parameter(String name) {
		this.NAME = name;
		this.ID = nextID;
		nextID++;
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Returns the maximum value that can be stored
	 * 
	 * @return The maximum value that can be stored
	 */
	@Override
	public double getMaxValue() {
		return _maxValue;
	}

	/**
	 * Returns the minimum value that can be stored
	 * 
	 * @return The minimum value that can be stored
	 */
	@Override
	public double getMinValue() {
		return _minValue;
	}

	@Override
	public int getID() {
		return ID;
	}
}