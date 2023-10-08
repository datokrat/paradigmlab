package de.paulr.math.numeric;

public interface IMatrix {

	int getHeight();

	int getWidth();

	IColumnVector times(IColumnVector vector);

}
