/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.math;

/**
 *
 * @author moxa
 */
public class Point {

    final public double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return (x == p.x) && (y == p.y);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return x + ";" + y;
    }

    public static class F {

        final public float x, y;

        public F(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof F) {
                F p = (F) obj;
                return (x == p.x) && (y == p.y);
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return x + ";" + y;
        }
    }

    public static class I {

        final public int x, y;

        public I(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof I) {
                I p = (I) obj;
                return (x == p.x) && (y == p.y);
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return x + ";" + y;
        }
    }
}
