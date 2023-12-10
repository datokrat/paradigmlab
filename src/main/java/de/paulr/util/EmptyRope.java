package de.paulr.util;

public final class EmptyRope<T> extends Rope<T> {

	@Override
	public int size() {
		return 0;
	}
}
