package de.paulr.math.numeric;

import java.util.ArrayList;
import java.util.Collections;

import de.paulr.util.RangeIterator;

public class BandedMatrix extends AMatrix implements IMutableBandedMatrix {

	private int height;
	private int width;
	private final int backward;
	private final int forward;
	private ArrayList<ArrayList<Double>> entries;

	public BandedMatrix(int height, int width, int backward, int forward) {
		this.backward = backward;
		this.forward = forward;

		this.height = 0;
		this.width = 0;
		entries = new ArrayList<>();
		resize(height, width);
//		entries = new ArrayList<>(width);
//		for (int i = 0; i < width; i++) {
//			int columnNonzeros = getColumnUpperBoundExcl(i) - getColumnLowerBoundIncl(i);
//			entries.add(new ArrayList<>(Collections.nCopies(columnNonzeros, 0D)));
//		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBackwardThickness() {
		return backward;
	}

	public int getForwardThickness() {
		return forward;
	}

	public double get(int y, int x) {
		int vert = getVerticalIndexOrNeg(y, x);

		if (vert < 0) {
			return 0;
		}

		return entries.get(x).get(vert);
	}

	public void set(int y, int x, double value) {
		int vert = getVerticalIndexOrNeg(y, x);

		if (vert < 0) {
			if (DoubleMath.zero(value))
				return;
			throw new IndexOutOfBoundsException("Trying to set non-zero value out of band");
		}

		entries.get(x).set(vert, value);
	}

	public IMutableBandedMatrix transpose() {
		return new TransposedBandedMatrix(this);
	}

	public void resize(int height, int width) {
		assert height >= getHeight();
		assert width >= getWidth();

		if (height > getHeight()) {
			var columnsToResizeIterator = new RangeIterator(
				Math.max(getHeight() - getBackwardThickness() - 1, 0), getWidth());
			while (columnsToResizeIterator.hasNext()) {
				int j = columnsToResizeIterator.nextInt();
				int currentSize = Math.min(j + getForwardThickness() + 1, getHeight())
					- Math.min(Math.max(j - getBackwardThickness(), 0), getHeight());
				assert currentSize == entries.get(j).size();
				int desiredSize = Math.min(j + getForwardThickness() + 1, height)
					- Math.min(Math.max(j - getBackwardThickness(), 0), getHeight());
				assert desiredSize >= currentSize;

				entries.get(j).addAll(Collections.nCopies(desiredSize - currentSize, 0D));
			}
			this.height = height;
		}
		if (width > getWidth()) {
			entries.ensureCapacity(width);
			for (int j = getWidth(); j < width; j++) {
				int size = getColumnUpperBoundExcl(j) - getColumnLowerBoundIncl(j);
				entries.add(new ArrayList<>(Collections.nCopies(size, 0D)));
			}
			this.width = width;
		}
	}

	private int getVerticalIndexOrNeg(int y, int x) {
		int begin = getColumnLowerBoundIncl(x);
		int end = getColumnUpperBoundExcl(x);

		if (y < begin || y >= end) {
			return -1;
		}

		return y - begin;
	}

	private static class TransposedBandedMatrix extends AMatrix implements IMutableBandedMatrix {

		private final BandedMatrix inner;

		public TransposedBandedMatrix(BandedMatrix inner) {
			this.inner = inner;
		}

		@Override
		public int getHeight() {
			return inner.getWidth();
		}

		@Override
		public int getWidth() {
			return inner.getHeight();
		}

		@Override
		public double get(int y, int x) {
			return inner.get(x, y);
		}

		@Override
		public int getBackwardThickness() {
			return inner.getForwardThickness();
		}

		@Override
		public int getForwardThickness() {
			return inner.getBackwardThickness();
		}

		public IMutableBandedMatrix transpose() {
			return inner;
		}

		@Override
		public void set(int i, int j, double value) {
			inner.set(j, i, value);
		}

	}

	// TODO: use this to determine if the matrix is tridiagonal
	private int deriveEffectiveBackwardThickness() {
		return deriveEffectiveThickness(backward);
	}

	private int deriveEffectiveForwardThickness() {
		return deriveEffectiveThickness(forward);
	}

	private int deriveEffectiveThickness(int declaredThickness) {
		int maxband = deriveEffectiveMaximumThickness();
		return declaredThickness < 0 || declaredThickness > maxband ? maxband : declaredThickness;
	}

	private int deriveEffectiveMaximumThickness() {
		return Math.max(Math.min(height, width) - 1, 0);
	}

}
