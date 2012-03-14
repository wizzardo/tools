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
        if (y1 == y2) {
            a = 0;
        } else {
            a = (x2 - x1) / (y2 - y1);
        }
        b = y1 - a * x1;
    }

    public Line(Point p1, Point p2) {
        this(p1.x, p1.y, p2.x, p2.y);
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

    public double getLength(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
    }

    public Point getPoint(double x, double distance) {
        double newX;
        if (distance > 0) {
            newX = Math.sqrt(distance * distance / (1 + a * a)) + x;
        } else {
            newX = Math.sqrt(distance * distance / (1 + a * a)) - x;
        }
        return new Point(newX, getY(newX));
    }

    public static void main(String[] args) {
        Line l = new Line(0, 0, 1, 2);
        System.out.println(l.getY(4));
        System.out.println(l.getPoint(2, -2.23607));
    }
}
