package com.wizzardo.tools.json;

/**
 * Created by wizzardo on 14.09.15.
 */
class NumberParsingContext {
    boolean negative;
    boolean started;
    boolean floatValue;
    boolean done;
    long l;
    long big;
    int fractional;

    void reset() {
        started = false;
        done = false;
        floatValue = false;
        negative = false;
        l = 0;
        big = 0;
        fractional = 0;
    }
}
