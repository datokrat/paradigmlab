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
		s.parseInput();
		MatrixInversionTableau tableau = s.createTableau();
		long denom = tableau.left.getAsLong(0, 0);
		assertThat(s.invertWithTableau(tableau, new Pos4d(-2, 1, -2, 1)), is(new Pos4d(denom, 0, 0, 0)));
		assertThat(s.invertWithTableau(tableau, new Pos4d(-1, -1, -2, 1)).space(), is(new Pos3d(0, denom, 0)));
		assertThat(s.invertWithTableau(tableau, new Pos4d(1, -6, 8, 0)).space(), is(new Pos3d(0, 0, denom)));
	}

}
