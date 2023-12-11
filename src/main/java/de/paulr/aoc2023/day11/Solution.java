package de.paulr.aoc2023.day11;

import static de.paulr.aoc2023.AoCUtil.input;
import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;

class Solution {

	public static void main(String[] args) {
		var solution = new Solution();
		solution.readInput("11.txt");
		prynt(solution.partA());
	}

	public List<Pair<Long, Long>> galpos;

	public void readInput(String filename) {
		var lines = input(filename);
		int h = lines.size();
		int l = lines.get(0).length();
		Set<Integer> cols = new LinkedHashSet<>(CollectionUtils.rangeBetween(0, l).toList());
		Set<Integer> rows = new LinkedHashSet<>(CollectionUtils.rangeBetween(0, h).toList());
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < l; x++) {
				char c = lines.get(y).charAt(x);
				if (c == '.') {
					continue;
				}
				cols.remove(x);
				rows.remove(y);
			}
		}
		galpos = new ArrayList<>();
		long ry = 0;
		for (int y = 0; y < h; y++, ry++) {
			if (rows.contains(y)) {
				ry += 1000000 - 1;
			}
			long rx = 0;
			for (int x = 0; x < l; x++, rx++) {
				if (cols.contains(x)) {
					rx += 1000000 - 1;
				}
				char c = lines.get(y).charAt(x);
				if (c == '.') {
					continue;
				}
				galpos.add(Pair.of(rx, ry));
			}
		}
	}

	public Object partA() {
		long total = 0;
		for (int i = 0; i < galpos.size(); i++) {
			for (int j = i + 1; j < galpos.size(); j++) {
				var p1 = galpos.get(i);
				var p2 = galpos.get(j);
				total += Math.abs(p1.first() - p2.first()) + Math.abs(p1.second() - p2.second());
			}
		}
		return total;
	}

	public Object partB() {
		return null;
	}

}
