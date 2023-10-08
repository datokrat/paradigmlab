package de.paulr.math.numeric;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class QRDecomposerTridiagonalTest {

	@Test
	public void test() {
		BandedMatrix m = new BandedMatrix(2, 2, 1, 1);
		m.getRow(0).setAll(2, 1);
		m.getRow(1).setAll(0, 1);

		var decomposition = QRDecomposerTridiagonal.decompose(m);

		assertThat(decomposition.getR(), is(m));

		IMutableColumnVector vector = new DenseVector(1, 2);
		decomposition.leastSquares(vector, vector);

		assertThat(vector, is(new DenseVector(-0.5, 2)));
	}

	@Test
	public void test2() {
		BandedMatrix m = new BandedMatrix(2, 2, 1, 1);
		m.getRow(0).setAll(1, 2);
		m.getRow(1).setAll(1, 1);

		var decomposition = QRDecomposerTridiagonal.decompose(m);

		IMutableColumnVector vector = new DenseVector(1, 2);
		decomposition.leastSquares(vector, vector);

		assertThat(vector, is(new DenseVector(3, -1)));
	}

}
