package com.wizzardo.tools.image;

import java.io.IOException;
import java.io.InputStream;

class BitStreamInputStream extends BitStreamByteArray {
    final InputStream in;

    BitStreamInputStream(byte[] buf, InputStream in) throws IOException {
        super(buf);
        this.in = in;
        read();
    }

    public void read() throws IOException {
        read = in.read(buf);
        offset = 0;
    }

    @Override
    public boolean ensureEnoughLength(int length) throws IOException {
        int left = read - offset;
        if (left >= length)
            return true;

        if (in == null)
            return false;

        System.arraycopy(buf, offset, buf, 0, left);
        read = in.read(buf, left, buf.length - left);
        offset = 0;
        boolean result = read != -1;
        read = Math.max(0, read) + left;
        return result;
    }

    @Override
    public void skip(int length) throws IOException {
        int left = read - offset;
        if (left < length) {
            int skip = length;
            skip -= read - offset;
            while (skip > 0) {
                read = in.read(buf);
                offset = Math.min(skip, read);
                skip -= Math.min(skip, read);
            }
        } else {
            offset += length;
        }
    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    public BitStream shallowCopy(int offset) {
        throw new IllegalStateException("Cannot create copy of " + BitStreamInputStream.class.getSimpleName());
    }
}
