package de.paulr.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public record Pair<T, U>(T first, U second) {

	public <R> R into(BiFunction<T, U, R> fn) {
		return fn.apply(first, second);
	}

	public static <T, U> Function<T, Pair<T, U>> annotate(U right) {
		return annotateRight(right);
	}

	public static <T, U> Function<U, Pair<T, U>> annotateLeft(T left) {
		return right -> new Pair<>(left, right);
	}

	public static <T, U> Function<T, Pair<T, U>> annotateRight(U right) {
		return left -> new Pair<>(left, right);
	}

	public static <T, U, R> Function<Pair<T, U>, R> fn(BiFunction<T, U, R> fn) {
		return pair -> pair.into(fn);
	}

}
