package com.wizzardo.tools.json;

/**
 * Created by wizzardo on 14.09.15.
 */
class StringParsingContext {
    boolean started;
    boolean done;
    boolean needDecoding;
    boolean escape;
    byte quote;
    byte[] buffer = new byte[16];
    int length;

    void put(byte[] bytes, int from, int to) {
        int length = to - from;
        try {
            System.arraycopy(bytes, from, buffer, this.length, length);
            this.length += length;
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + this.length);
            put(bytes, from, length);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + this.length);
            put(bytes, from, length);
        }
    }

    private void ensureCapacity(int length) {
        byte[] temp = new byte[length * 2];
        System.arraycopy(buffer, 0, temp, 0, this.length);
        buffer = temp;
    }

    void reset() {
        started = false;
        done = false;
        escape = false;
        needDecoding = false;
        quote = 0;
        length = 0;
    }
}
