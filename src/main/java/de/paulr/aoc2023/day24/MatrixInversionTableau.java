package de.paulr.aoc2023.day24;

import java.math.BigInteger;

import de.paulr.aoc2023.AoCUtil;

public class MatrixInversionTableau {

	public IntegralMatrix left;
	public IntegralMatrix right;

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

	public void swap(int rowA, int rowB) {
		left.swap(rowA, rowB);
		right.swap(rowA, rowB);
	}

	public void gauss() {
		int squareSize = Math.min(left.getWidth(), left.getHeight());
		for (int i = 0; i < squareSize; i++) {
			int j = 0;
			while (j + i < left.getHeight() && left.get(j + i, i).equals(BigInteger.ZERO)) {
				j++;
			}
			if (j + i == left.getHeight()) {
				// Found no row to pivot
				throw new RuntimeException();
			}
			swap(i, j + i);
			for (int k = i + 1; k < left.getHeight(); k++) {
				eliminate(i, k, i);
			}
		}
		for (int i = squareSize - 1; i >= 0; i--) {
			for (int j = 0; j < left.getHeight(); j++) {
				if (i == j) {
					continue;
				}
				eliminate(i, j, i);
			}
		}
		BigInteger lcm = BigInteger.ONE;
		for (int i = 0; i < squareSize; i++) {
			lcm = AoCUtil.lcm(lcm, left.get(i, i));
		}
		for (int i = 0; i < squareSize; i++) {
			multiplyRow(i, lcm.divide(left.get(i, i)));
		}
	}

}
