package de.paulr.util;

import java.util.Iterator;

public interface IntIterator extends Iterator<Integer> {

	int nextInt();

	@Override
	default Integer next() {
		return nextInt();
	}

}
