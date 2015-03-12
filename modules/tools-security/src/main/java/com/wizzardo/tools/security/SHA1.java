/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

import com.wizzardo.tools.misc.UncheckedThrow;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Moxa
 */
public class SHA1 extends Hash {

    @Override
    protected MessageDigest init() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw UncheckedThrow.rethrow(ex);
        }
    }

    @Override
    protected int hexStringLength() {
        return 40;
    }

    public static SHA1 create() {
        return new SHA1();
    }
}
