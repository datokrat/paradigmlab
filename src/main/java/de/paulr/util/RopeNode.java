package de.paulr.util;

public final class RopeNode<T> extends Rope<T> {

	private Rope<T> left;
	private Rope<T> right;

	public RopeNode(Rope<T> left, Rope<T> right) {
		this.left = left;
		this.right = right;
	}

	public Rope<T> getLeft() {
		return left;
	}

	public Rope<T> getRight() {
		return right;
	}

}
