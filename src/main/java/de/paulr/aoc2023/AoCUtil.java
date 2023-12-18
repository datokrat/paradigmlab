package de.paulr.aoc2023;

import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public enum Direction {
		LEFT, UP, RIGHT, DOWN;

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

		public T get(int x, int y) {
			return get(Pos.of(x, y));
		}

		public T get(Pos pos) {
			if (!containsKey(pos)) {
				throw new RuntimeException();
			}

			return matrix.getOrDefault(pos.rotate(rotateUPto, Direction.UP), defaultValue);
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

	}

}
