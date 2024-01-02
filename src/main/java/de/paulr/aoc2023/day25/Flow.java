package de.paulr.aoc2023.day25;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

public class Flow<T> {

	public Map<T, Map<T, Long>> flow = new HashMap<>();
	private FlowNetwork<T> network;

	public Flow(FlowNetwork<T> network) {
		this.network = network;
	}

	public long get(T first, T second) {
		return flow.getOrDefault(first, Map.of()).getOrDefault(second, 0L);
	}

	public void increase(T first, T second, long diff) {
		assert !first.equals(second);
		CollectionUtils.update(flow.computeIfAbsent(first, __ -> new HashMap<>()), second, 0L, x -> x + diff);
		CollectionUtils.update(flow.computeIfAbsent(second, __ -> new HashMap<>()), first, 0L, x -> x - diff);
		assert getResidualCapacity(first, second) >= 0;
		assert getResidualCapacity(second, first) >= 0;
	}

	public long getResidualMinCutSize(T from, T to) {
		return network.getCutSize(findResidualMinCut(from, to));
	}

	public boolean hasResidualCut(T from, T to, long size) {
		for (int i = 0; i < size + 1; i++) {
			if (!tryAugment(from, to)) {
				return true;
			}
		}

		return false;
	}

	public Set<T> findResidualMinCut(T from, T to) {
		while (tryAugment(from, to))
			;

		return extractCutFromSaturatedFlow(from);
	}

	private Set<T> extractCutFromSaturatedFlow(T from) {
		Set<T> visited = new HashSet<>();
		Deque<T> deque = new ArrayDeque<>();
		deque.add(from);
		while (!deque.isEmpty()) {
			var node = deque.removeLast(); // DFS

			if (visited.contains(node)) {
				continue;
			}
			visited.add(node);

			for (var target : network.getTargets(node)) {
				if (getResidualCapacity(node, target) > 0) {
					deque.add(target);
				}
			}
		}

		return visited;
	}

	public boolean tryAugment(T from, T to) {
		assert !from.equals(to);
		var path = findPath(from, to);
		if (!path.isPresent()) {
			return false;
		} else {
			augment(path.get());
			return true;
		}
	}

	public void augment(List<T> path) {
		for (int i = 0; i < path.size() - 1; i++) {
			increase(path.get(i), path.get(i + 1), 1);
		}
	}

	public Optional<List<T>> findPath(T from, T to) {
		Set<T> visited = new HashSet<>();
		Deque<Pair<T, Rope<T>>> deque = new ArrayDeque<>();
		deque.add(Pair.of(from, Rope.of(from)));
		while (!deque.isEmpty()) {
			var pair = deque.removeLast(); // DFS
			var node = pair.first();
			var path = pair.second();
			if (visited.contains(node)) {
				continue;
			}
			visited.add(node);

			if (node.equals(to)) {
				return Optional.of(path.toList());
			}

			for (T target : network.getTargets(node)) {
				if (getResidualCapacity(node, target) <= 0) {
					continue;
				}
				if (visited.contains(target)) {
					continue;
				}
				deque.addLast(Pair.of(target, path.addRight(target)));
			}
		}

		return Optional.empty();
	}

	public long getResidualCapacity(T source, T target) {
		return network.getCapacity(source, target) - getEdgeFlow(source, target);
	}

	public long getEdgeFlow(T source, T target) {
		return flow.getOrDefault(source, Map.of()).getOrDefault(target, 0L);
	}

}
