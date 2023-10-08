package de.paulr.math.numeric;

import java.util.Iterator;

import de.paulr.util.Pair;

public interface ISparseMatrix {

	int getHeight();

	int getWidth();

	Iterator<Pair<Integer, Double>> rowElementIterator(int i);

	default IColumnVector times(IColumnVector vector) {
		double[] result = new double[vector.getHeight()];
		for (int i = 0; i < getHeight(); i++) {
			var it = rowElementIterator(i);
			while (it.hasNext()) {
				var indexAndValue = it.next();
				int index = indexAndValue.first();
				double value = indexAndValue.second();
				result[i] += value * vector.get(index);
			}
		}
		return new DenseVector(result);
	}

}
