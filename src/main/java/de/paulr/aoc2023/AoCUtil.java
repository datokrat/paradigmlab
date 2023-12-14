package de.paulr.aoc2023;

import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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

}
