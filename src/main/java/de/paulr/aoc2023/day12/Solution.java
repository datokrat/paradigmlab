package de.paulr.aoc2023.day12;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public static final String FILE = "2023_12.txt";
	public static final String EXAMPLE = "2023_12s.txt";

	public Solution(String filename) {
		super(filename);
	}

	public static void main(String[] args) {
		var s = new Solution(FILE);
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		long sum = 0;
		for (var line : lines) {
			sum += countValidArrangements(line);
			prynt("{}: {}", line, countValidArrangements(line));
		}
		prynt(sum);
		return null;
	}

	@Override
	public Object partB() {
		long sum = 0;
		for (var line : lines) {
			String[] split = line.split(Pattern.quote(" "));
			List<Long> expectedGroups = longNumber.star(",").parseOne(split[1]).toList();
			expectedGroups = List
				.of(expectedGroups.stream(), expectedGroups.stream(), expectedGroups.stream(), expectedGroups.stream(),
					expectedGroups.stream()) //
				.stream().flatMap(x -> x).toList();

			String record = (List.of(split[0], split[0], split[0], split[0], split[0]).stream().collect(joining("?")));

			long r = 0L;
			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, false, false));
			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, false, true));
			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, true, false));
			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, true, true));
			sum += r;

			prynt("{}: {}", line, r);
		}
		return sum;
	}

	public long countValidArrangements(String line) {
		String[] split = line.split(Pattern.quote(" "));
		List<Long> expectedGroups = longNumber.star(",").parseOne(split[1]).addRight(0L).toList();
		String record = split[0] + ".";
		// return countValidArrangements2(record, expectedGroups);
		return countValidArrangements(record, expectedGroups);
	}

	public long countValidArrangements(String record, List<Long> expectedGroups) {
		List<State> states = List.of(new State(List.of(0L)));
		for (int i = 0; i < record.length(); i++) {
			int j = i;
			states = states.stream().flatMap(s -> s.next(record.charAt(j), expectedGroups).stream()).toList();
		}
		long count = 0L;
		for (var state : states) {
			if (state.groups.equals(expectedGroups)) {
				count++;
			}
		}
		return count;
	}

	public long countValidArrangementsWithMap(String record, List<Long> groups) {
		return collectPossibleGroupingsAndCount(record, 0, record.length()).getOrDefault(new State(groups), 0L);
	}

	public Map<State, Long> collectPossibleGroupingsAndCount(String record, int start, int end) {
		Map<State, Long> states = Map.of(State.init(), 1L);
		for (int i = start; i < end; i++) {
			char c = record.charAt(i);
			Map<State, Long> newStates = new LinkedHashMap<>();
			for (var entry : states.entrySet()) {
				long f = entry.getValue();
				State s = entry.getKey();
				for (var ns : s.next(c)) {
					newStates.put(ns, newStates.getOrDefault(ns, 0L) + f);
				}
			}
			states = newStates;
		}
		return states;
	}

	public long divideAndConquer(CharSequence record, int begin, int end, StateX groups) {
		return divideAndConquer(record, begin, end, groups, new HashMap<>());
	}

	public long divideAndConquer(CharSequence record, int begin, int end, StateX groups, Map<Query, Long> cache) {
		if (end - begin == 0) {
			throw new RuntimeException();
		} else if (end - begin == 1) {
			char c = record.charAt(begin);
			long r = 0L;
			if (c != '#') {
				r += (groups.dotStart && groups.dotEnd && groups.groups.size() == 0) ? 1 : 0;
			}
			if (c != '.') {
				r += (!groups.dotStart && !groups.dotEnd && groups.groups.equals(List.of(1L))) ? 1 : 0;
			}
			return r;
		}

		if (groups.groups.stream().reduce(0L, (x, y) -> x + y) + groups.groups.size() - 1 > record.length()) {
			return 0L;
		}

		int mid = (begin + end) / 2;
		long sum = 0L;
		for (var split : groups.split()) {
			Query q1 = new Query(begin, mid, split.first());
			Query q2 = new Query(mid, end, split.second());
			Long l1 = cache.get(q1);
			if (l1 != null && l1 == 0L) {
				continue;
			}
			Long l2 = cache.get(q2);
			if (l2 != null && l2 == 0L) {
				continue;
			}
			if (l1 == null) {
				l1 = divideAndConquer(record, begin, mid, split.first(), cache);
				cache.put(q1, l1);
			}
			if (l2 == null) {
				l2 = divideAndConquer(record, mid, end, split.second(), cache);
				cache.put(q2, l2);
			}
			sum += l1 * l2;
		}
		return sum;
	}

	public record Query(int begin, int end, StateX groups) {
	}

	public record StateX(List<Long> groups, boolean dotStart, boolean dotEnd) {

		public List<Pair<StateX, StateX>> split() {
			List<Pair<StateX, StateX>> r = new ArrayList<>();
			// proper group splits
			for (int i = 0; i < groups.size(); i++) {
				for (int j = 1; j < groups.get(i); j++) {
					List<Long> g1 = List.<Stream<Long>>of( //
						groups.subList(0, i).stream(), //
						Stream.of((long) j)).stream().flatMap(x -> x).toList();
					List<Long> g2 = List.<Stream<Long>>of( //
						Stream.of(groups.get(i) - j), //
						groups.subList(i + 1, groups.size()).stream()).stream().flatMap(x -> x).toList();

					r.add(Pair.of(new StateX(g1, dotStart, false), new StateX(g2, false, dotEnd)));
				}
			}
			// splits between groups; empty parts are ok if dotStart or dotEnd
			for (int i = 0; i <= groups.size(); i++) {
				if (i == 0 && !dotStart) {
					continue;
				}
				if (i == groups.size() && !dotEnd) {
					continue;
				}
				List<Long> g1 = groups.subList(0, i);
				List<Long> g2 = groups.subList(i, groups.size());
				r.add(Pair.of(new StateX(g1, dotStart, true), new StateX(g2, false, dotEnd)));
				r.add(Pair.of(new StateX(g1, dotStart, true), new StateX(g2, true, dotEnd)));
				r.add(Pair.of(new StateX(g1, dotStart, false), new StateX(g2, true, dotEnd)));
			}
			return r;
		}

	}

	public record State(List<Long> groups) {

		public static State init() {
			return new State(List.of(0L));
		}

		public List<State> next(char c, List<Long> expectedGroups) {
			return next(c).stream() //
				.filter(s -> s.satisfies(expectedGroups)) //
				.toList();
		}

		public List<State> next(char c) {
			if (c == '.') {
				if (lastGroup() == 0) {
					return List.of(new State(groups));
				} else {
					var ngroups = new ArrayList<>(groups);
					ngroups.add(0L);
					return List.of(new State(ngroups));
				}
			} else if (c == '#') {
				var ngroups = new ArrayList<>(groups);
				ngroups.set(ngroups.size() - 1, ngroups.get(ngroups.size() - 1) + 1);
				return List.of(new State(ngroups));
			} else {
				return Stream.concat(next('.').stream(), next('#').stream()).toList();
			}
		}

		public Map<State, Long> nextMap(char c) {
			return next(c).stream().collect(groupingBy(x -> x, counting()));
		}

		public long lastGroup() {
			return groups.get(groups.size() - 1);
		}

		public boolean satisfies(List<Long> expectedGroups) {
			for (int i = 0; i < groups.size() - 1; i++) {
				if (!groups.get(i).equals(expectedGroups.get(i))) {
					return false;
				}
			}
			return true;
		}

	}

	public long anExceedinglySlowRecursiveAttempt(CharSequence record, List<Long> groups) {
		if (groups.isEmpty()) {
			return record.chars().allMatch(i -> ((char) i) == '.') ? 1 : 0;
		}

		if (record.isEmpty()) {
			return 0L;
		}

		char c = record.charAt(0);
		long sum = 0L;
		if (c != '#') {
			sum += anExceedinglySlowRecursiveAttempt(record.subSequence(1, record.length()), groups);
		}
		if (c != '.') {
			if (record.length() <= groups.get(0)
				|| (record.length() > groups.get(0) && record.charAt((int) (long) groups.get(0)) == '#')) {
				return sum;
			}
			for (int i = 0; i < groups.get(0); i++) {
				if (record.charAt(i) == '.') {
					return sum;
				}
			}
			if (record.length() == groups.get(0)) {
				return sum;
			}
			sum += anExceedinglySlowRecursiveAttempt(
				record.subSequence((int) (long) groups.get(0) + 1, record.length()), groups.subList(1, groups.size()));
		}
		return sum;
	}

}
