package de.paulr.util;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.function.TriFunction;

public record Pair<T, U>(T first, U second) {

	public static <T> T get(Pair<T, T> pair, OneOrTwo key) {
		return switch (key) {
		case ONE -> pair.first;
		case TWO -> pair.second;
		};
	}

	public static <T> Pair<T, T> replacing(Pair<T, T> pair, OneOrTwo key, T value) {
		return switch (key) {
		case ONE -> pair.withFirst(value);
		case TWO -> pair.withSecond(value);
		};
	}

	public static <T> Pair<T, T> updating(Pair<T, T> pair, OneOrTwo key, UnaryOperator<T> updater) {
		return Pair.replacing(pair, key, updater.apply(Pair.get(pair, key)));
	}

	public Pair<T, U> withFirst(T first) {
		return Pair.of(first, second);
	}

	public Pair<T, U> withSecond(U second) {
		return Pair.of(first, second);
	}

	public static <T> Pair<T, T> sort(Pair<T, T> pair, Comparator<T> comparator) {
		if (comparator.compare(pair.first, pair.second) <= 0) {
			return pair;
		}
		return swap(pair);
	}

	public static <T> Pair<T, T> swap(Pair<T, T> pair) {
		return Pair.of(pair.second, pair.first);
	}

	public static <T, U> Pair<T, U> of(T first, U second) {
		return new Pair<>(first, second);
	}

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

	public static <T, U, V, R> Function<Pair<Pair<T, U>, V>, R> fn(TriFunction<T, U, V, R> fn) {
		return pair -> fn.apply(pair.first.first, pair.first.second, pair.second);
	}

	public static final <T, U> Lens<T, Pair<T, U>> lensFirst() {
		return new Lens<>() {

			@Override
			public T get(Pair<T, U> pair) {
				return pair.first;
			}

			@Override
			public Pair<T, U> withNewValue(Pair<T, U> pair, T first) {
				return pair.withFirst(first);
			}
		};
	}

	public static final <T, U> Lens<U, Pair<T, U>> lensSecond() {
		return new Lens<>() {

			@Override
			public U get(Pair<T, U> pair) {
				return pair.second;
			}

			@Override
			public Pair<T, U> withNewValue(Pair<T, U> pair, U second) {
				return pair.withSecond(second);
			}

		};
	}

	public enum OneOrTwo {
		ONE, TWO
	}

}
