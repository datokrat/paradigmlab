package de.paulr.aoc2023.day14;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.util.statemachine.DynamicUtils;

class Solution extends ASolution {

	public static final String FILE = "2023_14.txt";
	public static final String EXAMPLE = "2023_14s.txt";

	public Solution() {
		super(FILE);
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		Matrix matrix = new Matrix(new HashMap<>(AoCUtil.linesToMatrix(lines)), lines.size(), lines.get(0).length());
		tiltNorth(matrix);
		return load(matrix);
//
//		int h = lines.size();
//		int w = lines.get(0).length();
//		int totalLoad = 0;
//		for (int x = 0; x < w; x++) {
//			prynt("col {}", x);
//			int stop = 0;
//			int rocks = 0;
//			for (int y = 0; y < h; y++) {
//				char c = lines.get(y).charAt(x);
//				if (c == '#') {
//					for (int i = h - stop; i > h - stop - rocks; i--) {
//						totalLoad += i;
//						prynt("rock {}", i);
//					}
//					stop = y + 1;
//					rocks = 0;
//				}
//				if (c == 'O') {
//					rocks++;
//				}
//			}
//			for (int i = h - stop; i > h - stop - rocks; i--) {
//				totalLoad += i;
//				prynt("rocks {}", i);
//			}
//		}
//		return totalLoad;
	}

	@Override
	public Object partB() {
		Matrix matrix = new Matrix(new HashMap<>(AoCUtil.linesToMatrix(lines)), lines.size(), lines.get(0).length());
		UnaryOperator<Matrix> op = m -> cycle(m);
		Matrix res = DynamicUtils.iterateSmart(matrix, 1_000_000_000L, op);
		return load(res);
	}

	public Matrix cycle(Matrix matrix) {
		var m = matrix;
		m = m.clone();
		tiltNorth(m);
		m = rotateLeft(m);
		m = m.clone();
		tiltNorth(m);
		m = rotateLeft(m);
		m = m.clone();
		tiltNorth(m);
		m = rotateLeft(m);
		m = m.clone();
		tiltNorth(m);
		m = rotateLeft(m);
		return m;
	}

	public static Matrix rotateLeft(Matrix matrix) {
		Matrix nmatrix = new Matrix(new HashMap<>(), matrix.w, matrix.h);
		for (var entry : matrix.matrix.entrySet()) {
			nmatrix.matrix.put(new Pos(matrix.h - 1 - entry.getKey().y(), entry.getKey().x()), entry.getValue());
		}
		return nmatrix;
	}

	public void tiltNorth(Matrix m) {
		var matrix = m.matrix;
		int h = m.h;
		int w = m.w;
		for (long x = 0; x < w; x++) {
			long stop = 0;
			int rocks = 0;
			for (long y = 0; y < h; y++) {
				char c = matrix.get(new Pos(x, y));
				if (c == '#') {
					for (long i = stop; i < stop + rocks; i++) {
						matrix.put(new Pos(x, i), 'O');
					}
					stop = y + 1;
					rocks = 0;
				}
				if (c == 'O') {
					rocks++;
					matrix.put(new Pos(x, y), '.');
				}
			}
			for (long i = stop; i < stop + rocks; i++) {
				matrix.put(new Pos(x, i), 'O');
			}
		}
	}

	public long load(Matrix m) {
		long total = 0L;
		var matrix = m.matrix;
		for (var entry : matrix.entrySet()) {
			if (entry.getValue() != 'O') {
				continue;
			}
			total += m.h - entry.getKey().y();
		}
		return total;
	}

	public void pryntM(Matrix matrix) {
		for (long y = 0; y < matrix.h; y++) {
			for (long x = 0; x < matrix.w; x++) {
				System.out.print(matrix.matrix.get(new Pos(x, y)));
			}
			System.out.println();
		}
		System.out.println();
	}

	public record Matrix(Map<Pos, Character> matrix, int h, int w) {
		@Override
		public Matrix clone() {
			return rotateLeft(rotateLeft(rotateLeft(rotateLeft(this))));
		}
	}

}
