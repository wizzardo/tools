/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools;

/**
 * @author Moxa
 */
public class Stopwatch {

    private long start;
    private long stop = -1;
    private String message;
    private boolean nano;

    public Stopwatch(String message) {
        this(message, false);
    }

    public Stopwatch(String message, boolean nano) {
        this.message = message;
        this.nano = nano;
        restart();
    }

    public void stop() {
        if (nano) {
            stop = System.nanoTime();
        } else {
            stop = System.currentTimeMillis();
        }
    }

    public void restart() {
        stop = -1;
        if (nano) {
            start = System.nanoTime();
        } else {
            start = System.currentTimeMillis();
        }
    }

    public void restart(String message) {
        this.message = message;
        restart();
    }

    @Override
    public String toString() {
        if (stop == -1) {
            stop();
        }
        return message + ": " + (stop - start) + (nano ? "ns" : "ms");
    }

    public long getValue() {
        return stop - start;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public long getDuration() {
        if (stop == -1) {
            stop();
        }
        return stop - start;
    }

    public boolean isNano() {
        return nano;
    }
}