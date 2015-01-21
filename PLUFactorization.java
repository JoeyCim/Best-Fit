
public class PLUFactorization {
	
	private Matrix p; //Pivot matrix
	private Matrix l; //Unit Lower matrix
	private Matrix u; //Upper matrix
	
	public PLUFactorization(Matrix m) {
		u = new Matrix(m);
		p = new Matrix(u.getNumRow());
		l = new Matrix(u.getNumRow()); 
		decompose(u);
	}
	
	public Matrix getP() {
		return p;
	}
	
	public Matrix getL() { 
		return l;
	}
	
	public Matrix getU() { 
		return u;
	}
	
	public void decompose(Matrix m) {
		for (int i = 0; i < m.getNumCol(); i++) {
			//Swap rows so that the largest value in the i'th column is contained in the i'th row
			double maxValue = m.getEntries()[i][i];
			int maxRow = i;
			for (int k = i + 1; k < m.getNumRow(); k++) {
				if (Math.abs(m.getEntries()[k][i]) > Math.abs(maxValue)) {
					maxValue = m.getEntries()[k][i];
					maxRow = k;
				}
			}
			m.swapRow(maxRow, i);
			p.swapRow(maxRow, i);
			
			//Reduce to upper-triangular form (but retain original lower coefficients for now)
			for (int k = i + 1; k < m.getNumRow(); k++) {
				double multiplier = m.getEntries()[k][i]/m.getEntries()[i][i];
				m.setEntry(k, i, multiplier);
				for (int j = i + 1; j < m.getNumRow(); j++) {
					m.setEntry(k, j, m.getEntries()[k][j] - multiplier*m.getEntries()[i][j]);
				}
			}
		}
		
		/*Remove lower coefficients from m and add them to l to make m upper-triangular.
		 Do this after the previous step to allow necessary pivoting to take place first */
		for (int i = 0; i < m.getNumCol(); i++) {
			for (int j = i + 1; j < m.getNumCol(); j++) {
				l.setEntry(j,i,m.getEntries()[j][i]);
				m.setEntry(j,i,0);
			}
		}
	}
}
