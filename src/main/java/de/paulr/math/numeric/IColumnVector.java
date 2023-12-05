package de.paulr.math.numeric;

import java.util.List;

public interface IColumnVector {

	int getHeight();

	double get(int i);

	IRowVector transpose();

	List<Double> toList();

	default double norm() {
		double squaredNorm = 0;
		for (int i = 0; i < getHeight(); i++) {
			double value = get(i);
			squaredNorm += value * value;
		}
		return Math.sqrt(squaredNorm);
	}

	default double supDistanceTo(IColumnVector other) {
		double maximum = 0;
		for (int i = 0; i < getHeight(); i++) {
			maximum = Math.max(maximum, Math.abs(get(i) - other.get(i)));
		}
		return maximum;
	}

	default IColumnVector minus(IColumnVector other) {
		assert getHeight() == other.getHeight();
		IMutableColumnVector difference = new DenseVector(new double[getHeight()]);
		for (int i = 0; i < getHeight(); i++) {
			difference.set(i, get(i) - other.get(i));
		}
		return difference;
	}

	default IColumnVector plus(IColumnVector other) {
		assert getHeight() == other.getHeight();
		IMutableColumnVector sum = new DenseVector(new double[getHeight()]);
		for (int i = 0; i < getHeight(); i++) {
			sum.set(i, get(i) + other.get(i));
		}
		return sum;
	}

	default IColumnVector times(double factor) {
		IMutableColumnVector product = new DenseVector(new double[getHeight()]);
		for (int i = 0; i < getHeight(); i++) {
			product.set(i, get(i) * factor);
		}
		return product;
	}

	default IColumnVector dividedBy(double factor) {
		IMutableColumnVector product = new DenseVector(new double[getHeight()]);
		for (int i = 0; i < getHeight(); i++) {
			product.set(i, get(i) / factor);
		}
		return product;
	}

	default double dot(IColumnVector other) {
		double positive = 0;
		double negative = 0;
		for (int i = 0; i < getHeight(); i++) {
			double product = get(i) * other.get(i);
			if (product >= 0) {
				positive += product;
			} else {
				negative += product;
			}
		}
		return positive + negative;
	}

}
