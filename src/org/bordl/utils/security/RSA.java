/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.security;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RSA {

    private final static BigInteger one = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();
    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;

    // generate an N-bit (roughly) public and private key
    public RSA(int N) {
        BigInteger p = BigInteger.probablePrime(N / 2, random);
        BigInteger q = BigInteger.probablePrime(N / 2, random);
        BigInteger phi = (p.subtract(one)).multiply(q.subtract(one));

        modulus = p.multiply(q);
        publicKey = new BigInteger("65537");     // common value in practice = 2^16 + 1
        privateKey = publicKey.modInverse(phi);
    }

    public String getStringToTransfer() {
        String s = publicKey.toString() + "\n" + modulus;
        s = s + "\n" + MD5.getMD5AsString(s.getBytes());
        return s;
    }

    public RSA(String publicKey, String modulus) {
        this.publicKey = new BigInteger(publicKey);
        this.modulus = new BigInteger(modulus);
    }

    public BigInteger encrypt(BigInteger message) {
        return message.modPow(publicKey, modulus);
    }

    public BigInteger decrypt(BigInteger encrypted) {
        return encrypted.modPow(privateKey, modulus);
    }

    public byte[] encrypt(byte[] message) {
        return hexStringToByteArray(new BigInteger(1, message).modPow(publicKey, modulus).toString(16));
    }

    public byte[] decrypt(byte[] encrypted, int lenght) {
        return hexStringToByteArray(new BigInteger(1, encrypted).modPow(privateKey, modulus).toString(16), lenght);
    }

    public byte[] decrypt(byte[] encrypted, int offset, int arrLength, int length) {
        return hexStringToByteArray(new BigInteger(1, Arrays.copyOfRange(encrypted, offset, arrLength + offset)).modPow(privateKey, modulus).toString(16), length);
    }

    public static byte[] hexStringToByteArray(String hex, int lenght) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        byte[] b = new byte[lenght];
        int k = b.length - 1;
        for (int i = hex.length() - 2; i >= 0 && k >= 0; i -= 2) {
            b[k--] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return b;
    }

    public static byte[] hexStringToByteArray(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            b[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return b;
    }

    @Override
    public String toString() {
        String s = "";
        s += "public  = " + publicKey + "\n";
        s += "private = " + privateKey + "\n";
        s += "modulus = " + modulus;
        return s;
    }

    public static void main(String[] args) {
//        int N = Integer.parseInt(args[0]);
        int N = 1024;
        RSA key = new RSA(N);
        System.out.println(key);

        // create random message, encrypt and decrypt
        BigInteger message = new BigInteger(N - 1, random);
        String md5 = null;
        try {
            md5 = MD5.getMD5AsString(new FileInputStream("/home/moxa/Техника уклонения.flv"));
            md5 = "0" + md5.substring(1);
            System.out.println("MD5 = " + md5);
            message = new BigInteger(md5, 16);
        } catch (Exception ex) {
            Logger.getLogger(RSA.class.getName()).log(Level.SEVERE, null, ex);
        }
        //// create message by converting string to integer
        // String s = "test";
        // byte[] bytes = s.getBytes();
        // BigInteger message = new BigInteger(s);
//
//        BigInteger encrypt = key.encrypt(message);
//        BigInteger decrypt = key.decrypt(encrypt);
        byte[] b = new byte[]{-120, 125, 114, 100, -4, -18, 121, -99, -60, -41, 47, -13, 113, -110, -121, -81, -60, -92, -35, 91, -24, -45, 102, 99, 108, -25, -104, 84, 107, 39, 22, 99, -45, -1, -128, -95};
        byte[] enc = key.encrypt(b);
        byte[] dec = key.decrypt(enc, 36);

        System.out.println("message   = " + Arrays.toString(b));
        System.out.println("encrpyted = " + Arrays.toString(enc));
        System.out.println("decrypted = " + Arrays.toString(dec));
//        System.out.println("MD5 = " + decrypt.toString(16));

//        System.out.println("======================================");
//        long start, stop, s = 0, k = 0;
//        for (int i = 0; i < 1000; i++) {
//            byte[] m = new BigInteger(random.nextLong() + random.nextLong() + random.nextLong() + random.nextLong() + "").toByteArray();
//            start = System.currentTimeMillis();
//            if (!Arrays.toString(m).equals(Arrays.toString(key.decrypt(key.encrypt(m), m.length)))) {
//                System.out.println(Arrays.toString(m));
//                System.out.println(MD5.md5BytesToString(m));
//                System.out.println(Arrays.toString(key.encrypt(m)));
//                System.out.println(Arrays.toString(key.decrypt(key.encrypt(m), m.length)));
//            } else {
//                stop = System.currentTimeMillis();
//                s += (stop - start);
//                k++;
//            }
//            if (i % 100 == 0) {
//                System.out.println(i);
//            }
//        }
//        System.out.println(s / k);

        System.out.println("end");
    }
}
