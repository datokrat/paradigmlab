package de.paulr.algorithms.search;

import java.util.List;
import java.util.Map;

import de.paulr.util.Pair;

public class SearchUtils {

	public static <T> void floydWarshall(List<T> nodes, Map<Pair<T, T>, Long> edges) {

		for (var node : nodes) {
			edges.put(Pair.of(node, node), 0L);
		}

		for (var n1 : nodes) {
			for (var n2 : nodes) {
				for (var n3 : nodes) {
					Long d21 = edges.get(Pair.of(n2, n1));
					Long d13 = edges.get(Pair.of(n1, n3));
					if (d21 == null || d13 == null) {
						continue;
					}
					Pair<T, T> p23 = Pair.of(n2, n3);
					Long d23 = edges.get(p23);
					if (d23 == null) {
						edges.put(p23, d21 + d13);
					} else {
						edges.put(p23, Math.min(d23, d21 + d13));
					}
				}
			}
		}
	}

	private SearchUtils() {
	}

}
