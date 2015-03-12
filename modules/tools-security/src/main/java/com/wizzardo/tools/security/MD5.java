/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

import com.wizzardo.tools.misc.UncheckedThrow;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Moxa
 */
public class MD5 extends Hash {

    @Override
    protected MessageDigest init() {
        try {
            return MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException ex) {
            throw UncheckedThrow.rethrow(ex);
        }
    }

    @Override
    protected int hexStringLength() {
        return 32;
    }

    public static MD5 create() {
        return new MD5();
    }
}
