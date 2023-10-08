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

		int m = 0;
		BandedMatrix h = new BandedMatrix(m + 1, m, 1, 1);
		IColumnVector r = b;
		double beta = r.norm();

		if (beta < tolerance) {
			return DenseVector.zero(n);
		}

		List<IColumnVector> arnoldiBasis = new ArrayList<IColumnVector>();
		IColumnVector va = r; // unnormalized next Arnoldi vector
		IColumnVector vb = va.dividedBy(beta);
		arnoldiBasis.add(vb);
		QRDecompositionTridiagonal qrDecomposition = null;
		IMutableColumnVector q = null;

		while (beta >= tolerance && m <= maxIterations) {
			m++;
			h.resize(m + 1, m);
			IColumnVector vNew = A.times(vb);
			for (int i = 0; i < m; i++) {
				h.set(i, m - 1, arnoldiBasis.get(i).dot(A.times(vb)));
				vNew = vNew.minus(arnoldiBasis.get(i).times(h.get(i, m - 1)));
			}
			h.set(m, m - 1, vNew.norm());
			assert !DoubleMath.zero(h.get(m, m - 1)); // TODO?
			vNew = vNew.dividedBy(h.get(m, m - 1));
			vb = vNew;

			q = new DenseVector(new double[m + 1]);
			q.set(0, 1);
			q.multiplyBy(beta);
			qrDecomposition = QRDecomposerTridiagonal.decompose(h);
			qrDecomposition.applyQ(q, q);
			beta = q.get(m); // TODO: compute the exact residual when this is small
		}

		if (m == maxIterations) {
			System.out.println("Too many iterations!");
		}

		IMutableColumnVector y = new DenseVector(new double[m + 1]);
		qrDecomposition.leastSquares(q, y);

		IMutableColumnVector result = DenseVector.zero(n);

		for (int j = 0; j < m; j++) {
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
