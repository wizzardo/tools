package com.wizzardo.tools.image;

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JpegDecoderProgressiveTest {

    static BufferedImage decodeImageIO(String path) throws IOException {
        return ImageIO.read(JpegDecoderProgressiveTest.class.getResourceAsStream(path));
    }

    static BufferedImage decode(String path) throws IOException {
        return new JpegDecoder().read(JpegDecoderProgressiveTest.class.getResourceAsStream(path));
    }

    static int compare(BufferedImage expected, BufferedImage actual, int delta) {
        int pixelsCount = actual.getWidth() * actual.getHeight();
        int[] pixels = new int[pixelsCount * 3];
        int[] pixelsExpected = new int[pixelsCount * 3];

        actual.getRaster().getPixels(0, 0, actual.getWidth(), actual.getHeight(), pixels);
        expected.getRaster().getPixels(0, 0, actual.getWidth(), actual.getHeight(), pixelsExpected);

        int diffs = 0;
        for (int i = 0; i < pixelsCount; i++) {
            if (Math.abs(pixels[i * 3] - pixelsExpected[i * 3]) > delta
                    || Math.abs(pixels[i * 3 + 1] - pixelsExpected[i * 3 + 1]) > delta
                    || Math.abs(pixels[i * 3 + 2] - pixelsExpected[i * 3 + 2]) > delta
            ) {
//                System.out.println((i % expected.getWidth()) + "x" + (i / expected.getWidth())
//                        + " [" + pixels[i * 3] + ", " + pixels[i * 3 + 1] + ", " + pixels[i * 3 + 2] + "] != "
//                        + " [" + pixelsExpected[i * 3] + ", " + pixelsExpected[i * 3 + 1] + ", " + pixelsExpected[i * 3 + 2] + "]"
//                );
                diffs++;
            }
        }
        return diffs;
    }

    static int compare(BufferedImage expected, BufferedImage actual, int delta, BufferedImage src) {
        int pixelsCount = actual.getWidth() * actual.getHeight();
        int[] pixels = new int[pixelsCount * 3];
        int[] pixelsExpected = new int[pixelsCount * 3];
        int[] pixelsSrc = new int[pixelsCount * 3];

        actual.getRaster().getPixels(0, 0, actual.getWidth(), actual.getHeight(), pixels);
        expected.getRaster().getPixels(0, 0, actual.getWidth(), actual.getHeight(), pixelsExpected);
        src.getRaster().getPixels(0, 0, actual.getWidth(), actual.getHeight(), pixelsSrc);

        double totalDistanceActual = 0;
        double totalDistanceExpected = 0;
        int diffs = 0;
        for (int i = 0; i < pixelsCount; i++) {
            if (Math.abs(pixels[i * 3] - pixelsExpected[i * 3]) > delta
                    || Math.abs(pixels[i * 3 + 1] - pixelsExpected[i * 3 + 1]) > delta
                    || Math.abs(pixels[i * 3 + 2] - pixelsExpected[i * 3 + 2]) > delta
            ) {
//                if (i / expected.getWidth() == 2)
//                if (i % expected.getWidth() == 0)
                if (i / expected.getWidth() == 0)
//                if (diffs < 256)
                    System.out.println((i % expected.getWidth()) + "x" + (i / expected.getWidth())
                            + " (" + ((i % expected.getWidth()) % 16) + "x" + ((i / expected.getWidth()) % 16) + ")"
                            + " [" + pixels[i * 3] + ", " + pixels[i * 3 + 1] + ", " + pixels[i * 3 + 2] + "] != "
                            + "[" + pixelsExpected[i * 3] + ", " + pixelsExpected[i * 3 + 1] + ", " + pixelsExpected[i * 3 + 2] + "]"
                            + " src: [" + pixelsSrc[i * 3] + ", " + pixelsSrc[i * 3 + 1] + ", " + pixelsSrc[i * 3 + 2] + "]"
                            + " da: " + distance(pixels, pixelsSrc, i)
                            + " de: " + distance(pixelsExpected, pixelsSrc, i)
                    );
                diffs++;
            }
            totalDistanceActual += distance(pixels, pixelsSrc, i);
            totalDistanceExpected += distance(pixelsExpected, pixelsSrc, i);
        }
        System.out.println("totalDistanceActual:   " + totalDistanceActual);
        System.out.println("totalDistanceExpected: " + totalDistanceExpected);
        return diffs;
    }

    static float distance(int[] a, int[] b, int i) {
        return (float) Math.sqrt(
                (a[i * 3] - b[i * 3]) * (a[i * 3] - b[i * 3])
                        + (a[i * 3 + 1] - b[i * 3 + 1]) * (a[i * 3 + 1] - b[i * 3 + 1])
                        + (a[i * 3 + 2] - b[i * 3 + 2]) * (a[i * 3 + 2] - b[i * 3 + 2]));
    }

    static void assertLower(int threshold, int value) {
        Assert.assertTrue(value + " > " + threshold, value <= threshold);
    }

    @Test
    public void progressive_jpeg() throws IOException {
        String path = "/image/test_prog.jpg";
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test.png")));
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
//        ImageTools.saveJPG(decodeImageIO(path), "out.jpg",90);
    }

    @Test
    public void progressive_jpeg_2() throws IOException {
        String path = "/image/test_prog_2.jpg";
        assertLower(100, compare(decodeImageIO(path), decode(path), 1, decodeImageIO("/image/test_gradient.png")));
//        ImageTools.saveJPG(decodeImageIO(path), "out.jpg",90);
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
//        ImageTools.savePNG(decode(path), "out.png");
    }

    @Test
    public void progressive_jpeg_2_int() throws IOException {
        String path = "/image/test_prog_2_int.jpg";
        assertLower(100, compare(decodeImageIO(path), decode(path), 1, decodeImageIO("/image/test_gradient.png")));
//        ImageTools.saveJPG(decodeImageIO(path), "out.jpg",90);
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
//        ImageTools.savePNG(decode(path), "out.png");
    }

    @Test
    public void progressive_jpeg_420() throws IOException {
        String path = "/image/test_prog_gradient_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(160, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_422h() throws IOException {
        String path = "/image/test_prog_gradient_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_422v() throws IOException {
        String path = "/image/test_prog_gradient_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }


    @Test
    public void progressive_jpeg_444_2() throws IOException {
        String path = "/image/test_prog_subsampling.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(3000, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void progressive_jpeg_420_2() throws IOException {
        String path = "/image/test_prog_subsampling_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(3000, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void progressive_jpeg_422h_2() throws IOException {
        String path = "/image/test_prog_subsampling_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(1200, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void progressive_jpeg_422v_2() throws IOException {
        String path = "/image/test_prog_subsampling_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 3, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void progressive_jpeg_444_3() throws IOException {
        String path = "/image/test_prog_subsampling_2.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 0, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_420_3() throws IOException {
        String path = "/image/test_prog_subsampling_2_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(1500, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_422h_3() throws IOException {
        String path = "/image/test_prog_subsampling_2_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(600, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_422v_3() throws IOException {
        String path = "/image/test_prog_subsampling_2_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(128, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_444_non_interleaved() throws IOException {
        String path = "/image/test_prog_ni_gradient_444.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg", 100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 1, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_420_non_interleaved() throws IOException {
        String path = "/image/test_prog_ni_gradient_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg", 100);
        assertLower(1500, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_422h_non_interleaved() throws IOException {
        String path = "/image/test_prog_ni_gradient_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(600, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_422v_non_interleaved() throws IOException {
        String path = "/image/test_prog_ni_gradient_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(128, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void progressive_jpeg_444_non_interleaved_2() throws IOException {
        String path = "/image/test_prog_ni_subsampling_2_444.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 0, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_420_non_interleaved_2() throws IOException {
        String path = "/image/test_prog_ni_subsampling_2_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(1500, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_422h_non_interleaved_2() throws IOException {
        String path = "/image/test_prog_ni_subsampling_2_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(600, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void progressive_jpeg_422v_non_interleaved_2() throws IOException {
        String path = "/image/test_prog_ni_subsampling_2_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(128, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }


    //    @Test
    public void progressive_jpeg_buffer() throws IOException {
//        String path = "/image/test_prog_2.jpg";
//        String path = "/image/test_prog_gradient_422h.jpg";
//        String path = "/image/test_prog_gradient_420.jpg";
//        String path = "/image/test_prog_subsampling_2_420.jpg";
        String path = "/image/test_prog_subsampling_2_422h.jpg";

        JpegDecoder decoder = new JpegDecoder();
        BitStream stream = new BitStreamInputStream(new byte[10240], JpegDecoderProgressiveTest.class.getResourceAsStream(path));
        decoder.readMetaInfo(stream);

        int[][] scansResults = new int[10][];
        {
            int blocksWidth = ((decoder.width % 8 != 0) ? (int) (decoder.width / 8.0 + 1) * 8 : decoder.width) / 8;
            int blocksHeight = ((decoder.height % 8 != 0) ? (int) (decoder.height / 8.0 + 1) * 8 : decoder.height) / 8;
            int[] progressiveBuffer = new int[blocksWidth * blocksHeight * 3 * 8 * 8];
            int scanIndex = 0;
            do {
                stream.ensureEnoughLength(4);
                JpegDecoder.Marker marker = JpegDecoder.Marker.valueOf(stream.readShortUnsafe(true));
                if (marker == JpegDecoder.Marker.SOS) {
                    decoder.readProgressiveScan(stream, progressiveBuffer);
                    scansResults[scanIndex++] = Arrays.copyOf(progressiveBuffer, progressiveBuffer.length);
                } else if (marker == JpegDecoder.Marker.DHT) {
                    int length = stream.readShortUnsafe();
                    stream.ensureEnoughLength(length);
                    decoder.huffmanTables = decoder.readHuffmanTable(stream, length, decoder.huffmanTables);
                } else if (marker == JpegDecoder.Marker.EOI) {
                    break;
                } else
                    throw new IllegalStateException();
            } while (true);
        }

        decoder = new JpegDecoder();
        stream = new BitStreamInputStream(new byte[10240], JpegDecoderProgressiveTest.class.getResourceAsStream(path));
        decoder.readMetaInfo(stream);

        if (stream instanceof BitStreamInputStream) {
            stream = new BitStreamBuffered((BitStreamInputStream) stream);
        }

        if (!stream.isSeekable())
            throw new IllegalStateException();


        byte[] sosMarker = new byte[]{(byte) 0xFF, JpegDecoder.Marker.SOS.value};
        byte[] dhtMarker = new byte[]{(byte) 0xFF, JpegDecoder.Marker.DHT.value};

        List<JpegDecoder.ScanReader> readers = new ArrayList<>(16);
        int prev = 0;
        int dhtIndex = stream.indexOf(dhtMarker, prev);
        do {
            int scanIndex = stream.indexOf(sosMarker, prev);
            if (scanIndex == -1)
                break;

            while (dhtIndex != -1 && dhtIndex < scanIndex) {
                BitStream streamCopy = stream.shallowCopy(dhtIndex + 2);
                int length = streamCopy.readShortUnsafe();
                decoder.huffmanTables = decoder.readHuffmanTable(streamCopy, length, decoder.huffmanTables);
                dhtIndex = stream.indexOf(dhtMarker, dhtIndex + 2 + length);
            }
            BitStream shallowCopy = stream.shallowCopy(scanIndex + 2);
            readers.add(decoder.createScanReader(shallowCopy, readers.size()));

            prev = scanIndex + 2;
        } while (true);

        Assert.assertEquals(10, readers.size());

        int[] progressiveBuffer = new int[decoder.width * decoder.height * 3];

        int blocksWidth = ((decoder.width % 8 != 0) ? (int) (decoder.width / 8.0 + 1) * 8 : decoder.width) / 8;
        int blocksHeight = ((decoder.height % 8 != 0) ? (int) (decoder.height / 8.0 + 1) * 8 : decoder.height) / 8;

        int blocksCount = blocksWidth * blocksHeight;

        int readersCount = readers.size();
        int sh = decoder.components[0].samplingFactorHorizontal;
        int sv = decoder.components[0].samplingFactorVertical;

        JpegDecoder.DecodedData[] idctsExpected = new JpegDecoder.DecodedData[sh * sv + 2];
        JpegDecoder.DecodedData[] idctsActual = new JpegDecoder.DecodedData[sh * sv + 2];
        for (int i = 0; i < idctsExpected.length; i++) {
            idctsExpected[i] = new JpegDecoder.DecodedData();
            idctsActual[i] = new JpegDecoder.DecodedData();
        }

        JpegDecoder.MCU mcu = new JpegDecoder.MCU(sh * sv + 2);
//        int readersCount = 10;

//        for (int i = 0; i < blocksCount; i++) {
//            int x = i % blocksWidth;
//            int y = i / blocksWidth;
//            mcu.clear();
//            for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
//                JpegDecoder.ScanReader reader = readers.get(scanIndex);
////                reader.processMCU(i, progressiveBuffer);
//                reader.processMCU(mcu, i);
////                assertMCUEquals(scansResults[scanIndex], progressiveBuffer, x, y, decoder.width, scanIndex);
//                assertMCUEquals(scansResults[scanIndex], mcu, x, y, decoder.width, scanIndex);
//            }
//
//            decoder.processIdct(scansResults[scansResults.length - 1], idctsExpected, sh, sv, y, x);
//            decoder.processIdct(mcu, idctsActual);
//
////            System.out.println(Arrays.toString(mcu.coeffs[0]));
////            System.out.println(Arrays.toString(idctsActual[0].base));
//
//            for (int j = 0; j < idctsActual.length; j++) {
//                Assert.assertArrayEquals(idctsExpected[j].base, idctsActual[j].base, 0.001f);
//            }
//        }

        for (int y = 0; y < blocksHeight; y += sv) {
            for (int x = 0; x < blocksWidth; x += sh) {
                int i = y * blocksWidth + x;
                mcu.clear();
                for (int scanIndex = 0; scanIndex < readersCount; scanIndex++) {
                    JpegDecoder.ScanReader reader = readers.get(scanIndex);
//                reader.processMCU(i, progressiveBuffer);
                    reader.processMCU(mcu, i, x, y);
//                assertMCUEquals(scansResults[scanIndex], progressiveBuffer, x, y, decoder.width, scanIndex);
                    assertMCUEquals(scansResults[scanIndex], mcu, x, y, decoder.width, scanIndex, sh, sv);
                }

//                decoder.processIdct(scansResults[scansResults.length - 1], idctsExpected, sh, sv, y, x);
                decoder.processIdct(mcu, idctsActual, sh, sv);

//            System.out.println(Arrays.toString(mcu.coeffs[0]));
//            System.out.println(Arrays.toString(idctsActual[0].base));

                for (int j = 0; j < idctsActual.length; j++) {
                    if (!Arrays.equals(idctsExpected[j].data, idctsActual[j].data))
                        Assert.assertArrayEquals("Assertion failed for  at mcu " + x + "x" + y + " (" + j + ")", idctsExpected[j].data, idctsActual[j].data, 0.001f);
                }
            }
        }

//        Assert.assertArrayEquals(scansResults[scansResults.length-1], progressiveBuffer);


//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 1, decodeImageIO("/image/test_gradient.png")));
//        ImageTools.saveJPG(decodeImageIO(path), "out.jpg",90);
//        ImageTools.savePNG(decode(path), "out.png");
    }

    public void assertMCUEquals(int[] expected, int[] actual, int x, int y, int width, int scan) {
        for (int component = 0; component < 3; component++) {
            for (int yy = 0; yy < 8; yy++) {
                for (int xx = 0; xx < 8; xx++) {
                    int position = ((x * 8) + xx + (y * 8 + yy) * width) * 3;
                    if (expected[position + component] != actual[position + component])
                        Assert.assertEquals("Assertion failed for scan " + scan + " component " + component + " at mcu " + x + "x" + y + " (" + xx + "x" + yy + ")", expected[position + component], actual[position + component]);
                }
            }
        }
    }

    public void assertMCUEquals(int[] expected, JpegDecoder.MCU actual, int x, int y, int width, int scan, int sh, int sv) {
        for (int component = 0; component < 3; component++) {
            for (int yy = 0; yy < 8; yy++) {
                for (int xx = 0; xx < 8; xx++) {
                    if (component == 0) {
                        for (int sy = 0; sy < sv; sy++) {
                            for (int sx = 0; sx < sh; sx++) {
                                int mcuIndex = sy * sv + sx;
                                int position = (((x + sx) * 8) + xx + ((y + sy) * 8 + yy) * width) * 3;
                                if (expected[position + component] != actual.coeffs[mcuIndex][xx + (yy << 3)])
                                    Assert.assertEquals("Assertion failed for scan " + scan + " component " + component + " at mcu " + x + "x" + y + " (" + xx + "x" + yy + ")", expected[position + component], actual.coeffs[component][xx + (yy << 3)]);
                            }
                        }
                    } else {
                        int mcuIndex = component + (sh * sv - 1);
                        int position = ((x * 8) + xx + (y * 8 + yy) * width) * 3;
                        if (expected[position + component] != actual.coeffs[mcuIndex][xx + (yy << 3)])
                            Assert.assertEquals("Assertion failed for scan " + scan + " component " + component + " at mcu " + x + "x" + y + " (" + xx + "x" + yy + ")", expected[position + component], actual.coeffs[component][xx + (yy << 3)]);
                    }
                }
            }
        }
    }

    @Test
    public void test_idct() {
        double[] input = new double[]{68.62499, -4.1611195, -0.32664073, -1.0288911, 0.37499997, 0.09821187, -0.06764951, 0.068974845, -3.2942195, -0.48096985, 0.22653185, 0.20387328, 0.0, 0.40867132, 0.18766513, 0.14350629, -1.1432426, 0.4530637, 0.21338834, 1.7283998, -0.65328145, -0.51328, 0.26516503, 0.0, 2.0577822, 0.40774655, 0.0, -0.6913417, -0.14698444, 0.3464548, 0.15909481, -0.12165876, 0.74999994, 1.2136598, 0.0, 0.73492223, -0.87499994, -0.39284748, -0.20294854, -0.103462264, 0.0, 0.27244756, -0.25664, 0.11548494, 0.0, 0.0, 0.21260752, 0.027096596, 0.0, 0.37533027, 0.088388346, 0.23864222, 0.20294854, 0.0, -0.03661165, -0.018664459, -0.034487423, 0.14350629, 0.045059986, -0.040552918, 0.0, -0.05419319, -0.037328918, -0.009515058};
        double[] output = new double[]{65.69709, 65.3215, 66.631874, 64.106705, 66.68014, 71.81023, 68.46896, 66.9701, 57.180714, 55.033905, 63.230778, 63.2281, 65.76518, 65.01501, 71.52111, 72.81823, 60.701332, 57.124798, 61.27229, 64.92299, 67.66154, 61.048187, 67.451805, 71.08273, 62.13749, 63.692574, 68.95904, 69.59598, 70.33639, 68.9551, 72.844666, 72.76989, 64.876434, 73.30988, 71.36231, 75.92923, 70.52294, 69.21612, 76.20354, 77.58021, 68.029976, 65.47643, 72.06256, 77.841324, 72.068794, 70.69533, 75.57244, 80.5641, 63.9222, 65.01456, 71.5555, 71.182724, 71.859985, 71.81582, 72.10937, 77.169945, 66.11782, 72.002304, 64.18163, 65.62727, 72.97918, 73.739746, 71.3004, 70.07305};

        int[] coeffs = new int[]{549, -24, -19, -7, -2, -2, -7, 1, 2, 14, 6, 2, 1, 1, 3, 1, 0, 9, 0, 7, 0, 0, 2, 0, -4, -4, 3, -1, 2, 2, -4, -1, 5, -2, 4, -1, 3, 1, 1, -7, 3, 3, 3, 0, 2, -4, 0, 3, 1, -1, 3, 0, -3, -3, -3, 4, 0, 0, -2, -1, 1, -1, -2, -1};

        int[] inputInt = new int[input.length];
        for (int i = 0; i < inputInt.length; i++) {
            inputInt[i] = (int) input[i];
        }
        float[] inputFloat = new float[input.length];
        for (int i = 0; i < inputInt.length; i++) {
            inputFloat[i] = (float) input[i];
        }
        float[] outputFloat = new float[input.length];
        for (int i = 0; i < inputInt.length; i++) {
            outputFloat[i] = (float) output[i];
        }


        System.out.println(Arrays.toString(output));
//        System.out.println(Arrays.toString(idctFloat.get()));

//        System.out.println(Arrays.toString(idct(inputFloat)));
//        System.out.println(Arrays.toString(idct(toFloats(coeffs))));
//        System.out.println(Arrays.toString(aanIDCT(inputFloat)));

        {
            float[] block = toFloats(coeffs);
            byte[] m =new byte[64];
            Arrays.fill(m, (byte) 1);
            float[] prescale = IDCT.scaleDequantizationMatrix(m);

            for (int i = 0; i < 64; i++) {
                block[i] = block[i] * prescale[i];
            }
            IDCT.inverseDCT8x8(block);
            System.out.println(Arrays.toString(block));
        }

        IDCT.inverseDCT8x8(inputFloat);
        System.out.println(Arrays.toString(inputFloat));
        Assert.assertArrayEquals(outputFloat, inputFloat, 0.0001f);
    }


    static float[] toFloats(int[] ints) {
        float[] floats = new float[ints.length];
        for (int i = 0; i < ints.length; i++) {
            floats[i] = ints[i];
        }
        return floats;
    }

}
