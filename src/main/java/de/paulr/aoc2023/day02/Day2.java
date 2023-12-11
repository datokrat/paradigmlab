package de.paulr.aoc2023.day02;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day2 {

	private static long sum = 0;

	private static Map<String, Long> maxima = Map.of("red", 12L, "green", 13L, "blue", 14L);

	private static Map<String, Long> minima = null;

	public static void main(String[] args) throws FileNotFoundException {
		lines().forEach(Day2::process);
		System.out.println(sum);
	}

	private static void process(String line) {
		minima = new HashMap<>(Map.of("red", 0L, "green", 0L, "blue", 0L));
		String[] colonsep = line.split(Pattern.quote(": "));
		long gamenum = Long.parseLong(colonsep[0].split(Pattern.quote(" "))[1]);
		String[] runs = colonsep[1].split(Pattern.quote("; "));
		for (String run : runs) {
			processRun(run);
		}
		sum += minima.get("red") * minima.get("blue") * minima.get("green");
	}

	private static void processRun(String run) {
		for (String pair : run.split(Pattern.quote(", "))) {
			String[] parts = pair.split(Pattern.quote(" "));
			long num = Long.parseLong(parts[0]);
			minima.put(parts[1], Math.max(num, minima.get(parts[1])));
		}
	}

	private static Stream<String> lines() throws FileNotFoundException {
		return new BufferedReader(new FileReader("/home/paul/temp/aoc2.txt")).lines();
	}

}
