/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moxa
 */
public class MD5 {

    private MessageDigest md5;

    public MD5() {
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void update(byte[] b, int offset, int length) {
        md5.update(b, 0, length);
    }

    public void reset() {
        md5.reset();
    }

    @Override
    public String toString() {
        return md5BytesToString(md5.digest());
    }

    /** 
    @return MD5 hash as string from given bytes
     */
    public static String getMD5AsString(byte[] b) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(b, 0, b.length);
            return md5BytesToString(md5.digest());
        } catch (NoSuchAlgorithmException ex) {
        }
        return "";
    }

    /** 
    @return MD5 hash as string from given string
     */
    public static String getMD5AsString(String s) {
        return getMD5AsString(s.getBytes());
    }

    /** 
    @return MD5 hash from given bytes
     */
    public static byte[] getMD5(byte[] b) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(b, 0, b.length);
            return md5.digest();
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    /** 
    @return MD5 hash as string from given stream
     */
    public static String getMD5AsString(InputStream in) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] b = new byte[102400];
            int r = 0;
            while ((r = in.read(b)) != -1) {
                md5.update(b, 0, r);
            }
            return md5BytesToString(md5.digest());
        } catch (NoSuchAlgorithmException ex) {
        }
        return "";
    }

    /** 
    @return string representation of MD5 hash 
     */
    public static String md5BytesToString(byte[] b) {
        String str = new BigInteger(1, b).toString(16);
        while (str.length() < 32) {
            str = "0" + str;
        }
        return str;
    }

    /** 
    @return MD5 hash as bytes from given bytes
     */
    public static byte[] getMD5(InputStream in) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] b = new byte[102400];
            int r = 0;
            while ((r = in.read(b)) != -1) {
                md5.update(b, 0, r);
            }
            return md5.digest();
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    /** 
    get MD5 from given string and check for equals it with md5String
     */
    public static boolean check(String value, String md5String) {
        return getMD5AsString(value.getBytes()).equalsIgnoreCase(md5String);
    }

    /** 
    get MD5 from given bytes and check for equals it with md5Bytes
     */
    public static boolean check(byte[] b, byte[] md5Bytes) {
        return Arrays.equals(getMD5(b), md5Bytes);
    }
}
