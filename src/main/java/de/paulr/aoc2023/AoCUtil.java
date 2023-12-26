package de.paulr.aoc2023;

import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.helpers.MessageFormatter;

import de.paulr.parser.IParser;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Rope;
import de.paulr.util.Stopwatch;

public class AoCUtil {

	// === Output ===

	public static String format(String string, Object... args) {
		return MessageFormatter.arrayFormat(string, args).getMessage();
	}

	public static void prynt(Stopwatch stopwatch) {
		prynt("Time elapsed: {}", stopwatch.elapsedMillis());
	}

	public static void prynt(String string, Object... args) {
		System.out.println(format(string, args));
	}

	public static void prynt(Object... args) {
		String message = "";
		for (var arg : args) {
			message += arg;
		}
		System.out.println(message);
	}

	// === Input ===

	public static List<String> input(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(AoCSettings.SAMPLE_BASE + filename))) {
			return reader.lines().toList();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Sample file not found: " + AoCSettings.SAMPLE_BASE + filename);
		}
	}

	public static String inputAsString(String filename) {
		return input(filename).stream().collect(joining("\n"));
	}

	// === Parsers ===

	public static IParser<Void> whitespacePsr = //
		exact(" ").plus().mapToNull();

	public static IParser<Void> newlinePsr = //
		exact("\n").or(exact("\r\n")).mapToNull();

	public static IParser<Void> emptyLinePsr = //
		newlinePsr.silentlyThen(newlinePsr);

	public static IParser<Void> verticalSpacePsr = //
		emptyLinePsr.plus().mapToNull();

	public static IParser<List<Long>> longListPsr = //
		longNumber.plus(whitespacePsr).map(Rope::toList);

	// === ACII matrix ===

	public record Pos(long x, long y) {

		public static Pos of(long x, long y) {
			return new Pos(x, y);
		}

		public long manhattanDist(Pos other) {
			return Math.abs(x - other.x) + Math.abs(y - other.y);
		}

		public long alignedDist(Pos other) {
			if (x != other.x && y != other.y) {
				throw new IllegalArgumentException("points must be horizontally or vertically aligned");
			}
			return manhattanDist(other);
		}

		public long cross(Pos other) {
			return x * other.y - y * other.x;
		}

		public Pos rotate(Rotation rotation) {
			return rotate(Direction.UP, rotation.rotateUPto());
		}

		public Pos rotateLeft() {
			return Pos.of(-y, x);
		}

		public Pos rotateRight() {
			return Pos.of(y, -x);
		}

		public Pos rotate(Direction sourceDir, Direction targetDir) {
			Direction dir = sourceDir;
			Pos pos = this;
			while (dir != targetDir) {
				pos = pos.rotateRight();
				dir = dir.rotateRight();
			}
			return pos;
		}

		public Pos move(Direction direction) {
			return move(direction, 1L);
		}

		public Pos move(Direction direction, long n) {
			return switch (direction) {
			case LEFT -> Pos.of(x - n, y);
			case UP -> Pos.of(x, y - n);
			case RIGHT -> Pos.of(x + n, y);
			case DOWN -> Pos.of(x, y + n);
			};
		}

		public static long moveX(long x, Direction direction) {
			return switch (direction) {
			case LEFT -> x - 1;
			case RIGHT -> x + 1;
			case UP, DOWN -> x;
			};
		}

		public static long moveY(long y, Direction direction) {
			return switch (direction) {
			case UP -> y - 1;
			case DOWN -> y + 1;
			case LEFT, RIGHT -> y;
			};
		}

	}

	public enum Rotation {
		ID, LEFT, RIGHT, REVERSE;

		public Direction rotateUPto() {
			return switch (this) {
			case ID -> Direction.UP;
			case LEFT -> Direction.LEFT;
			case RIGHT -> Direction.RIGHT;
			case REVERSE -> Direction.DOWN;
			};
		}

		public Rotation inverse() {
			return switch (this) {
			case ID -> ID;
			case REVERSE -> REVERSE;
			case LEFT -> RIGHT;
			case RIGHT -> LEFT;
			};
		}

		public static Rotation UPto(Direction rotateUPto) {
			return switch (rotateUPto) {
			case UP -> Rotation.ID;
			case LEFT -> Rotation.LEFT;
			case RIGHT -> Rotation.RIGHT;
			case DOWN -> Rotation.REVERSE;
			};
		}
	}

	public enum Direction {
		LEFT, UP, RIGHT, DOWN;

		public static Direction ofChar(char c) {
			return switch (c) {
			case '<' -> Direction.LEFT;
			case '>' -> Direction.RIGHT;
			case '^' -> Direction.UP;
			case 'v' -> Direction.DOWN;
			default -> null;
			};
		}

		public Direction rotateRight() {
			switch (this) {
			case LEFT:
				return Direction.UP;
			case UP:
				return Direction.RIGHT;
			case RIGHT:
				return Direction.DOWN;
			case DOWN:
				return Direction.LEFT;
			}
			;
			throw new RuntimeException();
		}

		public Direction rotateLeft() {
			return switch (this) {
			case LEFT -> Direction.DOWN;
			case DOWN -> Direction.RIGHT;
			case RIGHT -> Direction.UP;
			case UP -> Direction.LEFT;
			};
		}

		public Direction rotate(Direction sourceDir, Direction targetDir) {
			Direction referenceDir = sourceDir;
			Direction dir = this;
			while (referenceDir != targetDir) {
				referenceDir = referenceDir.rotateRight();
				dir = dir.rotateRight();
			}
			return dir;
		}

		public boolean isVertical() {
			return switch (this) {
			case LEFT, RIGHT -> false;
			case UP, DOWN -> true;
			};
		}

		public boolean isHorizontal() {
			return !isVertical();
		}

		public Direction reverse() {
			return switch (this) {
			case UP -> Direction.DOWN;
			case DOWN -> Direction.UP;
			case LEFT -> Direction.RIGHT;
			case RIGHT -> Direction.LEFT;
			};
		}
	}

	public static List<Pos> rowPositions(List<String> lines, long y) {
		return CollectionUtils.rangeBetween(0L, lines.get((int) y).length()) //
			.map(x -> new Pos(x, y)) //
			.toList();
	}

	public static List<Pos> colPositions(List<String> lines, long x) {
		return CollectionUtils.rangeBetween(0L, lines.size()) //
			.map(y -> new Pos(x, y)) //
			.toList();
	}

	public static Map<Pos, Character> linesToMatrix(List<String> lines) {
		return CollectionUtils.rangeBetween(0L, lines.size()) //
			.flatMap(y -> rowPositions(lines, y).stream()) //
			.collect(toMap(pos -> pos, pos -> lines.get((int) pos.y).charAt((int) pos.x)));
	}

	public static List<String> transpose(List<String> pattern) {
		List<String> t = new ArrayList<>();
		for (int i = 0; i < pattern.get(0).length(); i++) {
			StringBuffer sb = new StringBuffer();
			for (int y = 0; y < pattern.size(); y++) {
				sb.append(pattern.get(y).charAt(i));
			}
			t.add(sb.toString());
		}
		return t;
	}

	public static class CharMatrix extends ObjectMatrix<Character> {

		public CharMatrix(List<String> rows) {
			super(linesToMatrix(rows), Direction.UP, rows.get(0).length(), rows.size());
		}

	}

	public static class ObjectMatrix<T> {

		private final Map<Pos, T> matrix;
		private final Direction rotateUPto;
		public final int height;
		public final int width;
		private T defaultValue = null;

		public ObjectMatrix(Map<Pos, T> matrix, Direction rotateUPto, int width, int height) {
			this.matrix = matrix;
			this.rotateUPto = rotateUPto;
			this.width = width;
			this.height = height;
		}

		public ObjectMatrix(int width, int height) {
			this.matrix = new HashMap<>();
			this.rotateUPto = Direction.UP;
			this.width = width;
			this.height = height;
		}

		public ObjectMatrix(ObjectMatrix<T> other) {
			this.matrix = new HashMap<>(other.matrix);
			this.rotateUPto = other.rotateUPto;
			this.width = other.width;
			this.height = other.height;
		}

		public void setDefault(T defaultValue) {
			this.defaultValue = defaultValue;
		}

		public Set<Pos> getPositions() {
			return matrix.keySet();
		}

		public T get(int x, int y) {
			return get(Pos.of(x, y));
		}

		public T getMod(Pos pos) {
			long x = pos.x() % width;
			if (x < 0) {
				x += width;
			}
			long y = pos.y() % height;
			if (y < 0) {
				y += height;
			}
			return get(Pos.of(x, y));
		}

		public T get(Pos pos) {
			if (!containsKey(pos)) {
				throw new RuntimeException();
			}

			return matrix.getOrDefault(rotateInMatrix(pos, Rotation.UPto(rotateUPto)), defaultValue);
		}

		public Pos rotateInMatrix(Pos pos, Rotation rotation) {
			return switch (rotation) {
			case ID -> pos;
			case REVERSE -> Pos.of(width - 1 - pos.x, height - 1 - pos.y);
			case LEFT -> Pos.of(pos.y, width - 1 - pos.x);
			case RIGHT -> Pos.of(height - 1 - pos.y, pos.x);
			};
		}

		public boolean containsKey(Pos pos) {
			return pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height;
		}

		public void set(int x, int y, T c) {
			set(Pos.of(x, y), c);
		}

		public void set(Pos pos, T c) {
			if (!containsKey(pos)) {
				throw new RuntimeException();
			}

			matrix.put(pos, c);
		}

		public ObjectMatrix<T> rotateLeft() {
			return new ObjectMatrix<>(matrix, rotateUPto.rotate(Direction.UP, Direction.LEFT), height, width);
		}

		public ObjectMatrix<T> rotateRight() {
			return new ObjectMatrix<T>(matrix, rotateUPto.rotate(Direction.UP, Direction.RIGHT), height, width);
		}

		public Pos topLeft() {
			return Pos.of(0, 0);
		}

		public Pos bottomRight() {
			return Pos.of(width - 1, height - 1);
		}

		public Set<Pos> boundary() {
			Set<Pos> boundary = new HashSet<>();
			for (int x = 0; x < width; x++) {
				boundary.add(Pos.of(x, 0L));
				boundary.add(Pos.of(x, height - 1));
			}
			for (int y = 0; y < height; y++) {
				boundary.add(Pos.of(0L, y));
				boundary.add(Pos.of(width - 1, y));
			}
			return boundary;
		}

	}

	public static long lcm(long a, long b, long c, long d) {
		return lcm(lcm(a, b, c), d);
	}

	public static long lcm(long a, long b, long c) {
		return lcm(lcm(a, b), c);
	}

	public static long lcm(long a, long b) {
		long gcd = BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).longValue();
		return (a / gcd) * b;
	}

	// === Geometry ===

	/**
	 * Inspired by
	 * https://github.com/Torben2000/adventofcode-java/commit/a9a6dac9ea1aa5ff464726a54adc56fb5206b607
	 * and https://en.wikipedia.org/wiki/Green's_theorem#Area_Calculation
	 * 
	 * See also
	 * https://stackoverflow.com/questions/451426/how-do-i-calculate-the-area-of-a-2d-polygon
	 * 
	 * Assumes that the edges are axis-aligned and that the thick paths do not
	 * overlap
	 */
	public static long polygonSizeFatLines(List<Pos> path, long thickness, boolean includeBorder) {
		long area = polygonSizeThinLinesAligned(path);

		if (includeBorder) {
			// For every left or upper edge, add this edge with `thickness`
			// For every top-left corner, add this corner with `thickness^2`
			// Similarly, remove `thickness^2` for every corner pointing inward towards
			// bottom-right
			// There is one more corner of the former kind
			area += thickness * polygonCircumferenceThinLinesAligned(path) / 2 + thickness * thickness;
		} else {
			// Not sure whether we need to add/subtract a correction....
			// I think we also need a special handling for the empty polygon?
			throw new NotImplementedException();
			// area -= thickness * polygonCircumferenceThinLinesAligned(path) / 2;
		}

		return area;
	}

	public static long polygonSizeThinLinesAligned(List<Pos> path) {
		long doubleArea = 0L;
		for (int i = 0; i < path.size(); i++) {
			Pos p = path.get(i);
			Pos q = path.get((i + 1) % path.size());
			doubleArea += p.cross(q);
		}
		return Math.abs(doubleArea) / 2;
	}

	/**
	 * is always even
	 */
	public static long polygonCircumferenceThinLinesAligned(List<Pos> path) {
		long circumference = 0L;
		for (int i = 0; i < path.size(); i++) {
			Pos p = path.get(i);
			Pos q = path.get((i + 1) % path.size());
			circumference += p.alignedDist(q);
		}
		return circumference;
	}

}
