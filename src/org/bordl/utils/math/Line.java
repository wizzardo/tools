/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.math;

/**
 *
 * @author Moxa
 */
public class Line {

    private double a, b;

    public Line(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public Line(double x1, double y1, double x2, double y2) {
        if (y1 == y1) {
            a = 0;
        } else {
            a = (x2 - x1) / (y2 - y1);
        }
        b = y1 - a * x1;
    }

    public double getY(double x) {
        return a * x + b;
    }

    public double y(double x) {
        return getY(x);
    }

    public double getX(double y) {
        return (y - b) / a;
    }

    public double x(double y) {
        return getX(y);
    }
}
