package de.paulr.math.numeric;

public interface IRowVector {

	int getWidth();

	double get(int j);

	IColumnVector transpose();

}
