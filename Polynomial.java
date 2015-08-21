import java.text.DecimalFormat;

public class Polynomial {

    double[] coefficients;

    public Polynomial(Matrix coeffMat) {
        coefficients = new double[coeffMat.getEntries().length];
        for (int i = 0; i < coefficients.length; i++) {
            coefficients[i] = coeffMat.getEntries()[i][0];
        }
    }

    //Returns the output for an input 'x'
    public double evaluate(double x) {
        double total = 0;

        for (int i = 0; i < coefficients.length; i++) {
            total += coefficients[i]*Math.pow(x, i);
        }
        return total;
    }

    public String toString() {
        DecimalFormat dF = new DecimalFormat("#.####");
        String pString = new String();

        for (int i= coefficients.length - 1; i > 0; i--) {
            pString += dF.format(coefficients[i]) + "x^" + i + " + ";
        }
        pString += dF.format(coefficients[0]) + "";
        return pString;
    }
}
