package de.paulr.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import org.apache.commons.lang3.function.TriFunction;

public abstract sealed class Rope<T> implements Iterable<T> permits EmptyRope, RopeLeaf, RopeNode {

	public static <T> EmptyRope<T> empty() {
		return new EmptyRope<T>();
	}

	public static <T> RopeLeaf<T> singleton(T value) {
		return new RopeLeaf<T>(value);
	}

	public static <T> RopeNode<T> concat(Pair<Rope<T>, Rope<T>> pair) {
		return concat(pair.first(), pair.second());
	}

	public static <T> RopeNode<T> concat(Rope<T> left, Rope<T> right) {
		return new RopeNode<T>(left, right);
	}

	public static <T> RopeNode<T> ofPair(Pair<T, T> pair) {
		return concat(singleton(pair.first()), singleton(pair.second()));
	}

	@SafeVarargs
	public static <T> Rope<T> of(T... items) {
		Rope<T> result = Rope.empty();
		for (T item : items) {
			result = result.addRight(item);
		}
		return result;
	}

	public static <T> RopeNode<T> addLeft(Pair<T, Rope<T>> pair) {
		return concat(singleton(pair.first()), pair.second());
	}

	public static <T> RopeNode<T> addLeft(T left, Rope<T> rope) {
		return concat(singleton(left), rope);
	}

	public static <T> RopeNode<T> addRight(Pair<Rope<T>, T> pair) {
		return concat(pair.first(), singleton(pair.second()));
	}

	public static <T> RopeNode<T> addRight(Rope<T> rope, T right) {
		return concat(rope, singleton(right));
	}

	public abstract int size();

	public RopeNode<T> addRight(T item) {
		return Rope.addRight(this, item);
	}

	public RopeNode<T> addLeft(T item) {
		return Rope.addLeft(item, this);
	}

	public RopeNode<T> concat(Rope<T> right) {
		return concat(this, right);
	}

	public List<T> toList() {
		List<T> result = new ArrayList<T>();
		forEach(result::add);
		return result;
	}

	public Set<T> toSet() {
		Set<T> result = new LinkedHashSet<T>();
		forEach(result::add);
		return result;
	}

	public static <T, R> Function<Rope<T>, R> fn(TriFunction<T, T, T, R> fn) {
		return rope -> {
			if (rope.size() != 3) {
				throw new RuntimeException("rope of size 3 expected");
			}
			List<T> list = rope.toList();
			return fn.apply(list.get(0), list.get(1), list.get(2));
		};
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {

			private Stack<Rope<T>> stack = new Stack<>();
			{
				stack.add(Rope.this);
				reduce();
			}

			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}

			@Override
			public T next() {
				RopeLeaf<T> leaf = (RopeLeaf<T>) stack.pop();
				reduce();
				return leaf.getValue();
			}

			private void reduce() {
				while (!stack.isEmpty() && !(stack.peek() instanceof RopeLeaf<T>)) {
					Rope<T> rope = stack.pop();
					if (rope instanceof RopeNode<T> node) {
						stack.push(node.getRight());
						stack.push(node.getLeft());
					}
				}
			}
		};
	}

}
