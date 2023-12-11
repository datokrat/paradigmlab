package de.paulr.aoc2022.day16;

import static de.paulr.aoc2023.AoCUtil.format;
import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static de.paulr.parser.Parsers.regex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.paulr.algorithms.search.SearchUtils;
import de.paulr.aoc2023.ASolution;
import de.paulr.aoc2023.AoCUtil;
import de.paulr.parser.IParser;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;
import de.paulr.util.Rope;
import de.paulr.util.Stopwatch;

/**
 * A DP solution for AoC 2022 Day 16. Here is the output:
 * 
 * <pre>
 * === Part A ===
 * Computation took 765ms
 * On our own, the best we can do is releasing 1857 pressure.
 * 0min: We're at valve AA (flow rate 0). Pressure guaranteed to be released: 0.
 * 3min: We're at valve MA (flow rate 10). Pressure guaranteed to be released: 270.
 * 6min: We're at valve II (flow rate 20). Pressure guaranteed to be released: 750.
 * 9min: We're at valve AS (flow rate 16). Pressure guaranteed to be released: 1086.
 * 13min: We're at valve RU (flow rate 11). Pressure guaranteed to be released: 1273.
 * 16min: We're at valve PM (flow rate 25). Pressure guaranteed to be released: 1623.
 * 19min: We're at valve KQ (flow rate 18). Pressure guaranteed to be released: 1821.
 * 26min: We're at valve ED (flow rate 9). Pressure guaranteed to be released: 1857.
 * 
 * === PART B ===
 * Computation took 932ms
 * With the elephant's help, we can release 2536 = 1421 + 1115 pressure.
 * 0min: We're at valve AA (flow rate 0). Pressure guaranteed to be released: 0.
 * 3min: We're at valve MA (flow rate 10). Pressure guaranteed to be released: 230.
 * 6min: We're at valve II (flow rate 20). Pressure guaranteed to be released: 630.
 * 9min: We're at valve AS (flow rate 16). Pressure guaranteed to be released: 902.
 * 13min: We're at valve RU (flow rate 11). Pressure guaranteed to be released: 1045.
 * 16min: We're at valve PM (flow rate 25). Pressure guaranteed to be released: 1295.
 * 19min: We're at valve KQ (flow rate 18). Pressure guaranteed to be released: 1421.
 * 
 * 0min: We're at valve AA (flow rate 0). Pressure guaranteed to be released: 0.
 * 3min: We're at valve HR (flow rate 14). Pressure guaranteed to be released: 322.
 * 6min: We're at valve DW (flow rate 4). Pressure guaranteed to be released: 402.
 * 10min: We're at valve XO (flow rate 23). Pressure guaranteed to be released: 770.
 * 13min: We're at valve VI (flow rate 22). Pressure guaranteed to be released: 1056.
 * 21min: We're at valve MW (flow rate 7). Pressure guaranteed to be released: 1091.
 * 24min: We're at valve FQ (flow rate 12). Pressure guaranteed to be released: 1115.
 * </pre>
 */
class ElephantsTrappedInDPArray extends ASolution {

	static final String FILE = "2022_16.txt";
	static final String EXAMPLE = "2022_16s.txt";

	public ElephantsTrappedInDPArray(String filename) {
		super(filename);
	}

	public static void main(String[] args) {
		var s = new ElephantsTrappedInDPArray(FILE);
		s.readInput();
		s.partA();
		s.partB();
	}

	/*
	 * We skip all valves that are not working (flow rate zero) and compute shortest
	 * paths between the remaining valves. It turns out that, together with the
	 * start valve AA, only 16 valves remain. We indicate them with numbers 0 to 15.
	 * They are sorted decreasingly by their flow rate, so AA is 15.
	 */

	public int startValve;
	public List<String> valveIds;
	public List<Long> flowRates;
	public List<Long> distances;

	/*
	 * On our quest to release as much pressure as possible, we realize that the
	 * optimal way will require us to start in AA and then to open other valves
	 * without making any breaks until there is nothing more we can do in time.
	 * 
	 * The idea is to implement a DP algorithm: For any set of positive-flow valves
	 * (represented by a bitmask (0..(2^15 - 1))), a valve in this set (0..15), and
	 * a duration (0..MAX_TIME), we compute the maximum pressure that can be
	 * achieved in exactly this time, opening exactly the valves in the given set
	 * and ending with exactly the given valve.
	 * 
	 * Each result is increased by 1 and stored as an entry of max under a certain
	 * index computed from the parameters. By default all entries in max are zeroed
	 * by Java, and we use this to signal that we found no way at all to get into
	 * the given situation (e.g., it might turns out to be impossible to open all
	 * valves). The prev array stores helpful information to reconstruct how the
	 * best solution can be achieved. It would be sad to die among the elephants
	 * because our program only tells us that we could survive without explaining
	 * how.
	 */

	public long[] max;
	public int[] prev;
	long best;
	int bestIndex;

	/*
	 * This is a bit hacky: We use this global variable to define the total time
	 * available.
	 */
	int totalTime;

	/**
	 * Prints an explanation of the solution represented by <code>index</code>.
	 * Example:
	 * 
	 * <pre>
	 * 0min: We're at valve AA (flow rate 0). Pressure guaranteed to be released: 0.
	 * 3min: We're at valve MA (flow rate 10). Pressure guaranteed to be released: 270.
	 * 6min: We're at valve II (flow rate 20). Pressure guaranteed to be released: 750.
	 * 9min: We're at valve AS (flow rate 16). Pressure guaranteed to be released: 1086.
	 * 13min: We're at valve RU (flow rate 11). Pressure guaranteed to be released: 1273.
	 * 16min: We're at valve PM (flow rate 25). Pressure guaranteed to be released: 1623.
	 * 19min: We're at valve KQ (flow rate 18). Pressure guaranteed to be released: 1821.
	 * 26min: We're at valve ED (flow rate 9). Pressure guaranteed to be released: 1857.
	 * </pre>
	 * 
	 */
	public void showStepByStepSolution(int index) {
		CollectionUtils.reverse(CollectionUtils.genArrayList(g -> {
			int i = index;
			while (i != 0) {
				int last = (i >> 15) % 16;
				int time = (i >> 19) % (totalTime + 1);
				g.add(format("{}min: We're at valve {} (flow rate {}). Pressure guaranteed to be released: {}.", time,
					valveIds.get(last), flowRates.get(last), max[i] - 1));
				i = prev[i];
			}
		})).forEach(AoCUtil::prynt);
	}

	public long getMaxPressure(int mask, int last, int time) {
		return max[getIndex(mask, last, time)] - 1;
	}

	public void updatePressure(int mask, int last, int time, long pressure, int reason) {
		int index = getIndex(mask, last, time);
		long oldPressure = max[index] - 1;
		if (oldPressure < pressure) {
			prev[index] = reason;
			max[index] = pressure + 1;
		}
	}

	public int getIndex(int mask, int last, int time) {
		return mask + (1 << 15) * last + (1 << 19) * time;
	}

	public int getArraySize() {
		return (1 << 15) * 16 * (totalTime + 1);
	}

	public int getDistance(int from, int to) {
		return (int) (long) distances.get(from * 16 + to);
	}

	public Object partA() {
		prynt("=== Part A ===");
		Stopwatch sw = new Stopwatch();

		totalTime = 30;
		dp();

		prynt("Computation took {}ms", sw.elapsedMillis());

		prynt("On our own, the best we can do is releasing {} pressure.", best);
		showStepByStepSolution(bestIndex);
		return best;
	}

	public Object partB() {
		prynt();
		prynt("=== PART B ===");
		Stopwatch sw = new Stopwatch();

		totalTime = 26;
		dp();
		long[] maxPressureForMask = new long[1 << 15];
		int[] reasonsForMask = new int[1 << 15];
		for (int i = 0; i < getArraySize(); i++) {
			if (maxPressureForMask[i % (1 << 15)] < max[i]) {
				maxPressureForMask[i % (1 << 15)] = max[i];
				reasonsForMask[i % (1 << 15)] = i;
			}
		}
		for (int mask = 0; mask < (1 << 15); mask++) {
			for (int valve = 0; valve < 15; valve++) {
				if (maxPressureForMask[mask] < maxPressureForMask[mask & ~(1 << valve)]) {
					maxPressureForMask[mask] = maxPressureForMask[mask & ~(1 << valve)];
					reasonsForMask[mask] = reasonsForMask[mask & ~(1 << valve)];
				}
			}
		}

		long maxPressureWithElephant = 0L;
		int reasonMask = 0;
		int reasonMaskElephant = 0;
		for (int mask = 0; mask < (1 << 14); mask++) {
			int elephantMask = (1 << 15) - 1 - mask;
			if (maxPressureWithElephant < maxPressureForMask[mask] + maxPressureForMask[elephantMask] - 2) {
				maxPressureWithElephant = maxPressureForMask[mask] + maxPressureForMask[elephantMask] - 2;
				reasonMask = mask;
				reasonMaskElephant = elephantMask;
			}
		}
		prynt("Computation took {}ms", sw.elapsedMillis());
		prynt("With the elephant's help, we can release {} = {} + {} pressure.", maxPressureWithElephant,
			maxPressureForMask[reasonMask] - 1, maxPressureForMask[reasonMaskElephant] - 1);
		showStepByStepSolution(reasonsForMask[reasonMask]);
		prynt();
		showStepByStepSolution(reasonsForMask[reasonMaskElephant]);
		return maxPressureWithElephant;
	}

	public void dp() {
		max = new long[getArraySize()];
		prev = new int[getArraySize()];
		best = -1;
		bestIndex = 0;
		// All entries are -1 by default, except this one:
		// We are forced to start at AA (=15) at time 0.
		updatePressure(0, 15, 0, 0, 0);
		for (int mask = 0; mask < (1 << 15); mask++) {
			for (int valve = 0; valve < 15; valve++) {
				int largerMask = mask | (1 << valve);
				if (mask == largerMask) {
					continue;
				}
				for (int last = 0; last < 16; last++) {
					if (last == 15 && mask > 0) {
						// AA is only allowed when the mask is 0
						continue;
					}
					if (((mask | (1 << 15)) & (1 << last)) == 0) {
						// "last" is not contained in the mask and is also not AA
						continue;
					}
					int timeToOpenValve = getDistance(last, valve) + 1;
					for (int time = 0; time + timeToOpenValve <= totalTime; time++) {
						int timeNeeded = time + timeToOpenValve;
						int remaining = totalTime - timeNeeded;
						long oldPressure = getMaxPressure(mask, last, time);
						if (oldPressure == -1) {
							continue; // -1 means that this is not possible!
						}
						long pressure = oldPressure + remaining * flowRates.get(valve);
						int reason = getIndex(mask, last, time);
						updatePressure(largerMask, valve, timeNeeded, pressure, reason);
						if (pressure > best) {
							best = pressure;
							bestIndex = getIndex(largerMask, valve, timeNeeded);
						}
					}
				}
			}
		}
	}

	// === Input ===

	static IParser<List<String>> tunnelPsr = exact("tunnels lead to valves ")
		.silentlyThen(regex("..").star(", ").map(Rope::toList)) //
		.or(exact("tunnel leads to valve ").silentlyThen(regex("..").map(List::of)));

	static IParser<InputValve> valvePsr = exact("Valve ").silentlyThen(regex("..")).thenSilently(" has flow rate=")
		.then(longNumber).thenSilently("; ") //
		.then(tunnelPsr) //
		.map(Pair.fn(InputValve::new));

	public void readInput() {
		List<InputValve> valves = lines.stream().map(valvePsr::parseOne) //
			.sorted(Comparator.comparing((InputValve v) -> -v.flowRate)) //
			.toList();

		Map<Pair<String, String>, Long> metric = new HashMap<>();
		List<String> inputValveIds = new ArrayList<>();
		for (var valve : valves) {
			for (var tunnel : valve.tunnels) {
				metric.put(Pair.of(valve.id, tunnel), 1L);
			}
			inputValveIds.add(valve.id);
		}
		SearchUtils.floydWarshall(inputValveIds, metric);

		List<InputValve> relevantValves = valves.stream() //
			.filter(v -> v.flowRate > 0 || v.id.equals("AA")) //
			.toList();

		startValve = relevantValves.size() - 1; // AA is the only valve with flow rate zero

		Map<String, Integer> valveToIndex = new HashMap<>();

		distances = new ArrayList<>(relevantValves.size() * relevantValves.size());

		valveIds = new ArrayList<>(relevantValves.size());
		flowRates = new ArrayList<>(relevantValves.size());
		for (int i = 0; i < relevantValves.size(); i++) {
			var valve = relevantValves.get(i);
			valveToIndex.put(valve.id, i);
			valveIds.add(valve.id);
			flowRates.add(valve.flowRate);

			for (int j = 0; j < relevantValves.size(); j++) {
				InputValve valve2 = relevantValves.get(j);
				Pair<String, String> v12 = Pair.of(valve.id, valve2.id);
				if (!metric.containsKey(v12)) {
					throw new RuntimeException();
				}
				distances.add(metric.get(v12));
			}
		}
	}

	public record InputValve(String id, long flowRate, List<String> tunnels) {
	}

}
