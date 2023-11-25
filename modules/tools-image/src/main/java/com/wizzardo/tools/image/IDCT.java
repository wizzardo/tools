package com.wizzardo.tools.image;

public class IDCT {

    static final float[] IDCT_SCALING_FACTORS = {
            (float) (1 / Math.sqrt(2.0) / 2),
            (float) (4.0 * Math.cos(Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(2.0 * Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(3.0 * Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(4.0 * Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(5.0 * Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(6.0 * Math.PI / 16.0) / 8),
            (float) (4.0 * Math.cos(7.0 * Math.PI / 16.0) / 8),
    };

    static final int[] ZIGZAG = {
            0, 1, 5, 6, 14, 15, 27, 28,
            2, 4, 7, 13, 16, 26, 29, 42,
            3, 8, 12, 17, 25, 30, 41, 43,
            9, 11, 18, 24, 31, 40, 44, 53,
            10, 19, 23, 32, 39, 45, 52, 54,
            20, 22, 33, 38, 46, 51, 55, 60,
            21, 34, 37, 47, 50, 56, 59, 61,
            35, 36, 48, 49, 57, 58, 62, 63
    };

    static final float C2 = (float) (2.0 * Math.cos(Math.PI / 8));
    static final float C4 = (float) (2.0 * Math.cos(2 * Math.PI / 8));
    static final float C6 = (float) (2.0 * Math.cos(3 * Math.PI / 8));
    static final float Q = C2 - C6;
    static final float R = C2 + C6;

    public static float[] scaleDequantizationMatrix(byte[] matrix) {
        float[] result = new float[matrix.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (matrix[ZIGZAG[i]] & 0xFF) * IDCT_SCALING_FACTORS[i / 8] * IDCT_SCALING_FACTORS[i % 8];
        }
        return result;
    }

    public static void inverseDCT8x8(float[] data) {
        float a1, a2, a3, a4, a6, a7, b5, n0, n1, n2, n3, m1, m2, m3, m4, m5, m6, m7;

        for (int i = 0; i < 64; i += 8) {
            a2 = data[i + 2] + data[i + 6];
            a4 = data[i + 5] - data[i + 3];
            a1 = data[i + 1] + data[i + 7];
            a3 = data[i + 3] + data[i + 5];
            a6 = data[i + 1] - data[i + 7];
            a7 = a1 + a3;
            m1 = C6 * (a4 + a6);
            b5 = (a1 - a3) * C4;
            m2 = R * a6 - m1 - a7;
            n0 = m2 - b5;
            n1 = data[i] - data[i + 4];
            n2 = (data[i + 2] - data[i + 6]) * C4 - a2;
            n3 = data[i] + data[i + 4];
            m3 = n1 + n2;
            m4 = n3 + a2;
            m5 = n1 - n2;
            m6 = n3 - a2;
            m7 = Q * a4 + m1 + n0;
            data[i] = m4 + a7;
            data[i + 1] = m3 + m2;
            data[i + 2] = m5 - n0;
            data[i + 3] = m6 + m7;
            data[i + 4] = m6 - m7;
            data[i + 5] = m5 + n0;
            data[i + 6] = m3 - m2;
            data[i + 7] = m4 - a7;
        }

        for (int i = 0; i < 8; i++) {
            a2 = data[16 + i] + data[48 + i];
            a4 = data[40 + i] - data[24 + i];
            a1 = data[8 + i] + data[56 + i];
            a3 = data[24 + i] + data[40 + i];
            a6 = data[8 + i] - data[56 + i];
            a7 = a1 + a3;
            m1 = C6 * (a4 + a6);
            b5 = (a1 - a3) * C4;
            m2 = R * a6 - m1 - a7;
            n0 = m2 - b5;
            n1 = data[i] - data[32 + i];
            n2 = (data[16 + i] - data[48 + i]) * C4 - a2;
            n3 = data[i] + data[32 + i];
            m3 = n1 + n2;
            m4 = n3 + a2;
            m5 = n1 - n2;
            m6 = n3 - a2;
            m7 = Q * a4 + m1 + n0;
            data[i] = m4 + a7;
            data[8 + i] = m3 + m2;
            data[16 + i] = m5 - n0;
            data[24 + i] = m6 + m7;
            data[32 + i] = m6 - m7;
            data[40 + i] = m5 + n0;
            data[48 + i] = m3 - m2;
            data[56 + i] = m4 - a7;
        }
    }

    public static void zigZagToBlock(final int[] zz, final int[] block) {
        for (int i = 0; i < ZIGZAG.length; i++) {
            block[i] = zz[ZIGZAG[i]];
        }
    }
}
