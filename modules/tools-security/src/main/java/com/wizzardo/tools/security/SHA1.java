/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

/**
 * @author Moxa
 */
public class SHA1 extends Hash {
    public SHA1() {
        super("SHA-1");
    }

    @Override
    protected int hexStringLength() {
        return 40;
    }

    public static SHA1 create() {
        return new SHA1();
    }
}
