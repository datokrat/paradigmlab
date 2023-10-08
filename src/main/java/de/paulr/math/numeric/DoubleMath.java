package de.paulr.math.numeric;

public final class DoubleMath {

	public static final double EPSILON = 1e-9;

	private DoubleMath() {
	}

	public static boolean equal(double x, double y) {
		return Math.abs(x - y) <= EPSILON;
	}

	public static boolean zero(double x) {
		return Math.abs(x) <= EPSILON;
	}

	public static double euclideanNorm(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

}
