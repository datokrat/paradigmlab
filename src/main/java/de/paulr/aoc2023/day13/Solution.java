package de.paulr.aoc2023.day13;

import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.aoc2023.AoCUtil.transpose;

import java.util.ArrayList;
import java.util.List;

import de.paulr.aoc2023.ASolution;

class Solution extends ASolution {

	public static final String FILE = "2023_13.txt";
	public static final String EXAMPLE = "2023_13s.txt";

	public List<List<String>> patterns = new ArrayList<>();

	public Solution(String filename) {
		super(filename);
	}

	public static void main(String[] args) {
		var s = new Solution(FILE);
		s.parse();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		int vertical = patterns.stream().map(this::findVerticalReflections) //
			.flatMap(x -> x.stream()) //
			.map(x -> x + 1) //
			.reduce(0, (a, b) -> a + b);

		prynt(vertical);

		int horizontal = 100 * patterns.stream().map(this::findHorizontalReflections) //
			.flatMap(x -> x.stream()) //
			.map(x -> x + 1) //
			.reduce(0, (a, b) -> a + b);

		prynt(horizontal);

		return vertical + horizontal;
	}

	@Override
	public Object partB() {
		int sum = 0;
		for (var pattern : patterns) {
			boolean foundSmudge = false;
			for (int i = 0; i < pattern.size(); i++) {
				for (int j = 0; j < pattern.get(0).length(); j++) {
					if (!isSmudge(pattern, j, i)) {
						continue;
					}
					prynt("Smudge at {}, {}", j, i);

					var repaired = repair(pattern, j, i);
					var oldh = findHorizontalReflections(pattern);
					var newh = findHorizontalReflections(repaired);
					var oldv = findVerticalReflections(pattern);
					var newv = findVerticalReflections(repaired);

					List<Integer> diffh = new ArrayList<>(newh);
					diffh.removeAll(oldh);
					List<Integer> diffv = new ArrayList<>(newv);
					diffv.removeAll(oldv);

					int v = diffv.stream() //
						.map(x -> x + 1) //
						.reduce(0, (a, b) -> a + b);

					int h = 100 * diffh.stream() //
						.map(x -> x + 1) //
						.reduce(0, (a, b) -> a + b);

					prynt(newh);
					prynt(newv);

					prynt("v {} h {}", v, h);

					sum += v + h;

					foundSmudge = true;
					break;
				}
				if (foundSmudge) {
					break;
				}
			}
		}
		return sum;
	}

	public void parse() {
		List<String> pattern = null;
		for (var line : lines) {
			if (pattern == null) {
				pattern = new ArrayList<>();
				patterns.add(pattern);
			}
			if (line.isEmpty()) {
				pattern = null;
				continue;
			}
			pattern.add(line);
		}
	}

	public List<Integer> findVerticalReflections(List<String> pattern) {
		return findHorizontalReflections(transpose(pattern));
	}

	public List<Integer> findHorizontalReflections(List<String> pattern) {
		List<Integer> refl = new ArrayList<>();
		for (int i = 0; i + 1 < pattern.size(); i++) {
			if (isSymmetricH(pattern, i)) {
				refl.add(i);
			}
		}
		return refl;
	}

	public boolean isSymmetricH(List<String> pattern, int i) {
		for (int j = 0; j < pattern.size(); j++) {
			int reflected = i + i + 1 - j;
			if (reflected < 0 || reflected >= pattern.size()) {
				continue;
			}
			if (!pattern.get(j).equals(pattern.get(reflected))) {
				return false;
			}
		}
		return true;
	}

	public boolean isSmudge(List<String> pattern, int x, int y) {
		var repaired = repair(pattern, x, y);
		var oldh = findHorizontalReflections(pattern);
		var newh = findHorizontalReflections(repaired);
		var oldv = findVerticalReflections(pattern);
		var newv = findVerticalReflections(repaired);

		List<Integer> diffh = new ArrayList<>(newh);
		diffh.removeAll(oldh);
		List<Integer> diffv = new ArrayList<>(newv);
		diffv.removeAll(oldv);

		return diffh.size() > 0 || diffv.size() > 0;
	}

	public List<String> repair(List<String> pattern, int x, int y) {
		List<String> repaired = new ArrayList<>();
		for (int i = 0; i < pattern.size(); i++) {
			if (i == y) {
				StringBuffer sb = new StringBuffer();
				for (int p = 0; p < pattern.get(0).length(); p++) {
					char c = pattern.get(i).charAt(p);
					if (p == x) {
						sb.append(c == '.' ? '#' : '.');
						continue;
					}
					sb.append(c);
				}
				repaired.add(sb.toString());
				continue;
			}
			repaired.add(pattern.get(i));
		}
		return repaired;
	}

}
