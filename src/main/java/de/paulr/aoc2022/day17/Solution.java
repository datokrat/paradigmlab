package de.paulr.aoc2022.day17;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;

class Solution extends ASolution {

	public final static String FILE = "2022_17.txt";
	public final static String EXAMPLE = "2022_17s.txt";

	public static final List<String> ROCKS = List.of( //
		"""
			####
			""", """
			.#.
			###
			.#.
			""", """
			..#
			..#
			###
			""", """
			#
			#
			#
			#
			""", """
			##
			##
			""");

	static int width = 7;
	static int leftsep = 2;
	static int downsep = 3;

	List<List<Character>> arena;
	int rock = -1;
	int rockWidth;
	Pair<Integer, Integer> position;

	public static void main(String[] args) {
		var s = new Solution(FILE);
		s.partB();
	}

	public Solution(String filename) {
		super(filename);
	}

	@Override
	public Object partA() {
		arena = new ArrayList<>();
		rock = -1;

		int rocksFinished = 0;
		for (int i = 0; rocksFinished < 2022; i++) {
			if (rock == -1) {
				spawnRock(rocksFinished % ROCKS.size());
			}

			var cmd = lines.get(0).charAt(i % lines.get(0).length());
			Pair<Integer, Integer> oldPos = position;
			if (cmd == '<') {
				position = position.withFirst(Math.max(0, position.first() - 1));
			} else {
				position = position.withFirst(Math.min(width - rockWidth, position.first() + 1));
			}

			if (collision()) {
				position = oldPos;
			}

			oldPos = position;
			position = position.withSecond(position.second() - 1);

			if (position.second() == -1 || collision()) {
				position = oldPos;
				consolidate(rock, position, arena);
				rock = -1;
				rocksFinished++;
				continue;
			}
		}

		prynt(arena.size());
		return arena.size();
	}

	public static void consolidate(int rock, Pair<Integer, Integer> position, List<List<Character>> arena) {
		rockSet(rock, position).stream().forEach(p -> addToArena(p, arena));
	}

	public boolean collision() {
		return rockSet(rock, position).stream().anyMatch(x -> inArena(x));
	}

	private void spawnRock(int type) {
		rock = type;
		rockWidth = ROCKS.get(rock).lines().findFirst().get().length();
		position = Pair.of(leftsep, arena.size() + downsep);

	}

	public boolean inArena(Pair<Integer, Integer> pos) {
		if (pos.first() >= width || pos.first() < 0)
			return false;
		if (pos.second() >= arena.size() || pos.second() < 0)
			return false;

		return arena.get(pos.second()).get(pos.first()) == '#';
	}

	public static void addToArena(Pair<Integer, Integer> pos, List<List<Character>> arena) {
		if (pos.first() >= width || pos.first() < 0)
			throw new RuntimeException();
		if (pos.second() < 0)
			throw new RuntimeException();

		while (pos.second() >= arena.size()) {
			addEmptyRow(arena);
		}

		arena.get(pos.second()).set(pos.first(), '#');
	}

	private static Set<Pair<Integer, Integer>> rockSet(int rockType, Pair<Integer, Integer> lowerLeft) {
		Set<Pair<Integer, Integer>> set = new HashSet<>();
		var rows = ROCKS.get(rockType).lines().toList();
		for (int y = 0; y < rows.size(); y++) {
			var row = rows.get(rows.size() - 1 - y);
			for (int i = 0; i < row.length(); i++) {
				char c = row.charAt(i);
				if (c == '#') {
					set.add(Pair.of(i + lowerLeft.first(), y + lowerLeft.second()));
				}
			}
		}
		return set;
	}

	private static void addEmptyRow(Collection<List<Character>> a) {
		List<Character> row = CollectionUtils.repeat('.', width).collect(Collectors.toCollection(ArrayList::new));
		a.add(row);
	}

	public void prune() {
		while (!arena.isEmpty() && arena.get(arena.size() - 1).stream().allMatch(c -> c == '.')) {
			arena.remove(arena.size() - 1);
		}
	}

	@Override
	public Object partB() {
		var state = new State(0, List.of(), 0, 0);
		for (int i = 0; i < 2022; i++) {
			state = state.propagateRock(lines.get(0));
			prynt(state);
		}
		return null;
	}

	public void pryntArena() {
		for (var line : arena) {
			for (var c : line) {
				System.out.print(c);
			}
			System.out.println();
		}
	}

	public static final int LOOKBEHIND = 200;

	public record State(long height, List<List<Character>> arena, int rock, int cmd) {

		public State propagateRock(String cmds) {
			int ncmd = cmd;
			int rockWidth = ROCKS.get(rock).lines().findFirst().get().length();
			Pair<Integer, Integer> position = Pair.of(leftsep, arena.size() + downsep);
			Pair<Integer, Integer> oldPos = null;

			while (position.second() >= 0 && !collision(position)) {
				oldPos = position;
				if (cmds.charAt(cmd) == '<') {
					position = position.withFirst(Math.max(0, position.first() - 1));
				} else {
					position = position.withFirst(Math.min(width - rockWidth, position.first() + 1));
				}

				if (collision(position)) {
					position = oldPos;
				}

				oldPos = position;
				position = position.withSecond(position.second() - 1);
				ncmd = (ncmd + 1) % cmds.length();
			}

			position = oldPos;
			List<List<Character>> arena2 = new ArrayList<>(arena);
			consolidate(rock, position, arena2);
			int dheight = arena2.size() - arena.size();
			long nheight = height + dheight;
			var narena = arena2.subList(Math.max(0, arena2.size() - LOOKBEHIND), arena2.size());
			int nrock = (rock + 1) % ROCKS.size();
			return new State(nheight, narena, nrock, ncmd);
		}

		public boolean collision(Pair<Integer, Integer> position) {
			return rockSet(rock, position).stream().anyMatch(this::inArena);
		}

		public boolean inArena(Pair<Integer, Integer> pos) {
			if (pos.first() >= width || pos.first() < 0)
				return false;
			if (pos.second() >= arena.size() || pos.second() < 0)
				return false;

			return arena.get(pos.second()).get(pos.first()) == '#';
		}

	}

}
