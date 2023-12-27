package de.paulr.aoc2023.day24;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class IntegralMatrix {

	private List<List<BigInteger>> rows;

	public IntegralMatrix(int height, int width) {
		rows = new ArrayList<>();
		for (int i = 0; i < height; i++) {
			var cols = new ArrayList<BigInteger>();
			for (int j = 0; j < width; j++) {
				cols.add(BigInteger.ZERO);
			}
			rows.add(cols);
		}
	}

	public void addRowToRow(int rowToAdd, int modifiedRow, BigInteger factor) {
		for (int x = 0; x < rows.get(0).size(); x++) {
			set(modifiedRow, x, get(modifiedRow, x).add(get(rowToAdd, x).multiply(factor)));
		}
	}

	public long getAsLong(int y, int x) {
		BigInteger value = rows.get(y).get(x);
		if (!value.equals(BigInteger.valueOf(value.longValue()))) {
			throw new RuntimeException();
		}
		return value.longValue();
	}

	public BigInteger get(int y, int x) {
		return rows.get(y).get(x);
	}

	public void set(int y, int x, long value) {
		set(y, x, BigInteger.valueOf(value));
	}

	public void set(int y, int x, BigInteger value) {
		rows.get(y).set(x, value);
	}

	public void multiplyRow(int row, BigInteger factor) {
		for (int x = 0; x < rows.get(0).size(); x++) {
			set(row, x, get(row, x).multiply(factor));
		}
	}

}
