package de.paulr.aoc2023.day25;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import de.paulr.util.Pair;

public class FlowTest {

	@Test
	public void test() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of());
		Flow<String> flow = new Flow<>(network);

		assertEquals(network.getCutSize(flow.findResidualMinCut("a", "z")), 0L);
	}

	@Test
	public void test2() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of( //
			Pair.of("a", "z"), 1L));
		Flow<String> flow = new Flow<>(network);

		assertEquals(flow.getResidualMinCutSize("a", "z"), 1L);
	}

	@Test
	public void test3() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of( //
			Pair.of("a", "z"), 3L));
		Flow<String> flow = new Flow<>(network);

		assertEquals(flow.getResidualMinCutSize("a", "z"), 3L);
	}

	@Test
	public void test4() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of( //
			Pair.of("a", "z"), 1L, //
			Pair.of("z", "a"), 1L));
		Flow<String> flow = new Flow<>(network);

		assertEquals(flow.getResidualMinCutSize("a", "z"), 1L);
	}

	@Test
	public void test5() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of( //
			Pair.of("a", "z"), 1L, //
			Pair.of("a", "b"), 1L, //
			Pair.of("b", "z"), 1L, //
			Pair.of("z", "a"), 1L));
		Flow<String> flow = new Flow<>(network);

		assertEquals(flow.getResidualMinCutSize("a", "z"), 2L);
	}

	@Test
	public void test6() {
		FlowNetwork<String> network = new FlowNetwork<>(Map.of( //
			Pair.of("a", "b"), 2L, //
			Pair.of("b", "z"), 1L));
		Flow<String> flow = new Flow<>(network);

		assertEquals(flow.getResidualMinCutSize("a", "z"), 1L);
	}

}
