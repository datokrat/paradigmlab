package de.paulr.aoc2023.day16;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil.CharMatrix;
import de.paulr.aoc2023.AoCUtil.Direction;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.util.Pair;

public class Solution extends ASolution {

	public Solution() {
		super(2023, 16, "");
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	@Override
	public Object partA() {
		var matrix = new CharMatrix(lines);
		var initialPhoton = new Photon(matrix, Pos.of(0L, 0L), Direction.RIGHT);
		return determineEnergization(initialPhoton);
	}

	@Override
	public Object partB() {
		long max = 0L;
		var matrix = new CharMatrix(lines);
		for (int x = 0; x < matrix.width; x++) {
			max = Math.max(max, determineEnergization(new Photon(matrix, Pos.of(x, 0L), Direction.DOWN)));
			max = Math.max(max, determineEnergization(new Photon(matrix, Pos.of(x, matrix.height - 1), Direction.UP)));
		}
		for (int y = 0; y < matrix.height; y++) {
			max = Math.max(max, determineEnergization(new Photon(matrix, Pos.of(0L, y), Direction.RIGHT)));
			max = Math.max(max, determineEnergization(new Photon(matrix, Pos.of(matrix.width - 1, y), Direction.LEFT)));
		}
		return max;
	}

	public static long determineEnergization(Photon initialPhoton) {
		Set<Pair<Pos, Direction>> phases = new HashSet<>();
		Deque<Photon> photonsToProcess = new ArrayDeque<>();
		photonsToProcess.addLast(initialPhoton);
		while (!photonsToProcess.isEmpty()) {
			var photon = photonsToProcess.removeLast();
			if (phases.contains(photon.phase())) {
				continue;
			}
			phases.add(photon.phase());
			photon.successors().forEach(succ -> photonsToProcess.addLast(succ));
		}
		return phases.stream().map(ph -> ph.first()).distinct().count();
	}

	public record Photon(CharMatrix context, Pos pos, Direction dir) {

		public Pair<Pos, Direction> phase() {
			return Pair.of(pos, dir);
		}

		public List<Photon> successors() {
			char c = context.get(pos);
			switch (c) {
			case '.':
				return singlePhotonIfContained(dir);
			case '/':
				return singlePhotonIfContained(dir.isVertical() //
					? dir.rotateRight() //
					: dir.rotateLeft());
			case '\\':
				return singlePhotonIfContained(dir.isVertical() //
					? dir.rotateLeft() //
					: dir.rotateRight());
			case '|':
				if (dir.isVertical()) {
					return singlePhotonIfContained(dir);
				} else {
					return photonsIfContained(List.of(Direction.UP, Direction.DOWN));
				}
			case '-':
				if (dir.isHorizontal()) {
					return singlePhotonIfContained(dir);
				} else {
					return photonsIfContained(List.of(Direction.LEFT, Direction.RIGHT));
				}
			default:
				throw new RuntimeException();
			}
		}

		private List<Photon> singlePhotonIfContained(Direction ndir) {
			Pos npos = pos.move(ndir);
			return context.containsKey(npos) ? List.of(new Photon(context, npos, ndir)) : List.of();
		}

		private List<Photon> photonsIfContained(List<Direction> ndir) {
			return ndir.stream().map(this::singlePhotonIfContained).flatMap(List::stream).toList();
		}

	}

}
