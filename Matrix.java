public class Matrix {

    private double[][] entries;
    private int numRow;
    private int numCol;

    public Matrix(double[][] entries) {
        numRow = entries.length;
        numCol = entries[0].length;
        this.entries = new double[numRow][numCol];
        setEntries(entries);
    }

    public Matrix(int numRow, int numCol) {
        this.numRow = numRow;
        this.numCol = numCol;
        entries = new double[numRow][numCol];
    }

    /*Creates a matrix with entries of the form
      |1 entries[0] ...                  entries[0]^(entries.length - 1)					|
      |1 entries[1] ...				    entries[1]^(entries.length - 1)					|
      |...																				|
      |1 entries[entries.length - 1] ... entries[entries.length - 1]^(entries.length - 1)|
     */
    public Matrix(int numCol, double[] entries) {
        numRow = entries.length;
        this.numCol = numCol;
        this.entries = new double[numRow][numCol];
        for (int i = 0; i < numRow; i++) {
            for (int j = 0; j < numCol; j++) {
                this.entries[i][j] = Math.pow(entries[i], j);
            }
        }
    }

    /*Creates a matrix with entries of the form
      |entries[0]				  |
      |entries[1]				  |
      |...						  |
      |entries[entries.length - 1]|
     */
    public Matrix(double entries[]) {
        numRow = entries.length;
        numCol = 1;
        this.entries = new double[numRow][numCol];
        for (int i = 0; i < numRow; i++) {
            this.entries[i][0] = entries[i];
        }
    }

    public Matrix(Matrix m) {
        numRow = m.numRow;
        numCol = m.numCol;
        entries = new double[m.numRow][m.numCol];
        setEntries(m.entries);
    }


    //Create an identity matrix with length/width equal to numRow
    public Matrix(int numRow) {
        this.numRow = numRow;
        this.numCol = numRow;
        this.entries = new double[numRow][numRow];
        for (int i = 0; i < numRow; i++) {
            entries[i][i] = 1;
        }
    }

    public int getNumRow() { 
        return numRow;
    }

    public int getNumCol() {
        return numCol;
    }

    public double[][] getEntries() {
        return entries;
    }

    public void setEntry(int row, int col, double val) {
        entries[row][col] = val;
    }

    public void setEntries(double[][] entries) {
        for (int i= 0; i < entries.length; i++) {
            for (int j = 0; j < entries[0].length; j++) {
                this.entries[i][j] = entries[i][j];
            }
        }
    }

    //Multiply two matrices, return the product
    public Matrix multiply(Matrix b) {
        Matrix product = new Matrix(numRow,b.numCol);

        for (int i= 0; i < product.numRow; i++) {
            for (int j = 0; j < product.numCol; j++) {
                for (int k = 0; k < numCol; k++) {
                    product.entries[i][j] += entries[i][k] * b.entries[k][j];
                }
            }
        }
        return product;
    }

    public void swapRow(int rowA, int rowB) {
        double[] temp = new double[entries[rowA].length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = entries[rowA][i];
            entries[rowA][i] = entries[rowB][i];
            entries[rowB][i] = temp[i];
        }
    }

    //Returns the transpose of a maitrx: ith row becomes ith column, jth column becomes jth row
    public Matrix transpose() {
        Matrix t = new Matrix(numCol, numRow);
        for (int i = 0; i < numCol; i++) {
            for (int j = 0; j < numRow; j++) {
                t.entries[i][j] = entries[j][i];	
            }
        }
        return t;
    }

    public void printEntries() {
        for (int i = 0; i < numRow; i++) {
            for (int j = 0; j < numCol; j++) {
                System.out.print(entries[i][j] + " ");
            }
            System.out.println();
        }
    }

    /*Takes inverse of a Matrix. First finds PLU facotrization: P*A = L*U,
      then obtains AInv = UInv*LInv*P */
    public Matrix invert() {
        PLUFactorization fac = new PLUFactorization(new Matrix(this));
        Matrix u = fac.getU();
        Matrix uInv = new Matrix(u.numRow);
        Matrix l = fac.getL();
        Matrix lInv = new Matrix(l.numRow);
        Matrix p = fac.getP();

        for (int j = u.numRow - 1; j >= 0; j--) {

            //Scale row so entry on diagonal equals 1
            for (int k = j; k < u.numRow; k++) {
                uInv.entries[j][k] /= u.entries[j][j];
            }

            //Gaussian-elimination: perform the row-subtractions on UInv that reduce U to the identity matrix
            for (int k = j; k < u.numRow; k++) {
                for (int i = j - 1; i >= 0; i--) {
                    uInv.entries[i][k] -= u.entries[i][j]*uInv.entries[j][k];
                }
            }
        }

        //Repeat for lower-triangular matrix: no scaling needed though
        for (int j = 0; j < l.numRow - 1; j++) {
            for (int k = j; k >= 0; k--) {
                for (int i = j + 1; i < l.numRow; i++) {
                    lInv.entries[i][k] -= l.entries[i][j]*lInv.entries[j][k];
                }
            }
        }
        return uInv.multiply(lInv.multiply(p));	
    }

    /*a.solveLeastSquares(b) finds the least-squares solution to a * x = b,
      namely, ((a-transpose * a)^-1) * a-transpose * b */
    public Matrix solveLeastSquares(Matrix b) {
        Matrix innerProd = transpose().multiply(this);
        Matrix innerProdInv = innerProd.invert();
        Matrix transProd = innerProdInv.multiply(transpose());

        return transProd.multiply(b);
    }
}
