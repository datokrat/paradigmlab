package de.paulr.aoc2023.day12;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.paulr.aoc2023.day12.Solution.StateX;

public class SolutionTest {

	@Test
	public void test() {
		Solution s = new Solution(Solution.FILE);
		assertEquals(s.divideAndConquer(".#??.", 1, 2, new StateX(List.of(2L), false, false)), 0L);
		assertEquals(s.divideAndConquer(".#??.", 0, 2, new StateX(List.of(2L), true, false)), 0L);
		assertEquals(s.divideAndConquer(".#??.", 0, 5, new StateX(List.of(2L), true, true)), 1L);
		assertEquals(s.divideAndConquer(".#??.", 1, 2, new StateX(List.of(2L), true, false)), 0L);
		assertEquals(s.divideAndConquer(".", 0, 1, new StateX(List.of(), true, true)), 1L);
		assertEquals(s.divideAndConquer("..", 0, 2, new StateX(List.of(), true, true)), 1L);
		assertEquals(s.divideAndConquer(".....", 0, 5, new StateX(List.of(), true, true)), 1L);
		assertEquals(s.divideAndConquer("..#..", 0, 5, new StateX(List.of(), true, true)), 0L);
		assertEquals(s.divideAndConquer("..?..", 0, 5, new StateX(List.of(), true, true)), 1L);
		assertEquals(s.divideAndConquer("..?..", 0, 5, new StateX(List.of(), false, true)), 0L);
		assertEquals(s.divideAndConquer("?#?..", 0, 5, new StateX(List.of(2L), false, true)), 1L);
		assertEquals(s.divideAndConquer(".???..", 0, 5, new StateX(List.of(1L), true, true)), 3L);
	}

}
