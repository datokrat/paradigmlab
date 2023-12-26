package de.paulr.aoc2023.day23;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil.CharMatrix;
import de.paulr.aoc2023.AoCUtil.Direction;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public CharMatrix matrix;
	public Pos start, end;
	public Map<Pos, Map<Pos, Long>> neighborhood;

	public Solution() {
		super(2023, 23, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		parseInput();
		long maxPath = 0L;
		Deque<State> deque = new ArrayDeque<>();
		deque.addLast(State.initial(start));
		while (!deque.isEmpty()) {
			var state = deque.removeLast(); // DFS
			if (state.pos.equals(end)) {
				maxPath = Math.max(maxPath, state.distanceTravelled());
			}
			for (var succ : proceedA(state)) {
				deque.addLast(succ);
			}
		}
		return maxPath;
	}

	@Override
	public Object partB() {
		parseInput();
		preprocess();
		long maxPath = 0L;
		Deque<State> deque = new ArrayDeque<>();
		deque.addLast(State.initial(start));
		int i = 0;
		while (!deque.isEmpty()) {
			var state = deque.removeLast(); // DFS
			if (state.pos.equals(end)) {
				maxPath = Math.max(maxPath, state.distanceTravelled());
			}
			for (var succ : proceedB(state)) {
				deque.addLast(succ);
			}
			i++;
			if (i % 100_000 == 0) {
				prynt(i);
			}
		}
		return maxPath;
	}

	public List<State> proceedA(State state) {
		List<State> states = new ArrayList<>();
		Direction slopeDir = Direction.ofChar(matrix.get(state.pos));
		for (var d : Direction.values()) {
			Pos candidate = state.pos.move(d);
			if (!matrix.containsKey(candidate)) {
				continue;
			}
			if (!state.visited(candidate) && (slopeDir == null || slopeDir == d) && matrix.get(candidate) != '#') {
				states.add(state.step(candidate));
			}
		}
		return states;
	}

	public List<State> proceedB(State state) {
		List<State> states = new ArrayList<>();
		for (var e : neighborhood.get(state.pos).entrySet()) {
			Pos candidate = e.getKey();
			long distance = e.getValue();
			if (!state.visited(candidate)) {
				states.add(state.step(candidate, distance));
			}
		}
		return states;
	}

	public void preprocess() {
		neighborhood = new HashMap<>();
		Deque<Pos> deque = new ArrayDeque<>();
		deque.addLast(start);
		while (!deque.isEmpty()) {
			var pos = deque.removeLast();
			if (neighborhood.containsKey(pos)) {
				continue;
			}
			neighborhood.put(pos, new HashMap<>());
			for (var next : collectNext(null, pos)) {
				Pair<Pos, Long> crossing = toCrossing(pos, next);
				neighborhood.get(pos).put(crossing.first(), crossing.second());
				deque.addLast(crossing.first());
			}
		}
	}

	public Pair<Pos, Long> toCrossing(Pos last, Pos pos) {
		List<Pos> next = List.of(pos);
		pos = last;
		long stepsFromLast = 0;
		do {
			stepsFromLast++;
			last = pos;
			pos = next.get(0);
			next = collectNext(last, pos);
		} while (next.size() == 1);
		return Pair.of(pos, stepsFromLast);
	}

	public List<Pos> collectNext(Pos last, Pos pos) {
		List<Pos> next = new ArrayList<>();
		for (var d : Direction.values()) {
			var candidate = pos.move(d);
			if (!matrix.containsKey(candidate) || candidate.equals(last)) {
				continue;
			}
			char c = matrix.get(candidate);
			if (c == '#') {
				continue;
			}
			next.add(candidate);
		}
		return next;
	}

	public void parseInput() {
		matrix = new CharMatrix(lines);
		start = Pos.of(lines.get(0).indexOf('.'), 0);
		end = Pos.of(lines.get(lines.size() - 1).indexOf('.'), lines.size() - 1);
	}

	public record State(Pos pos, Set<Pos> visitedRecently, State pred, long distanceTravelled) {

		public static State initial(Pos pos) {
			return new State(pos, Set.of(pos), null, 0L);
		}

		public State step(Pos next) {
			return step(next, 1L);
		}

		public State step(Pos next, long distance) {
			if (visitedRecently.size() < 100) {
				Set<Pos> newVisitedRecently = new HashSet<>(visitedRecently);
				newVisitedRecently.add(next);
				return new State(next, newVisitedRecently, pred, distanceTravelled + distance);
			} else {
				return new State(next, Set.of(next), this, distanceTravelled + distance);
			}
		}

		public boolean visited(Pos pos) {
			return visitedRecently.contains(pos) || (pred != null && pred.visited(pos));
		}

	}

}
