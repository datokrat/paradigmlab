package de.paulr.math.numeric;

public interface IColumnVector {

	int getHeight();

	double get(int i);

	IRowVector transpose();

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
		double result = 0;
		for (int i = 0; i < getHeight(); i++) {
			result += get(i) * other.get(i);
		}
		return result;
	}

}
