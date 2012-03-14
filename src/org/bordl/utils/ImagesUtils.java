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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author Moxa
 */
public class ImagesUtils {

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
        Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iter.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwp.setCompressionQuality(quality);
        IIOImage image = new IIOImage(im, null, null);
        FileImageOutputStream output = new FileImageOutputStream(new File(file));
        writer.setOutput(output);
        writer.write(null, image, iwp);
        writer.dispose();
    }

    public static void savePNG(BufferedImage im, String file) throws IOException {
        ImageIO.write(im, "png", new File(file));
    }

    public static BufferedImage crop(BufferedImage src, int x1, int y1, int x2, int y2) {
        int x = x1 > x2 ? x2 : x1;
        int y = y1 > y2 ? y2 : y1;
        return src.getSubimage(x, y, Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    public static BufferedImage getImage(File f) throws IOException {
        return ImageIO.read(f);
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

    public static BufferedImage resize(BufferedImage im, double scale) {
        BufferedImage img = new BufferedImage((int) (im.getWidth() * scale), (int) (im.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
        Image imgg = im.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_SMOOTH);
        Graphics2D gr = (Graphics2D) img.getGraphics();
        gr.drawImage(imgg, 0, 0, null);
        return img;
    }
}
