/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author moxa
 */
public class RobotTools extends Robot {

    private int rand;
    private int add;
    private int constant;
    private boolean debug = false;
    private Toolkit t = Toolkit.getDefaultToolkit();

    public RobotTools() throws AWTException {
    }

    public void setDebugModeOn() {
        debug = true;
    }

    public void setDebugModeOff() {
        debug = false;
    }

    public void ctrlPlus(int key) {
        setAutoDelay(30);
        keyPress(KeyEvent.VK_CONTROL);
        keyPress(key);
        if (debug) {
            System.out.println("ctrl + " + (char) key);
            System.out.println(new Date());
            System.out.println("");
        }
        keyRelease(key);
        keyRelease(KeyEvent.VK_CONTROL);
        updateDelay();
    }

    public void setClipboard(String s) {
        t.getSystemClipboard().setContents(new StringSelection(s), null);
    }

    public String getClipboard() {
        String s = null;
        try {
            s = (String) t.getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Robot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    public void altPlus(int key) {
        setAutoDelay(30);
        keyPress(KeyEvent.VK_ALT);
        keyPress(key);
        if (debug) {
            System.out.println("alt + " + (char) key);
            System.out.println(new Date());
            System.out.println("");
        }
        keyRelease(key);
        keyRelease(KeyEvent.VK_ALT);
        updateDelay();
    }

    public void ctrlPlusC() {
        ctrlPlus(KeyEvent.VK_C);
    }

    public void ctrlPlusV() {
        ctrlPlus(KeyEvent.VK_V);
    }

    public void mouseShift(int xFrom, int yFrom, int xTo, int yTo) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        mouseMove(xFrom, yFrom);
        mousePress(InputEvent.BUTTON1_MASK);
        mouseMove(xTo, yTo);
        setAutoDelay(30);
        mouseRelease(InputEvent.BUTTON1_MASK);
        if (debug) {
            System.out.println("mouseShift");
            System.out.println(new Date());
            System.out.println("");
        }
        mouseMove(pi.getLocation().x, pi.getLocation().y);
        updateDelay();
    }

    public void mouseShift(Point from, Point to) {
        mouseShift(from.x, from.y, to.x, to.y);
    }

    public void mouseClickLeft(int x, int y) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        mouseMove(x, y);
        mouseClick(InputEvent.BUTTON1_MASK);
        if (debug) {
            System.out.println("mouseClickLeft");
            System.out.println(new Date());
            System.out.println("");
        }
        mouseMove(pi.getLocation().x, pi.getLocation().y);
    }

    public void mouseClickLeft(Point p) {
        mouseClickLeft(p.x, p.y);
    }

    public void mouseClickRight(int x, int y) {
        PointerInfo pi = MouseInfo.getPointerInfo();
        mouseMove(x, y);
        mouseClick(InputEvent.BUTTON3_MASK);
        if (debug) {
            System.out.println("mouseClickRight");
            System.out.println(new Date());
            System.out.println("");
        }
        mouseMove(pi.getLocation().x, pi.getLocation().y);
    }

    public void mouseClickRight(Point p) {
        mouseClickRight(p.x, p.y);
    }

    private void mouseClick(int button) {
        setAutoDelay(10);
        mousePress(button);
        mouseRelease(button);
        updateDelay();
    }

    public void setDelay(int rand, int add) {
        this.rand = rand;
        this.add = add;
        if (rand == 0 & add == 0) {
            this.constant = 0;
        } else {
            this.constant = 100;
        }
        setAutoDelay((int) (constant + add + Math.round(Math.random() * rand)));
    }

    private void updateDelay() {
        setAutoDelay((int) (constant + add + Math.round(Math.random() * rand)));
    }

    public synchronized Color getPixelColor(Point p) {
        return getPixelColor(p.x, p.y);
    }
}
