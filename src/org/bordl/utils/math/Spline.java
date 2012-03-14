/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.math;

import java.util.List;

/**
 *
 * @author moxa
 */
public class Spline {

    private ThirdDegreePolynomial[] polynomials; // Сплайн
    private double[] x;
    private double[] y;
    private double lastX;
    private int lastSpline = -1;

    public double getY(double x) {
        int i = 0;
        if (lastSpline != -1 && x > lastX) {
            i = lastSpline;
        }
        while (i < polynomials.length - 1 && x > polynomials[i + 1].x) {
            i++;
        }
        lastSpline = i;
        lastX = x;
        ThirdDegreePolynomial sd = polynomials[i];
        double h = (x - sd.x);
        return sd.a + sd.b * h + sd.c * h * h + sd.d * h * h * h;
    }

    public double getX(double y) {
        int i = 0;
        while (i < this.y.length - 1 && y > this.y[i + 1]) {
            i++;
        }
        double h = (this.x[i + 1] - this.x[i]);
        double d = h / 1000;
        double x1 = this.x[i], x2 = (this.x[i + 1] + this.x[i]) / 2, x3 = this.x[i + 1];
        double y1, y3;
        while (h > d) {
            y1 = getY(x1);
            y3 = getY(x3);
            if (Math.abs(y - y1) > Math.abs(y - y3)) {
                x1 = x2;
            } else {
                x3 = x2;
            }
            x2 = (x1 + x3) / 2;
            h = x3 - x1;
        }
        return x2;
    }

    public static double[] toArray(List<Double> l) {
        double[] d = new double[l.size()];
        for (int i = 0; i < l.size(); i++) {
            d[i] = l.get(i);
        }
        return d;
    }

    public double getMaxX() {
        return x[x.length - 1];
    }

    public double getMinX() {
        return x[0];
    }

    public double getMaxY() {
        return getMax(y);
    }

    public double getMinY() {
        return getMin(y);
    }

    public int getPointsCount() {
        return x.length;
    }

    public Point getPoint(int i) {
        return new Point(x[i], y[i]);
    }

    public static double[][] toArrays(List<Point> points) {
        double[][] arrays = new double[2][];
        arrays[0] = new double[points.size()];
        arrays[1] = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            arrays[0][i] = points.get(i).x;
            arrays[1][i] = points.get(i).y;
        }
        return arrays;
    }

    public Spline(List<Point> points) {
        double[][] temp = toArrays(points);
        this.x = temp[0];
        this.y = temp[1];
        int n = points.size();
        polynomials = new ThirdDegreePolynomial[n];
        for (int i = 0; i < n; ++i) {
            polynomials[i] = new ThirdDegreePolynomial();
            polynomials[i].x = x[i];
            polynomials[i].a = y[i];
        }
        double[] h = new double[n];
        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
        }
        double[] alpha = new double[n];
        for (int i = 1; i < n - 1; i++) {
            alpha[i] = 3 / h[i] * (polynomials[i + 1].a - polynomials[i].a) - 3 / h[i - 1] * (polynomials[i].a - polynomials[i - 1].a);
        }

        double[] l = new double[n];
        double[] m = new double[n];
        double[] z = new double[n];
        l[0] = 1;
        for (int i = 1; i < n - 1; i++) {
            l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * m[i - 1];
            m[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }
        l[n - 1] = 1;
        for (int i = n - 2; i >= 0; i--) {
            polynomials[i].c = z[i] - m[i] * polynomials[i + 1].c;
            polynomials[i].b = (polynomials[i + 1].a - polynomials[i].a) / h[i] - (polynomials[i + 1].c + 2 * polynomials[i].c) * h[i] / 3;
            polynomials[i].d = (polynomials[i + 1].c - polynomials[i].c) / (3 * h[i]);
        }
    }

    public static double getMin(double[] d) {
        double min = d[0];
        for (int i = 1; i < d.length; i++) {
            if (d[i] < min) {
                min = d[i];
            }
        }
        return min;
    }

    public static double getMax(double[] d) {
        double max = d[0];
        for (int i = 1; i < d.length; i++) {
            if (d[i] > max) {
                max = d[i];
            }
        }
        return max;
    }

    private class ThirdDegreePolynomial {

        public double a, b, c, d, x;

        @Override
        public String toString() {
            return a + "\t" + b + "\t" + c + "\t" + d + "\t" + x;
        }
    }
}
