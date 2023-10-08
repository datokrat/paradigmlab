package de.paulr.math.numeric;

public interface IRandomAccessMatrix extends IMatrix {

	double get(int y, int x);

	IRandomAccessMatrix transpose();

	default int getBandLength(int deviationFromDiagonal) {
		if (deviationFromDiagonal < 0) {
			return Math.max(0, Math.min(getHeight(), getWidth() - deviationFromDiagonal));
		} else {
			return Math.max(0, Math.min(getHeight() - deviationFromDiagonal, getWidth()));
		}
	}

	default IColumnVector getColumn(int j) {
		return new DefaultMatrixColumn(this, j);
	}

	default IColumnVector times(IColumnVector vector) {
		assert vector.getHeight() == getWidth();
		double[] result = new double[getHeight()];
		for (int j = 0; j < getWidth(); j++) {
			double factor = vector.get(j);
			for (int i = 0; i < getHeight(); i++) {
				result[i] += factor * get(i, j);
			}
		}
		return new DenseVector(result);
	}

	public static class DefaultMatrixColumn extends AVector {

		private IRandomAccessMatrix matrix;
		private int j;

		public DefaultMatrixColumn(IRandomAccessMatrix matrix, int j) {
			this.matrix = matrix;
			this.j = j;
		}

		@Override
		public int getHeight() {
			return matrix.getHeight();
		}

		@Override
		public int getWidth() {
			return matrix.getHeight(); // this is always a column in the matrix
		}

		@Override
		public double get(int i) {
			return matrix.get(i, j);
		}

		@Override
		public DefaultMatrixColumn transpose() {
			return this;
		}

	}

}
