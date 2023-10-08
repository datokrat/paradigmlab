package de.paulr.math.numeric;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GmresSolverTest {

	@Test
	public void test() {
		IMutableMatrix matrix = new BandedMatrix(4, 4, 3, 3);
		matrix.getRow(0).setAll(1, 0, 0, 0);
		matrix.getRow(1).setAll(0, 1, 0, 0);
		matrix.getRow(2).setAll(0, 0, 1, 0);
		matrix.getRow(3).setAll(0, 0, 0, 1);
		IMutableColumnVector b = new DenseVector(1, 2, 3, 4);
		IMutableColumnVector guess = new DenseVector(0, 0, 0, 0);
		IColumnVector solution = GmresSolver.solveWithSymmetricMatrix(matrix, b, guess, 10, 1e-6);

		assertThat(solution, is(b));
	}

}
