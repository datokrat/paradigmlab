package de.paulr.aoc2022.day18;

import static de.paulr.aoc2023.AoCUtil.newlinePsr;
import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;

import java.util.Set;
import java.util.function.Predicate;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class Solution extends ASolution {

	public static IParser<Coord> coordPsr = longNumber.thenSilently(",").then(longNumber).thenSilently(",")
		.then(longNumber) //
		.map(Pair.fn(Coord::new));

	public static IParser<Set<Coord>> coordsPsr = coordPsr.star(newlinePsr).map(Rope::toSet);

	public Solution() {
		super(2022, 18, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partA());
	}

	@Override
	public Object partA() {
		var coords = coordsPsr.parseOne(file);
		long ctr = 0L;
		for (var coord : coords) {
			ctr += coord.neighbors().stream().filter(Predicate.not(coords::contains)).count();
		}
		return ctr;
	}

	@Override
	public Object partB() {
		// TODO Auto-generated method stub
		return null;
	}

	public record Coord(long x, long y, long z) {

		public Set<Coord> neighbors() {
			return Set.of(new Coord(x - 1, y, z), new Coord(x + 1, y, z), new Coord(x, y - 1, z),
				new Coord(x, y + 1, z), new Coord(x, y, z - 1), new Coord(x, y, z + 1));
		}

	}

}
