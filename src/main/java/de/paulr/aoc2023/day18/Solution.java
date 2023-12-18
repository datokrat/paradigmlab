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
import de.paulr.aoc2023.PipeEater;
import de.paulr.aoc2023.PipeEater.PipePart;
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

	List<Pos> eventList = new ArrayList<>();
	List<Pair<Long, List<Long>>> events = new ArrayList<>();
	SortedSet<Long> verticalPipes = new TreeSet<>();

	public Object partA() {
		Pos pos = Pos.of(0, 0);
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

			eventList.add(pos);
			nextDirection.put(pos, dir);
			pos = pos.move(dir, streak);
			prevDirection.put(pos, dir);
		}

		return countInnerAndPipes();
	}

	@Override
	public Object partB() {

		Pos pos = Pos.of(0, 0);
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

			eventList.add(pos);
			nextDirection.put(pos, dir);
			pos = pos.move(dir, streak);
			prevDirection.put(pos, dir);
		}

		return countInnerAndPipes();
	}

	private long countInnerAndPipes() {
		eventList.sort(Comparator.comparing(Pos::y).thenComparing(Pos::x));

		for (int i = 0; i < eventList.size(); i++) {
			List<Long> xs;
			if (events.size() == 0 || events.get(events.size() - 1).first() < eventList.get(i).y()) {
				xs = new ArrayList<>();
				events.add(Pair.of(eventList.get(i).y(), xs));
			} else {
				xs = events.get(events.size() - 1).second();
			}
			xs.add(eventList.get(i).x());
		}

		long count = 0L;
		long lastY = 0;
		PipeEater eater = new PipeEater();
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

			eater.reset();
			SortedSet<Long> nextVerticalPipes = new TreeSet<>();

			for (int j = 0; j < xs.size(); j++) {
				long x = xs.get(j);
				long gap = j == 0 ? 0L : (x - xs.get(j - 1) - 1);
				PipePart pipe = getPipeAt(Pos.of(x, y));

				if (pipe == PipePart.NONE) {
					pipe = PipePart.VERTICAL; // pipe comes from verticalPipes
				}

				if (pipe.getEnds().contains(Direction.DOWN)) {
					nextVerticalPipes.add(x);
				}

				eater.next(pipe);
				if (eater.lastIsInner() || eater.lastIsPipe()) {
					count += gap + 1;
				} else {
					count += 1;
				}
			}

			verticalPipes = nextVerticalPipes;
			lastY = y;
		}
		return count;
	}

	private PipePart getPipeAt(Pos pos) {
		Direction pdir = Optional.ofNullable(prevDirection.get(pos)).map(Direction::reverse).orElse(null);
		Direction ndir = nextDirection.get(pos);
		if (pdir == null) {
			return PipePart.NONE;
		}
		return PipePart.ofEnds(Set.of(pdir, ndir));
	}

}
