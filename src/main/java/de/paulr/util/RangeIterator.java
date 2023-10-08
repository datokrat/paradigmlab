package de.paulr.util;

public class RangeIterator implements IntIterator {

	private int next;
	private final int end;

	public RangeIterator(int begin, int end) {
		this.next = begin;
		this.end = end;
	}

	@Override
	public boolean hasNext() {
		return next < end;
	}

	@Override
	public int nextInt() {
		return next++;
	}

}
