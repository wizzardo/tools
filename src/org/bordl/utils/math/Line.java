/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.math;

import java.util.Arrays;

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

    public static double getLength(Point p1, Point p2) {
        return Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
    }

    public static double getLength(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double getLength(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double getLength(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static boolean isBigger(Point p1, Point p2, double distance) {
        return (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y) > distance * distance;
    }

    public static boolean isBigger(double x1, double y1, double x2, double y2, double distance) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) > distance * distance;
    }

    public static boolean isBigger(float x1, float y1, float x2, float y2, float distance) {
        return ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) > distance * distance;
    }

    public static boolean isBigger(int x1, int y1, int x2, int y2, int distance) {
        return ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) > distance * distance;
    }

    public static Line approximate(Point[] data) {
        return approximate(data, 0, data.length);
    }

    public static Line approximate(Point[] data, int offset, int length) {
        double sx = 0;
        double sy = 0;
        double sxx = 0;
        double sxy = 0;
        int n = length;
        for (int i = offset; i < offset + length && i < data.length; i++) {
            sx += data[i].x;
            sy += data[i].y;
            sxx += data[i].x * data[i].x;
            sxy += data[i].x * data[i].y;
        }
        double d = (n * sxx - sx * sx);
        if (d == 0) {
            throw new IllegalArgumentException("The equation can't be solved, check the data: " + Arrays.toString(data));
        }
        double a = (n * sxy - sx * sy) / d;
        double b = (sy * sxx - sxy * sx) / d;
        return new Line(a, b);
    }
//    public static void main(String[] args) {
////        Line l = new Line(-2, -1, 2, 1);
////        System.out.println(l);
////        System.out.println(l.getLength(new Point(0, 0), new Point(2, 1)));
////        System.out.println(l.getY(4));
////        System.out.println(l.getPoint(0, -l.getLength(new Point(0, 0), new Point(2, 1))));
////        System.out.println(l.getPoint(0, l.getLength(new Point(0, 0), new Point(2, 1))));
////        System.out.println(l);
////        System.out.println(l.getNormal(2));
////        System.out.println(l.getNormal(2).getPoint(2, 4.472135954999579));
////        System.out.println(l.getNormal(2).getPoint(2, -4.472135954999579));
//        Line l = approximate(new Point[]{new Point(0, 1), new Point(1, 0), new Point(2, 1)});
//        System.out.println(l.getY(0));
//        System.out.println(l.getY(1));
//        System.out.println(l.getY(2));
//        System.out.println(l);
//    }

    public static class F {

        private float a, b;

        public F(float a, float b) {
            this.a = a;
            this.b = b;
        }

        public F(float x1, float y1, float x2, float y2) {
            if (y1 == y2) {
                a = 0;
            } else {
                a = (y2 - y1) / (x2 - x1);
            }
            b = y1 - a * x1;
        }

        public F(Point.F p1, Point.F p2) {
            this(p1.x, p1.y, p2.x, p2.y);
        }

        public float getY(float x) {
            return a * x + b;
        }

        public float y(float x) {
            return getY(x);
        }

        public float getX(float y) {
            return (y - b) / a;
        }

        public float x(float y) {
            return getX(y);
        }

        public Point.F getPoint(float x, float distance) {
            float newX;
            if (distance > 0) {
                newX = (float) (x + Math.sqrt(distance * distance / (1 + a * a)));
            } else {
                newX = (float) (x - Math.sqrt(distance * distance / (1 + a * a)));
            }
            return new Point.F(newX, getY(newX));
        }

        public Point.F getPoint(Point.F p, float distance) {
            if (a == Float.NEGATIVE_INFINITY || a == Float.POSITIVE_INFINITY) {
                return new Point.F(p.x, p.y + distance);
            } else {
                return getPoint(p.x, distance);
            }
        }

        public F getNormal(float x) {
            return new F(-1 / a, getY(x) + x * 1 / a);
        }

        @Override
        public String toString() {
            return a + " * X + " + b;
        }
    }
}
