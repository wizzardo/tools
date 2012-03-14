/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils;

/**
 *
 * @author Moxa
 */
public class Chrono {

    private long start;
    private long stop = -1;
    private String mes;
    private boolean nano;

    public Chrono(String mes) {
        this(mes, false);
    }

    public Chrono(String mes, boolean nano) {
        this.mes = mes;
        this.nano = nano;
        if (nano) {
            start = System.nanoTime();
        } else {
            start = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (nano) {
            stop = System.nanoTime();
        } else {
            stop = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        if (stop == -1) {
            stop();
        }
        return getMes() + ": " + (stop - start);
    }

    public long getValue() {
        return stop - start;
    }

    /**
     * @return the mes
     */
    public String getMes() {
        return mes;
    }

    /**
     * @param mes the mes to set
     */
    public void setMes(String mes) {
        this.mes = mes;
    }
}