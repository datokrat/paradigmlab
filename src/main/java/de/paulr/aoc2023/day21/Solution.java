package de.paulr.aoc2023.day21;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil.CharMatrix;
import de.paulr.aoc2023.AoCUtil.Direction;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.aoc2023.AoCUtil.Rotation;
import de.paulr.util.Pair;
import de.paulr.util.statemachine.DynamicUtils;
import de.paulr.util.statemachine.DynamicUtils.CycleInfo;

class Solution extends ASolution {

	public Solution() {
		super(2023, 21, "");
	}

	public Pos start;
	public CharMatrix matrix;

	public long stepsToSaturation;
	public Map<Pos, Map<Pos, Long>> borderDist;

	@Override
	public Object partA() {
		parseInput();
		return countInStartPatch(64L);
	}

	public Set<Pos> successors(Set<Pos> lastPositions) {
		// prynt("Calling successors() with {} positions", lastPositions.size());
		Set<Pos> positions = new HashSet<>();
		for (var lastPos : lastPositions) {
			for (var d : Direction.values()) {
				var pos = lastPos.move(d);
				if (!matrix.containsKey(pos) || matrix.get(pos) == '#') {
					continue;
				}
				positions.add(pos);
			}
		}
		return positions;
	}

	public Set<Pos> successorsB(Set<Pos> lastPositions) {
		Set<Pos> positions = new HashSet<>();
		for (var lastPos : lastPositions) {
			for (var d : Direction.values()) {
				var pos = lastPos.move(d);
				if (matrix.getMod(pos) == '#') {
					continue;
				}
				positions.add(pos);
			}
		}
		return positions;
	}

	@Override
	public Object partB() {
		parseInput();
		preprocessB();
		long count = 0L;
		long maxSteps = 26501365L;
//		long maxSteps = 5000L;
		prynt("countInStartPatch");
		count += countInStartPatch(maxSteps);
		prynt("countInUnaligned");
		count += countInUnaligned(maxSteps);
		prynt("countInAligned");
		count += countInAligned(maxSteps);
		return count;
	}

	public long countInStartPatch(long maxSteps) {
		Set<Pos> positions = new HashSet<>();
		Set<Pos> lastPositions = new HashSet<>();
		positions.add(start);
		for (int i = 0; i < maxSteps; i++) {
			int lastSize = lastPositions.size();
			lastPositions = positions;
			positions = successors(positions);
			if (lastSize == positions.size() && (i + maxSteps - 1) % 2 == 0) {
				return positions.size();
			}
		}
		return positions.size();
	}

	public long countInUnaligned(long maxSteps) {
		long count = 0L;
		for (var rotation : Rotation.values()) {
			count += countInUnaligned(maxSteps, rotation);
		}
		return count;
	}

	public long countInAligned(long maxSteps) {
		long count = 0L;
		prynt(count);
		for (var d : Direction.values()) {
			count += countInAligned(maxSteps, d);
			prynt(count);
		}
		return count;
	}

	public long countInAligned(long maxSteps, Direction direction) {
		prynt("countInAligned {}", direction);
		Rotation rotation = Rotation.UPto(direction.rotateLeft());
		// Map<Long, List<Set<Pos>>> idxToStepsToTiles = null; //
		// precomputeIdxToStepsToTiles(rotation);

		long[] dist = initializeDistances(rotation);
		long[] relativeDist = new long[dist.length - 1];
		for (int i = 0; i < dist.length - 1; i++) {
			relativeDist[i] = dist[i + 1] - dist[0];
		}

		prynt("cycle detection");

		// cycle detection
		UnaryOperator<List<Long>> op = d -> iterateDistancesRelative(d, rotation).relativeDist;
		CycleInfo<List<Long>> cinfo = DynamicUtils.detectCycle(Arrays.stream(dist).boxed().toList(), op);
		long edenSteps = cinfo.edenSteps();
		long cycleSteps = cinfo.cycleSteps();
		if (cycleSteps % 2 == 1) {
			cycleSteps *= 2;
		}

		long count = 0L;

		// Step 1: Unsaturated tiles
		long iterations = 0;
		List<long[]> dists = new ArrayList<>();
		dists.add(dist);
		for (; iterations < edenSteps + cycleSteps; iterations++) {
			dists.add(iterateDistances(dists.get(dists.size() - 1), rotation));
		}
		long[] distsBeforeCycle = dists.get((int) edenSteps);
		long[] distsAfterCycle = dists.get((int) edenSteps + (int) cycleSteps);
		long[] shifts = new long[matrix.width];
		for (int j = 0; j < matrix.width; j++) {
			shifts[j] = distsAfterCycle[j] - distsBeforeCycle[j];
		}
		long totalCycles = 0L;
		for (int j = 0; j < matrix.width; j++) {
			totalCycles = Math.max(totalCycles, (maxSteps - distsBeforeCycle[j]) / shifts[j] + 1);
		}

		dist = Arrays.copyOf(distsBeforeCycle, matrix.width);
		for (int j = 0; j < matrix.width; j++) {
			dist[j] += totalCycles * shifts[j];
			assert dist[j] > maxSteps;
		}

		// Preparations
		SuccessorIterator[] iterators = new SuccessorIterator[matrix.width];
		for (int j = 0; j < matrix.width; j++) {
			iterators[j] = new SuccessorIterator(matrix.rotateInMatrix(Pos.of(0, j), rotation));
		}

		// Step 1: Possibly unsaturated cyclic tiles
		long saturatedCycles = 0;
		for (long c = 0; c < totalCycles; c++) {
			for (int i = (int) cycleSteps - 1; i >= 0; i--) {
				Set<Pos> reachedTiles = new HashSet<>();
				for (int j = 0; j < matrix.width; j++) {
					long shift = dists.get((int) edenSteps + i + 1)[j] - dists.get((int) edenSteps + i)[j];
					dist[j] -= shift;
					if (dist[j] > maxSteps) {
						continue;
					}
					iterators[j].fillSteps(maxSteps - dist[j]);
					reachedTiles.addAll(iterators[j].getReachedTiles());
				}
				count += reachedTiles.size();
			}
			if (Arrays.stream(iterators).allMatch(it -> it.isSaturated())) {
				saturatedCycles = totalCycles - c - 1;
				break;
			}
		}

		if (saturatedCycles > 0) {
			for (int i = (int) cycleSteps - 1; i >= 0; i--) {
				Set<Pos> reachedTiles = new HashSet<>();
//				for (int j = 0; j < matrix.width; j++) {
				int j = 0;
				long shift = dists.get((int) edenSteps + i + 1)[j] - dists.get((int) edenSteps + i)[j];
				dist[j] -= shift;
				if (dist[j] > maxSteps) {
					continue;
				}
				iterators[j].fillSteps(maxSteps - dist[j]);
				reachedTiles.addAll(iterators[j].getReachedTiles());
//				}
				count += saturatedCycles * reachedTiles.size();
			}
		}

		if (saturatedCycles > 1) {
			for (int i = (int) cycleSteps - 1; i >= 0; i--) {
				for (int j = 0; j < matrix.width; j++) {
					long shift = dists.get((int) edenSteps + i + 1)[j] - dists.get((int) edenSteps + i)[j];
					dist[j] -= (saturatedCycles - 1) * shift;
					iterators[j].fillSteps(maxSteps - dist[j]);
				}
			}
		}

		for (int i = (int) edenSteps - 1; i >= 0; i--) {
			Set<Pos> reachedTiles = new HashSet<>();
//			for (int j = 0; j < matrix.width; j++) {
			int j = 0;
			long shift = dists.get(i + 1)[j] - dists.get(i)[j];
			dist[j] -= shift;
			assert dist[j] == dists.get(i)[j];
			if (dist[j] > maxSteps) {
				continue;
			}
			iterators[j].fillSteps(maxSteps - dist[j]);
			reachedTiles.addAll(iterators[j].getReachedTiles());
//			}
			count += reachedTiles.size();
		}

		return count;
	}

	public class SuccessorIterator {

		private long steps;
		private Set<Pos> pos0;
		private Set<Pos> pos1;
		private boolean saturated;
		private long saturatedAfterSteps;

		public SuccessorIterator(Pos start) {
			steps = 0L;
			pos0 = new HashSet<>();
			pos1 = new HashSet<>(Set.of(start));
			saturated = false;
			saturatedAfterSteps = -1L;
		}

		public long getSteps() {
			return steps;
		}

		public boolean isSaturated() {
			return saturated;
		}

		public Set<Pos> getReachedTiles() {
			return pos1;
		}

		public void fillSteps(long totalSteps) {
			if (totalSteps >= steps) {
				next(totalSteps - steps);
			}
		}

		public void next(long steps) {
			long remaining = steps;
			for (; remaining > 0 && !saturated; remaining--) {
				next();
			}
			if (remaining % 2 == 1) {
				next();
				remaining--;
			}
			steps += remaining;
		}

		public void next() {
			steps++;
			if (saturated) {
				var tmp = pos0;
				pos0 = pos1;
				pos1 = tmp;
				return;
			}
			int lastSize = pos0.size();
			var tmp = pos0;
			pos0 = pos1;
			tmp.addAll(successors(pos1));
			pos1 = tmp;
			if (lastSize == pos1.size()) {
				saturated = true;
				saturatedAfterSteps = steps;
			}
		}

	}

	public long countInAlignedSlow(long maxSteps, Direction direction) {
		prynt("countInAligned {}", direction);
		Rotation rotation = Rotation.UPto(direction.rotateLeft());
		// Map<Long, List<Set<Pos>>> idxToStepsToTiles = null; //
		// precomputeIdxToStepsToTiles(rotation);

		long[] dist = initializeDistances(rotation);
		long[] relativeDist = new long[dist.length - 1];
		for (int i = 0; i < dist.length - 1; i++) {
			relativeDist[i] = dist[i + 1] - dist[0];
		}

		prynt("cycle detection");

		// cycle detection
		UnaryOperator<List<Long>> op = d -> iterateDistancesRelative(d, rotation).relativeDist;
		CycleInfo<List<Long>> cinfo = DynamicUtils.detectCycle(Arrays.stream(dist).boxed().toList(), op);

		long count = 0L;

		// Step 1: Eden states
		prynt("Eden states");
		Set<Pos> reachableTilesInPatch = findReachableTiles(maxSteps, dist, rotation);
		count += reachableTilesInPatch.size();

		for (int i = 0; i < cinfo.edenSteps(); i++) {
			dist = iterateDistances(dist, rotation);
			reachableTilesInPatch = findReachableTiles(maxSteps, dist, rotation);
			count += reachableTilesInPatch.size();
		}

		// Step 2: Saturated cyclic states
		prynt("Saturated cyclic states");
		long cycleSteps = cinfo.cycleSteps();
		if (cycleSteps % 2 == 1) {
			cycleSteps *= 2; // Make sure the cycle length is even so that we don't need to care so much
								// about parity
		}
		long[] shiftPerCycle = new long[matrix.width];
		long[] distBeforeCycle = dist;
		for (int i = 0; i < cinfo.cycleSteps(); i++) {
			dist = iterateDistances(dist, rotation);
		}
		prynt("Determine maximal saturated steps");
		long[] saturatedSteps = maxSaturatedSteps(maxSteps, rotation);
		long[] saturatedCycles = new long[matrix.width];
		long maxSaturatedCycles = 0L;
		for (int j = 0; j < matrix.width; j++) {
			shiftPerCycle[j] = dist[j] - distBeforeCycle[j];
			saturatedCycles[j] = Math.min(0L, (saturatedSteps[j] - distBeforeCycle[j]) / shiftPerCycle[j]);
			maxSaturatedCycles = Math.max(maxSaturatedCycles, saturatedCycles[j]);
		}

		prynt("Quickly apply cycles");
		dist = distBeforeCycle;
		count += maxSaturatedCycles * cinfo.cycleSteps();
		for (int i = 0; i < cinfo.cycleSteps(); i++) {
			dist = iterateDistances(dist, rotation);
			reachableTilesInPatch = findReachableTiles(maxSteps, dist, rotation);
			count += maxSaturatedCycles * reachableTilesInPatch.size();
		}

		dist = Arrays.copyOf(distBeforeCycle, matrix.width);
		for (int j = 0; j < matrix.width; j++) {
			dist[j] += maxSaturatedCycles * shiftPerCycle[j];
		}

		// Step 3: Possibly unsaturated cyclic states
		prynt("Rest steps");
		while (!reachableTilesInPatch.isEmpty()) {
			dist = iterateDistances(dist, rotation);
			reachableTilesInPatch = findReachableTiles(maxSteps, dist, rotation);
			count += reachableTilesInPatch.size();
		}

		return count;
	}

	public Set<Pos> findReachableTiles(long maxSteps, long[] dist, Rotation rotation) {
		Set<Pos> reachableTiles = new HashSet<>();
		for (int i = 0; i < dist.length; i++) {
			long remaining = maxSteps - dist[i];
			if (remaining < 0) {
				continue;
			} else {
				Set<Pos> pos0 = new HashSet<>();
				Set<Pos> pos1 = Set.of(matrix.rotateInMatrix(Pos.of(0, i), rotation)); // step 0
				for (int j = 0; j < remaining; j++) {
					int lastSize = pos0.size();
					pos0 = pos1;
					pos1 = successors(pos1);
					if (lastSize == pos1.size() && (remaining - j - 1) % 2 == 0) {
						break;
					}
				}
				reachableTiles.addAll(pos1);
			}
		}
		return reachableTiles;
	}

	public long[] maxSaturatedSteps(long maxSteps, Rotation rotation) {
		long[] steps = new long[matrix.width];
		for (int i = 0; i < matrix.width; i++) {
			long stepsToSaturation = 0;
			Set<Pos> pos0 = Set.of();
			Set<Pos> pos1 = Set.of(matrix.rotateInMatrix(Pos.of(0, i), rotation)); // step 0
			while (true) {
				stepsToSaturation++;
				int lastSize = pos0.size();
				pos0 = pos1;
				pos1 = successors(pos1);
				if (lastSize == pos1.size()) {
					break;
				}
			}
			steps[i] = Math.max(0L, maxSteps - stepsToSaturation);
		}
		return steps;
	}

	public Set<Pos> findSaturatedTiles(Map<Long, List<Set<Pos>>> idxToStepsToTiles) {
		List<Set<Pos>> list = idxToStepsToTiles.get(0L);
		List<Set<Pos>> list2 = idxToStepsToTiles.get(1L);
		assert list.get(list.size() - 1).equals(list2.get(list2.size() - 1));
		return list.get(list.size() - 1);
	}

	public Map<Long, List<Set<Pos>>> precomputeIdxToStepsToTiles(Rotation rotation) {
		Map<Long, List<Set<Pos>>> idxToStepsToTiles = new HashMap<>();
		for (long i = 0; i < matrix.width; i++) {
			List<Set<Pos>> counts = new ArrayList<>();
			idxToStepsToTiles.put(i, counts);
			counts.add(Set.of(matrix.rotateInMatrix(Pos.of(0, i), rotation))); // step 0
			while (counts.size() < 3 || counts.get(counts.size() - 3).size() != counts.get(counts.size() - 1).size()) {
				counts.add(successors(counts.get(counts.size() - 1)));
			}
		}
		return idxToStepsToTiles;
	}

	public long[] initializeDistances(Rotation rotation) {
		long[] dist = new long[matrix.width];
		for (int i = 0; i < dist.length; i++) {
			Pos targetPos = matrix.rotateInMatrix(Pos.of(matrix.width - 1, i), rotation);
			dist[i] = borderDist.get(targetPos).get(start) + 1;
		}
		return dist;
	}

	private long[] iterateDistances(long[] dist, Rotation rotation) {
		long[] newDist = new long[matrix.width];
		for (int i = 0; i < dist.length; i++) {
			newDist[i] = Long.MAX_VALUE;
			Pos targetPos = matrix.rotateInMatrix(Pos.of(matrix.width - 1, i), rotation);
			for (int j = 0; j < dist.length; j++) {
				Pos sourcePos = matrix.rotateInMatrix(Pos.of(0, j), rotation);
				long sourceTargetDist = borderDist.get(sourcePos).get(targetPos);
				newDist[i] = Math.min(newDist[i], dist[j] + sourceTargetDist + 1);
			}
		}
		return newDist;
	}

	private Step iterateDistancesRelative(List<Long> dist, Rotation rotation) {
		Long[] newDist = new Long[matrix.width];
		Long[] distChange = new Long[matrix.width];
		for (int i = 0; i < dist.size(); i++) {
			newDist[i] = Long.MAX_VALUE;
			Pos targetPos = matrix.rotateInMatrix(Pos.of(matrix.width - 1, i), rotation);
			for (int j = 0; j < dist.size(); j++) {
				Pos sourcePos = matrix.rotateInMatrix(Pos.of(0, j), rotation);
				long sourceTargetDist = borderDist.get(sourcePos).get(targetPos);
				newDist[i] = Math.min(newDist[i], dist.get(j) + sourceTargetDist + 1);
			}
		}
		for (int i = dist.size() - 1; i >= 0; i--) {
			distChange[i] = newDist[i] - dist.get(i);
			newDist[i] = newDist[i] - newDist[0];
		}
		return new Step(List.of(newDist), List.of(distChange));
	}

	public record Step(List<Long> relativeDist, List<Long> distChange) {
	}

	public long countInUnaligned(long maxSteps, Rotation rotation) {
		final long patchLength = matrix.width;
		final Pos targetCorner = matrix.rotateInMatrix(Pos.of(0L, 0L), rotation);
		final Pos startCorner = matrix.rotateInMatrix(targetCorner, Rotation.REVERSE);
		final long distStartToClosestPatch = borderDist.get(startCorner).get(start) + 2;
		final long maxPatchDist = maxSteps >= distStartToClosestPatch //
			? (maxSteps - distStartToClosestPatch) / patchLength //
			: -1;

		long count = 0L;
		long stepsDone = 0L;
		Set<Pos>[] positions = new Set[2];
		positions[0] = new HashSet<>();
		positions[1] = new HashSet<>();
		long patchDist = maxPatchDist;
		positions[0].add(targetCorner);
		boolean saturated = false;
		for (; patchDist >= 0 && !saturated; patchDist--) {
			long[] numPatches = new long[2];
			long stepsToDo = maxSteps - distStartToClosestPatch - patchDist * patchLength;
			numPatches[(int) stepsToDo % 2] = countPatchesOfDistanceInQuadrant(patchDist); // patchDist == 0 ==> 1 patch
			for (; stepsDone < stepsToDo; stepsDone++) {
				int lastParity = (int) stepsDone % 2;
				int thisParity = (int) (stepsDone + 1) % 2;
				long lastSize = positions[thisParity].size();
				positions[thisParity] = successors(positions[lastParity]);
				if (lastSize == positions[thisParity].size()) {
					saturated = true;
					// Apply all patches with this maximum distance
					assert patchLength % 2 == 1;
					long parityOfDistancesWithEvenSteps = (patchDist + stepsToDo) % 2;
					numPatches[0] = countPatchesOfMaximumDistanceInQuadrantWithParity(patchDist,
						parityOfDistancesWithEvenSteps);
					numPatches[1] = countPatchesOfMaximumDistanceInQuadrantWithParity(patchDist,
						1 - parityOfDistancesWithEvenSteps);
					break;
				}
			}
			count += numPatches[0] * positions[0].size();
			count += numPatches[1] * positions[1].size();
		}
		return count;
	}

	public long countPatchesOfDistanceInQuadrant(long distance) {
		return distance + 1;
	}

	public long countPatchesOfMaximumDistanceInQuadrantWithParity(long distance, long parity) {
		assert parity == 0 || parity == 1;
		if (parity == 1) {
			// 0 -> 0, 1 -> 1, 2 -> 1, 3 -> 2, ...
			long numOddDistances = (distance + 1) / 2;
			// 2 + 4 + 6 + 8 + ...
			return (numOddDistances * (numOddDistances + 1));
		} else {
			return countPatchesOfMaximumDistanceInQuadrant(distance)
				- countPatchesOfMaximumDistanceInQuadrantWithParity(distance, 1);
		}
	}

	public long countPatchesOfMaximumDistanceInQuadrant(long distance) {
		long numDistances = distance + 1;
		// For each distance, including zero, add (distance + 1)
		return numDistances * (numDistances + 1) / 2;
	}

	public void preprocessB() {
//		Set<Pos> positions = new HashSet<>();
//		positions.add(start);
//		for (int i = 0; i < 1_000_000; i++) {
//			int lastSize = positions.size();
//			positions.addAll(successors(positions));
//			if (lastSize == positions.size()) {
//				// All pairwise shortest paths are less than or equal to 2i
//				stepsToSaturation = 2 * i; // In the real example: 260
//				break;
//			}
//		}

		// distances to boundary tiles
		borderDist = new HashMap<>();
		for (var start : matrix.boundary()) {
			Map<Pos, Long> dist = new HashMap<>();
			borderDist.put(start, dist);
			Deque<Pair<Pos, Long>> queue = new ArrayDeque<>();
			queue.add(Pair.of(start, 0L));
			while (!queue.isEmpty()) {
				var pair = queue.removeFirst(); // BFS
				Pos pos = pair.first();
				if (!matrix.containsKey(pos) || dist.containsKey(pos)) {
					continue;
				}
				if (matrix.get(pos) == '#') {
					continue;
				}
				dist.put(pos, pair.second());
				for (Direction d : Direction.values()) {
					queue.add(Pair.of(pos.move(d), pair.second() + 1));
				}
			}
		}
	}

	public static void main(String[] args) {
		var s = new Solution();
		prynt(s.partB());
	}

	public void parseInput() {
		matrix = new CharMatrix(lines);
		for (var pos : matrix.getPositions()) {
			char c = matrix.get(pos);
			if (c == 'S') {
				start = pos;
				matrix.set(pos, '.');
			}
		}
	}

}
