package de.paulr.util.statemachine;

import static de.paulr.aoc2023.AoCUtil.prynt;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import de.paulr.util.Pair;

public class DynamicUtils {

	public static <S> UnaryOperator<Pair<Integer, S>> counting(UnaryOperator<S> op) {
		return p -> Pair.of(p.first() + 1, op.apply(p.second()));
	}

	public static <S, R> S transToCyclicState2(S state, Function<S, Pair<S, R>> op) {
		return transToCyclicState(state, s -> op.apply(s).first());
	}

	public static <S> S transToCyclicState(S state, UnaryOperator<S> transition) {
		S current = state;
		Set<S> states = new HashSet<>();
		int i = 0;
		while (!states.contains(current)) {
			states.add(current);
			current = transition.apply(current);
			i++;
			if (i % 100_000 == 0) {
				prynt("Still looking for cycle (iteration {})", i);
			}
		}
		return current;
	}

	public static <S> long countStepsBetween(S start, S end, UnaryOperator<S> transition) {
		long counter = 0;
		S current = start;
		while (!current.equals(end)) {
			counter++;
			current = transition.apply(current);
		}
		return counter;
	}

	public static <S> long countRoundtripSteps(S start, UnaryOperator<S> transition) {
		return countStepsBetween(transition.apply(start), start, transition) + 1;
	}

}
