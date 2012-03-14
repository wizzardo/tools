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
            a = (y2 - y1) / (x2 - x1);
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

    public static double getLength(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
    }

    public Point getPoint(double x, double distance) {
        double newX;
        if (distance > 0) {
            newX = x + Math.sqrt(distance * distance / (1 + a * a));
        } else {
            newX = x - Math.sqrt(distance * distance / (1 + a * a));
        }
        return new Point(newX, getY(newX));
    }

    public Point getPoint(Point p, double distance) {
        if (a == Double.NEGATIVE_INFINITY || a == Double.POSITIVE_INFINITY) {
            return new Point(p.x, p.y + distance);
        } else {
            return getPoint(p.x, distance);
        }
    }

    public Line getNormal(double x) {
        return new Line(-1 / a, getY(x) + x * 1 / a);
    }

    @Override
    public String toString() {
        return a + " * X + " + b;
    }
//    public static void main(String[] args) {
//        Line l = new Line(-2, -1, 2, 1);
//        System.out.println(l);
//        System.out.println(l.getLength(new Point(0, 0), new Point(2, 1)));
//        System.out.println(l.getY(4));
//        System.out.println(l.getPoint(0, -l.getLength(new Point(0, 0), new Point(2, 1))));
//        System.out.println(l.getPoint(0, l.getLength(new Point(0, 0), new Point(2, 1))));
//        System.out.println(l);
//        System.out.println(l.getNormal(2));
//        System.out.println(l.getNormal(2).getPoint(2, 4.472135954999579));
//        System.out.println(l.getNormal(2).getPoint(2, -4.472135954999579));
//    }
}
