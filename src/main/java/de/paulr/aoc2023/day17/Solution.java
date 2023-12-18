package de.paulr.aoc2023.day17;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil.CharMatrix;
import de.paulr.aoc2023.AoCUtil.Direction;
import de.paulr.aoc2023.AoCUtil.Pos;
import de.paulr.util.Pair;
import de.paulr.util.Stopwatch;

class Solution extends ASolution {

	public Solution() {
		super(2023, 17, "");
	}

	public static void main(String[] args) {
		var s = new Solution();
		for (int i = 0; i < 50; i++) {
			prynt(s.partB2());
		}
	}

	@Override
	public Object partA() {
		final int MAX_STREAK = 3;
		var matrix = new CharMatrix(lines);
		Pos begin = matrix.topLeft();
		Pos end = matrix.bottomRight();

		Map<DPKey, Long> dp = new HashMap<>();
		PriorityQueue<Pair<DPKey, Long>> queue = new PriorityQueue<>(Comparator.comparing(Pair::second));
		queue.add(Pair.of(new DPKey(begin, null, 0), 0L));
		while (!queue.isEmpty()) {
			var pair = queue.remove();
			var key = pair.first();
			var value = pair.second();
			Long oldValue = dp.get(key);
			Long newValue = min(dp.get(key), value);
			dp.put(key, newValue);
			if (Objects.equals(oldValue, newValue)) {
				continue;
			}

			Direction[] dirs;
			if (key.direction == null) {
				dirs = Direction.values();
			} else {
				dirs = new Direction[] { key.direction, key.direction.rotateLeft(), key.direction.rotateRight() };
			}

			for (var dir : dirs) {
				var nextKey = key.then(dir);
				if (nextKey.streakLength > MAX_STREAK || !matrix.containsKey(nextKey.pos)) {
					continue;
				}
				queue.add(Pair.of(nextKey, value + Long.parseLong(matrix.get(nextKey.pos).toString())));
			}
		}

		return dp.entrySet().stream() //
			.filter(e -> e.getKey().pos.equals(end)) //
			.filter(e -> e.getValue() != null) //
			.mapToLong(e -> e.getValue()) //
			.min();
	}

	public record DPKey(Pos pos, Direction direction, int streakLength) {

		public DPKey then(Direction nextDirection) {
			return new DPKey(pos.move(nextDirection), nextDirection, nextDirection == direction ? streakLength + 1 : 1);
		}

	}

	public record DPKey2(int x, int y, boolean cameVertically, long cost) {
	}

	// Central insight in this version:
	// If a streak is executed at once, we don't need to remember the sign of the
	// direction we came from.
	// This cuts the size of the DP map -- and the runtime -- in half.
	// Getting rid of the DP map and just using a boolean array to track which
	// states we already visited again significantly reduces runtime.
	// Longifying objects also helped, and eliminating parseLong won me another
	// 100ms.
	// Finally, ditching the hand-made heap in favor of a bucketed priority queue
	// gave another performace boost.
	public Object partB2() {
		final int MIN_STREAK = 4;
		final int MAX_STREAK = 10;

		Stopwatch sw = new Stopwatch();
		int height = lines.size();
		int width = lines.get(0).length();

		long result = -1L;
		boolean[] visited = new boolean[(1 << 10) /* x */ * (1 << 10) /* y */ * 2 /* orientation */];
		BucketedPriorityQueue queue = new BucketedPriorityQueue();
		queue.add(longify(0, 0, true, 0L));
		queue.add(longify(0, 0, false, 0L));
		while (!queue.isEmpty()) {
			var key = queue.removeSmallest();
			int index = (int) (key % (1L << 21));
			if (visited[index]) {
				continue;
			}
			visited[index] = true;

			if (key % (1L << 20) == width - 1L + (1L << 10) * (height - 1)) {
				result = key >> 21;
				break;
			}

			for (int sign = -1; sign < 2; sign += 2) {
				boolean cameVertically = (key & (1L << 20)) > 0;
				long cost = key >> 21;
				int x = (int) (key % (1L << 10));
				int y = (int) ((key >> 10) % (1L << 10));
				for (int streak = 1; streak <= MAX_STREAK; streak++) {
					if (cameVertically) {
						x += sign;
						if (x < 0 || x >= width) {
							break;
						}
					} else {
						y += sign;
						if (y < 0 || y >= height) {
							break;
						}
					}
					cost += lines.get(y).charAt(x) - '0';
					if (streak < MIN_STREAK) {
						continue;
					}
					queue.add(longify(x, y, !cameVertically, cost));
				}
			}

		}

		prynt("Part B took {}ms", sw.elapsedMillis());

		return result;
	}

	public long longify(int x, int y, boolean cameVertically, long cost) {
		return x + (y << 10) + (cameVertically ? (1L << 20) : 0) + (cost << 21);
	}

	@Override
	public Object partB() {
		Stopwatch sw = new Stopwatch();
		final int MIN_STREAK = 4;
		final int MAX_STREAK = 10;
		var matrix = new CharMatrix(lines);
		Pos begin = matrix.topLeft();
		Pos end = matrix.bottomRight();

		Map<DPKey, Long> dp = new HashMap<>();
		PriorityQueue<Pair<DPKey, Long>> queue = new PriorityQueue<>(Comparator.comparing(Pair::second));
		queue.add(Pair.of(new DPKey(begin, null, 0), 0L));
		while (!queue.isEmpty()) {
			var pair = queue.remove();
			var key = pair.first();
			var value = pair.second();
			Long oldValue = dp.get(key);
			Long newValue = min(dp.get(key), value);
			dp.put(key, newValue);
			if (Objects.equals(oldValue, newValue)) {
				continue;
			}

			Direction[] dirs;
			if (key.direction == null) {
				dirs = Direction.values();
			} else if (key.streakLength >= MIN_STREAK) {
				dirs = new Direction[] { key.direction, key.direction.rotateLeft(), key.direction.rotateRight() };
			} else {
				dirs = new Direction[] { key.direction };
			}

			for (var dir : dirs) {
				var nextKey = key.then(dir);
				if (nextKey.streakLength > MAX_STREAK || !matrix.containsKey(nextKey.pos)) {
					continue;
				}
				queue.add(Pair.of(nextKey, value + Long.parseLong(matrix.get(nextKey.pos).toString())));
			}
		}

		long result = dp.entrySet().stream() //
			.filter(e -> e.getKey().pos.equals(end)) //
			.filter(e -> e.getKey().streakLength >= MIN_STREAK) //
			.filter(e -> e.getValue() != null) //
			.mapToLong(e -> e.getValue()) //
			.min().getAsLong();

		prynt("Part B took {}ms", sw.elapsedMillis());

		return result;
	}

	public Long max(Long a, Long b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return Math.max(a, b);
	}

	public Long min(Long a, Long b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return Math.min(a, b);
	}

	public Long plus(Long a, Long b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return a + b;
	}

	public static class BucketedPriorityQueue {

		private int size;
		private int begin;
		private int end;
		private long[][] buckets;

		public BucketedPriorityQueue() {
			size = 0;
			begin = 0;
			end = 0;
			buckets = new long[1024][];
		}

		public boolean isEmpty() {
			return size == 0;
		}

		public void add(long item) {
			int cost = (int) (item >> 21);
			if (size == 0) {
				begin = cost;
				end = cost + 1;
			} else {
				begin = Math.min(begin, cost);
				end = Math.max(end, cost + 1);
			}
			size++;
			if (cost >= buckets.length) {
				buckets = Arrays.copyOf(buckets, Integer.highestOneBit(cost + 1) << 1);
			}
			if (buckets[cost] == null) {
				buckets[cost] = new long[16];
				(buckets[cost])[0] = 0L; // size of the bucket
			}
			int bucketSize = (int) (buckets[cost])[0];
			if (bucketSize + 1 == buckets[cost].length) {
				buckets[cost] = Arrays.copyOf(buckets[cost], buckets[cost].length * 2);
			}
			(buckets[cost])[bucketSize + 1] = item;
			(buckets[cost])[0]++;
		}

		public long removeSmallest() {
			int index = (int) (buckets[begin])[0];
			long item = (buckets[begin])[index];

			(buckets[begin])[0]--;
			size--;
			if (size == 0) {
				begin = 0;
				end = 0;
			}
			while (buckets[begin] == null || (buckets[begin])[0] == 0) {
				begin++;
			}

			return item;
		}

	}

	public static class Heap {

		private int size = 0;
		private long[] items;

		public Heap() {
			this.items = new long[1024];
		}

		public Heap(int initialCapacity) {
			items = new long[initialCapacity];
		}

		public boolean isEmpty() {
			return size == 0;
		}

		public void add(long item) {
			int cursor = size;
			size++;
			if (size > items.length) {
				items = Arrays.copyOf(items, items.length * 2);
			}
			while (cursor > 0) {
				long child = items[cursor / 2];
				if (child <= item) {
					break;
				}
				items[cursor] = child;
				cursor /= 2;
			}
			items[cursor] = item;
		}

		public long removeSmallest() {
			var itemToReturn = items[0];
			size--;
			if (size == 0) {
				return itemToReturn;
			}
			var itemToMove = items[size];
			int cursor = 0;
			while (true) {
				if (size <= 2 * cursor + 1) {
					// no leaves
					break;
				} else if (size <= 2 * cursor + 2) {
					// one leaf
					var leaf = items[2 * cursor + 1];
					if (!(itemToMove <= leaf)) {
						items[cursor] = leaf;
						cursor = 2 * cursor + 1;
						continue;
					} else {
						break;
					}
				} else {
					// two leaves
					var leaf1 = items[2 * cursor + 1];
					var leaf2 = items[2 * cursor + 2];
					int smallerLeafIdx = leaf1 <= leaf2 ? (2 * cursor + 1) : (2 * cursor + 2);
					var smallerLeaf = smallerLeafIdx % 2 == 1 ? leaf1 : leaf2;
					if (!(itemToMove <= smallerLeaf)) {
						items[cursor] = smallerLeaf;
						cursor = smallerLeafIdx;
						continue;
					} else {
						break;
					}
				}

			}

			items[cursor] = itemToMove;
			return itemToReturn;
		}

	}

}
