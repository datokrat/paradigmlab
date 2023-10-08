package de.paulr.math.numeric;

import java.util.ArrayList;
import java.util.List;

public class GmresSolver {

	private GmresSolver() {
	}

	public static IColumnVector solveWithSymmetricMatrix(IMatrix A, IColumnVector b,
		IColumnVector guess, int maxIterations, double tolerance) {
		IColumnVector translation = solveWithSymmetricMatrix(A, b.minus(A.times(guess)),
			maxIterations, tolerance);
		return guess.plus(translation);
	}

	/**
	 * Solve with initial guess being zero
	 */
	public static IColumnVector solveWithSymmetricMatrix(IMatrix A, IColumnVector b,
		int maxIterations, double tolerance) {
		int n = b.getHeight();
		assert A.getHeight() == n;
		assert tolerance >= DoubleMath.EPSILON;
		assert maxIterations > 0;

		BandedMatrix h = new BandedMatrix(0, 0, 1, 1);
		double beta = b.norm();
		double error = beta; // error when 0 is taken as the solution

		if (error < tolerance) {
			return DenseVector.zero(n);
		}

		List<IColumnVector> arnoldiBasis = new ArrayList<IColumnVector>();

		// prepare first Arnoldi vector
		IColumnVector vb = b.dividedBy(error);
		arnoldiBasis.add(vb);

		QRDecompositionTridiagonal qrDecomposition = null;
		IMutableColumnVector q = null;

		System.out.println("Error before first iteration: " + error);

		int m = 1;
		while (error >= tolerance && m <= maxIterations) {
			// The last Arnoldi vector was computed by dividing by h[m - 1, m - 2]
			assert m == 1 || !DoubleMath.zero(h.get(m - 1, m - 2));

			// Arnoldi
			assert m == arnoldiBasis.size();
			h.resize(m + 1, m);
			vb = A.times(vb);
			if (m >= 2) {
				h.set(m - 2, m - 1, h.get(m - 1, m - 2));
				vb = vb.minus(arnoldiBasis.get(m - 2).times(h.get(m - 2, m - 1)));
			}
			h.set(m - 1, m - 1, arnoldiBasis.get(m - 1).dot(vb));
			vb = vb.minus(arnoldiBasis.get(m - 1).times(h.get(m - 1, m - 1)));
			double vbNorm = vb.norm();
			h.set(m, m - 1, vbNorm);
			vb = vb.dividedBy(vbNorm); // We might divide by zero, but we will not use the new
										// Arnoldi vector since this is the last loop iteration
			arnoldiBasis.add(vb);

			// Determine the goodness of the estimate in the m-dimensional Krylow space
			// Note that the freshly generated basis vector is not part of this space yet!
			q = DenseVector.zero(m + 1);
			q.set(0, 1);
			q.multiplyBy(beta);
			qrDecomposition = QRDecomposerTridiagonal.decompose(h);
			qrDecomposition.applyInverseQ(q, q); // TODO: do it inversely?
			error = Math.abs(q.get(m)); // TODO: compute the exact residual when this is small

			System.out.println("Error after iteration " + m + ": " + error);

			m++;
		}

		if (m == maxIterations + 1) {
			System.out.println("Too many iterations!");
		}

		IMutableColumnVector y = qrDecomposition.leastSquaresR(q);

		IMutableColumnVector result = DenseVector.zero(n);
		assert y.getHeight() == m - 1;
		for (int j = 0; j < m - 1; j++) {
			double yj = y.get(j);
			for (int i = 0; i < A.getHeight(); i++) {
				result.set(i, result.get(i) + yj * arnoldiBasis.get(j).get(i));
			}
		}

		System.out.println("Iterations: " + m);
		System.out.println("H:" + h);
		System.out.println("Arnoldi basis: " + arnoldiBasis);

		return result;
	}

}
