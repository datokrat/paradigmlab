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
		Map<GlobalDPKey, Long> cache = new HashMap<>();
		int i = 0;
		for (var line : lines) {
			String[] split = line.split(Pattern.quote(" "));
			List<Long> expectedGroups = longNumber.star(",").parseOne(split[1]).toList();
			expectedGroups = List
				.of(expectedGroups.stream(), expectedGroups.stream(), expectedGroups.stream(), expectedGroups.stream(),
					expectedGroups.stream()) //
				.stream().flatMap(x -> x).toList();

			String record = (List.of(split[0], split[0], split[0], split[0], split[0]).stream().collect(joining("?")));

//			long r = 0L;
//			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, false, false));
//			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, false, true));
//			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, true, false));
//			r += divideAndConquer(record, 0, record.length(), new StateX(expectedGroups, true, true));
//			sum += r;

			long r = dp(record, expectedGroups, cache);
			sum += r;

			prynt("{} -> {}: {}", i, line, r);
			i++;
		}
		return sum;
	}

	public long dp(String record, List<Long> groups) {
		return dp(record, groups, new HashMap<>());
	}

	public long dp(String record, List<Long> groups, Map<GlobalDPKey, Long> cache) {
		record = record + '.';
		// Map<DPKey, Long> cache = new HashMap<>();
		long sum = 0L;
		for (int i = 0; i < record.length(); i++) {
			char c = record.charAt(i);
			DPKey key = new DPKey(i, record.length(), 0, groups.size());
			switch (c) {
			case '.':
				continue;
			case '#':
				return sum + dpInternalFivefold(key.globalize(record, groups), cache);
			case '?':
				sum += dpInternalFivefold(key.globalize(record, groups), cache);
				continue;
			}
		}
		// Here we may assume that all characters are either . or ?
		// and in the latter case, the # branch was already taken care of
		if (groups.size() == 0) {
			return sum + 1;
		} else {
			return sum;
		}
	}

	public long dpInternalFivefold(GlobalDPKey key, Map<GlobalDPKey, Long> cache) {
		if (key.groups.size() % 5 != 0 || key.groups.size() == 0) {
			throw new RuntimeException();
		}
		int step = key.groups.size() / 5;

		long sum = 0L;
		for (int sep1 = 2; sep1 + 1 < key.record.length(); sep1++) {
			GlobalDPKey key1 = new DPKey(0, sep1, 0, step).globalize(key.record, key.groups);
			final long l1 = dpInternal(key1, cache);
			if (l1 == 0)
				continue;
			for (int sep2 = sep1 + 2; sep2 + 1 < key.record.length(); sep2++) {
				GlobalDPKey key2 = new DPKey(sep1, sep2, step, 2 * step).globalize(key.record, key.groups);
				final long l2 = dpInternal(key2, cache);
				if (l2 == 0)
					continue;
				for (int sep3 = sep2 + 2; sep3 + 1 < key.record.length(); sep3++) {
					GlobalDPKey key3 = new DPKey(sep2, sep3, 2 * step, 3 * step).globalize(key.record, key.groups);
					final long l3 = dpInternal(key3, cache);
					if (l3 == 0)
						continue;
					for (int sep4 = sep3 + 2; sep4 + 1 < key.record.length(); sep4++) {
						GlobalDPKey key4 = new DPKey(sep3, sep4, 3 * step, 4 * step).globalize(key.record, key.groups);
						final long l4 = dpInternal(key4, cache);
						if (l4 == 0)
							continue;
						GlobalDPKey key5 = new DPKey(sep4, key.record.length(), 4 * step, key.groups.size())
							.globalize(key.record, key.groups);
						long l5 = dpInternal(key5, cache);
						if (l5 == 0)
							continue;

						sum += l1 * l2 * l3 * l4 * l5;
					}
				}
			}
		}

		return sum;
	}

	/**
	 * Case groups.size() == 1: returns the number of possibilities such that
	 * record[char1, char2) starts with `group` many '#', followed only by at least
	 * one '.'.
	 */
	public long dpInternal(GlobalDPKey key, Map<GlobalDPKey, Long> cache) {
		Long cached = cache.get(key);
		if (cached != null) {
			return cached;
		}

		String record = key.record;
		List<Long> groups = key.groups;

		if (groups.size() == 0) {
			return record.length() == 0 ? 1L : 0L;
		} else if (groups.size() == 1) {
			long group = groups.get(0);
			if (record.length() < group + 1)
				return 0L;
			for (int i = 0; i < record.length(); i++) {
				char c = record.charAt(i);
				if (i < group && c == '.')
					return 0L;
				if (i >= group && c == '#')
					return 0L;
			}
			return 1L;
		}

		long minimalLength = 0L;
		for (int i = 0; i < groups.size(); i++) {
			minimalLength += groups.get(i) + 1;
		}
		if (record.length() < minimalLength) {
			return 0L;
		}

		long sum = 0L;
		int groupMid = groups.size() / 2;
		for (int charMid = 1; charMid < record.length(); charMid++) {
			GlobalDPKey key1 = new DPKey(0, charMid, 0, groupMid).globalize(record, groups);
			long l1 = dpInternal(key1, cache);
			if (l1 == 0L) {
				continue;
			}
			GlobalDPKey key2 = new DPKey(charMid, record.length(), groupMid, groups.size()).globalize(record, groups);
			long l2 = dpInternal(key2, cache);
			sum += l1 * l2;
		}
		return sum;
	}

	/**
	 * Case groups.size() == 1: returns the number of possibilities such that
	 * record[char1, char2) starts with `group` many '#', followed only by at least
	 * one '.'.
	 */
	public long dpInternalLocal(String record, List<Long> groups, DPKey key, Map<DPKey, Long> cache) {
		Long cached = cache.get(key);
		if (cached != null) {
			return cached;
		}

		if (key.group2 - key.group1 == 0) {
			return key.char1 == key.char2 ? 1L : 0L;
		} else if (key.group2 - key.group1 == 1) {
			long group = groups.get(key.group1);
			if (key.char2 - key.char1 <= group)
				return 0L;
			for (int i = key.char1; i < key.char2; i++) {
				char c = record.charAt(i);
				if (i < key.char1 + group && c == '.')
					return 0L;
				if (i >= key.char1 + group && c == '#')
					return 0L;
			}
			return 1L;
		}

		long minimalLength = 0L;
		for (int i = key.group1; i < key.group2; i++) {
			minimalLength += groups.get(i) + 1;
		}
		if (key.char2 - key.char1 < minimalLength) {
			return 0L;
		}

		long sum = 0L;
		int groupMid = (key.group1 + key.group2) / 2;
		for (int charMid = key.char1 + 1; charMid < key.char2; charMid++) {
			DPKey key1 = new DPKey(key.char1, charMid, key.group1, groupMid);
			long l1 = dpInternalLocal(record, groups, key1, cache);
			if (l1 == 0L) {
				continue;
			}
			DPKey key2 = new DPKey(charMid, key.char2, groupMid, key.group2);
			long l2 = dpInternalLocal(record, groups, key2, cache);
			sum += l1 * l2;
		}
		return sum;
	}

	public record DPKey(int char1, int char2, int group1, int group2) {

		public GlobalDPKey globalize(String record, List<Long> groups) {
			return new GlobalDPKey(record.substring(char1, char2), groups.subList(group1, group2));
		}
	}

	public record GlobalDPKey(String record, List<Long> groups) {

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
