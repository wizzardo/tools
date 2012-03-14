/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.math;

import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author moxa
 */
public class Lagrange {

    LinkedList<Point> points;
    double[] k;

    public Lagrange(LinkedList<Point> points) {
        this.points = points;
        k = new double[points.size()];
    }

    public Point getPoint(double x) {
        return new Point((int) x, (int) getY(x));
    }

    public void getK() {
        for (int i = 0; i < points.size(); i++) {
            double t = 1;
            for (int j = 0; j < points.size(); j++) {
                if (j != i) {
                    if (points.get(i).x - points.get(j).x != 0) {
                        t *= (points.get(i).x - points.get(j).x);
                    } else {
                        break;
                    }
                }
            }
            if (t != 0) {
                for (int z = 1; z < k.length; z++) {
                    double temp = 0;
                    for (int p = 0; p < z; p++) {//p - порядок (сколько раз перемножать коэффициенты)
                        double t2 = 0;
                        for (int j = 0; j < points.size(); j++) {
                        }
                        temp += t2;
                    }
                }
            }
        }
    }

    public double getY(double x) {
        double y = 0, temp;
        for (int i = 0; i < points.size(); i++) {
            temp = points.get(i).y;
            for (int j = 0; j < points.size(); j++) {
                if (j != i) {
                    if (points.get(i).x - points.get(j).x != 0) {
                        temp *= (x - points.get(j).x) / (points.get(i).x - points.get(j).x);
                    } else {
                        temp = 0;
                        break;
                    }
                }
            }
            y += temp;
        }
        return y;
    }
}
