/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 *
 * @author Moxa
 */
public class ImageUtils {

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

    public static void saveJPG(BufferedImage im, String file, float quality) throws IOException {
        saveJPG(im, new FileOutputStream(file), quality);
    }

    public static void saveJPG(BufferedImage im, OutputStream out, float quality) throws IOException {
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);
        IIOImage image = new IIOImage(im, null, null);
        ImageOutputStream output = new MemoryCacheImageOutputStream(out);
        writer.setOutput(output);
        writer.write(null, image, iwp);
        writer.dispose();
        output.close();
    }

    public static void savePNG(BufferedImage im, String file) throws IOException {
        ImageIO.write(im, "png", new File(file));
    }

    public static void savePNG(BufferedImage im, OutputStream out) throws IOException {
        ImageIO.write(im, "png", out);
    }

    public static BufferedImage crop(BufferedImage src, int x1, int y1, int x2, int y2) {
        int x = x1 > x2 ? x2 : x1;
        int y = y1 > y2 ? y2 : y1;
        return src.getSubimage(x, y, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public static BufferedImage getImage(File f) throws IOException {
        return ImageIO.read(f);
    }

    public static BufferedImage getImage(String f) throws IOException {
        return ImageIO.read(new File(f));
    }

    public static BufferedImage read(File f) throws IOException {
        return ImageIO.read(f);
    }

    public static BufferedImage read(String f) throws IOException {
        return ImageIO.read(new File(f));
    }

    public static BufferedImage read(InputStream in) throws IOException {
        return ImageIO.read(in);
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
        int left = x > 0 ? x - 1 : 0;
        clear = true;
        for (x = src.getWidth() - 1; x >= 0 && clear; x--) {
            int y = 0;
            while (clear && y < src.getHeight()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                y++;
            }
        }
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
        int top = y > 0 ? y - 1 : 0;
        clear = true;
        for (y = src.getHeight() - 1; y >= 0 && clear; y--) {
            int x = 0;
            while (clear && x < src.getWidth()) {
                clear = diff(color, src.getRGB(x, y)) <= diff;
                x++;
            }
        }
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

    public static BufferedImage resize(BufferedImage im, double scale) {
        BufferedImage img = new BufferedImage((int) (im.getWidth() * scale), (int) (im.getHeight() * scale), BufferedImage.TYPE_INT_ARGB);
        Image imgg = im.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_SMOOTH);
        Graphics2D gr = (Graphics2D) img.getGraphics();
        gr.drawImage(imgg, 0, 0, null);
        return img;
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
}
