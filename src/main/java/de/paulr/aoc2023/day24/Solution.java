package de.paulr.aoc2023.day24;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.longNumber;
import static de.paulr.parser.Parsers.regex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.paulr.aoc2023.ASolution;
import de.paulr.parser.IParser;
import de.paulr.util.Pair;
import de.paulr.util.Rope;

class Solution extends ASolution {

	public IParser<Pos3d> posPsr = longNumber.plus(regex(",\\s*")).map(Rope.fn(Pos3d::new));
	public IParser<Pair<Pos3d, Pos3d>> phasePsr = posPsr.thenSilently(regex("\\s*@\\s*")).then(posPsr);
	public List<Pair<Pos3d, Pos3d>> phases;

	public static long MIN = 200_000_000_000_000L;
	public static long MAX = 400_000_000_000_000L;

//	public static long MIN = 7L;
//	public static long MAX = 27L;

	public Solution() {
		super(2023, 24, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		parseInput();
		long count = 0L;
		for (int i = 0; i < phases.size(); i++) {
			for (int j = i + 1; j < phases.size(); j++) {
				if (doLinesIntersectA(phases.get(i), phases.get(j))) {
					count++;
				}
			}
		}
		return count;
	}

	public Object partB() {
		parseInput();

		IntegralMatrix a = new IntegralMatrix(4, 3);
		a.set(3, 0, BigInteger.ONE);
		a.set(3, 1, BigInteger.ONE);
		a.set(3, 2, BigInteger.ZERO);
		for (int y = 0; y < 3; y++) {
			a.set(y, 0, phases.get(0).second().get(y));
			a.set(y, 1, phases.get(1).second().get(y));
			a.set(y, 2, phases.get(0).first().minus(phases.get(1).first()).get(y));
		}

		IntegralMatrix b = new IntegralMatrix(4, 4);
		for (int i = 0; i < 4; i++) {
			b.set(i, i, BigInteger.ONE);
		}
		MatrixInversionTableau tableau = new MatrixInversionTableau(a, b);
		tableau.eliminate(0, 1, 0);
		tableau.eliminate(0, 2, 0);
		tableau.eliminate(0, 3, 0);
		tableau.eliminate(1, 2, 1);
		tableau.eliminate(1, 3, 1);

		tableau.eliminate(3, 0, 2);
		tableau.eliminate(3, 1, 2);
		tableau.eliminate(3, 2, 2);

		tableau.eliminate(1, 0, 1);

		for (long tc = 0; tc < 1000L; tc++) {
			if (check(phases.get(0), phases.get(1), phases.get(3), tc, tableau)) {
				prynt(tc);
			}
		}
		return "Nothing found";
	}

	public Pos4d invertWithTableau(MatrixInversionTableau tableau, Pos4d z) {
		Pos4d result = new Pos4d(0, 0, 0, 0);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				// result.set(i, result.get(i) + )
			}
		}
		return null;
	}

	public Pos4d invertForSample(Pos4d z) {
		long c1 = -2 * (z.y + 6 * z.x + 7 * z.t);
		long c1alt = z.z - 8 * z.x - 6 * z.t;
		long c2 = 8 * z.x - z.z + 14 * z.t;
		long c3 = 2 * z.t + z.z;
		return new Pos4d(c1, c1alt, c2, c3);
	}

	public boolean check(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b, Pair<Pos3d, Pos3d> c, long tc,
		MatrixInversionTableau tableau) {
		Pos4d z = Pos4d.ofSpacetime(c.first().add(c.second().times(tc)).minus(a.first()), tc);
		Pos4d w1 = Pos4d.ofSpacetime(a.second(), 1);
		Pos4d w2 = Pos4d.ofSpacetime(b.second(), 1);
		Pos4d w3 = Pos4d.ofSpacetime(a.first().minus(b.first()), 0);
		// Find the coefficients for w1 and w2; if a collision happens, they should be
		// whole, I hope
		// prynt("{}, {}, {}", w1, w2, w3);

		// I inverted the matrix manually...
		Pos4d inverse = invertForSample(z);
		long c1 = inverse.x;
		long c1alt = inverse.y;
		long c2 = inverse.z;
		long c3 = inverse.t;
//		long c1 = -2 * (z.y + 6 * z.x + 7 * z.t);
//		long c1alt = z.z - 8 * z.x - 6 * z.t;
//		long c2 = 8 * z.x - z.z + 14 * z.t;
//		long c3 = -4 * z.x + 2 * z.y - 2 * z.t;

		if (c1 != c1alt) {
			return false;
		}
		assert z.times(8).equals(w1.times(c1).plus(w2.times(c2)).plus(w3.times(c3)));
//		if (c3 % 8 != 0) {
//			return false;
//		}
		if (c2 % c3 != 0) {
			return false;
		}
		if (c1 % (8 + c3) != 0) {
			return false;
		}

		long t1 = c1 / (8 + c3);
		long t2 = -c2 / c3;
		Pos3d p1 = a.first().add(a.second().times(t1));
		Pos3d p2 = b.first().add(b.second().times(t2));
		Pos3d velocity = p1.minus(p2).dividedBy(t1 - t2);
		Pos3d start = p1.minus(velocity.times(t1));

		assert p1.minus(p2).equals(velocity.times(t1 - t2));

		for (int i = 0; i < phases.size(); i++) {
			if (!isCollision(Pair.of(start, velocity), phases.get(i))) {
				return false;
			}
		}

		return true;
	}

	public boolean isCollision(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b) {
		Pos3d dstart = b.first().minus(a.first());
		Pos3d dvelocity = b.second().minus(a.second());

		long t;
		if (dvelocity.x != 0) {
			t = -dstart.x / dvelocity.x;
		} else if (dvelocity.y != 0) {
			t = -dstart.y / dvelocity.y;
		} else if (dvelocity.z != 0) {
			t = -dstart.z / dvelocity.z;
		} else {
			throw new RuntimeException();
		}

		if (t < 0) {
			return false;
		}

		return dvelocity.times(-t).equals(dstart);
	}

	public boolean doLinesIntersectA(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b) {
		prynt("");
		prynt("Hailstone A: {}, {}, {} @ {}, {}, {}", a.first().x, a.first().y, a.first().z, a.second().x, a.second().y,
			a.second().z);
		prynt("Hailstone B: {}, {}, {} @ {}, {}, {}", b.first().x, b.first().y, b.first().z, b.second().x, b.second().y,
			b.second().z);

		BigPos3d ba = xyToBigPos(a.first()).cross(xyToBigPos(a.first().add(a.second())));
		BigPos3d bb = xyToBigPos(b.first()).cross(xyToBigPos(b.first().add(b.second())));
		BigPos3d c = ba.cross(bb);

		if (c.z.equals(BigInteger.ZERO)) {
			prynt("Parallel");
			return false;
		}

		BigFractional x = new BigFractional(c.x, c.z);
		BigFractional y = new BigFractional(c.y, c.z);

		prynt("Paths will cross at x={}, y={}", x.toDouble(), y.toDouble());

		if (!isValid(x) || !isValid(y)) {
			prynt("...outside the test area");
			return false;
		}

		prynt("...INSIDE the test area");
		if (!isIntersectionFuture(a, x, y) || !isIntersectionFuture(b, x, y)) {
			prynt("...but in the past");
			return false;
		} else {
			prynt("...in the FUTURE");
			return true;
		}
	}

	public boolean isIntersectionFuture(Pair<Pos3d, Pos3d> phase, BigFractional x, BigFractional y) {
		if (phase.second().x != 0) {
			return x.numberEquals(phase.first().x) || ((phase.second().x > 0) == (x.compareTo(phase.first().x) > 0));
		} else if (phase.second().y != 0) {
			return y.numberEquals(phase.first().y) || ((phase.second().y > 0) == (y.compareTo(phase.first().y) > 0));
		} else {
			throw new IllegalStateException();
		}
	}

	public boolean isValid(BigFractional x) {
		return x.compareTo(BigFractional.of(MIN)) >= 0 && x.compareTo(BigFractional.of(MAX)) <= 0;
	}

	public BigPos3d xyToBigPos(Pos3d pos) {
		return new BigPos3d(BigInteger.valueOf(pos.x), BigInteger.valueOf(pos.y), BigInteger.ONE);
	}

	public void parseInput() {
		phases = new ArrayList<>();
		for (var line : lines) {
			phases.add(phasePsr.parseOne(line));
		}
	}

	public record BigFractional(BigInteger n, BigInteger d) {

		public static BigFractional of(long x) {
			return new BigFractional(BigInteger.valueOf(x), BigInteger.ONE);
		}

		public int compareTo(long other) {
			return compareTo(BigFractional.of(other));
		}

		public int compareTo(BigFractional other) {
			return d.signum() * other.d.signum() * n.multiply(other.d).compareTo(other.n.multiply(d));
		}

		public boolean numberEquals(long other) {
			return compareTo(other) == 0;
		}

		public double toDouble() {
			return n.multiply(BigInteger.valueOf(100L)).divide(d).doubleValue() / 100D;
		}

	}

	public record BigPos3d(BigInteger x, BigInteger y, BigInteger z) {

		public BigPos3d(long x, long y, long z) {
			this(BigInteger.valueOf(x), BigInteger.valueOf(y), BigInteger.valueOf(z));
		}

		public BigPos3d cross(BigPos3d other) {
			return new BigPos3d(y.multiply(other.z).subtract(z.multiply(other.y)),
				z.multiply(other.x).subtract(x.multiply(other.z)), x.multiply(other.y).subtract(y.multiply(other.x)));
		}

	}

	public record Pos3d(long x, long y, long z) {

		public long get(int index) {
			return switch (index) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			default -> throw new RuntimeException();
			};
		}

		public Pos3d dividedBy(long factor) {
			return new Pos3d(x / factor, y / factor, z / factor);
		}

		public Pos3d minus(Pos3d other) {
			return add(other.times(-1L));
		}

		public Pos3d add(Pos3d other) {
			return new Pos3d(x + other.x, y + other.y, z + other.z);
		}

		public Pos3d times(long factor) {
			return new Pos3d(factor * x, factor * y, factor * z);
		}

	}

	public record Pos4d(long x, long y, long z, long t) {

		public static Pos4d ofSpacetime(Pos3d space, long time) {
			return new Pos4d(space.x, space.y, space.z, time);
		}

		public Pos4d plus(Pos4d other) {
			return new Pos4d(x + other.x, y + other.y, z + other.z, t + other.t);
		}

		public Pos4d times(long factor) {
			return new Pos4d(factor * x, factor * y, factor * z, factor * t);
		}

		public Pos3d space() {
			return new Pos3d(x, y, z);
		}

		public long get(int index) {
			return switch (index) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			case 3 -> t;
			default -> throw new RuntimeException();
			};
		}

		public Pos4d set(int index, long value) {
			switch (index) {
			case 0:
				return new Pos4d(value, y, z, t);
			case 1:
				return new Pos4d(x, value, z, t);
			case 2:
				return new Pos4d(x, y, value, t);
			case 3:
				return new Pos4d(x, y, z, value);
			default:
				throw new RuntimeException();
			}
		}

	}

}
