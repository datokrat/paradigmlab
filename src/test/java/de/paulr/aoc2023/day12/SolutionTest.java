package de.paulr.aoc2023.day12;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import de.paulr.aoc2023.day12.Solution.DPKey;
import de.paulr.aoc2023.day12.Solution.StateX;

public class SolutionTest {

	@Test
	public void testDivideAndConquer() {
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

	@Test
	public void testDP() {
		Solution s = new Solution(Solution.FILE);
		assertEquals(s.dp(".", List.of()), 1L);
		assertEquals(s.dp("..", List.of()), 1L);
		assertEquals(s.dp("...", List.of()), 1L);
		assertEquals(s.dp("....", List.of()), 1L);
		assertEquals(s.dp(".?..", List.of()), 1L);
		assertEquals(s.dp("#", List.of()), 0L);
		assertEquals(s.dp(".#", List.of()), 0L);
		assertEquals(s.dp(".#.", List.of()), 0L);
		assertEquals(s.dp(".#?", List.of(1L)), 1L);
		assertEquals(s.dp(".#?", List.of(2L)), 1L);
		assertEquals(s.dp("#?", List.of(2L)), 1L);
		assertEquals(s.dp("????", List.of(1L)), 4L);
		assertEquals(s.dp("????", List.of(2L)), 3L);
		assertEquals(s.dpInternalLocal("#?.?#??#??", List.of(1L, 7L), new DPKey(0, 3, 0, 1), new HashMap<>()), 1L);
		assertEquals(s.dpInternalLocal("#?.?#??#??.", List.of(1L, 7L), new DPKey(3, 11, 1, 2), new HashMap<>()), 1L);
		assertEquals(s.dp("#?.?#??#??", List.of(1L, 7L)), 1L);
		assertEquals(s.dp("##.?", List.of(2L, 1L)), 1L);
		assertEquals(s.dp(".#?.?#??#??.?#?#?.?", List.of(1L, 7L, 5L, 1L)), 1L);
		assertEquals(s.dp("??????.#.?????.", List.of(1L, 1L, 1L, 4L)), 0L);
		// A nasty case: ????..??.????????.?? 1,2,1,1,6,1
	}

	@Test
	public void testDPNastyCase() {
		Solution s = new Solution(Solution.FILE);
		String elementaryRecord = "????..??.????????.??";
		List<Long> elementaryGroups = List.of(1L, 2L, 1L, 1L, 6L, 1L);
		StringBuffer record = new StringBuffer();
		List<Long> groups = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			record.append(elementaryRecord);
			groups.addAll(elementaryGroups);
		}
		s.dp(record.toString(), groups);
	}

}
