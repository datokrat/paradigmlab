package de.paulr.math.numeric;

public class GivensRotation {

	private final int i;
	private final int j;
	private final double c;
	private final double s;

	public GivensRotation(int i, int j, double c, double s) {
		this.i = i;
		this.j = j;
		this.c = c;
		this.s = s;
	}

	public static GivensRotation zeroing(IColumnVector vector, int indexToZero, int indexToUse) {
		return zeroing(vector.get(indexToZero), vector.get(indexToUse), indexToZero, indexToZero);
	}

	public static GivensRotation zeroing(double az, double au, int indexToZero, int indexToUse) {
		if (DoubleMath.zero(az)) {
			return nop(indexToZero, indexToUse);
		}

		if (DoubleMath.zero(au)) {
			return flip(indexToZero, indexToUse);
		}

		double rho = Math.signum(au) * DoubleMath.euclideanNorm(au, az);
		assert !DoubleMath.zero(rho);
		double c = au / rho;
		double s = az / rho;

		return new GivensRotation(indexToZero, indexToUse, c, s);
	}

	public static GivensRotation nop(int i, int j) {
		return new GivensRotation(i, j, 1, 0);
	}

	public static GivensRotation flip(int i, int j) {
		return new GivensRotation(i, j, 0, 1);
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	public double getC() {
		return c;
	}

	public double getS() {
		return s;
	}

	public void apply(IColumnVector source, IMutableColumnVector target) {
		apply(source.get(i), source.get(j), target);
	}

	public void apply(double ai, double aj, IMutableColumnVector target) {
		target.set(i, c * ai - s * aj);
		target.set(j, s * ai + c * aj);
	}

	public void applyInversely(IColumnVector source, IMutableColumnVector target) {
		applyInversely(source.get(i), source.get(j), target);
	}

	public void applyInversely(double ai, double aj, IMutableColumnVector target) {
		target.set(i, s * aj + c * ai);
		target.set(j, c * aj - s * ai);
	}

	public GivensRotation inverse() {
		return new GivensRotation(i, j, c, -s);
	}

}
