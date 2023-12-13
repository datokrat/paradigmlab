package de.paulr.aoc2023.day12;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SolutionTest {

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
		assertEquals(s.dp("#?.?#??#??", List.of(1L, 7L)), 1L);
		assertEquals(s.dp("##.?", List.of(2L, 1L)), 1L);
		assertEquals(s.dp(".#?.?#??#??.?#?#?.?", List.of(1L, 7L, 5L, 1L)), 1L);
		assertEquals(s.dp("??????.#.?????.", List.of(1L, 1L, 1L, 4L)), 10L);
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
