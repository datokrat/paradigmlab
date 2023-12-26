package de.paulr.aoc2023.day22;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class Solution extends ASolution {

	public IParser<Pos3d> posPsr = longNumber.plus(",").map(Rope.fn(Pos3d::new));
	public IParser<Brick> brickPsr = posPsr.thenSilently("~").then(posPsr).map(Pair.fn(Brick::new));

	public List<Brick> bricks;
	public Map<Pos3d, Brick> consolidatedBricks;
	public Map<Brick, Set<Brick>> support;
	public Map<Brick, Set<Brick>> dependants;

	public Map<Brick, Set<Brick>> fallingBricks;

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	public Solution() {
		super(2023, 22, "");
	}

	public void preprocess() {
		consolidatedBricks = new HashMap<>();
		support = new HashMap<>();
		dependants = new HashMap<>();
		PriorityQueue<Brick> pq = new PriorityQueue<>(Comparator.comparing(Brick::depth));
		pq.addAll(bricks);
		while (!pq.isEmpty()) {
			var brick = pq.remove();
			Set<Brick> brickSupport = new HashSet<>();
			for (long z = brick.depth(); z > 0; z--) {
				for (var pos : brick.positions()) {
					if (consolidatedBricks.containsKey(pos.withZ(z))) {
						brickSupport.add(consolidatedBricks.get(pos.withZ(z)));
					}
				}
				if (!brickSupport.isEmpty()) {
					for (var pos : brick.positions()) {
						consolidatedBricks.put(pos.moveZ(z + 1 - brick.depth()), brick);
					}
					support.put(brick, brickSupport);
					break;
				} else if (z == 1) {
					for (var pos : brick.positions()) {
						consolidatedBricks.put(pos.moveZ(1 - brick.depth()), brick);
					}
					support.put(brick, brickSupport);
					break;
				}
			}
		}

		for (var brick : bricks) {
			dependants.put(brick, new HashSet<>());
		}

		for (var brick : bricks) {
			for (var supporter : support.get(brick)) {
				dependants.get(supporter).add(brick);
			}
		}
	}

	@Override
	public Object partA() {
		parseInput();
		preprocess();

		long count = 0L;
		for (var brick : bricks) {
			if (dependants.get(brick).stream().allMatch(d -> support.get(d).size() > 1)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public Object partB() {
		parseInput();
		preprocess();

		long count = 0L;

		for (var brick : bricks) {
			Set<Brick> fallenBricks = new HashSet<>();
			fallenBricks.add(brick);
			Deque<Brick> queue = new ArrayDeque<>();
			queue.addAll(dependants.get(brick));
			while (!queue.isEmpty()) {
				var currentBrick = queue.removeFirst();
				if (fallenBricks.contains(currentBrick)) {
					continue;
				}
				if (fallenBricks.containsAll(support.get(currentBrick))) {
					fallenBricks.add(currentBrick);
					queue.addAll(dependants.get(currentBrick));
				}
			}

			count += fallenBricks.size() - 1;
		}

		return count;
	}

	public void parseInput() {
		bricks = new ArrayList<>();
		for (var line : lines) {
			bricks.add(brickPsr.parseOne(line));
		}
	}

	public record Brick(Pos3d p1, Pos3d p2) {

		public boolean isVertical() {
			return p1.z() == p2.z();
		}

		public long depth() {
			return Math.min(p1.z(), p2.z());
		}

		public Set<Pos3d> positions() {
			return CollectionUtils.rangeIncludingEnds(p1.x(), p2.x()) //
				.flatMap(x -> CollectionUtils.rangeIncludingEnds(p1.y(), p2.y()) //
					.flatMap(y -> CollectionUtils.rangeIncludingEnds(p1.z(), p2.z()) //
						.map(z -> new Pos3d(x, y, z)))) //
				.collect(toSet());
		}

	}

	public record Pos3d(long x, long y, long z) {

		public static Pos3d of(long x, long y, long z) {
			return new Pos3d(x, y, z);
		}

		public Pos3d withZ(long newZ) {
			return Pos3d.of(x, y, newZ);
		}

		public Pos3d moveZ(long diffZ) {
			return Pos3d.of(x, y, z + diffZ);
		}

	}

}
