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
import de.paulr.util.Stopwatch;

class Solution extends ASolution {

	public IParser<Pos3d> posPsr = longNumber.map(BigInteger::valueOf).plus(regex(",\\s*")).map(Rope.fn(Pos3d::new));
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

	public MatrixInversionTableau createTableau() {
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
		tableau.gauss();
		return tableau;
	}

	public Object partB() {
		Stopwatch sw = new Stopwatch();
		parseInput();
//		scanVelocities(phases.get(0), phases.get(1));

//		MatrixInversionTableau tableau = createTableau();
//
//		for (long tc = 0; tc < 1_000_000_000L; tc++) {
//			if (check(phases.get(0), phases.get(1), phases.get(2), tc, tableau)) {
//				return "Found";
//			}
//			if (tc % 1_000_000_0L == 999_999_9L) {
//				prynt("tc {}", tc);
//			}
//		}

		for (int i = 0; i < phases.size() - 3; i++) {
			if (solve(phases.get(i), phases.get(i + 1), phases.get(i + 2))) {
				prynt("took {}ms", sw.elapsedMillis());
				return "Found";
			}
		}

		return "Nothing found";
	}

	public boolean solve(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b, Pair<Pos3d, Pos3d> c) {
		var ab = List.of(a, b, c);
		IntegralMatrix A = new IntegralMatrix(3, 3);
		IntegralMatrix rhs = new IntegralMatrix(3, 1);
		for (int i = 0; i < 3; i++) {
			var p = ab.get(i);
			var q = ab.get((i + 1) % 3);
			Pos3d vec = p.first().minus(q.first()).cross(p.second().minus(q.second()));
			for (int j = 0; j < 3; j++) {
				A.set(i, j, vec.get(j));
			}
			rhs.set(i, 0, p.first().minus(q.first()).dot(p.second().cross(q.second())));
		}
		MatrixInversionTableau tableau = new MatrixInversionTableau(A, rhs);
		if (!tableau.gauss()) {
			return false;
		}
		Pos3d velocity = new Pos3d(tableau.right.get(0, 0).divide(tableau.left.get(0, 0)), //
			tableau.right.get(1, 0).divide(tableau.left.get(1, 1)), //
			tableau.right.get(2, 0).divide(tableau.left.get(2, 2)));

		Pos3d start = posFromVelocity(a, b, velocity);

		assert isCollision(Pair.of(start, velocity), a);
		assert isCollision(Pair.of(start, velocity), b);
		assert isCollision(Pair.of(start, velocity), c);

		for (int i = 0; i < phases.size(); i++) {
			if (!isCollision(Pair.of(start, velocity), phases.get(i))) {
				prynt("Does not collide with {}", phases.get(i));
			}
		}

		prynt("x0={}, v={}", start, velocity);

		prynt("sum: {}", start.sum());

		return true;
	}

	public Pos3d posFromVelocity(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b, Pos3d velocity) {
		var phases = List.of(a, b);
		IntegralMatrix A = new IntegralMatrix(3, 2);
		IntegralMatrix rhs = new IntegralMatrix(3, 1);
		for (int i = 0; i < 2; i++) {
			var p = phases.get(i);
			for (int j = 0; j < 3; j++) {
				A.set(j, i, p.second().minus(velocity).get(j));
			}
		}
		for (int j = 0; j < 3; j++) {
			rhs.set(j, 0, b.first().minus(a.first()).get(j));
		}
		MatrixInversionTableau tableau = new MatrixInversionTableau(A, rhs);
		if (!tableau.gauss()) {
			throw new RuntimeException();
		}
		BigInteger tnum = tableau.right.get(0, 0);
		BigInteger tden = tableau.left.get(0, 0);

		return a.first().add(a.second().times(tnum).dividedBy(tden)).minus(velocity.times(tnum).dividedBy(tden));
	}

	public Pos4d invertWithTableau(MatrixInversionTableau tableau, Pos4d z) {
		Pos4d result = new Pos4d(0, 0, 0, 0);
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				BigInteger toAdd = tableau.right.get(i, j).multiply(z.get(j));
				result = result.set(i, result.get(i).add(toAdd));
			}
		}
		return result;
	}

//	public Pos4d invertForSample(Pos4d z) {
//		long c1 = -2 * (z.y + 6 * z.x + 7 * z.t);
//		long c1alt = z.z - 8 * z.x - 6 * z.t;
//		long c2 = 8 * z.x - z.z + 14 * z.t;
//		long c3 = 2 * z.t + z.z;
//		return new Pos4d(c1, c1alt, c2, c3);
//	}

	public void scanVelocities(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b) {
		int min = -1000;
		int max = 1000;
		for (int i = min; i < max; i++) {
			prynt("i");
			for (int j = min; j < max; j++) {
				for (int k = min; k < max; k++) {
					Pos3d velocity = new Pos3d(i, j, k);
					if (checkVelocity(a, b, velocity)) {
						prynt(velocity);
					}
				}
			}
		}
	}

	public boolean checkVelocity(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b, Pos3d velocity) {
		// x1 + t v1 + u v = x2 + (t + u) v2
		// t (v1 + v2) + u (v - v2) = t v1 + u v - (t + u) v2 = x2 - x1
		Pos3d v = a.second().add(b.second());
		Pos3d w = velocity.minus(b.second());
		Pos3d rhs = b.first().minus(a.first());
		if (!v.cross(w).dot(rhs).equals(BigInteger.ZERO)) {
			return false;
		}

		return true;
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
//		long c1 = -2 * (z.y + 6 * z.x + 7 * z.t);
//		long c1alt = z.z - 8 * z.x - 6 * z.t;
//		long c2 = 8 * z.x - z.z + 14 * z.t;
//		long c3 = -4 * z.x + 2 * z.y - 2 * z.t;

		Pos4d inverse = invertWithTableau(tableau, z);
		BigInteger c1 = inverse.x;
		BigInteger c2 = inverse.y;
		BigInteger c3 = inverse.z;
		BigInteger check = inverse.t;

		if (!check.equals(BigInteger.ZERO)) {
			return false;
		}

		BigInteger denominator = tableau.left.get(0, 0);
		assert tableau.left.get(0, 0).equals(tableau.left.get(1, 1));
		assert tableau.left.get(0, 1).equals(BigInteger.ZERO);
		assert z.times(denominator).equals(w1.times(c1).plus(w2.times(c2)).plus(w3.times(c3)));

//		if (c3 % 8 != 0) {
//			return false;
//		}

		// only accept integral collision times
		BigInteger denominator2 = c3;
		BigInteger denominator1 = denominator.add(c3);
		BigInteger commonDenominator = denominator2.multiply(denominator1);
		BigInteger numerator2 = c2.negate();
		BigInteger numerator1 = c1;

		// Check that the collision times are integers
//		if (!numerator2.remainder(denominator2).equals(BigInteger.ZERO)) {
//			return false;
//		}
//
//		if (!numerator1.remainder(denominator1).equals(BigInteger.ZERO)) {
//			return false;
//		}

		// BigInteger t1 = numerator1.divide(denominator1);
		// BigInteger t2 = numerator2.divide(denominator2).negate();
		Pos3d p1TimesDenom1 = a.first().times(denominator1).add(a.second().times(numerator1));
		Pos3d p2TimesDenom2 = b.first().times(denominator2).add(b.second().times(numerator2));

		Pos3d arrowTimesCommonDenom = p1TimesDenom1.times(denominator2).minus(p2TimesDenom2.times(denominator1));
		BigInteger timespanTimesCommonDenom = numerator1.multiply(denominator2)
			.subtract(numerator2.multiply(denominator1));

		Pos3d velocity = arrowTimesCommonDenom.dividedBy(timespanTimesCommonDenom);
		Pos3d start = p1TimesDenom1.minus(velocity.times(numerator1)).dividedBy(denominator1);

		// assert p1.minus(p2).equals(velocity.times(t1.subtract(t2)));

		for (int i = 0; i < phases.size(); i++) {
			if (!isCollision(Pair.of(start, velocity), phases.get(i))) {
				return false;
			}
		}

		prynt("x0={}, v={}", start, velocity);
		return true;
	}

	public boolean isCollision(Pair<Pos3d, Pos3d> a, Pair<Pos3d, Pos3d> b) {
		Pos3d dstart = b.first().minus(a.first());
		Pos3d dvelocity = b.second().minus(a.second());

		BigInteger t;
		if (!dvelocity.x.equals(BigInteger.ZERO)) {
			t = dstart.x.divide(dvelocity.x).negate();
		} else if (!dvelocity.y.equals(BigInteger.ZERO)) {
			t = dstart.y.divide(dvelocity.y).negate();
		} else if (!dvelocity.z.equals(BigInteger.ZERO)) {
			t = dstart.z.divide(dvelocity.z).negate();
		} else {
			throw new RuntimeException();
		}

		if (t.compareTo(BigInteger.ZERO) < 0) {
			return false;
		}

		return dvelocity.times(t.negate()).equals(dstart);
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
		if (!phase.second().x.equals(BigInteger.ZERO)) {
			return x.numberEquals(phase.first().x)
				|| ((phase.second().x.compareTo(BigInteger.ZERO) > 0) == (x.compareTo(phase.first().x) > 0));
		} else if (!phase.second().y.equals(BigInteger.ZERO)) {
			return y.numberEquals(phase.first().y)
				|| ((phase.second().x.compareTo(BigInteger.ZERO) > 0) == (y.compareTo(phase.first().y) > 0));
		} else {
			throw new IllegalStateException();
		}
	}

	public boolean isValid(BigFractional x) {
		return x.compareTo(BigFractional.of(MIN)) >= 0 && x.compareTo(BigFractional.of(MAX)) <= 0;
	}

	public BigPos3d xyToBigPos(Pos3d pos) {
		return new BigPos3d(pos.x, pos.y, BigInteger.ONE);
	}

	public void parseInput() {
		phases = new ArrayList<>();
		for (var line : lines) {
			phases.add(phasePsr.parseOne(line));
		}
	}

	public record BigFractional(BigInteger n, BigInteger d) {

		public static BigFractional of(long x) {
			return of(BigInteger.valueOf(x));
		}

		public static BigFractional of(BigInteger x) {
			return new BigFractional(x, BigInteger.ONE);
		}

		public int compareTo(BigInteger other) {
			return compareTo(BigFractional.of(other));
		}

		public int compareTo(BigFractional other) {
			return d.signum() * other.d.signum() * n.multiply(other.d).compareTo(other.n.multiply(d));
		}

		public boolean numberEquals(BigInteger other) {
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

	public record Pos3d(BigInteger x, BigInteger y, BigInteger z) {

		public Pos3d(long x, long y, long z) {
			this(BigInteger.valueOf(x), BigInteger.valueOf(y), BigInteger.valueOf(z));
		}

		public BigInteger get(int index) {
			return switch (index) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			default -> throw new RuntimeException();
			};
		}

		public Pos3d dividedBy(BigInteger factor) {
			return new Pos3d(x.divide(factor), y.divide(factor), z.divide(factor));
		}

		public Pos3d minus(Pos3d other) {
			return add(other.times(BigInteger.ONE.negate()));
		}

		public Pos3d add(Pos3d other) {
			return new Pos3d(x.add(other.x), y.add(other.y), z.add(other.z));
		}

		public Pos3d times(long factor) {
			return times(BigInteger.valueOf(factor));
		}

		public Pos3d times(BigInteger factor) {
			return new Pos3d(x.multiply(factor), y.multiply(factor), z.multiply(factor));
		}

		public Pos3d times(Pos3d other) {
			return new Pos3d(x.multiply(other.x), y.multiply(other.y), z.multiply(other.z));
		}

		public BigInteger sum() {
			return x.add(y).add(z);
		}

		public BigInteger dot(Pos3d other) {
			return times(other).sum();
		}

		public Pos3d cross(Pos3d other) {
			return new Pos3d( //
				y.multiply(other.z).subtract(z.multiply(other.y)), //
				z.multiply(other.x).subtract(x.multiply(other.z)), x.multiply(other.y).subtract(y.multiply(other.x)));
		}

	}

	public record Pos4d(BigInteger x, BigInteger y, BigInteger z, BigInteger t) {

		public Pos4d(long x, long y, long z, long t) {
			this(BigInteger.valueOf(x), BigInteger.valueOf(y), BigInteger.valueOf(z), BigInteger.valueOf(t));
		}

		public static Pos4d ofSpacetime(Pos3d space, long time) {
			return new Pos4d(space.x, space.y, space.z, BigInteger.valueOf(time));
		}

		public static Pos4d ofSpacetime(Pos3d space, BigInteger time) {
			return new Pos4d(space.x, space.y, space.z, time);
		}

		public Pos4d plus(Pos4d other) {
			return new Pos4d(x.add(other.x), y.add(other.y), z.add(other.z), t.add(other.t));
		}

		public Pos4d times(BigInteger factor) {
			return new Pos4d(x.multiply(factor), y.multiply(factor), z.multiply(factor), t.multiply(factor));
		}

		public Pos4d times(Pos4d other) {
			return new Pos4d(x.multiply(other.x), y.multiply(other.y), z.multiply(other.z), t.multiply(other.t));
		}

		public Pos3d space() {
			return new Pos3d(x, y, z);
		}

		public BigInteger get(int index) {
			return switch (index) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			case 3 -> t;
			default -> throw new RuntimeException();
			};
		}

		public Pos4d set(int index, BigInteger value) {
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
