package de.paulr.aoc2022.day16;

import static de.paulr.aoc2023.AoCUtil.input;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import de.paulr.aoc2022.day16.ElephantsTrappedInLabyrinth.ActorState;
import de.paulr.aoc2022.day16.ElephantsTrappedInLabyrinth.State;

public class ElephantsTrappedInLabyrinthTest {

	static ElephantsTrappedInLabyrinth again = new ElephantsTrappedInLabyrinth();
	static {
		again.readInput(input("2022/16s.txt"));
		again.preprocess();
	}

	@Test
	public void test() {
		TestStateList.initial() //
				.selectTargets("DD", "JJ") //
				.assertPressureReleased(20L * 24L + 21L * 23L) //
				.assertRemaining(26L) //
				.successors() //
				.selectTargets("DD", "JJ") //
				.assertPressureReleased(20L * 24L + 21L * 23L) //
				.assertRemaining(25L) //
				.successors() //
				.selectTargets("HH", "JJ") //
				.assertRemaining(24L) //
				.assertPressureReleased(20L * 24L + 21L * 23L + 22L * 19L);
	}

	@Test
	public void test2() {
//		Set<RelevantValve> valvesClosed = new HashSet<>(again.relevantValves);
//		valvesClosed.remove(again.idToRelevantValve.get("DD"));
//		valvesClosed.remove(again.idToRelevantValve.get("JJ"));
//		State state = new State(Pair.of(new ActorState(again.idToRelevantValve.get("DD"), 1L),
//				new ActorState(again.idToRelevantValve.get("JJ"), 2L)), new VolcanoState(again.relevantValves));
	}

	private boolean areTargetsPresent(State state, String s1, String s2) {
		return Set.of(s1, s2)
				.equals(Set.of(state.actors().first().targetValve().id(), state.actors().second().targetValve().id()));
	}

	static class TestStateList {

		private List<State> state;

		public TestStateList(List<State> state) {
			this.state = state;
		}

		public static TestStateList initial() {
			return new TestStateList(State.initialStates(again.initialValve, again.relevantValves, 26L));
		}

		public TestState selectTargets(String s1, String s2) {
			return state.stream() //
					.map(TestState::new) //
					.filter(ts -> ts.areTargetsPresent(s1, s2)) //
					.findFirst().get();
		}

	}

	static class TestState {

		private State state;

		public TestState(State state) {
			this.state = state;
		}

		public TestState assertTargets(String s1, String s2) {
			assertTrue(areTargetsPresent(s1, s2));
			return this;
		}

		public TestState assertTargets(String s1, long l1, String s2, long l2) {
			assertTrue(areTargetsPresent(s1, l1, s2, l2));
			return this;
		}

		public TestState assertPressureReleased(long pressureReleased) {
			assertThat(state.volcano().pressureReleased(), is(pressureReleased));
			return this;
		}

		public TestState assertRemaining(long remaining) {
			assertThat(state.volcano().remaining(), is(remaining));
			return this;
		}

		public TestStateList successors() {
			return new TestStateList(state.successors());
		}

		public boolean areTargetsPresent(String s1, String s2) {
			return Set.of(s1, s2).equals(
					Set.of(state.actors().first().targetValve().id(), state.actors().second().targetValve().id()));
		}

		public boolean areTargetsPresent(String s1, long l1, String s2, long l2) {
			ActorState as1 = new ActorState(again.idToRelevantValve.get(s1), l1);
			ActorState as2 = new ActorState(again.idToRelevantValve.get(s2), l2);
			return state.actors().equals(Pair.of(as1, as2)) || state.actors().equals(Pair.of(as2, as1));
		}

	}

}
