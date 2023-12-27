package de.paulr.aoc2023.day24;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.paulr.aoc2023.day24.Solution.Pos3d;
import de.paulr.aoc2023.day24.Solution.Pos4d;

public class SolutionTest {

	@Test
	public void test() {
		var s = new Solution();
		assertThat(s.invertForSample(new Pos4d(-2, 1, -2, 1)), is(new Pos3d(8, 0, 0)));
		assertThat(s.invertForSample(new Pos4d(-1, -1, -2, 1)), is(new Pos3d(0, 8, 0)));
		assertThat(s.invertForSample(new Pos4d(1, -6, 8, 0)), is(new Pos3d(0, 0, 8)));
	}

}
