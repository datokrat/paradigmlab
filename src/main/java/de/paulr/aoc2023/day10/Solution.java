package de.paulr.aoc2023.day10;

import static de.paulr.aoc2023.AoCUtil.input;
import static de.paulr.aoc2023.AoCUtil.prynt;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class Solution {

	public static void main(String[] args) {
		var solution = new Solution();
		solution.readInput("10.txt");
		prynt(solution.partB());
	}

	// Computation

	public Object partA() {
		var cycle = findCycle(animalPos);
		for (int y = 0; y < lines.size(); y++) {
			for (int x = 0; x < lines.get(y).length(); x++) {
				Pos p = new Pos(x, y);
				if (cycle.contains(p)) {
					System.out.print('X');
				} else {
					System.out.print(get(p));
				}
			}
			System.out.println();
		}
		System.out.println();

		for (int y = 0; y < lines.size(); y++) {
			for (int x = 0; x < lines.get(y).length(); x++) {
				Pos p = new Pos(x, y);
				System.out.print(get(p));
			}
			System.out.println();
		}
		return cycle.size() / 2;
	}

	public Object partB() {
		Set<Pos> innerTiles = new HashSet<>();
		var cycle = findCycle(animalPos);
		replaceS(animalPos);
		for (int y = 0; y < lines.size(); y++) {
			PipeState state = new TerminalPipeState(false);
			for (int x = 0; x < lines.get(y).length(); x++) {
				boolean partOfCycle = cycle.contains(new Pos(x, y));
				if (!partOfCycle && state.innerTileExpected()) {
					innerTiles.add(new Pos(x, y));
				}
				state = state.next(get(new Pos(x, y)), partOfCycle);
			}
		}
		visualize(innerTiles);
		visualize(cycle);
		return innerTiles.size();
	}

	public abstract sealed class PipeState permits TerminalPipeState, HorizontalPipeState {

		public abstract boolean innerTileExpected();

		public abstract PipeState next(char c, boolean partOfCycle);

	}

	public final class TerminalPipeState extends PipeState {

		private boolean rightIsInner;

		public TerminalPipeState(boolean rightIsInner) {
			this.rightIsInner = rightIsInner;
		}

		@Override
		public boolean innerTileExpected() {
			return rightIsInner;
		}

		@Override
		public PipeState next(char c, boolean partOfCycle) {
			if (!partOfCycle) {
				c = '.';
			}
			return switch (c) {
			case 'F' -> new HorizontalPipeState(rightIsInner);
			case 'L' -> new HorizontalPipeState(!rightIsInner);
			case '|' -> new TerminalPipeState(!rightIsInner);
			case '-', 'J', '7' -> throw new RuntimeException();
			default -> new TerminalPipeState(rightIsInner);
			};
		}

	}

	public final class HorizontalPipeState extends PipeState {

		private boolean upIsInner;

		public HorizontalPipeState(boolean upIsInner) {
			this.upIsInner = upIsInner;
		}

		@Override
		public boolean innerTileExpected() {
			return false;
		}

		@Override
		public PipeState next(char c, boolean partOfCycle) {
			if (!partOfCycle) {
				c = '.';
			}
			return switch (c) {
			case '-' -> this;
			case 'J' -> new TerminalPipeState(!upIsInner);
			case '7' -> new TerminalPipeState(upIsInner);
			case 'F', 'L', '|' -> throw new RuntimeException();
			default -> throw new RuntimeException();
			};
		}

	}

	public void visualize(Set<Pos> set) {
		for (int y = 0; y < lines.size(); y++) {
			for (int x = 0; x < lines.get(y).length(); x++) {
				Pos p = new Pos(x, y);
				if (set.contains(p)) {
					System.out.print('X');
				} else {
					System.out.print(get(p));
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	public Set<Pos> findCycle(Pos start) {
		Set<Pos> cycle = new HashSet<>();
		Deque<Pos> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			var pos = queue.removeLast(); // DFS
			if (cycle.contains(pos)) {
				continue;
			}

			cycle.add(pos);
			queue.addAll(getConnectedPipes(pos).toList());
		}
		return cycle;
	}

	public void replaceS(Pos start) {
		List<Pos> cycleList = new ArrayList<>();
		Set<Pos> cycle = new HashSet<>();
		Deque<Pos> queue = new ArrayDeque<>();
		queue.add(start);
		while (!queue.isEmpty()) {
			var pos = queue.removeLast(); // DFS
			if (cycle.contains(pos)) {
				continue;
			}

			cycle.add(pos);
			cycleList.add(pos);
			queue.addAll(getConnectedPipes(pos).toList());
		}
		Set<Pos> neighbors = Set.of(cycleList.get(1), cycleList.get(cycleList.size() - 1));
		lines = new ArrayList<>(lines);
		for (var c : List.of('F', 'J', 'L', '7', '-', '|')) {
			var l = lines.get((int) animalPos.y);
			lines.set((int) animalPos.y, l.substring(0, (int) animalPos.x) + c + l.substring((int) animalPos.x + 1));
			if (neighbors.equals(getPipeEnds(animalPos).collect(toSet()))) {
				return;
			}
		}
	}

	public Stream<Pos> getConnectedPipes(Pos pos) {
		char c = get(pos);
		if (c == 'S') {
			return getNeighbors(pos).filter(n -> getPipeEnds(n).anyMatch(pos::equals));
		}
		return getPipeEnds(pos);
	}

	public Stream<Pos> getPipeEnds(Pos pos) {
		if (get(pos) == 'S') {
			throw new RuntimeException();
		}
		return switch (get(pos)) {
		case 'J' -> Stream.of(pos.up(), pos.left());
		case 'L' -> Stream.of(pos.right(), pos.up());
		case '7' -> Stream.of(pos.left(), pos.down());
		case 'F' -> Stream.of(pos.right(), pos.down());
		case '|' -> Stream.of(pos.up(), pos.down());
		case '-' -> Stream.of(pos.left(), pos.right());
		default -> Stream.of();
		};
	}

	public char get(Pos p) {
		if (p.y < 0 || p.y >= lines.size()) {
			return '.';
		}
		var line = lines.get((int) p.y);
		if (p.x < 0 || p.x >= line.length()) {
			return '.';
		}
		return lines.get((int) p.y).charAt((int) p.x);
	}

	public Stream<Pos> getNeighbors(Pos pos) {
		Pos[] positions = new Pos[4];
		return Stream.of(new Pos(pos.x - 1, pos.y), new Pos(pos.x + 1, pos.y), //
				new Pos(pos.x, pos.y - 1), new Pos(pos.x, pos.y + 1));
	}

	public record Pos(long x, long y) {

		public Pos left() {
			return new Pos(x - 1, y);
		}

		public Pos right() {
			return new Pos(x + 1, y);
		}

		public Pos up() {
			return new Pos(x, y - 1);
		}

		public Pos down() {
			return new Pos(x, y + 1);
		}

	}

	// Input

	public List<String> lines;

	public Pos animalPos;

	public void readInput(String filename) {
		lines = input(filename);
		for (var y = 0; y < lines.size(); y++) {
			var line = lines.get(y);
			for (var x = 0; x < line.length(); x++) {
				var pos = new Pos(x, y);
				if (get(pos) == 'S') {
					animalPos = pos;
					return;
				}
			}
		}
	}

}
