package de.paulr.aoc2023.day12;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;
import static de.paulr.parser.Parsers.regex;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class FiniteNondeterministicInputAutomataSolution extends ASolution {

	public FiniteNondeterministicInputAutomataSolution() {
		super(2023, 12, "");
	}

	public static void main(String[] args) {
		var s = new FiniteNondeterministicInputAutomataSolution();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		return batchCountMatches(lines.stream().map(Query::fromInput).toList());
	}

	@Override
	public Object partB() {
		return batchCountMatches(lines.stream().map(Query::fromInput).map(q -> q.repeat(5, "?")).toList());
	}

	public static long batchCountMatches(List<Query> queries) {
		long total = 0L;
		for (var q : queries) {
			long matches = countMatches(q.record, q.groups);
			// prynt("{}: {} matches", q, matches);
			total += matches;
		}
		return total;
	}

	public static long countMatches(String record, List<Long> groups) {
		var superposition = matchNondeterministic(record, groups);

		long total = 0L;
		for (var entry : superposition.entrySet()) {
			State state = entry.getKey();
			long multiplicity = entry.getValue();
			if (state.isAccepted(groups)) {
				total += multiplicity;
			}
		}

		return total;
	}

	public static Map<State, Long> matchNondeterministic(String record, List<Long> groups) {
		Map<State, Long> superposition = Map.of(State.init(), 1L);
		for (int i = 0; i < record.length(); i++) {
			Map<State, Long> nextSuperposition = new LinkedHashMap<>();
			for (var entry : superposition.entrySet()) {
				State state = entry.getKey();
				long multiplicity = entry.getValue();
				state.consumeNondeterministic(record.charAt(i), multiplicity, groups, nextSuperposition);
			}
			superposition = nextSuperposition;
		}
		return superposition;
	}

	/**
	 * Invariant: 0 <= hashCount < groups.get(group) AND (hashCount > 0 ==>
	 * expectation == HASH)
	 */
	public record State(int group, int hashCount, Expectation expectation) {

		public static State init() {
			return new State(0, 0, Expectation.MIXED);
		}

		public boolean isAccepted(List<Long> groups) {
			return group == groups.size() && hashCount == 0;
		}

		public void consumeNondeterministic(char c, long multiplicity, List<Long> groups, Map<State, Long> output) {
			if (c == '?') {
				consumeNondeterministic('.', multiplicity, groups, output);
				consumeNondeterministic('#', multiplicity, groups, output);
			} else {
				consume(c, groups).ifPresent(next -> output.put(next, output.getOrDefault(next, 0L) + multiplicity));
			}
		}

		public Optional<State> consume(String input, List<Long> groups) {
			State state = this;
			for (int i = 0; i < input.length(); i++) {
				Optional<State> next = state.consume(input.charAt(i), groups);
				if (next.isEmpty()) {
					return Optional.empty();
				}
				state = next.get();
			}
			return Optional.of(state);
		}

		public Optional<State> consume(char c, List<Long> groups) {
			switch (c) {
			case '.':
				switch (expectation) {
				case DOT:
					return Optional.of(new State(group, 0, Expectation.MIXED));
				case MIXED:
					if (hashCount > 0) {
						return Optional.empty();
					} else {
						return Optional.of(this);
					}
				}
			case '#':
				switch (expectation) {
				case DOT:
					return Optional.empty();
				case MIXED:
					if (group >= groups.size()) {
						return Optional.empty();
					}
					if (hashCount + 1 == groups.get(group)) {
						return Optional.of(new State(group + 1, 0, Expectation.DOT));
					}
					return Optional.of(new State(group, hashCount + 1, Expectation.MIXED));
				}
			}
			throw new RuntimeException();
		}

	}

	public enum Expectation {
		DOT, MIXED
	}

	public record Query(String record, List<Long> groups) {

		public static IParser<Query> psr = regex("[#.?]*").thenSilently(" ")
			.then(longNumber.star(",").map(Rope::toList)) //
			.map(Pair.fn(Query::new));

		public static Query fromInput(String line) {
			return psr.parseOne(line);
		}

		public Query repeat(int n, String delimiter) {
			StringBuffer nrecord = new StringBuffer(record.length() * n);
			List<Long> ngroups = new ArrayList<>(groups.size() * n);
			for (int i = 0; i < n; i++) {
				if (i > 0) {
					nrecord.append(delimiter);
				}
				nrecord.append(record);
				ngroups.addAll(groups);
			}
			return new Query(nrecord.toString(), ngroups);
		}

	}

}
