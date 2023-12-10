package de.paulr.util;

public final class RopeLeaf<T> extends Rope<T> {

	private final T value;

	public RopeLeaf(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	@Override
	public int size() {
		return 1;
	}

}
