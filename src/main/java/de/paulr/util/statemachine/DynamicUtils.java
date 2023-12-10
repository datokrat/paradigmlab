package de.paulr.util.statemachine;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

import de.paulr.util.Pair;

public class DynamicUtils {

	public static <S> UnaryOperator<Pair<Integer, S>> counting(UnaryOperator<S> op) {
		return p -> Pair.of(p.first() + 1, op.apply(p.second()));
	}

	public static <S> S transToCyclicState(S state, UnaryOperator<S> transition) {
		S current = state;
		Set<S> states = new HashSet<>();
		while (!states.contains(current)) {
			states.add(current);
			current = transition.apply(current);
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
