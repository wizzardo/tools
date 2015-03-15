package com.wizzardo.tools.security;

import com.wizzardo.tools.misc.UncheckedThrow;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by wizzardo on 12.03.15.
 */
public abstract class Hash {
    private static final Charset UTF8 = Charset.forName("utf-8");
    private MessageDigest hash;

    public Hash(String name) {
        hash = init(name);
    }

    protected MessageDigest init(String name) {
        try {
            return MessageDigest.getInstance(name);
        } catch (NoSuchAlgorithmException ex) {
            throw UncheckedThrow.rethrow(ex);
        }
    }

    protected abstract int hexStringLength();

    public Hash update(byte[] b, int offset, int length) {
        hash.update(b, offset, length);
        return this;
    }

    public Hash reset() {
        hash.reset();
        return this;
    }

    @Override
    public String toString() {
        return asString();
    }

    public Hash update(byte[] b) {
        return update(b, 0, b.length);
    }

    /**
     * update hash with getBytes(UTF8)
     */
    public Hash update(String s) {
        return update(s.getBytes(UTF8));
    }

    public Hash update(byte b) {
        hash.update(b);
        return this;
    }

    /**
     * @return hash as hex string
     */
    public String asString() {
        return toHexString(asBytes());
    }

    /**
     * @return hash as bytes
     */
    public byte[] asBytes() {
        return hash.digest();
    }

    public Hash update(InputStream in) throws IOException {
        byte[] b = new byte[10240];
        int r;
        while ((r = in.read(b)) != -1) {
            update(b, 0, r);
        }
        return this;
    }

    private String toHexString(byte[] b) {
        String str = new BigInteger(1, b).toString(16);
        while (str.length() < hexStringLength()) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * get hash from given string and check for equals it with hashString
     */
    public boolean check(String value, String hashString) {
        return update(value.getBytes()).asString().equalsIgnoreCase(hashString);
    }

    /**
     * get hash from given bytes and check for equals it with hashBytes
     */
    public boolean check(byte[] b, byte[] hashBytes) {
        return Arrays.equals(update(b).asBytes(), hashBytes);
    }
}
