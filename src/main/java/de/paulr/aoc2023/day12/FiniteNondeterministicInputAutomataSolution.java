package de.paulr.aoc2023.day12;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.Stopwatch;

class FiniteNondeterministicInputAutomataSolution extends ASolution {

	public static Stopwatch initSw = new Stopwatch();
	public static Stopwatch forwardSw = new Stopwatch();
	public static Stopwatch backwardSw = new Stopwatch();
	public static Stopwatch combineSw = new Stopwatch();
	public static Stopwatch parseSw = new Stopwatch();

	public FiniteNondeterministicInputAutomataSolution() {
		super(2023, 12, "");
	}

	public static void main(String[] args) {
		var s = new FiniteNondeterministicInputAutomataSolution();
		Stopwatch sw = new Stopwatch();
		prynt(s.partB());
		prynt("Part B took {}ms", sw.elapsedMillis());
		prynt("PARSE took {}ms", parseSw.totalRecordedMillis());
		prynt("INIT took {}ms", initSw.totalRecordedMillis());
		prynt("FORWARD took {}ms", forwardSw.totalRecordedMillis());
		prynt("BACKWARD took {}ms", backwardSw.totalRecordedMillis());
		prynt("COMBINE took {}ms", combineSw.totalRecordedMillis());
		prynt("LOOP BODY took {}ms", sw.totalRecordedMillis());
	}

	@Override
	public Object partA() {
		return batchCountMatches(lines.stream().map(Query::fromInput).toList());
	}

	@Override
	public Object partB() {
		parseSw.reset();
		var queries = lines.stream().map(Query::fromInput).map(q -> q.repeat(5, "?")).toList();
//		List<Query> queries = new ArrayList<>(lines.size());
//		for (var line : lines) {
//			queries.add(Query.fromInput(line));
//		}
		parseSw.recordAndReset();
		return batchCountMatches(queries);
	}

	public long batchCountMatches(List<Query> queries) {
		Stopwatch sw = new Stopwatch();
		combineSw.reset();
		long total = 0L;
		for (var q : queries) {
			sw.reset();
			long matches = countVectorized(q.record, q.groups);
			// prynt("{}: {} matches", q, matches);
			sw.recordAndReset();
			total += matches;
		}
		combineSw.recordAndReset();
		return total;
	}

	public static long countMatchesMeetInTheMiddle(String record, List<Long> groups) {
		int mid = record.length() / 2;

		forwardSw.reset();
		var superpositionForward = matchNondeterministic(record, 0, mid, groups);
		forwardSw.recordAndReset();
		backwardSw.reset();
		var superpositionBackward = matchNondeterministicBackwards(record, mid, record.length(), groups);
		backwardSw.recordAndReset();

		combineSw.reset();
		long total = 0L;
		for (var state : superpositionForward.keySet()) {
			long forwardMultiplicity = superpositionForward.getOrDefault(state, 0L);
			long backwardMultiplicity = superpositionBackward.getOrDefault(state, 0L);
			total += forwardMultiplicity * backwardMultiplicity;
		}
		combineSw.recordAndReset();
		return total;
	}

	public static long countMatches(String record, List<Long> groups) {
		forwardSw.reset();
		var superposition = matchNondeterministic(record, groups);

		long total = 0L;
		for (var entry : superposition.entrySet()) {
			State state = entry.getKey();
			long multiplicity = entry.getValue();
			if (state.isAccepted(groups)) {
				total += multiplicity;
			}
		}
		forwardSw.recordAndReset();

		return total;
	}

	public static long countVectorized(String record, List<Long> groups) {
		initSw.reset();
		Context context = new Context(record, groups.stream().mapToInt(l -> (int) (long) l).toArray());
		initSw.recordAndReset();
		forwardSw.reset();
		Superposition superposition = Superposition.initial(context);
		superposition.forward(0, record.length());
		forwardSw.recordAndReset();
		long ret = superposition.countAcceptedStates();
		return ret;
	}

	public static long dummy() {
		long i = 5;
		if (forwardSw == null) {
			i += 3;
		}
		i %= 6;
		return i;
	}

	public static long countVectorizedMeetInTheMiddle(String record, List<Long> groups) {
		Context context = new Context(record, groups.stream().mapToInt(l -> (int) (long) l).toArray());
		int mid = record.length() / 2;
		forwardSw.reset();
		Superposition initial = Superposition.initial(context);
		initial.forward(0, mid);
		forwardSw.recordAndReset();
		backwardSw.reset();
		Superposition accepted = Superposition.accepted(context);
		accepted.backward(mid, record.length());
		backwardSw.recordAndReset();
		combineSw.reset();
		long total = 0L;
		for (int state = initial.hbegin; state < initial.hend; state++) {
			total += initial.histogram[state] * accepted.histogram[state];
		}
		combineSw.recordAndReset();
		return total;
	}

	public static class Context {

		public final String record;
		public final int[] groupSize;

		public final int[] stateToGroup;
		public final int[] stateToHashCount;
		public final boolean[] stateToDotExpected;

		public Context(String record, int[] groups) {
			this.record = record;
			this.groupSize = groups;

			int stateCount = Arrays.stream(groupSize) //
				.map(x -> x + 1) //
				.reduce(0, (x, y) -> x + y) + 2;
			stateToGroup = new int[stateCount];
			stateToHashCount = new int[stateCount];
			stateToDotExpected = new boolean[stateCount];

			int group = 0;
			int hashCount = 0;
			boolean dotExpected = true;
			int state = 0;
			while (group < groupSize.length || (group == groupSize.length && hashCount == 0)) {
				stateToGroup[state] = group;
				stateToHashCount[state] = hashCount;
				stateToDotExpected[state] = dotExpected;

				if (state + 1 == stateCount) {
					state++;
					break;
				}

				if (dotExpected) {
					assert hashCount == 0;
					dotExpected = false;
				} else if (hashCount + 1 == groupSize[group]) {
					group++;
					hashCount = 0;
					dotExpected = true;
				} else {
					hashCount++;
				}

				state++;
			}
			assert state == stateCount;
		}

	}

	public static class Superposition {

		public final Context context;
		public long[] histogram;
		public int hbegin;
		public int hend;

		public Superposition(Context context, long[] histogram, int hbegin, int hend) {
			this.context = context;
			this.histogram = histogram;
			this.hbegin = hbegin;
			this.hend = hend;
		}

		public static Superposition initial(Context context) {
			long[] histogram = new long[context.stateToGroup.length];
			histogram[1] = 1; // group 0, hash count 0, dot not expected
			return new Superposition(context, histogram, 1, 2);
		}

		public static Superposition accepted(Context context) {
			long[] histogram = new long[context.stateToGroup.length];
			histogram[histogram.length - 2] = 1;
			histogram[histogram.length - 1] = 1;
			return new Superposition(context, histogram, histogram.length - 2, histogram.length);
		}

		public long countAcceptedStates() {
			return histogram[histogram.length - 2] + histogram[histogram.length - 1];
		}

		public void forward(int rbegin, int rend) {
			for (int i = rbegin; i < rend; i++) {
				// Eliminate states that are too far behind to catch up
				for (int state = hbegin; state < hend
					&& state + (context.record.length() - i) < histogram.length - 2; state++) {
					histogram[state] = 0L;
				}

				// update non-zero bounds
				updateNonzeroBounds();

				// process character
				char c = context.record.charAt(i);
				for (int state = hend - 1; state >= hbegin; state--) {
					long multiplicity = histogram[state];
					histogram[state] = 0L;
					insertSuccessors(c, multiplicity, state);
				}
			}
		}

		public void backward(int rbegin, int rend) {
			for (int i = rend - 1; i >= rbegin; i--) {
				// Eliminate states that are too far behind to catch up
				for (int state = hend - 1; state >= hbegin && state - (i + 1) > 0; state--) {
					histogram[state] = 0L;
				}

				// update non-zero bounds
				updateNonzeroBounds();

				// process character
				char c = context.record.charAt(i);
				for (int state = hbegin; state < hend; state++) {
					long multiplicity = histogram[state];
					histogram[state] = 0L;
					insertPredecessors(c, multiplicity, state);
				}
			}
		}

		private void updateNonzeroBounds() {
			while (hbegin < histogram.length && histogram[hbegin] == 0L) {
				hbegin++;
			}
			while (hend > 0L && histogram[hend - 1] == 0L) {
				hend--;
			}
		}

		private void insertSuccessors(char c, long multiplicity, int state) {
			if (multiplicity == 0L) {
				return;
			}
			switch (c) {
			case '.':
				if (context.stateToDotExpected[state]) {
					histogram[state + 1] += multiplicity;
					hend = Math.max(hend, state + 2);
				} else if (context.stateToHashCount[state] == 0) {
					histogram[state] += multiplicity;
					hend = Math.max(hend, state + 1);
				}
				return;
			case '#':
				if (context.stateToDotExpected[state] || state + 1 == histogram.length) {
					return;
				}
				histogram[state + 1] += multiplicity;
				hend = Math.max(hend, state + 2);
				return;
			case '?':
				insertSuccessors('.', multiplicity, state);
				insertSuccessors('#', multiplicity, state);
				return;
			}
			throw new RuntimeException();
		}

		private void insertPredecessors(char c, long multiplicity, int state) {
			if (multiplicity == 0L) {
				return;
			}
			switch (c) {
			case '.':
				if (context.stateToDotExpected[state] || context.stateToHashCount[state] > 0) {
					return;
				}
				histogram[state - 1] += multiplicity;
				histogram[state] += multiplicity;
				hbegin = Math.min(hbegin, state - 1);
				return;
			case '#':
				if (state == 0) {
					return;
				}
				if (!context.stateToDotExpected[state] && context.stateToHashCount[state] == 0) {
					return;
				}
				histogram[state - 1] += multiplicity;
				hbegin = Math.min(hbegin, state - 1);
				return;
			case '?':
				insertPredecessors('.', multiplicity, state);
				insertPredecessors('#', multiplicity, state);
				return;
			}
		}
	}

	public static Map<State, Long> matchNondeterministic(String record, List<Long> groups) {
		return matchNondeterministic(record, 0, record.length(), groups);
	}

	public static Map<State, Long> matchNondeterministic(String record, int begin, int end, List<Long> groups) {
		Map<State, Long> superposition = Map.of(State.init(), 1L);
		for (int i = begin; i < end; i++) {
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

	public static Map<State, Long> matchNondeterministicBackwards(String record, List<Long> groups) {
		return matchNondeterministicBackwards(record, 0, record.length(), groups);
	}

	public static Map<State, Long> matchNondeterministicBackwards(String record, int begin, int end,
		List<Long> groups) {
		Map<State, Long> superposition = Map.of( //
			new State(groups.size(), 0, Expectation.DOT), 1L, //
			new State(groups.size(), 0, Expectation.MIXED), 1L);
		for (int i = end - 1; i >= begin; i--) {
			Map<State, Long> nextSuperposition = new LinkedHashMap<>();
			for (var entry : superposition.entrySet()) {
				State state = entry.getKey();
				long multiplicity = entry.getValue();
				state.deconsumeNondeterministic(record.charAt(i), multiplicity, groups, nextSuperposition);
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

		public void deconsumeNondeterministic(char c, long multiplicity, List<Long> groups, Map<State, Long> output) {
			if (c == '?') {
				deconsumeNondeterministic('.', multiplicity, groups, output);
				deconsumeNondeterministic('#', multiplicity, groups, output);
			} else {
				deconsume(c, groups).stream().forEach(s -> output.put(s, output.getOrDefault(s, 0L) + multiplicity));
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

		public List<State> deconsume(char c, List<Long> groups) {
			switch (c) {
			case '.':
				switch (expectation) {
				case DOT:
					return List.of();
				case MIXED:
					if (hashCount == 0) {
						return List.of(this, new State(group, 0, Expectation.DOT));
					} else {
						return List.of();
					}
				}
			case '#':
				switch (expectation) {
				case DOT:
					if (group == 0)
						return List.of();
					int ngroup = group - 1;
					return List.of(new State(ngroup, (int) (long) groups.get(ngroup) - 1, Expectation.MIXED));
				case MIXED:
					if (hashCount == 0)
						return List.of();
					return List.of(new State(group, hashCount - 1, Expectation.MIXED));
				}
			case '?':
				return Stream.concat(deconsume('.', groups).stream(), deconsume('#', groups).stream()).toList();
			}
			throw new RuntimeException();
		}

	}

	public enum Expectation {
		DOT, MIXED
	}

	public record Query(String record, List<Long> groups) {

		public static Query fromInput(String line) {
			String[] split = line.split(" ");
			String[] numstrs = split[1].split(",");
			List<Long> groups = Arrays.stream(numstrs).map(Long::parseLong).toList();
			return new Query(split[0], groups);
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
