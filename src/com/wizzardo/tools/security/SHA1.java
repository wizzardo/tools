/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

import com.wizzardo.tools.WrappedException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Moxa
 */
public class SHA1 {

    private MessageDigest sha1;

    public SHA1() {
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new WrappedException(ex);
        }
    }

    public void update(byte[] b, int offset, int length) {
        sha1.update(b, 0, length);
    }

    public void reset() {
        sha1.reset();
    }

    @Override
    public String toString() {
        return sha1BytesToString(sha1.digest());
    }

    /**
     * @return SHA1 hash as string from given bytes
     */
    public static String getSHA1AsString(byte[] b) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(b, 0, b.length);
            return sha1BytesToString(sha1.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new WrappedException(ex);
        }
    }

    /**
     * @return SHA1 hash as string from given string
     */
    public static String getSHA1AsString(String s) {
        return getSHA1AsString(s.getBytes());
    }

    /**
     * @return SHA1 hash from given bytes
     */
    public static byte[] getSHA1(byte[] b) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update(b, 0, b.length);
            return sha1.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new WrappedException(ex);
        }
    }

    /**
     * @return SHA1 hash as string from given stream
     */
    public static String getSHA1AsString(InputStream in) throws IOException {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] b = new byte[10240];
            int r = 0;
            while ((r = in.read(b)) != -1) {
                sha1.update(b, 0, r);
            }
            return sha1BytesToString(sha1.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new WrappedException(ex);
        }
    }

    /**
     * @return string representation of SHA1 hash
     */
    public static String sha1BytesToString(byte[] b) {
        String str = new BigInteger(1, b).toString(16);
        while (str.length() < 40) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * @return SHA1 hash as bytes from given bytes
     */
    public static byte[] getSHA1(InputStream in) throws IOException {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] b = new byte[10240];
            int r = 0;
            while ((r = in.read(b)) != -1) {
                sha1.update(b, 0, r);
            }
            return sha1.digest();
        } catch (NoSuchAlgorithmException ex) {
            throw new WrappedException(ex);
        }
    }

    /**
     * get SHA1 from given string and check for equals it with sha1String
     */
    public static boolean check(String value, String sha1String) {
        return getSHA1AsString(value.getBytes()).equalsIgnoreCase(sha1String);
    }

    /**
     * get SHA1 from given bytes and check for equals it with sha1Bytes
     */
    public static boolean check(byte[] b, byte[] sha1Bytes) {
        return Arrays.equals(getSHA1(b), sha1Bytes);
    }

}
