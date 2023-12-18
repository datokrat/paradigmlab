package de.paulr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionUtils {

	public static Stream<Integer> rangeBetween(int begin, int end) {
		return Stream.iterate(begin, i -> i < end, i -> i + 1);
	}

	public static Stream<Long> rangeBetween(long begin, long end) {
		return Stream.iterate(begin, i -> i < end, i -> i + 1);
	}

	public static <T> Stream<T> repeat(T value, long length) {
		return Stream.generate(() -> value).limit(length);
	}

	public static Stream<Character> chars(String string) {
		return string.chars().mapToObj(i -> (char) i);
	}

	public static String reverse(String string) {
		StringBuffer sb = new StringBuffer(string.length());
		for (int i = 0; i < string.length(); i++) {
			sb.append(string.charAt(i));
		}
		return sb.toString();
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

	public static <K1, K2, V> Map<K2, V> mapKeys(Map<K1, V> map, Function<K1, K2> fn) {
		return map.entrySet().stream().collect(Collectors.toMap(e -> fn.apply(e.getKey()), e -> e.getValue()));
	}

	@FunctionalInterface
	public static interface ListItemAdder<T> {

		void add(T item);

	}

	private CollectionUtils() {
	}

}
