package de.paulr.math.numeric;

/**
 * r is a (2, 0)-banded matrix
 */
public class QRDecompositionTridiagonal {

	private final IBandedMatrix r;
	// contains the rotations that form the inverse of Q if applied in reverse order
	private final GivensRotation[] givensRotations;

	public QRDecompositionTridiagonal(IBandedMatrix r, GivensRotation[] rotations) {
		this.r = r;
		this.givensRotations = rotations;
	}

	public IBandedMatrix getR() {
		return r;
	}

	public GivensRotation[] getGivensRotations() {
		return givensRotations;
	}

	public IMutableColumnVector leastSquaresR(IColumnVector rightSideVector) {
		return getR().solveByElimination(rightSideVector);
	}

	/**
	 * The vectors are allowed to be equal.
	 */
	public void leastSquares(IColumnVector rightSideVector, IMutableColumnVector target) {
		IMutableColumnVector intermediateResult = applyInverseQ(rightSideVector);
		r.solveByElimination(intermediateResult, target);
	}

	public IMutableColumnVector leastSquares(IColumnVector rightSideVector) {
		IMutableColumnVector result = DenseVector.zero(r.getWidth());
		leastSquares(rightSideVector, result);
		return result;
	}

	public IMutableColumnVector applyInverseQ(IColumnVector source) {
		IMutableColumnVector result = DenseVector.zero(source.getHeight());
		applyInverseQ(source, result);
		return result;
	}

	/**
	 * The vectors are allowed to be equal.
	 */
	public void applyInverseQ(IColumnVector source, IMutableColumnVector target) {
		for (int k = 0; k < givensRotations.length; k++) {
			GivensRotation rotation = givensRotations[k];
			assert rotation.getI() == k + 1;
			assert rotation.getJ() == k;
			double ai = source.get(k + 1);
			double aj = k == 0 ? source.get(k) : target.get(k);
			rotation.apply(ai, aj, target);
		}
	}

	/**
	 * The vectors are allowed to be equal.
	 */
	public void applyQ(IColumnVector source, IMutableColumnVector target) {
		for (int k = givensRotations.length - 1; k >= 0; k--) {
			GivensRotation rotation = givensRotations[k];
			assert rotation.getI() == k + 1;
			assert rotation.getJ() == k;
			double ai = k == givensRotations.length - 1 ? source.get(k + 1) : target.get(k + 1);
			double aj = source.get(k);
			rotation.applyInversely(ai, aj, target);
		}
	}

}
