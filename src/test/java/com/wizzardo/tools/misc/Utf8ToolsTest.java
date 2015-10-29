package com.wizzardo.tools.misc;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by wizzardo on 30.03.15.
 */
public class Utf8ToolsTest {

    @Test
    public void encode() {
        char[] chars = new char[1];
        Charset utf8 = Charset.forName("UTF-8");
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            chars[0] = i;
            Assert.assertArrayEquals("fails on " + (int) i, String.valueOf(i).getBytes(utf8), UTF8.encode(chars));
        }
    }

    @Test
    public void encode_surrogates() {
        char[] chars = new char[2];
        Charset utf8 = Charset.forName("UTF-8");
        for (char i = '\uD800'; i < '\uDC00'; i++) {
            chars[0] = i;
            for (char j = '\uDC00'; j < '\uE000'; j++) {
                chars[1] = j;
                Assert.assertArrayEquals("fails on " + (int) i + " " + (int) j, new String(chars).getBytes(utf8), UTF8.encode(chars));
            }
        }
    }

    @Test
    public void decode() {
        Charset charset = Charset.forName("UTF-8");
        byte[] bytes;
        char[] chars = new char[2];

        bytes = new byte[1];
        for (int i = 0; i <= 0x7F; i++) {
            bytes[0] = (byte) i;
            int r = UTF8.decode(bytes, 0, bytes.length, chars);
            Assert.assertEquals(new String(bytes, charset), new String(chars, 0, r));
        }

        bytes = new byte[2];
        for (int i = 0; i < 256; i++) {
            bytes[0] = (byte) (i & 0x1f | 0xc0);
            for (int j = 0; j < 256; j++) {
                bytes[1] = (byte) (j & 0x7f | 0x80);
                int r = UTF8.decode(bytes, 0, bytes.length, chars);
                Assert.assertEquals(i + " " + j, new String(bytes, charset), new String(chars, 0, r));
            }
        }

        bytes = new byte[3];
        for (int i = 0; i < 256; i++) {
            bytes[0] = (byte) (i & 0xef | 0xe0);
            for (int j = 32; j < 64; j++) {
                bytes[1] = (byte) (j & 0x7f | 0x80);
                for (int k = 0; k < 64; k++) {
                    bytes[2] = (byte) (k & 0x7f | 0x80);
                    int r = UTF8.decode(bytes, 0, bytes.length, chars);
                    String expected = new String(bytes, charset);
                    if (expected.charAt(0) >= '\uD800' && expected.charAt(0) <= '\uDFFF')
                        Assert.assertEquals('�', chars[0]);
                    else
                        Assert.assertEquals(i + " " + j + " " + k, expected, new String(chars, 0, r));
                }
            }
        }

        bytes = new byte[4];
        for (int i = 0; i < 256; i++) {
            bytes[0] = (byte) (i & 0xf7 | 0xf0);
            for (int j = 16; j < 64; j++) {
                bytes[1] = (byte) (j & 0x7f | 0x80);
                for (int k = 0; k < 64; k++) {
                    bytes[2] = (byte) (k & 0x7f | 0x80);
                    for (int m = 0; m < 64; m++) {
                        bytes[3] = (byte) (m & 0x7f | 0x80);
                        int r = UTF8.decode(bytes, 0, bytes.length, chars);
                        if (r == 1 && chars[0] == '�') {
                            char[] c = new String(bytes, charset).toCharArray();
                            for (char aC : c) {
                                Assert.assertEquals('�', aC);
                            }
                        } else
                            Assert.assertEquals(i + " " + j + " " + k + " " + m, new String(bytes, charset), new String(chars, 0, r));
                    }
                }
            }
        }
    }

    @Test
    public void decode_continuous_1() throws UnsupportedEncodingException {
        String s = "€";
        char[] chars = new char[1];
        byte[] bytes = s.getBytes("utf-8");

        Assert.assertEquals(3, bytes.length);

        int read;
        UTF8.DecodeOffsets offsets = new UTF8.DecodeOffsets();

        read = 1;
        offsets = UTF8.decode(bytes, read, chars, offsets);
        Assert.assertEquals(0, offsets.bytesOffset);
        Assert.assertEquals(0, offsets.charsOffset);

        read++;
        offsets = UTF8.decode(bytes, read, chars, offsets);
        Assert.assertEquals(0, offsets.bytesOffset);
        Assert.assertEquals(0, offsets.charsOffset);

        read++;
        offsets = UTF8.decode(bytes, read, chars, offsets);
        Assert.assertEquals(3, offsets.bytesOffset);
        Assert.assertEquals(1, offsets.charsOffset);

        Assert.assertEquals(s.charAt(0), chars[0]);
    }

    @Test
    public void decode_continuous_2() throws UnsupportedEncodingException {
        String s = "€";
        char[] chars = new char[1];
        byte[] bytes = s.getBytes("utf-8");

        Assert.assertEquals(3, bytes.length);

        UTF8.DecodeContext context = new UTF8.DecodeContext();

        context = UTF8.decode(bytes, 0, 2, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(2, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);

        context = UTF8.decode(bytes, 2, 1, chars, context);
        Assert.assertEquals(1, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
    }

    @Test
    public void decode_continuous_3() throws UnsupportedEncodingException {
        String s = "€";
        char[] chars = new char[1];
        byte[] bytes = s.getBytes("utf-8");

        Assert.assertEquals(3, bytes.length);

        UTF8.DecodeContext context = new UTF8.DecodeContext();

        context = UTF8.decode(bytes, 0, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(1, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);

        context = UTF8.decode(bytes, 1, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(2, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);

        context = UTF8.decode(bytes, 2, 1, chars, context);
        Assert.assertEquals(1, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
    }

    @Test
    public void decode_continuous_4() throws UnsupportedEncodingException {
        String s = "\uD800\uDF48"; // 4 bytes char
        char[] chars = new char[2];
        byte[] bytes = s.getBytes("utf-8");

        Assert.assertEquals(4, bytes.length);

        UTF8.DecodeContext context = new UTF8.DecodeContext();

        context = UTF8.decode(bytes, 0, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(1, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);

        context = UTF8.decode(bytes, 1, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(2, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);

        context = UTF8.decode(bytes, 2, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(3, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);
        Assert.assertEquals(bytes[2], context.buffer[2]);

        context = UTF8.decode(bytes, 3, 1, chars, context);
        Assert.assertEquals(2, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
        Assert.assertEquals(s.charAt(1), chars[1]);


        context = new UTF8.DecodeContext();
        context = UTF8.decode(bytes, 0, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(1, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);

        context = UTF8.decode(bytes, 1, 3, chars, context);
        Assert.assertEquals(2, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
        Assert.assertEquals(s.charAt(1), chars[1]);


        context = new UTF8.DecodeContext();
        context = UTF8.decode(bytes, 0, 2, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(2, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);

        context = UTF8.decode(bytes, 2, 2, chars, context);
        Assert.assertEquals(2, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
        Assert.assertEquals(s.charAt(1), chars[1]);


        context = new UTF8.DecodeContext();
        context = UTF8.decode(bytes, 0, 3, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(3, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);
        Assert.assertEquals(bytes[2], context.buffer[2]);

        context = UTF8.decode(bytes, 3, 1, chars, context);
        Assert.assertEquals(2, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
        Assert.assertEquals(s.charAt(1), chars[1]);


        context = new UTF8.DecodeContext();
        context = UTF8.decode(bytes, 0, 1, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(1, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);

        context = UTF8.decode(bytes, 1, 2, chars, context);
        Assert.assertEquals(0, context.charsOffset);
        Assert.assertEquals(3, context.length);
        Assert.assertEquals(bytes[0], context.buffer[0]);
        Assert.assertEquals(bytes[1], context.buffer[1]);
        Assert.assertEquals(bytes[2], context.buffer[2]);

        context = UTF8.decode(bytes, 3, 1, chars, context);
        Assert.assertEquals(2, context.charsOffset);
        Assert.assertEquals(0, context.length);

        Assert.assertEquals(s.charAt(0), chars[0]);
        Assert.assertEquals(s.charAt(1), chars[1]);
    }

    @Test
    public void test_supplier_consumer() throws UnsupportedEncodingException {
        String s = "€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€€";
        char[] chars = s.toCharArray();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        UTF8.encode(chars, 0, chars.length, new Supplier<byte[]>() {
            @Override
            public byte[] supply() {
                return new byte[4];
            }
        }, new UTF8.BytesConsumer() {
            @Override
            public void consume(byte[] buffer, int offset, int length) {
                out.write(buffer, offset, length);
            }
        });

        Assert.assertArrayEquals(s.getBytes("utf-8"), out.toByteArray());
    }
}
