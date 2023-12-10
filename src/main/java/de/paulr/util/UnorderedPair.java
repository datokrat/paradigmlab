package de.paulr.util;

import java.util.Objects;

public record UnorderedPair<T, U>(T first, U second) {

	public static <T, U> UnorderedPair<T, U> of(T first, U second) {
		return new UnorderedPair<>(first, second);
	}

	public static <T> UnorderedPair<T, T> swap(UnorderedPair<T, T> pair) {
		return UnorderedPair.of(pair.second, pair.first);
	}

	public UnorderedPair<T, U> withFirst(T first) {
		return new UnorderedPair<>(first, second);
	}

	public UnorderedPair<T, U> withSecond(U second) {
		return new UnorderedPair<>(first, second);
	}

	@Override
	public int hashCode() {
		int h1 = first.hashCode();
		int h2 = second.hashCode();
		if (h1 > h2) {
			return 31 * h1 + h2;
		} else {
			return 31 * h2 + h1;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UnorderedPair<?, ?> other = (UnorderedPair<?, ?>) obj;
		return (Objects.equals(first, other.first) && Objects.equals(second, other.second)) //
				|| (Objects.equals(second, other.first) && Objects.equals(first, other.second));
	}

}
