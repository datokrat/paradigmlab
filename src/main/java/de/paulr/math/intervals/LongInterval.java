package de.paulr.math.intervals;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record LongInterval(long beginInclusive, long length) {

	public static LongInterval empty() {
		return new LongInterval(0, 0);
	}

	public static LongInterval leftAligned(long beginInclusive, long length) {
		assert length >= 0;
		return new LongInterval(beginInclusive, length);
	}

	public static LongInterval rightAligned(long endInclusive, long length) {
		long endExclusive = endInclusive + 1;
		return new LongInterval(endExclusive - length, length);
	}

	public static LongInterval boundedBy(long beginInclusive, long endExclusive) {
		return new LongInterval(beginInclusive, endExclusive - beginInclusive);
	}

	public static LongInterval safelyBoundedBy(long beginInclusive, long endExclusive) {
		return LongInterval.boundedBy(beginInclusive, Math.max(beginInclusive, endExclusive));
	}

	public long beginExclusive() {
		return beginInclusive() - 1;
	}

	public long endInclusive() {
		return endExclusive() - 1;
	}

	public long endExclusive() {
		return beginInclusive + length;
	}

	public boolean contains(long x) {
		return beginInclusive <= x && x < endExclusive();
	}

	public boolean supersetOf(LongInterval other) {
		return other.subsetOf(this);
	}

	public boolean subsetOf(LongInterval other) {
		if (isEmpty()) {
			return true;
		}
		return beginInclusive() >= other.beginInclusive() && endExclusive() <= other.endExclusive();
	}

	public boolean isEmpty() {
		return length == 0;
	}

	public LongInterval intersectWith(LongInterval other) {
		if (isDisjoint(other)) {
			return LongInterval.empty();
		}

		return LongInterval.boundedBy( //
				Math.max(beginInclusive(), other.beginInclusive()), //
				Math.min(endExclusive(), other.endExclusive()));
	}

	public List<LongInterval> minus(LongInterval other) {
		if (isDisjoint(other)) {
			return List.of(this);
		}

		return Stream.of( //
				LongInterval.safelyBoundedBy(other.endExclusive(), endExclusive()), //
				LongInterval.safelyBoundedBy(beginInclusive(), other.beginInclusive())) //
				.filter(Predicate.not(LongInterval::isEmpty)) //
				.toList();
	}

	public boolean isDisjoint(LongInterval other) {
		return isBefore(other) || isAfter(other);
	}

	public boolean isAfter(LongInterval other) {
		return other.isBefore(this);
	}

	public boolean isBefore(LongInterval other) {
		return this.endExclusive() <= other.beginInclusive();
	}

	public LongInterval translate(long x) {
		return LongInterval.leftAligned(beginInclusive + x, length);
	}

}
