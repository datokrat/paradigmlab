package de.paulr.math.numeric;

public class QRDecomposerTridiagonal {

	public static QRDecompositionTridiagonal decompose(IBandedMatrix matrix) {
		assert matrix.isTridiagonal();

		int numRotations = matrix.getBandLength(1);

		BandedMatrix r = new BandedMatrix(matrix.getHeight(), matrix.getWidth(), 2, 0);
		GivensRotation[] rotations = new GivensRotation[numRotations];

		// TODO: parallelize
		for (int l = 0; l < numRotations; l++) {
			double ai = matrix.get(l + 1, l);
			double aj = l == 0 ? matrix.get(l, l) : r.get(l, l);
			GivensRotation rotation = GivensRotation.zeroing(ai, aj, l + 1, l);
			rotations[l] = rotation;
			rotation.apply(ai, aj, r.getColumn(l));

			if (l + 1 >= matrix.getWidth()) {
				continue;
			}

			double bi = matrix.get(l + 1, l + 1);
			double bj = l == 0 ? matrix.get(l, l + 1) : r.get(l, l + 1);
			rotation.apply(bi, bj, r.getColumn(l + 1));

			if (l + 2 >= matrix.getWidth()) {
				continue;
			}

			double ci = matrix.get(l + 1, l + 2);
			double cj = 0; // == matrix.get(l, l + 2)
			rotation.apply(ci, cj, r.getColumn(l + 2));
		}

		return new QRDecompositionTridiagonal(r, rotations);
	}

}
