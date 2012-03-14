/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

/**
 *
 * @author Moxa
 */
public class MD5 {

    public static String getMD5AsString(byte[] b) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(b, 0, b.length);
            return md5BytesToString(md5.digest());
        } catch (NoSuchAlgorithmException ex) {
        }
        return "";
    }

    public static String getMD5AsString(String s) {
        return getMD5AsString(s.getBytes());
    }

    public static byte[] getMD5(byte[] b) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            md5.update(b, 0, b.length);
            return md5.digest();
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

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

    public static String md5BytesToString(byte[] b) {
        String str = new BigInteger(1, b).toString(16);
        while (str.length() < 32) {
            str = "0" + str;
        }
        return str;
    }

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

    public static boolean check(String value, String md5) {
        return getMD5AsString(value.getBytes()).equalsIgnoreCase(md5);
    }

    public static boolean check(byte[] b, byte[] md5) {
        return Arrays.equals(getMD5(b), md5);
    }
}
