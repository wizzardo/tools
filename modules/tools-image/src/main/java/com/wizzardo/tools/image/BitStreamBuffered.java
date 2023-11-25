package com.wizzardo.tools.image;

import java.io.IOException;

class BitStreamBuffered implements BitStream {
    int partSize = 1024 * 64;
    byte[][] parts;
    int offset;
    int read;
    int bitsBuffer;
    int bitsRemaining;

    BitStreamBuffered(BitStreamInputStream inputStream) throws IOException {
        this.parts = new byte[1][];

        buffer(inputStream);
    }

    BitStreamBuffered(BitStreamBuffered stream) {
        this.parts = stream.parts;
        this.read = stream.read;
    }

    void buffer(BitStreamInputStream inputStream) throws IOException {
        byte[] part = new byte[partSize];
        int r = inputStream.read - inputStream.offset;
        System.arraycopy(inputStream.buf, inputStream.offset, part, 0, r);
        parts[0] = part;
        this.read += r;

        do {
            int read = inputStream.in.read(part, r, part.length - r);
            if (read == -1) {
                break;
            } else {
                r += read;
                this.read += read;
                if (r == part.length) {
                    part = new byte[partSize];
                    byte[][] nextParts = new byte[parts.length + 1][];
                    System.arraycopy(parts, 0, nextParts, 0, parts.length);
                    parts = nextParts;
                    parts[parts.length - 1] = part;
                    r = 0;
                }
            }
        } while (true);
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

    @Override
    public int readByte() throws IOException {
        if (!ensureEnoughLength(2))
            return -1;

        byte[] buf = parts[offset >> 16];
        byte b = buf[(offset++) & 0xFFFF];
        int result = b & 0xFF;
        if (result == 0xFF) {
            buf = parts[offset >> 16];
            if (buf[(offset++) & 0xFFFF] != 0)
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
        byte[] buf = parts[offset >> 16];
        byte b = buf[((offset++) & 0xFFFF) & 0xFFFF];
        int result = b & 0xFF;
        if (result == 0xFF) {
            buf = parts[offset >> 16];
            if (buf[((offset++) & 0xFFFF) & 0xFFFF] != 0)
                throw new IllegalStateException();
        }
        return result;
    }

    @Override
    public int readByteUnsafe(boolean ignoreByteStuffing) {
        if (!ignoreByteStuffing)
            return readByteUnsafe();

        byte[] buf = parts[offset >> 16];
        byte b = buf[((offset++) & 0xFFFF) & 0xFFFF];
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
    public boolean isSeekable() {
        return true;
    }

    byte getByte(int offset) {
        return parts[offset >> 16][(offset & 0xFFFF) & 0xFFFF];
    }

    @Override
    public int indexOf(byte[] bytes, int from) {
        byte first = bytes[0];
        outer:
        for (int i = from; i < read; i++) {
            if (getByte(i) == first) {
                for (int j = 1; j < bytes.length; j++) {
                    if (getByte(i + j) != bytes[j])
                        continue outer;
                }
                return i;
            }
        }
        return -1;
    }

    @Override
    public BitStream shallowCopy(int offset) {
        BitStreamBuffered stream = new BitStreamBuffered(this);
        stream.offset = offset;
        return stream;
    }
}
