package org.bordl.utils.math;

import java.util.LinkedList;

class Bezier {

    private LinkedList<Point> points;

    public Bezier(LinkedList<Point> points) {
        this.points = points;
    }

    /**
    @return n!/(i!*(n-i)!)
     */
    private static double factorial(int n, int i) {
        if (i == 0 | i == n) {
            return 1;
        }
        if (n - i > i) {
            double k = 1.0;
            for (int j = n - i + 1; j <= n; j++) {
                k *= j;
                if (i > 1 & k % i == 0) {
                    k = k / i;
                    i--;
                }
            }
            while (i > 1) {
                k = k / i;
                i--;
            }
            return k;
        } else {
            double k = 1.0;
            int g = n - i;
            for (int j = i + 1; j <= n; j++) {
                k *= j;
                if (g > 1 & k % g == 0) {
                    k = k / g;
                    g--;
                }
            }
            while (g > 1) {
                k = k / g;
                g--;
            }
            return k;
        }
    }

    /** 
    @param t [0,1]
     */
    public static Point getPoint(LinkedList<Point> points, float t) {
        Point tmp;
        int n = points.size() - 1;
        double temp = (1.0 - t);
        double x = 0, y = 0;
        for (int i = n; i >= 0; i--) {
            x += Math.pow(temp, i) * points.get(n - i).x * Math.pow(t, n - i) * factorial(n, i);
            y += Math.pow(temp, i) * points.get(n - i).y * Math.pow(t, n - i) * factorial(n, i);
        }
        tmp = new Point((int) x, (int) y);
        return tmp;
    }

    /** 
    @param t [0,1]
     */
    public Point getPoint(float t) {
        return getPoint(points, t);
    }
}
