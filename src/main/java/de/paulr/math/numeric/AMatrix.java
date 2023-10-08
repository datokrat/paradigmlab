package de.paulr.math.numeric;

public abstract class AMatrix implements IRandomAccessMatrix {

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof IRandomAccessMatrix matrix)) {
			return false;
		}

		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				if (!DoubleMath.equal(get(i, j), matrix.get(i, j))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public String toString() {
		String result = "";

		for (int i = 0; i < getHeight(); i++) {
			for (int j = 0; j < getWidth(); j++) {
				result += get(i, j) + " ";
			}
		}

		return result;
	}

}
