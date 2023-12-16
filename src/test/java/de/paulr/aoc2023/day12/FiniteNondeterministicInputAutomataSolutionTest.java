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
	public void testCases() {
//		testCase("??", List.of(1L), 2L);
//		testCase("??", List.of(2L), 1L);
//		testCase("??", List.of(), 1L);
//		testCase("??", List.of(1L, 1L), 0L);
		testCase("??.??", List.of(1L, 1L), 4L);
	}

	private void testCase(String record, List<Long> groups, long expected) {
		assertThat(FiniteNondeterministicInputAutomataSolution.matchNondeterministicBackwards(record, groups)
			.getOrDefault(State.init(), 0L), is(expected));
		assertThat(FiniteNondeterministicInputAutomataSolution.countMatches(record, groups), is(expected));
		assertThat(FiniteNondeterministicInputAutomataSolution.countMatchesMeetInTheMiddle(record, groups),
			is(expected));
		assertThat(FiniteNondeterministicInputAutomataSolution.countVectorized(record, groups), is(expected));
		assertThat(FiniteNondeterministicInputAutomataSolution.countVectorizedMeetInTheMiddle(record, groups),
			is(expected));
	}

}
