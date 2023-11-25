package com.wizzardo.tools.image;

import java.io.IOException;

class BitStreamByteArray implements BitStream {
    final byte[] buf;
    int offset;
    int read;
    int bitsBuffer;
    int bitsRemaining;

    BitStreamByteArray(byte[] buf) {
        this.buf = buf;
        this.read = buf.length;
    }

    @Override
    public boolean ensureEnoughLength(int length) throws IOException {
        int left = read - offset;
        return left >= length;
    }

    @Override
    public void skip(int length) throws IOException {
        offset += length;
    }

    @Override
    public void unread(int offset) {
        this.offset -= offset;
    }

    @Override
    public int getBitsRemaining() {
        return bitsRemaining;
    }

    @Override
    public void setBitsRemaining(int bitsRemaining) {
        this.bitsRemaining = bitsRemaining;
    }

    @Override
    public int getBitsBuffer() {
        return bitsBuffer;
    }

    @Override
    public void setBitsBuffer(int bitsBuffer) {
        this.bitsBuffer = bitsBuffer;
    }


//    public void getBlock(byte[] block) throws IOException {
//        ensureEnoughLength(block.length);
//        for (int i = 0; i < block.length; i++) {
//            byte b = buf[offset++];
//            if (b == (byte) 0xFF) {
//                if (buf[offset++] != 0)
//                    throw new IllegalStateException();
//            }
//            block[i] = b;
//        }
//    }


    @Override
    public boolean isSeekable() {
        return true;
    }

    @Override
    public int readByte() throws IOException {
        if (!ensureEnoughLength(2))
            return -1;

        byte b = buf[offset++];
        int result = b & 0xFF;
        if (result == 0xFF) {
            if (buf[offset++] != 0)
                throw new IllegalStateException();
        }
        return result;
    }

    @Override
    public int readBit() throws IOException {
        if (bitsRemaining == 0) {
            bitsBuffer = readByte();
            if (bitsBuffer == -1) {
                return -1;
            }
            bitsRemaining = 8;
        }
        bitsRemaining--;
        return (bitsBuffer >>> bitsRemaining) & 1;
    }

    @Override
    public int readByteUnsafe() {
        byte b = buf[offset++];
        int result = b & 0xFF;
        if (result == 0xFF) {
            if (buf[offset++] != 0)
                throw new IllegalStateException();
        }
        return result;
    }

    @Override
    public int readByteUnsafe(boolean ignoreByteStuffing) {
        if (!ignoreByteStuffing)
            return readByteUnsafe();

        byte b = buf[offset++];
        return b & 0xFF;
    }

    @Override
    public int readBitUnsafe() {
        if (bitsRemaining == 0) {
            bitsBuffer = readByteUnsafe();
            bitsRemaining = 8;
        }
        bitsRemaining--;
        return (bitsBuffer >>> bitsRemaining) & 1;
    }

    @Override
    public int readBits(int n) throws IOException {
        int result = 0;
        while (n > 0) {
            if (bitsRemaining == 0) {
                bitsBuffer = readByte();
                if (bitsBuffer == -1) {
                    return -1;
                }
                bitsRemaining = 8;
            }
            int bitsToRead = Math.min(n, bitsRemaining);
            n -= bitsToRead;
            bitsRemaining -= bitsToRead;
            result <<= bitsToRead;
            result |= (bitsBuffer >>> bitsRemaining) & ((1 << bitsToRead) - 1);
        }
        return result;
    }

    @Override
    public int readBitsUnsafe(int n) {
        int result = 0;
        while (n > 0) {
            if (bitsRemaining == 0) {
                bitsBuffer = readByteUnsafe();
                if (bitsBuffer == -1) {
                    return -1;
                }
                bitsRemaining = 8;
            }
            int bitsToRead = Math.min(n, bitsRemaining);
            n -= bitsToRead;
            bitsRemaining -= bitsToRead;
            result <<= bitsToRead;
            result |= (bitsBuffer >>> bitsRemaining) & ((1 << bitsToRead) - 1);
        }
        return result;
    }

    @Override
    public int indexOf(byte[] bytes, int from) {
        byte first = bytes[0];
        outer:
        for (int i = from; i < buf.length; i++) {
            if (buf[i] == first) {
                for (int j = 1; j < bytes.length; j++) {
                    if (buf[i + j] != bytes[j])
                        continue outer;
                }
                return i;
            }
        }
        return -1;
    }

    @Override
    public BitStream shallowCopy(int offset) {
        BitStreamByteArray stream = new BitStreamByteArray(buf);
        stream.offset = offset;
        return stream;
    }
}
