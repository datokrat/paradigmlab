package de.paulr.aoc2023;

import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.paulr.parser.IParser;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Rope;

public class AoCUtil {

	// === Input ===

	private static String SAMPLE_BASE = "C:\\Users\\Paul.Reichert\\aoc\\";

	public static List<String> input(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(SAMPLE_BASE + filename))) {
			return reader.lines().toList();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Sample file not found: " + SAMPLE_BASE + filename);
		}
	}

	public static String inputAsString(String filename) {
		return input(filename).stream().collect(joining("\n"));
	}

	public static void prynt(Object... args) {
		String message = "";
		for (var arg : args) {
			message += arg;
		}
		System.out.println(message);
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
	}

	public static List<Character> stringToList(String string) {
		return string.chars().mapToObj(i -> (char) i).toList();
	}

	public static Stream<Character> stringToStream(String string) {
		return string.chars().mapToObj(i -> (char) i);
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

}
