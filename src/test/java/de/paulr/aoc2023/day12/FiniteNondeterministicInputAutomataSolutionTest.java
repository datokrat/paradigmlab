package de.paulr.aoc2023.day12;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.paulr.aoc2023.day12.FiniteNondeterministicInputAutomataSolution.State;

public class FiniteNondeterministicInputAutomataSolutionTest {

	@Test
	public void finiteStateMachine() {
		assertTrue(State.init().consume("#", List.of()).isEmpty());
		assertTrue(State.init().consume(".#", List.of()).isEmpty());
		assertTrue(State.init().consume("##", List.of(1L)).isEmpty());
		assertTrue(State.init().consume("#.#", List.of(1L)).isEmpty());
		assertTrue(State.init().consume("..#.##", List.of(1L, 2L)).isPresent());
		assertTrue(State.init().consume("..#...##.", List.of(1L, 2L)).isPresent());
		assertTrue(State.init().consume("..#.##.#", List.of(1L, 2L)).isEmpty());
	}

	@Test
	public void nondeterminism() {
		assertThat(FiniteNondeterministicInputAutomataSolution.countMatches("??.??", List.of(1L, 1L)), is(4L));
	}

}
