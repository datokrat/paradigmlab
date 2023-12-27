package de.paulr.aoc2023.day24;

import java.math.BigInteger;

public class MatrixInversionTableau {

	private IntegralMatrix left;
	private IntegralMatrix right;

	public MatrixInversionTableau(IntegralMatrix left, IntegralMatrix right) {
		this.left = left;
		this.right = right;
	}

	public void multiplyRow(int row, BigInteger factor) {
		left.multiplyRow(row, factor);
		right.multiplyRow(row, factor);
	}

	public void addRowToRow(int rowToAdd, int modifiedRow, BigInteger factor) {
		left.addRowToRow(rowToAdd, modifiedRow, factor);
		right.addRowToRow(rowToAdd, modifiedRow, factor);
	}

	public void eliminate(int row, int rowToChange, int x) {
		BigInteger gcd = left.get(row, x).gcd(left.get(rowToChange, x));
		BigInteger factor = left.get(rowToChange, x).divide(gcd).negate();
		multiplyRow(rowToChange, left.get(row, x).divide(gcd));
		addRowToRow(row, rowToChange, factor);
		assert left.get(rowToChange, x).equals(BigInteger.ZERO);
	}

}
