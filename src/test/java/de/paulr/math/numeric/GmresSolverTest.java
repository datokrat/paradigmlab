package de.paulr.math.numeric;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.github.sh0nk.matplotlib4j.PythonExecutionException;

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
		IColumnVector solution = GmresSolver.solveWithSymmetricMatrix(matrix, b, guess, 10, 1e-6)
			.result();

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
		IColumnVector solution = GmresSolver.solveWithSymmetricMatrix(matrix, b, 10, 1e-6).result();

		assertThat(matrix.times(expectedSolution), is(b));
		assertThat(solution, is(expectedSolution));
	}

	@Test
	public void propertyBasedTests() {
		Random random = new Random(42);
		int n = 20;
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				matrix.set(i, j, random.nextDouble());
				matrix.set(j, i, matrix.get(i, j));
			}
			b.set(i, random.nextDouble());
		}

		int maxIterations = n + 1;
		IColumnVector solution = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, maxIterations, maxIterations, 1e-6)
			.result();
		assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));
	}

	@Test
	public void propertyBasedTests2() {
		Random random = new Random(42);
		int n = 1000;
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				matrix.set(i, j, random.nextDouble());
				matrix.set(j, i, matrix.get(i, j));
			}
			b.set(i, random.nextDouble());
		}

		IColumnVector solution = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, n, 100 * n, 1e-8).result();
		assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));
	}

	@Test
	public void positiveDefiniteTridiagonal() {
		Random random = new Random(42);
		int n = 5000;
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		IMutableColumnVector guess = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			if (i + 1 < n) {
				matrix.set(i, i + 1, 1);
				matrix.set(i + 1, i, matrix.get(i, i + 1));
			}
			matrix.set(i, i, 4);
			b.set(i, random.nextDouble());
			guess.set(i, random.nextDouble());
		}

		System.out.println("Start");

		IColumnVector solution = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, n, 100 * n, 1e-8).result();
		IColumnVector solution2 = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, guess, n, 100 * n, 1e-8).result();
		assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));
		assertThat(solution.supDistanceTo(solution2), is(lessThan(1e-6)));
	}

	/**
	 * This is quite fast with a seemingly constant number of iterations...
	 */
	@Test
	public void diagonalDominantSymmetric() {
		for (int seed = 0; seed < 43; seed++) {
			Random random = new Random(seed);
			int n = 1000;
			IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
			IMutableColumnVector b = DenseVector.zero(n);
			IMutableColumnVector guess = DenseVector.zero(n);
			for (int i = 0; i < n; i++) {
				double absSum = 0;
				for (int j = 0; j < i; j++) {
					absSum += Math.abs(matrix.get(i, j));
				}
				for (int j = i + 1; j < n; j++) {
					matrix.set(i, j, random.nextDouble());
					matrix.set(j, i, matrix.get(i, j));
					absSum += Math.abs(matrix.get(i, j));
				}
				matrix.set(i, i, absSum);
				b.set(i, random.nextDouble());
				guess.set(i, random.nextDouble());
			}

			System.out.println("Start");

			IColumnVector solution = GmresSolver
				.solveWithSymmetricMatrixAndRestarts(matrix, b, n, 100 * n, 1e-8).result();
			IColumnVector solution2 = GmresSolver
				.solveWithSymmetricMatrixAndRestarts(matrix, b, guess, n, 100 * n, 1e-8).result();
			assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));
			assertThat(solution.supDistanceTo(solution2), is(lessThan(1e-6)));
		}
	}

	/**
	 * This is quite slow, even ignoring the expensive matrix multiplication and
	 * even though the symmetrized matrix is positive semidefinite (and positve
	 * definite with very high probability)
	 */
	@Test
	public void positiveDefiniteATATrick() {
		for (int seed = 42; seed < 43; seed++) {
			Random random = new Random(seed);
			int n = 1000;
			BandedMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
			IMutableColumnVector b = DenseVector.zero(n);
			IMutableColumnVector guess = DenseVector.zero(n);
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					matrix.set(i, j, random.nextDouble());
					matrix.set(j, i, random.nextDouble());
				}
				b.set(i, random.nextDouble());
				guess.set(i, random.nextDouble());
			}

			IRandomAccessMatrix squareSymmetrizedMatrix = matrix.transpose().times(matrix);

			System.out.println("Start");

			IColumnVector solution = GmresSolver
				.solveWithSymmetricMatrixAndRestarts(squareSymmetrizedMatrix, b, n, 100 * n, 1e-8)
				.result();
			IColumnVector solution2 = GmresSolver.solveWithSymmetricMatrixAndRestarts(
				squareSymmetrizedMatrix, b, guess, n, 100 * n, 1e-8).result();
			assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));
			assertThat(solution.supDistanceTo(solution2), is(lessThan(1e-6)));
		}
	}

	@Test
	public void dirichletParable() throws IOException, PythonExecutionException {
		int n = 100;
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			if (i + 1 < n) {
				matrix.set(i, i + 1, -1);
				matrix.set(i + 1, i, matrix.get(i, i + 1));
			}
			matrix.set(i, i, 2);
			b.set(i, 0.1D);
		}

		IColumnVector solution = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, n, 100 * n, 1e-8).result();

		assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));

//		Plot plt = Plot.create();
//		plt.plot() //
//			.add(solution.toList()) //
//			.label("Label") //
//			.linestyle("--");
//		plt.xlabel("x");
//		plt.ylabel("y");
//		plt.title("title");
//		plt.legend();
//		plt.show();
	}

	/**
	 * f'' = -frequency^2 * f
	 */
	@Test
	public void wave() throws IOException, PythonExecutionException {
		int n = 100;
		double frequency = 0.1; // Wave length is 10 * PI
		IMutableMatrix matrix = new BandedMatrix(n, n, n - 1, n - 1);
		IMutableColumnVector b = DenseVector.zero(n);
		for (int i = 0; i < n; i++) {
			if (i + 1 < n) {
				matrix.set(i, i + 1, -1);
				matrix.set(i + 1, i, matrix.get(i, i + 1));
			}
			matrix.set(i, i, 2 - frequency * frequency);
			b.set(i, 0);
		}

		b.set(0, 2);

		IColumnVector solution = GmresSolver
			.solveWithSymmetricMatrixAndRestarts(matrix, b, n, 100 * n, 1e-8).result();

		assertThat(matrix.times(solution).supDistanceTo(b), is(lessThan(1e-6)));

//		Plot plt = Plot.create();
//		plt.plot() //
//			.add(solution.toList()) //
//			.label("Label") //
//			.linestyle("--");
//		plt.xlabel("x");
//		plt.ylabel("y");
//		plt.title("title");
//		plt.legend();
//		plt.show();
	}

}
