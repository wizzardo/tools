package com.wizzardo.tools.image;

import java.io.IOException;
import java.util.Arrays;

public interface BitStream {

    boolean ensureEnoughLength(int length) throws IOException;

    int readByte() throws IOException;

    int readBit() throws IOException;

    int readByteUnsafe();

    int readByteUnsafe(boolean ignoreByteStuffing);

    int readBitUnsafe();

    int readBits(int n) throws IOException;

    int readBitsUnsafe(int n);

    void skip(int length) throws IOException;

    void unread(int offset);

    int getBitsRemaining();

    void setBitsRemaining(int bitsRemaining);

    int getBitsBuffer();

    void setBitsBuffer(int bitsBuffer);

    boolean isSeekable();

    int indexOf(byte[] bytes, int from);

    BitStream shallowCopy(int offset);

    default int readIntUnsafe() {
        return (readByteUnsafe() << 24) + (readByteUnsafe() << 16) + (readByteUnsafe() << 8) + readByteUnsafe();
    }

    default int readIntUnsafe(boolean ignoreByteStuffing) {
        return (readByteUnsafe(ignoreByteStuffing) << 24) + (readByteUnsafe(ignoreByteStuffing) << 16) + (readByteUnsafe(ignoreByteStuffing) << 8) + readByteUnsafe(ignoreByteStuffing);
    }

    default int readShortUnsafe() {
        return (readByteUnsafe() << 8) + readByteUnsafe();
    }

    default int readShortUnsafe(boolean ignoreByteStuffing) {
        return (readByteUnsafe(ignoreByteStuffing) << 8) + readByteUnsafe(ignoreByteStuffing);
    }

    default int readInt() throws IOException {
        return (readByte() << 24) + (readByte() << 16) + (readByte() << 8) + readByte();
    }

    default int readShort() throws IOException {
        return (readByte() << 8) + readByte();
    }

    default byte[] readBytes(byte[] block) throws IOException {
        ensureEnoughLength(block.length);
        for (int i = 0; i < block.length; i++) {
            block[i] = (byte) readByteUnsafe(true);
        }
        return block;
    }

    default byte[] readBytesUnsafe(byte[] block) {
        for (int i = 0; i < block.length; i++) {
            block[i] = (byte) readByteUnsafe(true);
        }
        return block;
    }

    //    int[] debugData = new int[4];
    default int fillBitsBuffer() {
        int result = 0;
        int i = 0;
//        Arrays.fill(debugData, -1);
        do {
            int value = readByteUnsafe(true);
            if (value == 255) {
                int next = readByteUnsafe(true);
                if (next != 0) {
                    if (next >= 0xD0 && next <= 0xD7) {
                        setBitsBuffer(result);
                        setBitsRemaining(i * 8);
                        unread(2);
                        if (i == 0) {
//                            return fillBitsBuffer();
                            throw new IllegalStateException();
                        }

                        return i * 8;
                    }
                    throw new IllegalStateException();
                }
            }
            result = (result << 8) + value;
//            debugData[i]=value;
        } while (++i < 4);
        setBitsBuffer(result);
        setBitsRemaining(32);
        return 32;
    }
}
