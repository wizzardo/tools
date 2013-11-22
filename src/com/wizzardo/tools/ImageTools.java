/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.wizzardo.tools.image.JpegEncoder;
import com.wizzardo.tools.image.Lanczos3Filter;
import com.wizzardo.tools.image.ResampleOp;
import com.wizzardo.tools.io.FileTools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.*;

/**
 * @author Moxa
 */
public class ImageTools {
    private static ICC_Profile CMYK_PROFILE;

    public static void setCmykProfile(File iccProfile) throws IOException {
        setCmykProfile(new FileInputStream(iccProfile));
    }

    public static void setCmykProfile(InputStream iccProfile) throws IOException {
        CMYK_PROFILE = ICC_Profile.getInstance(iccProfile);
    }

    public static BufferedImage toGrayScale2(BufferedImage source) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage im = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        op.filter(source, im);
        return im;
    }

    public static BufferedImage toGrayScale(BufferedImage source) {
        BufferedImage im = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        im.getGraphics().drawImage(source, 0, 0, null);
        return im;
    }

    public static void saveJPG(BufferedImage im, String file, int quality) throws IOException {
        saveJPG(im, new FileOutputStream(file), quality);
    }

    public static void saveJPG(BufferedImage im, File file, int quality) throws IOException {
        saveJPG(im, new FileOutputStream(file), quality);
    }

    public static byte[] saveJPGtoBytes(BufferedImage im, int quality) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        saveJPG(im, bytes, quality);
        return bytes.toByteArray();
    }

    public static void saveJPG(BufferedImage im, OutputStream out, int quality) throws IOException {
        JpegEncoder encoder = new JpegEncoder(im, quality, out);
        encoder.Compress();
        out.close();
    }

    public static void savePNG(BufferedImage im, String file) throws IOException {
        ImageIO.write(im, "png", new File(file));
    }

    public static void savePNG(BufferedImage im, File file) throws IOException {
        ImageIO.write(im, "png", file);
    }

    public static void savePNG(BufferedImage im, OutputStream out) throws IOException {
        ImageIO.write(im, "png", out);
    }

    public static byte[] savePNGtoBytes(BufferedImage im) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(im, "png", bytes);
        return bytes.toByteArray();
    }

    public static BufferedImage crop(BufferedImage src, int x1, int y1, int x2, int y2) {
        int x = x1 > x2 ? x2 : x1;
        int y = y1 > y2 ? y2 : y1;
        return src.getSubimage(x, y, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public static BufferedImage read(File f) throws IOException {
        return read(FileTools.bytes(f));
    }

    public static BufferedImage read(byte[] bytes) throws IOException {
        if (CMYK_PROFILE == null)
            return read(new ByteArrayInputStream(bytes));

        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (javax.imageio.IIOException e) {
            return readAndConvertFromCMYK(new ByteArrayInputStream(bytes));
        }
    }

    public static BufferedImage read(InputStream in) throws IOException {
        return ImageIO.read(in);
    }

    public static BufferedImage readAndConvertFromCMYK(InputStream in) throws IOException {
        if (CMYK_PROFILE == null)
            throw new IOException("You need to setup CMYK profile first by method setCmykProfile");
        return readAndConvertFromCMYK(in, CMYK_PROFILE);
    }

    public static BufferedImage readAndConvertFromCMYK(InputStream in, ICC_Profile cmykProfile) throws IOException {
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
        BufferedImage src = decoder.decodeAsBufferedImage();
        WritableRaster srcRaster = src.getRaster();
        //prepare result image
        BufferedImage result = new BufferedImage(srcRaster.getWidth(), srcRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
        WritableRaster resultRaster = result.getRaster();
        //prepare icc profiles
        ColorSpace sRGBColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        //invert k channel
        for (int x = srcRaster.getMinX(); x < srcRaster.getWidth(); x++) {
            for (int y = srcRaster.getMinY(); y < srcRaster.getHeight(); y++) {
                float[] pixel = srcRaster.getPixel(x, y, (float[]) null);
                pixel[3] = 255f - pixel[3];
                srcRaster.setPixel(x, y, pixel);
            }
        }
        //convert
        ColorConvertOp cmykToRgb = new ColorConvertOp(new ICC_ColorSpace(cmykProfile), sRGBColorSpace, null);
        cmykToRgb.filter(srcRaster, resultRaster);
        return result;
    }

    public static BufferedImage read(String f) throws IOException {
        return read(new File(f));
    }

    public static BufferedImage trim(BufferedImage src) {
        return trim(src, 0);
    }

    public static BufferedImage trim(BufferedImage src, int diff) {
        return trimHorizontal(trimVertical(src, diff), diff);
    }

    public static BufferedImage trimHorizontal(BufferedImage src) {
        return trimHorizontal(src, 0);
    }

    public static BufferedImage trimVertical(BufferedImage src) {
        return trimVertical(src, 0);
    }

    public static BufferedImage trimHorizontal(BufferedImage src, int diff) {
        int color = src.getRGB(0, 0);
        boolean clear = true;
        int x;
        for (x = 0; x < src.getWidth() && clear; x++) {
            int y = 0;
            while (clear && y < src.getHeight()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                y++;
            }
        }
        x--;
        int left = x > 0 ? x - 1 : 0;
        clear = true;
        for (x = src.getWidth() - 1; x >= 0 && clear; x--) {
            int y = 0;
            while (clear && y < src.getHeight()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                y++;
            }
        }
        x++;
        int right = x < src.getWidth() ? x + 1 : src.getWidth();
        return crop(src, left, 0, right, src.getHeight());
    }

    public static BufferedImage trimVertical(BufferedImage src, int diff) {
        int color = src.getRGB(0, 0);
        boolean clear = true;
        int y;
        for (y = 0; y < src.getHeight() && clear; y++) {
            int x = 0;
            while (clear && x < src.getWidth()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                x++;
            }
        }
        y--;
        int top = y > 0 ? y - 1 : 0;
        clear = true;
        for (y = src.getHeight() - 1; y >= 0 && clear; y--) {
            int x = 0;
            while (clear && x < src.getWidth()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                x++;
            }
        }
        y++;
        int bottom = y < src.getHeight() ? y + 1 : src.getHeight();
        return crop(src, 0, top, src.getWidth(), bottom);
    }

    public static int alpha(int rgb) {
        return (rgb >> 24) & 0xff;
    }

    public static int red(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    public static int green(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    public static int blue(int rgb) {
        return (rgb >> 0) & 0xFF;
    }

    public static int diff(Color c1, Color c2) {
        return diff(c1.getRGB(), c2.getRGB());
    }

    public static int diff(int c1, int c2) {
        return (int) Math.sqrt(Math.pow(alpha(c1) - alpha(c2), 2)
                + Math.pow(red(c1) - red(c2), 2)
                + Math.pow(green(c1) - green(c2), 2)
                + Math.pow(blue(c1) - blue(c2), 2));
    }

    public static void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {
        // The thick line is in fact a filled polygon
        g.setColor(c);
        int dX = x2 - x1;
        int dY = y2 - y1;
        // line length
        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = (double) (thickness) / (2 * lineLength);

        // The x,y increments from an endpoint needed to create a rectangle...
        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        // Now we can compute the corner points...
        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }

    public static BufferedImage resizeToWidth(BufferedImage im, int width) throws IOException {
        double scale = (width * 1.0) / im.getWidth();
        return resize(im, scale);
    }

    public static BufferedImage resizeToHeight(BufferedImage im, int height) throws IOException {
        double scale = (height * 1.0) / im.getHeight();
        return resize(im, scale);
    }

    public static BufferedImage resizeToFit(BufferedImage im, int width, int height) throws IOException {
        double scale = Math.min((width * 1.0) / im.getWidth(), (height * 1.0) / im.getHeight());
        return resize(im, scale);
    }

    public static BufferedImage resize(BufferedImage im, double scale) {
        int width = (int) (im.getWidth() * scale + 0.5f);
        int height = (int) (im.getHeight() * scale + 0.5f);
        ResampleOp resizeOp = new ResampleOp(new Lanczos3Filter(), width, height);
        return resizeOp.filter(im, null);
    }

    public static BufferedImage applyAlphaMask(BufferedImage src, BufferedImage mask) {
        WritableRaster alpha = mask.getAlphaRaster();
        int[] data = new int[mask.getWidth() * mask.getHeight()];
        alpha.getPixels(0, 0, mask.getWidth(), mask.getHeight(), data);
        if (src.getAlphaRaster() == null) {
            BufferedImage t = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            t.getGraphics().drawImage(src, 0, 0, null);
            src = t;
        }
        src.getAlphaRaster().setPixels(0, 0, mask.getWidth(), mask.getHeight(), data);
        return src;
    }

    public static BufferedImage applyMask(BufferedImage src, BufferedImage mask) {
        if (src.getAlphaRaster() == null) {
            BufferedImage t = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            t.getGraphics().drawImage(src, 0, 0, null);
            src = t;
        }
        int w = Math.min(src.getWidth(), mask.getWidth());
        int h = Math.min(src.getHeight(), mask.getHeight());
        int[] data = mask.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < data.length; i++) {
            data[i] = red(data[i]);
        }
        src.getAlphaRaster().setPixels(0, 0, w, h, data);
        return src;
    }

    public static BufferedImage drawOver(BufferedImage src, BufferedImage img) {
        src.getGraphics().drawImage(img, 0, 0, null);
        return src;
    }

    public static BufferedImage resizeCanvas(BufferedImage src, int width, int height, Color background) {
        return resizeCanvas(src, width, height, background, Position.CENTER);
    }

    public static enum Position {

        LEFT, RIGHT, TOP, BOTTOM, CENTER, LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM;
    }

    public static BufferedImage resizeCanvas(BufferedImage src, int width, int height, Color background, Position p) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        if (background != null) {
            g.setColor(background);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
        }
        int x, y;
        switch (p) {
            case TOP: {
                x = (width - src.getWidth()) / 2;
                y = 0;
                break;
            }
            case BOTTOM: {
                x = (width - src.getWidth()) / 2;
                y = (height - src.getHeight());
                break;
            }
            case LEFT: {
                x = 0;
                y = (height - src.getHeight()) / 2;
                break;
            }
            case RIGHT: {
                x = (width - src.getWidth());
                y = (height - src.getHeight()) / 2;
                break;
            }
            case LEFT_TOP: {
                x = 0;
                y = 0;
                break;
            }
            case LEFT_BOTTOM: {
                x = 0;
                y = (height - src.getHeight());
                break;
            }
            case RIGHT_TOP: {
                x = (width - src.getWidth());
                y = 0;
                break;
            }
            case RIGHT_BOTTOM: {
                x = (width - src.getWidth());
                y = (height - src.getHeight());
                break;
            }
            case CENTER:
            default: {
                x = (width - src.getWidth()) / 2;
                y = (height - src.getHeight()) / 2;
                break;
            }
        }
        g.drawImage(src, x, y, null);
        return img;
    }

    public static BufferedImage colorToAlpha(BufferedImage im, Color c) {
        BufferedImage img = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        int[] temp = new int[4];
        Raster src = im.getRaster();
        WritableRaster result = img.getRaster();
        boolean hasAlpha = im.getColorModel().hasAlpha();
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        for (int x = 0; x < im.getWidth(); x++) {
            for (int y = 0; y < im.getHeight(); y++) {
                colorToAlpha(src.getPixel(x, y, temp), hasAlpha, r, g, b);
                result.setPixel(x, y, temp);
            }
        }
        return img;
    }

    private static void colorToAlpha(int[] c, boolean hasAlpha, int r1, int g1, int b1) {
        int r, g, b, a;
        if (hasAlpha) {
            r = c[0];
            g = c[1];
            b = c[2];
            a = c[3];
        } else {
            a = 255;
            r = c[0];
            g = c[1];
            b = c[2];
        }
        float red, green, blue, alpha;

        if (r1 == 0) {
            red = r;
        } else if (r > r1) {
            red = (r - r1) / (255.0f - r1);
        } else if (r < r1) {
            red = (r1 - r) / (r1 * 1f);
        } else {
            red = 0.0f;
        }

        if (g1 == 0) {
            green = g;
        } else if (g > g1) {
            green = (g - g1) / (255.0f - g1);
        } else if (g < g1) {
            green = (g1 - g) / (g1 * 1f);
        } else {
            green = 0.0f;
        }

        if (b1 == 0) {
            blue = b;
        } else if (b > b1) {
            blue = (b - b1) / (255.0f - b1);
        } else if (b < b1) {
            blue = (b1 - b) / (b1 * 1f);
        } else {
            blue = 0.0f;
        }

        if (red > green) {
            if (red > blue) {
                alpha = red;
            } else {
                alpha = blue;
            }
        } else {
            if (green > blue) {
                alpha = green;
            } else {
                alpha = blue;
            }
        }


        if (alpha < 0.001) {
            c[3] = (int) (alpha * 255);
        }
        r = (int) ((r - r1) / alpha + r1 + 0.5f);
        g = (int) ((g - g1) / alpha + g1 + 0.5f);
        b = (int) ((b - b1) / alpha + b1 + 0.5f);
        a = (int) (a * alpha + 0.5f);

        c[0] = r;
        c[1] = g;
        c[2] = b;
        c[3] = a;
    }

    public static String toString(Color c) {
        return "[" + c.getAlpha() + "," + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "]";
    }
}