package de.paulr.aoc2023.day25;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.paulr.util.Pair;

public class FlowNetwork<T> {

	private Map<T, Map<T, Long>> edges;

	public FlowNetwork(Map<Pair<T, T>, Long> capacities) {
		edges = new HashMap<>();
		for (var entry : capacities.entrySet()) {
			var edge = entry.getKey();
			var capacity = entry.getValue();
			assert capacity >= 0;
			edges.computeIfAbsent(edge.first(), __ -> new HashMap<>()).put(edge.second(), capacity);
		}
	}

	public long getCutSize(Set<T> cut) {
		long size = 0L;
		for (var left : cut) {
			for (var right : getTargets(left)) {
				if (cut.contains(right)) {
					continue;
				}
				size += getCapacity(left, right);
			}
		}
		return size;
	}

	public long getCapacity(T source, T target) {
		return edges.getOrDefault(source, Map.of()).getOrDefault(target, 0L);
	}

	public Set<T> getTargets(T source) {
		return edges.computeIfAbsent(source, __ -> new HashMap<>()).keySet();
	}

}
