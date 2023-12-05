package de.paulr.math.numeric;

import java.util.ArrayList;
import java.util.List;

public abstract class AVector implements IColumnVector, IRowVector {

	public AVector transpose() {
		return this;
	}

	public int getWidth() {
		return getHeight();
	}

	@Override
	public List<Double> toList() {
		ArrayList<Double> result = new ArrayList<>(getHeight());
		for (int i = 0; i < getHeight(); i++) {
			result.add(get(i));
		}
		return result;
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
