/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.security;

import java.security.SecureRandom;

/**
 *
 * @author Moxa
 */
public class UUID {

    /*
     * The random number generator used by this class to create random
     * based UUIDs.
     */
    private static volatile SecureRandom numberGenerator = null;

    /**
     * Static factory to retrieve a type 4 (pseudo randomly generated) UUID.
     *
     * The <code>UUID</code> is generated using a cryptographically strong
     * pseudo random number generator.
     *
     * Source code was got from sources of java 
     * @return  a randomly generated UUID.
     */
    public static byte[] randomUUID() {
        SecureRandom ng = numberGenerator;
        if (ng == null) {
            numberGenerator = ng = new SecureRandom();
        }
        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version        */
        randomBytes[6] |= 0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant        */
        randomBytes[8] |= 0x80;  /* set to IETF variant  */
        return randomBytes;
    }

    public static java.util.UUID toJavaUUID(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new java.util.UUID(msb, lsb);
    }
}
