package de.paulr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class CollectionUtils {

	public static Stream<Integer> rangeBetween(int begin, int end) {
		return Stream.iterate(begin, i -> i < end, i -> i + 1);
	}

	public static Stream<Long> rangeBetween(long begin, long end) {
		return Stream.iterate(begin, i -> i < end, i -> i + 1);
	}

	public static <T> List<T> reverse(List<T> list) {
		List<T> result = new ArrayList<>();
		for (var i = list.size() - 1; i >= 0; i--) {
			result.add(list.get(i));
		}
		return result;
	}

	public static <T, U> void update(Map<T, U> map, T key, U defaultValue, UnaryOperator<U> updater) {
		map.put(key, updater.apply(map.getOrDefault(key, defaultValue)));
	}

	public static <T> List<Pair<T, Integer>> enumerate(Iterable<T> iterable) {
		List<Pair<T, Integer>> list = new ArrayList<>();
		int i = 0;
		for (var x : iterable) {
			list.add(Pair.of(x, i));
			i++;
		}
		return list;
	}

	public static <T> List<T> genArrayList(Consumer<ListItemAdder<T>> generator) {
		List<T> list = new ArrayList<>();
		generator.accept(list::add);
		return list;
	}

	@FunctionalInterface
	public static interface ListItemAdder<T> {

		void add(T item);

	}

	private CollectionUtils() {
	}

}
