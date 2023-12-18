package de.paulr.aoc2023.day18;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil.Direction;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public Solution() {
		super(2023, 18, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	Map<Pos, Direction> nextDirection = new HashMap<>();
	Map<Pos, Direction> prevDirection = new HashMap<>();

	List<Pair<Long, List<Long>>> events = new ArrayList<>();
	SortedSet<Long> verticalPipes = new TreeSet<>();

	public Object partA() {
		Pos pos = Pos.of(0, 0);
		List<Pos> ev = new ArrayList<>();

		for (var line : lines) {
			String[] split = line.split(" ");
			Direction dir = switch (split[0]) {
			case "U" -> Direction.UP;
			case "D" -> Direction.DOWN;
			case "L" -> Direction.LEFT;
			case "R" -> Direction.RIGHT;
			default -> throw new RuntimeException();
			};
			int streak = Integer.parseInt(split[1]);

			ev.add(pos);
			nextDirection.put(pos, dir);
			pos = pos.move(dir, streak);
			prevDirection.put(pos, dir);
		}

		ev.sort(Comparator.comparing(Pos::y).thenComparing(Pos::x));

		for (int i = 0; i < ev.size(); i++) {
			List<Long> xs;
			if (events.size() == 0 || events.get(events.size() - 1).first() < ev.get(i).y()) {
				xs = new ArrayList<>();
				events.add(Pair.of(ev.get(i).y(), xs));
			} else {
				xs = events.get(events.size() - 1).second();
			}
			xs.add(ev.get(i).x());
		}

		return countInnerAndPipes();
	}

	@Override
	public Object partB() {

		Pos pos = Pos.of(0, 0);
		List<Pos> ev = new ArrayList<>();

		for (var line : lines) {
			String[] split = line.split(" ");
			String code = split[2];
			Direction dir = switch (code.charAt(7)) {
			case '3' -> Direction.UP;
			case '1' -> Direction.DOWN;
			case '2' -> Direction.LEFT;
			case '0' -> Direction.RIGHT;
			default -> throw new RuntimeException();
			};
			int streak = Integer.parseInt(code.substring(2, 7), 16);

			ev.add(pos);
			nextDirection.put(pos, dir);
			pos = pos.move(dir, streak);
			prevDirection.put(pos, dir);
		}

		ev.sort(Comparator.comparing(Pos::y).thenComparing(Pos::x));

		for (int i = 0; i < ev.size(); i++) {
			List<Long> xs;
			if (events.size() == 0 || events.get(events.size() - 1).first() < ev.get(i).y()) {
				xs = new ArrayList<>();
				events.add(Pair.of(ev.get(i).y(), xs));
			} else {
				xs = events.get(events.size() - 1).second();
			}
			xs.add(ev.get(i).x());
		}

		return countInnerAndPipes();
	}

	private long countInnerAndPipes() {
		long count = 0L;
		long lastY = 0;
		for (int i = 0; i < events.size(); i++) {
			var line = events.get(i);
			long y = line.first();
			assert verticalPipes.size() % 2 == 0;

			List<Long> xs = new ArrayList<>(verticalPipes);
			if (i > 0 && y > lastY + 1) {
				long gap = y - lastY - 1;
				for (int j = 0; j < xs.size(); j++) {
					if (j % 2 == 1) {
						count += gap * (xs.get(j) - xs.get(j - 1) + 1);
					}
				}
			}

			xs = Stream.of(verticalPipes.stream(), line.second().stream()).flatMap(x -> x).sorted().distinct()
				.collect(Collectors.toCollection(ArrayList::new));

			PipeState state = new TerminalPipeState(false);
			SortedSet<Long> nextVerticalPipes = new TreeSet<>();

			for (int j = 0; j < xs.size(); j++) {
				long x = xs.get(j);
				long gap = j == 0 ? 0L : (x - xs.get(j - 1) - 1);
				char pipe = getPipeAt(Pos.of(x, y));
				if (pipe == '.' && verticalPipes.contains(x)) {
					pipe = '|';
				}
				if (Set.of('|', '7', 'F').contains(pipe)) {
					nextVerticalPipes.add(x);
				}
				if (state.pipeExpected()) {
					count += gap + 1;
				} else if (state.innerTileExpected()) {
					count += gap + 1;
				} else {
					count += 1;
				}
				state = state.next(pipe, true);
			}

			verticalPipes = nextVerticalPipes;
			lastY = y;
		}
		return count;
	}

	private char getPipeAt(Pos pos) {
		Direction pdir = Optional.ofNullable(prevDirection.get(pos)).map(Direction::reverse).orElse(null);
		Direction ndir = nextDirection.get(pos);
		if (pdir == null) {
			return '.';
		}
		if (pdir.isVertical() && ndir.isVertical()) {
			return '|';
		}
		if (pdir.isHorizontal() && ndir.isHorizontal()) {
			return '-';
		}
		var set = Set.of(pdir, ndir);
		if (set.equals(Set.of(Direction.LEFT, Direction.UP))) {
			return 'J';
		}
		if (set.equals(Set.of(Direction.RIGHT, Direction.UP))) {
			return 'L';
		}
		if (set.equals(Set.of(Direction.LEFT, Direction.DOWN))) {
			return '7';
		}
		if (set.equals(Set.of(Direction.RIGHT, Direction.DOWN))) {
			return 'F';
		}
		throw new RuntimeException();
	}

	public enum State {
		INNER, OUTER, ON
	}

	public abstract sealed class PipeState permits TerminalPipeState, HorizontalPipeState {

		public abstract boolean innerTileExpected();

		public abstract boolean pipeExpected();

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

		@Override
		public boolean pipeExpected() {
			return false;
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

		@Override
		public boolean pipeExpected() {
			return true;
		}

	}

}
