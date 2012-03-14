/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.math;

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
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return (x == p.x) && (y == p.y);
        }
        return super.equals(obj);
    }
}
