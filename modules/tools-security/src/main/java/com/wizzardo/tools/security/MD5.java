/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

/**
 * @author Moxa
 */
public class MD5 extends Hash {
    public MD5() {
        super("md5");
    }

    @Override
    protected int hexStringLength() {
        return 32;
    }

    public static MD5 create() {
        return new MD5();
    }
}
