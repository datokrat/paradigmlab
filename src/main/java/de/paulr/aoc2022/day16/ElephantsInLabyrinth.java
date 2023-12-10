package de.paulr.aoc2022.day16;

import static de.paulr.aoc2023.AoCUtil.input;
import static de.paulr.aoc2023.AoCUtil.prynt;
import static de.paulr.parser.Parsers.exact;
import static de.paulr.parser.Parsers.longNumber;
import static de.paulr.parser.Parsers.regex;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.paulr.parser.IParser;
import de.paulr.util.CollectionUtils;
import de.paulr.util.Pair;
import de.paulr.util.Pair.OneOrTwo;
import de.paulr.util.Rope;

class ElephantsInLabyrinth {

	static final String FILE = "2022/16.txt";
	static final long TOTAL_TIME = 26L;

	public static void main(String[] args) {
		ElephantsInLabyrinth s = new ElephantsInLabyrinth();
		prynt(s.run(input(FILE)));
	}

	public Object run(List<String> lines) {
		readInput(lines);
		preprocess();
		return optimize();
	}

	// === Computation

	public long optimize() {
		long max = 0L;

		long iterations = 0L;
		long heuristicSkips = 0L;
		Map<PressureAgnosticState, Long> stateToPressure = new HashMap<>();
		Map<Long, Long> stepStatistics = new HashMap<>();

		Deque<State> queue = new ArrayDeque<>();
		queue.addAll(State.initialStates(initialValve, relevantValves, TOTAL_TIME));
		while (!queue.isEmpty()) {
			var state = queue.removeFirst();
			var step = TOTAL_TIME - state.volcano.remaining;

			// filter out irrelevant states

			if (stateToPressure.getOrDefault(state.pressureAgnostic(), -1L) >= state.volcano.pressureReleased) {
				// This workpackage is not better than previous ones -> omit it
				continue;
			}
			if (heuristics.upperBound2(state) <= max) {
				heuristicSkips++;
				continue;
			}

			// process relevant states and update statistics

			if (state.volcano.pressureReleased > max) {
				max = state.volcano.pressureReleased;
			}

			stateToPressure.put(state.pressureAgnostic(), state.volcano.pressureReleased);
			CollectionUtils.update(stepStatistics, step, 0L, counter -> counter + 1);
			iterations++;

			// logging

			if (iterations % 100_000L == 0) {
				prynt(iterations, ": ", max, " (" + heuristicSkips + " skipped), ", stepStatistics);
			}

			// debug verifications

			state.verify();

			// spawn successor states

			for (var successorState : state.successors()) {
				queue.addFirst(successorState); // DFS
			}
		}

		return max;
	}

	public record State(Pair<ActorState, ActorState> actors, VolcanoState volcano) {

		public static List<State> initialStates(RelevantValve initialValve, List<RelevantValve> valvesClosed,
				long remaining) {
			List<State> initialStates = new ArrayList<>();
			for (var valveOne : valvesClosed) {
				var actorOne = ActorState.initial(initialValve, valveOne);
				for (var valveTwo : valvesClosed) {
					if (valveOne == valveTwo) {
						continue;
					}
					var actorTwo = ActorState.initial(initialValve, valveTwo);

					var updatedValvesClosed = new HashSet<>(valvesClosed);
					updatedValvesClosed.remove(valveOne);
					updatedValvesClosed.remove(valveTwo);
					VolcanoState volcano = VolcanoState.initial(new HashSet<>(valvesClosed), remaining) //
							.open(valveOne, actorOne.distance) //
							.open(valveTwo, actorTwo.distance);
					State state = new State(Pair.of(actorOne, actorTwo), volcano).normalForm();
					initialStates.add(state);
				}
			}
			return initialStates;
		}

		public void verify() {
			if (volcano.remaining < 0) {
				throw new RuntimeException("The remaining time is negative.");
			}
			if (actors.first().targetValve == actors.second().targetValve) {
				throw new RuntimeException("The actors must not target the same valve.");
			}
			if (!this.equals(normalForm())) {
				throw new RuntimeException("The state is not in normal form.");
			}
			if (volcano.valvesClosed.contains(actors.first().targetValve)
					|| volcano.valvesClosed.contains(actors.second().targetValve)) {
				throw new RuntimeException("The target valves must not be marked closed anymore.");
			}
		}

		public List<State> successors() {
			if (isTerminal()) {
				return List.of();
			}
			return tick().actorOptionsAfterTick(OneOrTwo.ONE).stream() //
					.flatMap(state1 -> state1.actorOptionsAfterTick(OneOrTwo.TWO).stream()) //
					.map(State::normalForm) //
					.toList();
		}

		private boolean isTerminal() {
			return volcano.remaining == 0L;
		}

		public List<State> actorOptionsAfterTick(OneOrTwo actorNumber) {
			var currentActor = Pair.get(actors, actorNumber);
			if (!currentActor.canOpen()) {
				return List.of(new State(Pair.replacing(actors, actorNumber, currentActor.closeIn()), volcano));
			}

			List<State> options = new ArrayList<>();
			for (var nextTarget : volcano.valvesClosed) {
				options.add(openValveAndChooseNextTarget(actorNumber, nextTarget));
			}
			return options;
		}

		private State openValveAndChooseNextTarget(OneOrTwo actorNumber, RelevantValve nextTarget) {
			var oldActor = Pair.get(actors, actorNumber);
			var newActor = oldActor.newTarget(nextTarget);
			return new State( //
					Pair.replacing(actors, actorNumber, oldActor.newTarget(nextTarget)),
					volcano.open(nextTarget, newActor.distance));
		}

		private State tick() {
			return new State(actors, volcano.tick());
		}

		private State normalForm() {
			return new State( //
					Pair.sort(actors, Comparator.comparing(ActorState::hashCode)), //
					volcano);
		}

		public PressureAgnosticState pressureAgnostic() {
			return new PressureAgnosticState(actors, volcano.valvesClosed, volcano.remaining);
		}

	}

	public record PressureAgnosticState(Pair<ActorState, ActorState> actors, Set<RelevantValve> valvesClosed,
			long remaining) {
	}

	public record ActorState(RelevantValve targetValve, long distance) {

		public static ActorState initial(RelevantValve startValve, RelevantValve targetValve) {
			return ActorState.newTarget(targetValve, startValve.successorToDistance.get(targetValve.id));
		}

		public static ActorState newTarget(RelevantValve targetValve, long distance) {
			return new ActorState(targetValve, distance);
		}

		public boolean canOpen() {
			return distance == 0;
		}

		public ActorState closeIn() {
			return new ActorState(targetValve, distance - 1);
		}

		public ActorState newTarget(RelevantValve newTarget) {
			return ActorState.initial(targetValve, newTarget);
		}

	}

	public record VolcanoState(Set<RelevantValve> valvesClosed, long remaining, long pressureReleased) {

		public static VolcanoState initial(Set<RelevantValve> valvesClosed, long remaining) {
			return new VolcanoState(valvesClosed, remaining, 0L);
		}

		public VolcanoState tick() {
			return new VolcanoState(valvesClosed, remaining - 1, pressureReleased);
		}

		public VolcanoState open(RelevantValve valve, long distance) {
			Set<RelevantValve> totalValvesClosed = new HashSet<>(valvesClosed);
			totalValvesClosed.remove(valve);
			long totalPressureReleased = pressureReleased + valve.flowRate * Math.max(0L, remaining - distance - 1);
			return new VolcanoState(totalValvesClosed, remaining, totalPressureReleased);
		}

	}

	// === Heuristics

	public Heuristics heuristics = new Heuristics();

	public class Heuristics {

		public long upperBound(State state) {
			long factor = state.volcano.remaining - 1;
			return state.volcano.pressureReleased + state.volcano.valvesClosed.stream() //
					.map(v -> v.flowRate * factor) //
					.reduce(0L, (a, b) -> a + b);
		}

		public long upperBound2(State state) {
			long result = state.volcano.pressureReleased;
			long actorOneFactor = state.volcano.remaining - state.actors.first().distance - 1;
			long actorTwoFactor = state.volcano.remaining - state.actors.second().distance - 1;
			for (var valve : relevantValves) { // relevantValves was sorted suitably during preprocessing
				long largeFactor = Math.max(actorOneFactor, actorTwoFactor);
				if (largeFactor < 1) {
					return result;
				}
				result += valve.flowRate * largeFactor;
				if (actorOneFactor == largeFactor) {
					actorOneFactor -= 2;
				} else {
					actorTwoFactor -= 2;
				}
			}
			return result;
		}

//		public long upperBound3(State state) {
//			long result = state.volcano.pressureReleased;
//			long actorOneRemaining = state.volcano.remaining - state.actors.first().distance - 1;
//			long actorTwoRemaining = state.volcano.remaining - state.actors.second().distance - 1;
//			var sortedValves = relevantValves.stream() //
//					.sorted(Comparator.comparing(v -> ((double) v.flowRate) / state.actors.first().targetValve.successorToDistance.get(v))).toList();
//			for (var valve : relevantValves) {
//				long actorOneFactor = actorOneRemaining
//						- state.actors.first().targetValve.successorToDistance.get(valve);
//				long actorTwoFactor = actorTwoRemaining
//						- state.actors.second().targetValve.successorToDistance.get(valve);
//				long largeFactor = Math.max(actorOneFactor, actorTwoFactor);
//				if (largeFactor < 1) {
//					return result;
//				}
//				result += valve.flowRate * largeFactor;
//				if (actorOneFactor == largeFactor) {
//					actorOneRemaining -= 2;
//				} else {
//					actorTwoRemaining -= 2;
//				}
//			}
//			return result;
//		}

	}

	// === Input preprocessing ===

	public List<RelevantValve> relevantValves;
	public Map<String, RelevantValve> idToRelevantValve;
	public RelevantValve initialValve;

	public void preprocess() {
		relevantValves = new ArrayList<>();
		for (var inputValve : valves) {
			if (inputValve.flowRate == 0 && !inputValve.id.equals("AA")) {
				continue;
			}
			RelevantValve relevantValve = new RelevantValve(inputValve.flowRate, new HashMap<>(), inputValve.id);
			relevantValves.add(relevantValve);
			if (inputValve.id.equals("AA")) {
				initialValve = relevantValve;
			}
		}
		relevantValves.sort(Comparator.comparing(v -> -v.flowRate));
		idToRelevantValve = new HashMap<>();
		for (var relevantValve : relevantValves) {
			determineDistancesFrom(relevantValve);
			idToRelevantValve.put(relevantValve.id, relevantValve);
		}
	}

	private void determineDistancesFrom(RelevantValve valve) {
		Map<String, Long> intermediateDistances = new HashMap<>();
		Deque<Pair<InputValve, Long>> queue = new ArrayDeque<>();
		queue.add(Pair.of(idToInputValve.get(valve.id), 0L));
		while (!queue.isEmpty()) {
			var pair = queue.removeFirst();
			var targetValve = pair.first();
			var distance = pair.second();
			if (intermediateDistances.containsKey(targetValve.id)) {
				continue;
			}
			intermediateDistances.put(targetValve.id, distance);
			for (var successorId : targetValve.tunnels) {
				var successorValve = idToInputValve.get(successorId);
				queue.addLast(Pair.of(successorValve, distance + 1));
			}
		}
		for (var relevantValve : relevantValves) {
			valve.successorToDistance.put(relevantValve.id, intermediateDistances.get(relevantValve.id));
		}
	}

	public record RelevantValve(long flowRate, Map<String, Long> successorToDistance, String id) {
	}

	// === Input ===

	public List<InputValve> valves;
	public Map<String, InputValve> idToInputValve;

	static IParser<List<String>> tunnelPsr = exact("tunnels lead to valves ")
			.silentlyThen(regex("..").star(", ").map(Rope::toList)) //
			.or(exact("tunnel leads to valve ").silentlyThen(regex("..").map(List::of)));

	static IParser<InputValve> valvePsr = exact("Valve ").silentlyThen(regex("..")).thenSilently(" has flow rate=")
			.then(longNumber).thenSilently("; ") //
			.then(tunnelPsr) //
			.map(Pair.fn(InputValve::new));

	public void readInput(List<String> lines) {
		valves = lines.stream().map(valvePsr::parseOne) //
				.sorted(Comparator.comparing((InputValve v) -> -v.flowRate)) //
				.toList();
		idToInputValve = valves.stream().collect(toMap(InputValve::id, x -> x));
	}

	public record InputValve(String id, long flowRate, List<String> tunnels) {
	}

}
