package de.paulr.math.numeric;

public interface IMutableRowVector extends IRowVector {

	void set(int j, double value);

	default void setAll(double... values) {
		for (int j = 0; j < getWidth(); j++) {
			set(j, values[j]);
		}
	}

	IMutableColumnVector transpose();

}
