package de.paulr.math.numeric;

public abstract class AMutableVector extends AVector
	implements IMutableColumnVector, IMutableRowVector {

	@Override
	public AMutableVector transpose() {
		return this;
	}

}
