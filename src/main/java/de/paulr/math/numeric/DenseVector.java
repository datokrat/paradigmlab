package de.paulr.math.numeric;

import java.util.Arrays;

public class DenseVector extends AMutableVector {

	private double[] components;

	public DenseVector(double... components) {
		this.components = components;
	}

	public static DenseVector zero(int size) {
		return new DenseVector(new double[size]);
	}

	@Override
	public int getHeight() {
		return components.length;
	}

	@Override
	public double get(int i) {
		return components[i];
	}

	@Override
	public void set(int i, double value) {
		components[i] = value;
	}

	@Override
	public String toString() {
		return "DenseVector [components=" + Arrays.toString(components) + "]";
	}

}
