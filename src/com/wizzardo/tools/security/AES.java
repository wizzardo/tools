package com.wizzardo.tools.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author moxa
 */
public class AES {

    private Cipher ecipher;
    private Cipher dcipher;
    private SecretKey key;

    public static SecretKey generateKey() {
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /** 
     * 16 chars max,  128-bit encription
     **/
    public static SecretKey generateKey(String key) {
        return generateKey(key.getBytes());
    }

    public static SecretKey generateKeyAsMD5(String key) {
        return generateKey(MD5.getMD5(key.getBytes()));
    }

    /** 
     * 16 bytes max,  128-bit encription
     **/
    public static SecretKey generateKey(final byte[] key) {
        return new SecretKey() {

            private byte[] k;

            {
                if (key.length == 16) {
                    k = key;
                } else {
                    k = new byte[16];
                    System.arraycopy(key, 0, k, 0, key.length < 16 ? key.length : 16);
                }
            }

            @Override
            public String getAlgorithm() {
                return "AES";
            }

            @Override
            public String getFormat() {
                return "RAW";
            }

            @Override
            public byte[] getEncoded() {
                return k;
            }
        };
    }

    public AES() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128);
            key = kg.generateKey();
            init();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /** 
     * 16 chars max,  128-bit encription
     **/
    public AES(SecretKey key) {
        try {
            this.key = key;
            init();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public AES(byte[] key) {
        this(generateKey(key));
    }

    /** 
     * 16 chars max,  128-bit encription
     **/
    public AES(String key) {
        this(generateKey(key.getBytes()));
    }

    public byte[] toTransfer() {
        return toTransfer(key);
    }

    public static byte[] toTransfer(SecretKey key) {
        byte[] b = new byte[32];
        System.arraycopy(key.getEncoded(), 0, b, 0, 16);
        System.arraycopy(MD5.getMD5(key.getEncoded()), 0, b, 16, 16);
        return b;
    }

    private void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] iv = key.getEncoded();
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        ecipher = Cipher.getInstance("AES/CFB8/NoPadding");
//            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        dcipher = Cipher.getInstance("AES/CFB8/NoPadding");
        ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
    }

    public byte[] decrypt(byte[] b) {
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ByteArrayOutputStream out = new ByteArrayOutputStream(b.length);
        decrypt(in, out);
        return out.toByteArray();
    }

    public byte[] encrypt(byte[] b) {
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        ByteArrayOutputStream out = new ByteArrayOutputStream(b.length);
        encrypt(in, out);
        return out.toByteArray();
    }

    public void decrypt(InputStream in, OutputStream out) {
        try {
            CipherInputStream inc = new CipherInputStream(in, dcipher);
            int r = 0;
            byte[] b = new byte[10240];
            while ((r = inc.read(b)) != -1) {
                out.write(b, 0, r);
                out.flush();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encrypt(InputStream in, OutputStream out) {
        try {
            out = new CipherOutputStream(out, ecipher);
            int r = 0;
            byte[] b = new byte[10240];
            while ((r = in.read(b)) != -1) {
                out.write(b, 0, r);
                out.flush();
            }
            out.write(ecipher.doFinal());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
