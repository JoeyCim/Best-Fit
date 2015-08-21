import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;

public class GraphFrame extends JFrame {

    private JLabel xLabel, yLabel, degLabel, result;
    private JPanel xPanel, yPanel, degPanel, inputPanel, dataPanel;
    private JButton button;
    private JTextField xText,yText, degText;
    private Plot plot;
    private double[] xData, yData;
    private Polynomial fitCurve; //least-squares best fit polynomial
    private Matrix lS; //Matrix corresponding to coefficients of 'fitCurve'

    //Minimum distance beteen two points before they are considered the same
    private final double MIN_PRECISION = 0.00001;

    //Radius of every point in the plot
    private final int POINT_RADIUS = 10;

    //Top-left of the grid is 60 pixels right of the left GUI boundary
    private final int RECT_X_START = 60;

    //Top-left of the grid is 60 pixels right of the top GUI boundary
    private final int RECT_Y_START = 60;


    public GraphFrame() {
        initializeComponents();
    }

    public void initializeComponents() {
        setLayout(new BorderLayout());
        xLabel = new JLabel("x values (separated by a space): ");
        yLabel = new JLabel("y values (separated by a space): ");
        degLabel = new JLabel("Enter the degree of the polynomial to fit the data: ");
        result = new JLabel("Best-fit polynomial: ");

        button = new JButton("Graph");
        button.addActionListener(new ButtonListener());

        //Panels/text fields for user input
        xText = new JTextField(20);
        yText = new JTextField(20);
        degText = new JTextField(20);
        xPanel = new JPanel();
        xPanel.setLayout(new BorderLayout());
        yPanel = new JPanel();
        yPanel.setLayout(new BorderLayout());
        degPanel = new JPanel();
        degPanel.setLayout(new BorderLayout());

        xPanel.add(xLabel, BorderLayout.WEST);
        xPanel.add(xText, BorderLayout.CENTER);

        yPanel.add(yLabel, BorderLayout.WEST);
        yPanel.add(yText, BorderLayout.CENTER);

        degPanel.add(degLabel, BorderLayout.WEST);
        degPanel.add(degText, BorderLayout.CENTER);

        //Panel consisting of the three user-input text fields/labels
        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(xPanel, BorderLayout.NORTH);
        inputPanel.add(yPanel, BorderLayout.CENTER);
        inputPanel.add(degPanel, BorderLayout.SOUTH);

        //User input panel plus the "Calculate" button and the best-fit equation text
        dataPanel = new JPanel();
        dataPanel.setLayout(new BorderLayout());
        dataPanel.add(inputPanel, BorderLayout.NORTH);
        dataPanel.add(button, BorderLayout.CENTER);
        dataPanel.add(result, BorderLayout.SOUTH);

        plot = new Plot();
        plot.setBackground(Color.WHITE);
        add(plot, BorderLayout.CENTER);
        add(dataPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        GraphFrame gFrame = new GraphFrame();
        gFrame.setSize(600,500);
        gFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gFrame.setVisible(true);
    }

    private class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            try {

                //Parse user input into arrays, ignoring space
                xData = parseDoubleArray(xText.getText().trim().split(" +"));
                yData = parseDoubleArray(yText.getText().trim().split(" +"));
                int deg = Integer.parseInt(degText.getText());

                if (xData == null || yData == null) {
                    result.setText("Error: Must enter at least two data points");
                }
                else if (xData.length != yData.length) {
                    result.setText("Error: Number of x and y data values must match");
                }
                else if (countUnique(xData) < deg + 1) {
                    result.setText("Error: must have at least " + (deg + 1) + " unique x-data points for a "
                            + deg + "-degree polynomial approximation");	
                }

                //Compute LS-solution
                else {
                    Matrix a = new Matrix(deg + 1, xData);
                    Matrix b = new Matrix(yData);

                    lS = a.solveLeastSquares(b);
                    fitCurve = new Polynomial(lS);
                    result.setText("Best-fit line: " + fitCurve);
                    plot.repaint();
                }
            }
            catch (NumberFormatException e) {
                result.setText("Error: Non-numerical data entered");
            }
        }

        //Takes string containing doubles, returns a double array of those values
        private double[] parseDoubleArray(String[] arr) {
            double[] d = new double[arr.length];

            for (int i = 0; i < d.length; i++) {
                d[i] = Double.parseDouble(arr[i]);
            }
            return d;
        }

        //Returns the number of unique values in a non-empty double array, arr
        private int countUnique(double[] arr) {
            double[] arrCopy = new double[arr.length]; //Soon-to-be sorted copy of input array
            System.arraycopy(arr, 0, arrCopy, 0, arrCopy.length);
            Arrays.sort(arrCopy); 
            int count = 1;
            double curVal = arrCopy[0]; //"Current value" being compared against -- reassigned every time a new value is found

            for (int i = 1; i < arrCopy.length; i++) {
                if (arrCopy[i] != curVal) {
                    count++;
                    curVal = arr[i];
                }
            }

            return count;
        }
    }

    private class Plot extends JPanel {

        private double maxX, minX, maxY, minY;
        private int plotWidth, plotHeight;
        private int rectXEnd, rectYEnd;

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawGrid(g);
            if (xData != null) {
                plotPoints(g);
                labelAxes(g);
                plotBestFit(g);
            }
        }

        //Draw boundaries of graph
        private void drawGrid(Graphics g) {
            plotWidth = plot.getBounds().width;
            plotHeight = plot.getBounds().height;
            rectXEnd = plotWidth - 60;
            rectYEnd = plotHeight - 60;

            g.drawRect(RECT_X_START, RECT_Y_START, rectXEnd - RECT_X_START, rectYEnd - RECT_Y_START);

            //Plot the "ticks" on the vertical and horizontal axes -- add 5 to space out
            for (int i = RECT_Y_START + 5; i <= rectYEnd - 5;  i += (rectYEnd - RECT_Y_START - POINT_RADIUS)/10) {
                g.drawLine(RECT_X_START - 5, i, RECT_X_START + 5, i);
            }
            for (int j = RECT_X_START + 5; j <= rectXEnd - 5;  j += (rectXEnd - RECT_X_START - POINT_RADIUS)/10) {
                g.drawLine(j, rectYEnd - 5, j, rectYEnd + 5);
            }
        }

        private void plotBestFit(Graphics g) {
            int xStart = scaleX(minX);
            int xEnd = scaleX(maxX);

            g.setColor(Color.RED);
            for (int x1 = xStart; x1 <= xEnd; x1+=1) {
                int y1 = scaleY(fitCurve.evaluate(unscaleX(x1)));
                int x2 = x1 + 1;
                int y2 = scaleY(fitCurve.evaluate(unscaleX(x2)));
                g.drawLine(x1 + 5, y1 + 5, x2 + 5, y2 + 5); //Add 5 to make line centered through points
            }
        }

        private void labelAxes(Graphics g) {
            DecimalFormat dF = new DecimalFormat("0.00");

            for (double i = RECT_Y_START + 5; i <= rectYEnd - 5;  i += (rectYEnd - RECT_Y_START - 10)/10) {
                String yStr = dF.format(unscaleY(i - 5));
                g.drawString(yStr, RECT_X_START - 7*(yStr.length()), (int)(i + 5));
            }
            for (double j = RECT_X_START + 5; j <= rectXEnd - 5;  j += (rectXEnd - RECT_X_START - 10)/10) {
                g.drawString(dF.format(unscaleX(j - 5)), (int)(j - 10), rectYEnd + 20);
            }
        }

        //Scale the grid based on the max/min values of the best fit curve and user-entered data
        private void setGridBounds() {
            minX = minY = Double.MAX_VALUE;
            maxX = maxY = Double.MIN_VALUE;

            //Find the smallest x and y values in the user-entered data
            for (int i = 0; i < xData.length; i++) {
                if (xData[i] > maxX) {
                    maxX = xData[i];
                }
                if (yData[i] > maxY) {
                    maxY = yData[i];
                }
                if (xData[i] < minX) {
                    minX = xData[i];
                }
                if (yData[i] < minY) {
                    minY = yData[i];
                }
            }

            /*Check if any of the best fit curve's y values are larger/smaller than the max/min, respectively, 
              in the user-entered data*/
            for (int x1 = scaleX(minX); x1 <= scaleX(maxX); x1+=1) {
                double y1 = fitCurve.evaluate(unscaleX(x1));

                if (y1 < minY) {
                    minY = y1;
                }
                if (y1 > maxY) {
                    maxY = y1;
                }
            }	

            //Spread out grid bounds if max/min happen to be equal
            if (maxY - minY < MIN_PRECISION) {
                minY -= (minY/2.0) + 1;
                maxY += (maxY/2.0) + 1;
            }
            if (maxX - minX < MIN_PRECISION) {
                minX -= (minX/2.0) + 1;
                maxX += (maxX/2.0) + 1;
            }
        }

        //Plot all the points on the graph
        private void plotPoints(Graphics g) {
            setGridBounds();
            for (int i = 0; i < xData.length; i++) {
                int xCoor = scaleX(xData[i]);
                int yCoor = scaleY(yData[i]);
                g.fillOval(xCoor, yCoor, POINT_RADIUS, POINT_RADIUS);
            }
        }    

        //Scale an x value into a pixel location on the graph based on the max/min x-values
        public int scaleX(double val) {
            double prop = (val - minX)/(maxX - minX); //Proportion of val between minX and maxX
            int leftShift = 2*RECT_X_START + POINT_RADIUS; //Ensures "maxX" point aligns with right-wall of grid
            int rightShift = RECT_X_START; //Ensures "minX" point aligned with left-wall of grid

            return (int)(prop*(plotWidth - leftShift) + rightShift);
        }

        //Same as above, but for y-values
        public int scaleY(double val) {
            double prop = ((val - minY)/(maxY - minY));
            int downShift = 2*RECT_X_START  + POINT_RADIUS; //Ensures "maxY" point aligns with top-wall of grid
            int upShift = RECT_Y_START + POINT_RADIUS; //Ensures "minY" point aligns with bottom-wall of grid

            return (int)(plotHeight - prop*(plotHeight - downShift) - upShift);
        }

        //Inverse of "scaleY": take pixel location, return y-value associated with that position
        public double unscaleY(double yCoor) {
            int downShift = 2*RECT_X_START  + POINT_RADIUS; //Ensures "maxY" point aligns with top-wall of grid
            int upShift = RECT_Y_START + POINT_RADIUS;

            return -(yCoor + upShift - plotHeight)*(maxY - minY)/(plotHeight - downShift) + minY;
        }

        //Same as above for an x-value
        public double unscaleX(double xCoor) {
            int leftShift = 2*RECT_X_START + POINT_RADIUS;
            int rightShift = RECT_X_START;

            return (xCoor - rightShift)*(maxX - minX)/(plotWidth - leftShift) + minX;
        }
    } 
}
