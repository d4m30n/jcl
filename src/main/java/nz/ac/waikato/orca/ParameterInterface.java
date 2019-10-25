package nz.ac.waikato.orca;

public interface ParameterInterface<T> {
	public enum Weights {
		POSITIVE(1), IGNORE(0), NEGATIVE(-1);

		private int value;

		private Weights(int value) {
			this.value = value;
		}

		protected int get() {
			return value;
		}
	}

	double getDouble();

	String getName();

	void printValue();

	T get();

	boolean set(Double newParameter);

	int getWeight();

	boolean setWeight(Weights newWeight);

	/**
	 * Returns the maximum value that can be stored
	 * 
	 * @return The maximum value that can be stored
	 */
	double getMaxValue();

	/**
	 * Returns the minimum value that can be stored
	 * 
	 * @return The minimum value that can be stored
	 */
	double getMinValue();

	int getID();
}