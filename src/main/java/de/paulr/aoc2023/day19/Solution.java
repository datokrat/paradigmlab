package de.paulr.aoc2023.day19;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public Solution() {
		super(2023, 19, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
//		prynt(s.partA());
		prynt(s.partB());
	}

	public Map<String, Workflow> wfs = new HashMap<>();
	public List<Map<String, Long>> parts = new ArrayList<>();

	public record Workflow(String name, List<Statement> statements, String fallback) {

	}

	public record Statement(String lhs, char op, long rhs, String target) {

		public boolean eval(Map<String, Long> part) {
			long val = part.get(lhs);
			if (op == '<') {
				return val < rhs;
			} else {
				return val > rhs;
			}
		}

	}

	@Override
	public Object partA() {
		parseInput();
		return parts.stream() //
			.filter(this::evaluatePart) //
			.mapToLong(this::sumPart) //
			.sum();
	}

	long sumPart(Map<String, Long> part) {
		return part.values().stream().reduce(0L, (a, b) -> a + b);
	}

	boolean evaluatePart(Map<String, Long> part) {
		String state = "in";
		int i = 0;
		while (!Set.of("A", "R").contains(state)) {
			i++;
			var wf = wfs.get(state);
			boolean done = false;
			for (var stmt : wf.statements) {
				if (stmt.eval(part)) {
					state = stmt.target;
					done = true;
					break;
				}
			}
			if (!done) {
				state = wf.fallback;
			}
		}
		return state.equals("A");
	}

	@Override
	public Object partB() {
		parseInput();
		Set<String> ks = Set.of("x", "m", "a", "s");
		Map<String, Pair<Long, Long>> bounds = ks.stream() //
			.collect(toMap(k -> k, k -> Pair.of(1L, 4001L)));
		return countAccepted("in", 0, bounds);
	}

	public long countAccepted(String state, int stmtIdx, Map<String, Pair<Long, Long>> bounds) {
		if (bounds.values().stream().anyMatch(p -> p.first() >= p.second())) {
			return 0L;
		}
		if (state.equals("R")) {
			return 0L;
		}
		if (state.equals("A")) {
			return bounds.values().stream() //
				.mapToLong(p -> p.second() - p.first()) //
				.reduce(1L, (a, b) -> a * b);
		}

		if (stmtIdx == wfs.get(state).statements.size()) {
			return countAccepted(wfs.get(state).fallback, 0, bounds);
		}
		var stmt = wfs.get(state).statements.get(stmtIdx);
		Map<String, Pair<Long, Long>> b1 = new HashMap<>(bounds);
		Map<String, Pair<Long, Long>> b2 = new HashMap<>(bounds);
		long l = bounds.get(stmt.lhs).first();
		long u = bounds.get(stmt.lhs).second();
		if (stmt.op == '<') {
			b1.put(stmt.lhs, b1.get(stmt.lhs).withSecond(Math.min(u, stmt.rhs)));
			b2.put(stmt.lhs, b2.get(stmt.lhs).withFirst(Math.max(l, stmt.rhs)));
		} else {
			b1.put(stmt.lhs, b1.get(stmt.lhs).withFirst(Math.max(l, stmt.rhs + 1)));
			b2.put(stmt.lhs, b2.get(stmt.lhs).withSecond(Math.min(u, stmt.rhs + 1)));
		}
		long count = 0L;
		count += countAccepted(stmt.target, 0, b1);
		count += countAccepted(state, stmtIdx + 1, b2);
		return count;
	}

	public void parseInput() {
		int i = 0;
		for (; i < lines.size(); i++) {
			if (lines.get(i).isEmpty()) {
				break;
			}

			String[] split1 = lines.get(i).split("\\{");
			String name = split1[0];
			String[] split2 = split1[1].substring(0, split1[1].length() - 1).split(",");
			List<Statement> stmts = new ArrayList<>();
			for (int j = 0; j < split2.length - 1; j++) {
				String[] split3 = split2[j].split(":");
				String targetName = split3[1];
				String[] split4 = split3[0].split("<|>");
				char op = split3[0].contains("<") ? '<' : '>';
				String testVar = split4[0];
				long testVal = Long.parseLong(split4[1]);
				stmts.add(new Statement(testVar, op, testVal, targetName));
			}
			wfs.put(name, new Workflow(name, stmts, split2[split2.length - 1]));
		}

		i++;

		for (; i < lines.size(); i++) {
			String stripped = lines.get(i).substring(1, lines.get(i).length() - 1);
			String[] assignments = stripped.split(",");
			Map<String, Long> part = new HashMap<>();
			for (var a : assignments) {
				String[] kv = a.split("=");
				part.put(kv[0], Long.parseLong(kv[1]));
			}
			parts.add(part);
		}
	}

}
