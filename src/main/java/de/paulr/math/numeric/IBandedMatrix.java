package de.paulr.math.numeric;

import de.paulr.util.IntIterator;
import de.paulr.util.RangeIterator;

public interface IBandedMatrix extends IRandomAccessMatrix {

	int getBackwardThickness();

	int getForwardThickness();

	IBandedMatrix transpose();

	default boolean isTridiagonal() {
		return getBackwardThickness() <= 1 && getForwardThickness() <= 1;
	}

	default boolean isUpperHessenberg() {
		return getForwardThickness() <= 1;
	}

	default int getColumnLowerBoundIncl(int j) {
		return Math.min(Math.max(j - getBackwardThickness(), 0), getHeight());
	}

	default int getColumnUpperBoundExcl(int j) {
		return Math.min(j + getForwardThickness() + 1, getHeight());
	}

	default int getRowLowerBoundIncl(int i) {
		return Math.min(Math.max(i - getForwardThickness(), 0), getWidth());
	}

	default int getRowUpperBoundExcl(int i) {
		return Math.min(i + getBackwardThickness() + 1, getWidth());
	}

	default IntIterator getNonzeroRowEntryIterator(int i) {
		return new RangeIterator(getRowLowerBoundIncl(i), getRowUpperBoundExcl(i));
	}

	default IntIterator getNonzeroColumnEntryIterator(int j) {
		return new RangeIterator(getColumnLowerBoundIncl(j), getColumnUpperBoundExcl(j));
	}

	/**
	 * The vectors are allowed to be equal.
	 */
	default void solveByElimination(IColumnVector rightSideVector, IMutableColumnVector target) {
		assert getForwardThickness() == 0; // only supports upper triangular matrices
		assert getHeight() >= getWidth();
		assert rightSideVector.getHeight() >= getWidth(); // The entries starting from getWidth()
															// are ignored
		int n = getWidth();

		for (int i = n - 1; i >= 0; i--) {
			assert !DoubleMath.zero(get(i, i));
			IntIterator it = getNonzeroRowEntryIterator(i);
			double ti = rightSideVector.get(i);
			while (it.hasNext()) {
				int j = it.nextInt();
				if (j == i) {
					continue;
				}
				ti -= get(i, j) * target.get(j);
			}
			target.set(i, ti / get(i, i));
		}
	}

}
