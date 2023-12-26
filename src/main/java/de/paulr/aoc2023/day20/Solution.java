package de.paulr.aoc2023.day20;

import static de.paulr.aoc2023.AoCUtil.lcm;
import static de.paulr.aoc2023.AoCUtil.prynt;
import static java.util.stream.Collectors.joining;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import de.paulr.aoc2023.ASolution;
import de.paulr.util.Pair;
import de.paulr.util.statemachine.DynamicUtils;

class Solution extends ASolution {

	public Solution(String variant) {
		super(2023, 20, variant);
	}

	public static void main(String[] args) {
		prynt(lcm(4073L, 3739L, 3911L, 4003L));
//		for (String v : Set.of("t", "t2", "t3", "t4")) {
//			var s = new Solution(v);
//			// s.reduce();
//			prynt(s.partB());
//		}
	}

	public Map<String, ModuleType> moduleTypes;
	public Map<String, List<String>> connections;
	public Map<String, Set<String>> targetToSources;

	public void viz() {
		parseInput();
		for (var e : connections.entrySet()) {
			var s = e.getKey();
			for (var t : e.getValue()) {
				prynt("{} -> {};", prefixed(s), prefixed(t));
			}
		}
	}

	public void reduce() {
		parseInput();
		Set<String> modules = new HashSet<>();
		modules.add("broadcaster");
		Deque<String> queue = new ArrayDeque<>();
		queue.add("cn");
		while (!queue.isEmpty()) {
			String m = queue.removeLast();
			if (modules.contains(m)) {
				continue;
			}
			modules.add(m);
			if (!connections.containsKey(m)) {
				continue;
			}
			queue.addAll(connections.get(m));
		}
		for (var m : modules) {
			if (!connections.containsKey(m)) {
				continue;
			}
			var bla = connections.get(m).stream().filter(modules::contains).collect(joining(", "));
			if (bla.isBlank()) {
				continue;
			}
			prynt("{}{} -> {}", prefix(m), m, bla);
		}
	}

	private String prefix(String module) {
		if (moduleTypes.get(module) == null) {
			return "";
		}
		return switch (moduleTypes.get(module)) {
		case BROADCASTER -> "";
		case FLIPFLOP -> "%";
		case AND -> "&";
		};
	}

	private String prefixed(String module) {
		if (moduleTypes.get(module) == null) {
			return module;
		}
		return switch (moduleTypes.get(module)) {
		case BROADCASTER -> module;
		case FLIPFLOP -> "f_" + module;
		case AND -> "c_" + module;
		};
	}

	public Stats push(State state) {
		long lowPulses = 0L;
		long highPulses = 0L;
		long rxHigh = 0L, rxLow = 0L;
		Deque<Signal> signals = new ArrayDeque<>();
		signals.addLast(new Signal("button", "broadcaster", false));
		lowPulses++;
		while (!signals.isEmpty()) {
			var p = signals.removeFirst();
			String sender = p.sender;
			String module = p.receiver;
			boolean high = p.high;
			// prynt("{} -{}-> {}", sender, high, module);
			if (!moduleTypes.containsKey(module)) {
				continue;
			}
			Optional<Boolean> output = switch (moduleTypes.get(module)) {
			case BROADCASTER -> Optional.of(high);
			case FLIPFLOP -> {
				if (high) {
					yield Optional.empty();
				}
				boolean wasOn = state.activeFlipFlops.contains(module);
				boolean isOn = !wasOn;
				if (!isOn) {
					state.activeFlipFlops.remove(module);
				} else {
					state.activeFlipFlops.add(module);
				}
				yield Optional.of(isOn);
			}
			case AND -> {
				if (high) {
					state.andMemory.add(Pair.of(sender, module));
				} else {
					state.andMemory.remove(Pair.of(sender, module));
				}
				if (targetToSources.get(module).stream().allMatch(s -> state.andMemory.contains(Pair.of(s, module)))) {
					yield Optional.of(false);
				} else {
					yield Optional.of(true);
				}
			}
			};

			if (output.isEmpty()) {
				continue;
			}

			for (var conn : connections.get(module)) {
				signals.addLast(new Signal(module, conn, output.get()));
				if (output.get()) {
					highPulses++;
					if (conn.equals("rx"))
						rxHigh++;
				} else {
					lowPulses++;
					if (conn.equals("rx"))
						rxLow++;
				}
			}
		}

		return new Stats(lowPulses, highPulses, rxLow, rxHigh);
	}

	public record Signal(String sender, String receiver, boolean high) {

	}

	@Override
	public Object partA() {
		Stats stats = new Stats(0L, 0L, 0L, 0L);
		parseInput();
		State state = State.initial();
		for (int i = 0; i < 1000; i++) {
			stats = stats.plus(push(state));
		}
		return stats.summary();
	}

	@Override
	public Object partB() {
		parseInput();
		State state = State.initial();
		UnaryOperator<State> op = s -> {
			State s2 = new State(s);
			push(s2);
			return s2;
		};
		State cyc = DynamicUtils.transToCyclicState(state, op);
		prynt("Cyclic state is {}, steps to cycle {}, cyclic steps {}", //
			cyc, DynamicUtils.countStepsBetween(state, cyc, op), DynamicUtils.countRoundtripSteps(cyc, op));
		return null;
//		for (int i = 0; i < 1000_000; i++) {
//			Stats stats = push(state);
//			prynt("{}: {} low, {} high", i + 1, stats.rxLow, stats.rxHigh);
//			if (stats.rxLow == 1) {
//				return i + 1;
//			}
//		}
//		return null;
	}

	public void parseInput() {
		connections = new HashMap<>();
		moduleTypes = new HashMap<>();
		targetToSources = new HashMap<>();
		for (var line : lines) {
			String[] split = line.split(Pattern.quote(" -> "));
			String source = split[0];
			if (source.startsWith("%")) {
				source = source.substring(1);
				moduleTypes.put(source, ModuleType.FLIPFLOP);
			} else if (source.startsWith("&")) {
				source = source.substring(1);
				moduleTypes.put(source, ModuleType.AND);
			} else {
				assert source.equals("broadcaster");
				moduleTypes.put(source, ModuleType.BROADCASTER);
			}
			final String sourceF = source;
			List<String> targets = List.of(split[1].split(", "));
			connections.put(source, targets);
			targetToSources.computeIfAbsent(source, __ -> new HashSet<>());
			targets.forEach(t -> targetToSources.computeIfAbsent(t, __ -> new HashSet<>()).add(sourceF));
		}
	}

	public record State(Set<String> activeFlipFlops, Set<Pair<String, String>> andMemory) {

		public static State initial() {
			return new State(new HashSet<>(), new HashSet<>());
		}

		public State(State other) {
			this(new HashSet<>(other.activeFlipFlops), new HashSet<>(other.andMemory));
		}

	}

	public record Stats(long low, long high, long rxLow, long rxHigh) {

		public long summary() {
			return low * high;
		}

		public Stats plus(Stats other) {
			return new Stats(low + other.low, high + other.high, rxLow + other.rxLow, rxHigh + other.rxHigh);
		}

	}

	public enum ModuleType {
		FLIPFLOP, AND, BROADCASTER
	}

}
