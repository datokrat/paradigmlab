package de.paulr.math.numeric;

public interface IMutableColumnVector extends IColumnVector {

	void set(int i, double value);

	default void setAllInColumn(double... values) {
		for (int j = 0; j < getHeight(); j++) {
			set(j, values[j]);
		}
	}

	IMutableRowVector transpose();

	default void multiplyBy(double factor) {
		for (int i = 0; i < getHeight(); i++) {
			set(i, get(i) * factor);
		}
	}

}
