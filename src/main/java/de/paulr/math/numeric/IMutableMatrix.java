package de.paulr.math.numeric;

public interface IMutableMatrix extends IRandomAccessMatrix {

	void set(int i, int j, double value);

	default IMutableColumnVector getColumn(int j) {
		return new DefaultMutableMatrixColumn(this, j);
	}

	default IMutableRowVector getRow(int i) {
		return transpose().getColumn(i).transpose(); // TODO think about mutability?
	}

	default IMutableMatrix transpose() {
		return new DefaultTransposedMutableMatrix(this);
	}

	static class DefaultMutableMatrixColumn extends AVector
		implements IMutableColumnVector, IMutableRowVector {

		private IMutableMatrix matrix;
		private int j;

		public DefaultMutableMatrixColumn(IMutableMatrix matrix, int j) {
			this.matrix = matrix;
			this.j = j;
		}

		@Override
		public int getHeight() {
			return matrix.getHeight();
		}

		@Override
		public int getWidth() {
			return matrix.getHeight(); // the vector is always associated to a column of `matrix`
		}

		@Override
		public double get(int i) {
			return matrix.get(i, j);
		}

		@Override
		public void set(int i, double value) {
			matrix.set(i, j, value);
		}

		@Override
		public DefaultMutableMatrixColumn transpose() {
			return this;
		}

	}

	static class DefaultTransposedMutableMatrix extends AMatrix implements IMutableMatrix {

		private IMutableMatrix inner;

		public DefaultTransposedMutableMatrix(IMutableMatrix inner) {
			this.inner = inner;
		}

		@Override
		public int getHeight() {
			return inner.getWidth();
		}

		@Override
		public int getWidth() {
			return inner.getHeight();
		}

		@Override
		public double get(int y, int x) {
			return inner.get(x, y);
		}

		@Override
		public void set(int i, int j, double value) {
			inner.set(j, i, value);
		}

	}

}
