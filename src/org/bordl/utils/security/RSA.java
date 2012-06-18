package org.bordl.utils.security;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

public class RSA {

    private final static BigInteger one = new BigInteger("1");
    private final static SecureRandom random = new SecureRandom();
    private BigInteger privateKey;
    private BigInteger publicKey;
    private BigInteger modulus;
    private int n;

    // generate an N-bit (roughly) public and private key
    public RSA(int n) {
        this.n = n;
        BigInteger p = BigInteger.probablePrime(n / 2, random);
        BigInteger q = BigInteger.probablePrime(n / 2, random);
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
        if (message.length > n / 8 - 11) {
            throw new IllegalArgumentException("The RSA algorithm can only encrypt data that has a maximum byte length of the RSA key length in bits divided with 8 minus 11 padding bytes");
        }
        return hexStringToByteArray(new BigInteger(1, message).modPow(publicKey, modulus).toString(16));
    }

    public byte[] decrypt(byte[] encrypted) {
        return hexStringToByteArray(new BigInteger(1, encrypted).modPow(privateKey, modulus).toString(16));
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

    public int getN() {
        return n;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }
}
