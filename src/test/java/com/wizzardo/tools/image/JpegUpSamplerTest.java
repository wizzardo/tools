package com.wizzardo.tools.image;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.wizzardo.tools.image.UpSampler.createUpSampler;

public class JpegUpSamplerTest {


    UpSampler upSampler = createUpSampler(2, 2);


    private void doUpSample(JpegDecoder.MCUsHolder mcu, int[] actual, float[] actualF, int y) {
        for (int x = 0; x < 16; x++) {
            actual[x] = (int) ((actualF[x] = upSampler.get(mcu, x, y)) + 0.5f);
        }
    }

    private static void setRow(float[] mcu, int y, int[] in) {
        for (int i = 0; i < in.length; i++) {
            mcu[i + y * 8] = in[i];
        }
    }

    @Test
    public void test_upsampler_row_0_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] in = new int[]{108, 108, 111, 109, 110, 111, 110, 110};
        int[] in2 = new int[]{108, 107, 108, 108, 108, 109, 108, 108};
        int[] expected = new int[]{108, 108, 108, 109, 110, 111, 110, 109, 110, 110, 111, 111, 110, 110, 110, 110};
        setRow(mcu.curr, 0, in);
        setRow(mcu.next, 0, in2);

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_0_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] in = new int[]{106, 107, 106, 107, 107, 107, 106, 109};
        int[] expected = new int[]{107, 106, 107, 107, 106, 106, 107, 107, 107, 107, 107, 107, 106, 107, 108, 109};
        setRow(mcu.curr, 0, in);
        setRow(mcu.prev, 0, new int[]{107, 108, 109, 107, 105, 107, 106, 108});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;

        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_0_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{109, 108, 107, 107, 108, 108, 108, 108, 108, 108, 109, 109, 108, 108, 108, 108};
        setRow(mcu.curr, 0, new int[]{108, 107, 108, 108, 108, 109, 108, 108});
        setRow(mcu.next, 0, new int[]{109, 109, 107, 106, 108, 106, 107, 107});
        setRow(mcu.prev, 0, new int[]{108, 108, 111, 109, 110, 111, 110, 110});

        mcu.index = 2;
        mcu.x = 2;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_1_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{108, 108, 108, 108, 110, 110, 109, 109, 110, 110, 110, 110, 110, 110, 110, 110};
        setRow(mcu.curr, 0, new int[]{108, 108, 111, 109, 110, 111, 110, 110});
        setRow(mcu.next, 0, new int[]{108, 107, 108, 108, 108, 109, 108, 108});
        setRow(mcu.curr, 1, new int[]{107, 107, 108, 109, 109, 109, 110, 110});
        setRow(mcu.next, 1, new int[]{108, 109, 110, 108, 108, 108, 108, 108});

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 1);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_1_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 106, 106, 106, 106, 106, 106, 106, 106, 106, 106, 106, 106, 106, 106, 107};
        setRow(mcu.curr, 0, new int[]{106, 107, 106, 107, 107, 107, 106, 109});
        setRow(mcu.prev, 0, new int[]{107, 108, 109, 107, 105, 107, 106, 108});
        setRow(mcu.curr, 1, new int[]{107, 105, 105, 104, 105, 104, 104, 100});
        setRow(mcu.prev, 1, new int[]{105, 109, 108, 105, 106, 107, 108, 107});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;

        doUpSample(mcu, actual, actualF, 1);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_1_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{109, 108, 108, 108, 108, 108, 108, 108, 108, 108, 109, 109, 108, 108, 108, 108};
        setRow(mcu.prev, 0, new int[]{108, 108, 111, 109, 110, 111, 110, 110});
        setRow(mcu.curr, 0, new int[]{108, 107, 108, 108, 108, 109, 108, 108});
        setRow(mcu.next, 0, new int[]{109, 109, 107, 106, 108, 106, 107, 107});

        setRow(mcu.prev, 1, new int[]{107, 107, 108, 109, 109, 109, 110, 110});
        setRow(mcu.curr, 1, new int[]{108, 109, 110, 108, 108, 108, 108, 108});
        setRow(mcu.next, 1, new int[]{108, 107, 106, 106, 106, 106, 106, 106});

        mcu.index = 2;
        mcu.x = 2;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 1);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_2_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 107, 107, 108, 108, 109, 109, 109, 109, 109, 109, 110, 110, 110, 110, 110};
        setRow(mcu.curr, 0, new int[]{108, 108, 111, 109, 110, 111, 110, 110});
        setRow(mcu.next, 0, new int[]{108, 107, 108, 108, 108, 109, 108, 108});
        setRow(mcu.curr, 1, new int[]{107, 107, 108, 109, 109, 109, 110, 110});
        setRow(mcu.next, 1, new int[]{108, 109, 110, 108, 108, 108, 108, 108});

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 2);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_2_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 106, 106, 105, 105, 105, 105, 105, 105, 105, 105, 105, 105, 104, 103, 102};
        setRow(mcu.curr, 0, new int[]{106, 107, 106, 107, 107, 107, 106, 109});
        setRow(mcu.prev, 0, new int[]{107, 108, 109, 107, 105, 107, 106, 108});
        setRow(mcu.curr, 1, new int[]{107, 105, 105, 104, 105, 104, 104, 100});
        setRow(mcu.prev, 1, new int[]{105, 109, 108, 105, 106, 107, 108, 107});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;

        doUpSample(mcu, actual, actualF, 2);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_2_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{109, 108, 108, 109, 109, 109, 108, 108, 108, 108, 108, 108, 108, 108, 108, 108};
        setRow(mcu.prev, 0, new int[]{108, 108, 111, 109, 110, 111, 110, 110});
        setRow(mcu.curr, 0, new int[]{108, 107, 108, 108, 108, 109, 108, 108});
        setRow(mcu.next, 0, new int[]{109, 109, 107, 106, 108, 106, 107, 107});

        setRow(mcu.prev, 1, new int[]{107, 107, 108, 109, 109, 109, 110, 110});
        setRow(mcu.curr, 1, new int[]{108, 109, 110, 108, 108, 108, 108, 108});
        setRow(mcu.next, 1, new int[]{108, 107, 106, 106, 106, 106, 106, 106});

        mcu.index = 2;
        mcu.x = 2;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 2);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_14_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{106, 106, 106, 107, 108, 108, 108, 108, 108, 108, 108, 107, 107, 107, 107, 106};
        setRow(mcu.curr, 7, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.next, 7, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
        setRow(mcu.curr, 6, new int[]{107, 105, 109, 107, 107, 109, 108, 107});
        setRow(mcu.next, 6, new int[]{107, 107, 107, 109, 106, 107, 108, 109});

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 14);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_14_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 107, 107, 106, 106, 106, 106, 106, 107, 106, 105, 104, 103, 102, 102, 102};
        setRow(mcu.curr, 7, new int[]{107, 107, 106, 106, 106, 103, 102, 101});
        setRow(mcu.prev, 7, new int[]{107, 108, 108, 109, 108, 106, 108, 108});
        setRow(mcu.curr, 6, new int[]{108, 105, 106, 107, 109, 108, 103, 103});
        setRow(mcu.prev, 6, new int[]{108, 108, 108, 109, 109, 108, 110, 107});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;

        doUpSample(mcu, actual, actualF, 14);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_14_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 107, 107, 107, 106, 107, 107, 107, 106, 106, 106, 106, 107, 108, 108, 108};
        setRow(mcu.prev, 7, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.curr, 7, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
        setRow(mcu.next, 7, new int[]{108, 109, 107, 109, 107, 107, 107, 105});

        setRow(mcu.prev, 6, new int[]{107, 105, 109, 107, 107, 109, 108, 107});
        setRow(mcu.curr, 6, new int[]{107, 107, 107, 109, 106, 107, 108, 109});
        setRow(mcu.next, 6, new int[]{108, 109, 109, 107, 108, 106, 106, 106});

        mcu.index = 2;
        mcu.x = 2;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 14);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_15_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{106, 106, 106, 107, 107, 108, 108, 108, 108, 108, 108, 107, 107, 107, 106, 106};
//        setRow(mcu.curr, 7, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
//        setRow(mcu.next, 7, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
        setRow(mcu.curr, 0, new int[]{105, 105, 107, 107, 109, 108, 107, 106});
        setRow(mcu.next, 0, new int[]{106, 107, 106, 108, 107, 105, 107, 108});

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 0, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.prevRow, 1, new int[]{107, 107, 106, 107, 106, 105, 108, 108});

        mcu.y = 1;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 15);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_15_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 107, 107, 106, 106, 106, 106, 106, 106, 105, 104, 103, 102, 102, 101, 101};
//        setRow(mcu.curr, 7, new int[]{107, 107, 106, 106, 106, 103, 102, 101});
//        setRow(mcu.prev, 7, new int[]{107, 108, 108, 109, 108, 106, 108, 108});
        setRow(mcu.curr, 0, new int[]{106, 106, 104, 105, 105, 106, 102, 102});
        setRow(mcu.prev, 0, new int[]{105, 108, 106, 107, 106, 106, 106, 105});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;
        mcu.y = 1;

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 4, new int[]{107, 107, 106, 106, 106, 103, 102, 101});
        setRow(mcu.prevRow, 3, new int[]{107, 108, 108, 109, 108, 106, 108, 108});

        doUpSample(mcu, actual, actualF, 15);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_15_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{107, 107, 107, 107, 106, 106, 107, 107, 107, 106, 105, 106, 107, 108, 108, 108};
//        setRow(mcu.prev, 7, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
//        setRow(mcu.curr, 7, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
//        setRow(mcu.next, 7, new int[]{108, 109, 107, 109, 107, 107, 107, 105});

        setRow(mcu.prev, 0, new int[]{105, 105, 107, 107, 109, 108, 107, 106});
        setRow(mcu.curr, 0, new int[]{106, 107, 106, 108, 107, 105, 107, 108});
        setRow(mcu.next, 0, new int[]{108, 107, 106, 107, 107, 107, 107, 106});

        mcu.index = 2;
        mcu.x = 2;
        mcu.y = 1;

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 1, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.prevRow, 2, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
        setRow(mcu.prevRow, 3, new int[]{108, 109, 107, 109, 107, 107, 107, 105});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 15);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_16_start() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{105, 105, 105, 106, 107, 107, 107, 108, 108, 109, 108, 108, 107, 107, 106, 106};
        setRow(mcu.curr, 0, new int[]{105, 105, 107, 107, 109, 108, 107, 106});
        setRow(mcu.next, 0, new int[]{106, 107, 106, 108, 107, 105, 107, 108});

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 0, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.prevRow, 1, new int[]{107, 107, 106, 107, 106, 105, 108, 108});

        mcu.y = 1;

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_16_end() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{106, 106, 106, 106, 105, 105, 105, 105, 105, 105, 105, 104, 103, 102, 102, 102};
        setRow(mcu.curr, 0, new int[]{106, 106, 104, 105, 105, 106, 102, 102});
        setRow(mcu.prev, 0, new int[]{105, 108, 106, 107, 106, 106, 106, 105});


        int[] actual = new int[16];
        float[] actualF = new float[16];
        mcu.lastColumn = 15;
        mcu.index = 4;
        mcu.x = 4;
        mcu.y = 1;

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 4, new int[]{107, 107, 106, 106, 106, 103, 102, 101});
        setRow(mcu.prevRow, 3, new int[]{107, 108, 108, 109, 108, 106, 108, 108});

        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test_upsampler_row_16_middle() {
        JpegDecoder.MCUsHolder mcu = new JpegDecoder.MCUsHolder();
        int[] expected = new int[]{106, 106, 107, 107, 106, 106, 107, 108, 107, 106, 105, 106, 107, 107, 108, 108};
        setRow(mcu.prev, 0, new int[]{105, 105, 107, 107, 109, 108, 107, 106});
        setRow(mcu.curr, 0, new int[]{106, 107, 106, 108, 107, 105, 107, 108});
        setRow(mcu.next, 0, new int[]{108, 107, 106, 107, 107, 107, 107, 106});

        mcu.index = 2;
        mcu.x = 2;
        mcu.y = 1;

        mcu.prevRow = new float[64];
        setRow(mcu.prevRow, 1, new int[]{106, 107, 108, 108, 108, 107, 107, 106});
        setRow(mcu.prevRow, 2, new int[]{107, 107, 106, 107, 106, 105, 108, 108});
        setRow(mcu.prevRow, 3, new int[]{108, 109, 107, 109, 107, 107, 107, 105});

        int[] actual = new int[16];
        float[] actualF = new float[16];
        doUpSample(mcu, actual, actualF, 0);
        System.out.println(Arrays.toString(expected));
        System.out.println(Arrays.toString(actual));
//        System.out.println(Arrays.toString(actualF));
        Assert.assertArrayEquals(expected, actual);
    }

}
