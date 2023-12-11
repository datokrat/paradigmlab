package de.paulr.aoc2023.day03;

import static de.paulr.parser.Parsers.unsignedLongNumber;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paulr.util.Pair;

public class Day3 {

	private List<String> lines;

	private long sum = 0;

	private Map<Pair<Integer, Integer>, List<Long>> gearNumbers = new HashMap<>();

	public static void main(String[] args) throws FileNotFoundException {
		Day3 x = new Day3();
		x.lines = new BufferedReader(new FileReader("/home/paul/temp/aoc3.txt")).lines().toList();
		x.run();
	}

	public void run() {
		for (int y = 0; y < lines.size(); y++) {
			String line = lines.get(y);
			for (int x = 0; x < line.length();) {
				var parsed = unsignedLongNumber().parse(line, x);
				if (!parsed.hasResult()) {
					x++;
					continue;
				} else {
					System.out.println(parsed.getResult());
					boolean done = false;
					for (int i = x - 1; i < parsed.getNewPosition() + 1 && !done; i++) {
						for (int j = y - 1; j < y + 2 && !done; j++) {
							if (i < 0 || j < 0 || j >= lines.size() || i >= lines.get(j).length()) {
								continue;
							}
							if (isGear(lines.get(j).charAt(i))) {
								gearNumbers
									.computeIfAbsent(new Pair<Integer, Integer>(i, j),
										__ -> new ArrayList<>()) //
									.add(parsed.getResult());
							}
						}
					}
					x = parsed.getNewPosition();
				}
			}
		}

		for (var entry : gearNumbers.entrySet()) {
			var pair = entry.getKey();
			if (entry.getValue().size() != 2) {
				continue;
			}
			sum += entry.getValue().get(0) * entry.getValue().get(1);
		}
		System.out.println(sum);
	}

	private boolean isSymbol(char x) {
		return !Character.isDigit(x) && x != '.';
	}

	private boolean isGear(char x) {
		return x == '*';
	}

}
