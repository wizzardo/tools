package com.wizzardo.tools.image;

import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class JpegDecoderBaselineTest {

    static BufferedImage decodeImageIO(String path) throws IOException {
        return ImageIO.read(JpegDecoderBaselineTest.class.getResourceAsStream(path));
    }

    static BufferedImage decode(String path) throws IOException {
        return new JpegDecoder().read(JpegDecoderBaselineTest.class.getResourceAsStream(path));
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
    public void baseline_jpeg_444() throws IOException {
        String path = "/image/test.jpg";
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test.png")));
    }

    @Test
    public void baseline_jpeg_420() throws IOException {
        String path = "/image/test_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test.png")));
    }

    @Test
    public void baseline_jpeg_422h() throws IOException {
        String path = "/image/test_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test.png")));
    }

    @Test
    public void baseline_jpeg_422v() throws IOException {
        String path = "/image/test_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        assertLower(100, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test.png")));
    }

    @Test
    public void baseline_jpeg_420_2() throws IOException {
        String path = "/image/test_subsampling_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(3000, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void baseline_jpeg_422h_2() throws IOException {
        String path = "/image/test_subsampling_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(1200, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void baseline_jpeg_422v_2() throws IOException {
        String path = "/image/test_subsampling_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 3, decodeImageIO("/image/test_subsampling.png")));
    }

    @Test
    public void baseline_jpeg_420_3() throws IOException {
        String path = "/image/test_gradient_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void baseline_jpeg_422h_3() throws IOException {
        String path = "/image/test_gradient_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void baseline_jpeg_422v_3() throws IOException {
        String path = "/image/test_gradient_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient.png")));
    }

    @Test
    public void baseline_jpeg_420_4() throws IOException {
        String path = "/image/test_gradient_small_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient_small.png")));
    }

    @Test
    public void baseline_jpeg_422h_4() throws IOException {
        String path = "/image/test_gradient_small_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient_small.png")));
    }

    @Test
    public void baseline_jpeg_422v_4() throws IOException {
        String path = "/image/test_gradient_small_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",90);
        Assert.assertEquals(0, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_gradient_small.png")));
    }

    @Test
    public void baseline_jpeg_420_5() throws IOException {
        String path = "/image/test_subsampling_2_420.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(1500, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void baseline_jpeg_422h_5() throws IOException {
        String path = "/image/test_subsampling_2_422h.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(768, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void baseline_jpeg_422v_5() throws IOException {
        String path = "/image/test_subsampling_2_422v.jpg";
//        ImageTools.saveJPG(decode(path), "out.jpg",100);
        assertLower(128, compare(decodeImageIO(path), decode(path), 2, decodeImageIO("/image/test_subsampling_2.png")));
    }

    @Test
    public void imageio_sanity_jpeg_444() throws IOException {
        Assert.assertEquals(0, compare(decodeImageIO("/image/test.png"), decodeImageIO("/image/test.jpg"), 4));
    }

}
