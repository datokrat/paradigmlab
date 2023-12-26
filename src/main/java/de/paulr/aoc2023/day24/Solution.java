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
		for (long t = 0L; t < 1_000_000L; t++) {
			for (long u = 0L; u < t; u++) {
				if (tryB(t, u)) {
					return "Done!";
				}
			}
		}
		return "Nothing found";
	}

	public boolean tryB(long t, long u) {
		var pa = getPosAtTime(phases.get(5), t);
		var pb = getPosAtTime(phases.get(6), u);
		var velocity = pb.minus(pa).dividedBy(u - t);

		if (!velocity.times(u - t).equals(pb.minus(pa))) {
			return false;
		}

		var start = pa.minus(velocity.times(t));
		Pair<Pos3d, Pos3d> phase = Pair.of(start, velocity);

		for (int i = 0; i < phases.size(); i++) {
			if (!isCollisionB(phase, phases.get(i))) {
				return false;
			}
		}

		prynt("Maybe use {}, velocity {}", start, velocity);
		return true;
	}

	public boolean isCollisionB(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b) {
		Pos3d startDifference = a.first().minus(b.first());
		Pos3d velocityDifference = a.second().minus(b.second());

		long t;
		if (velocityDifference.x != 0) {
			t = -startDifference.x / velocityDifference.x;
		} else if (velocityDifference.y != 0) {
			t = -startDifference.y / velocityDifference.y;
		} else if (velocityDifference.z != 0) {
			t = -startDifference.z / velocityDifference.z;
		} else {
			throw new IllegalStateException();
		}

		if (t < 0) {
			return false;
		}
		return velocityDifference.times(-t).equals(startDifference);
	}

	public Pos3d getPosAtTime(Pair<Pos3d, Pos3d> phase, long time) {
		return phase.first().add(phase.second().times(time));
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

}
