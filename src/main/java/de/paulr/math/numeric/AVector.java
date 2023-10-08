package de.paulr.math.numeric;

public abstract class AVector implements IColumnVector, IRowVector {

	public AVector transpose() {
		return this;
	}

	public int getWidth() {
		return getHeight();
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof IColumnVector vector)) {
			return false;
		}

		if (getHeight() != vector.getHeight()) {
			return false;
		}

		for (int i = 0; i < getHeight(); i++) {
			if (!DoubleMath.equal(get(i), vector.get(i))) {
				return false;
			}
		}

		return true;
	}

}
