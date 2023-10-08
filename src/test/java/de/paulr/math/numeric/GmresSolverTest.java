package de.paulr.math.numeric;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;

import org.junit.Test;

public class GmresSolverTest {

	@Test
	public void solveWithIdentityMatrix() {
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

	@Test
	public void solveSymmetrically() {
		IMutableMatrix matrix = new BandedMatrix(4, 4, 3, 3);
		matrix.getRow(0).setAll(1, 2, 0, 0);
		matrix.getRow(1).setAll(2, 1, 0, 0);
		matrix.getRow(2).setAll(0, 0, 1, 0);
		matrix.getRow(3).setAll(0, 0, 0, 1);
		IMutableColumnVector b = new DenseVector(1, 2, 3, 4);
		IMutableColumnVector expectedSolution = new DenseVector(1, 0, 3, 4);
		IColumnVector solution = GmresSolver.solveWithSymmetricMatrix(matrix, b, 10, 1e-6);

		assertThat(matrix.times(expectedSolution), is(b));
		assertThat(solution, is(expectedSolution));
	}

	@Test
	public void propertyBasedTests() {
		Random random = new Random(42);
		int n = 15;
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				matrix.set(i, j, random.nextDouble());
				matrix.set(j, i, matrix.get(i, j));
			}
			b.set(i, random.nextDouble());
		}

		IColumnVector solution = GmresSolver.solveWithSymmetricMatrix(matrix, b, n + 1, 10e-6);
		assertThat(matrix.times(solution), is(b));
	}

}
