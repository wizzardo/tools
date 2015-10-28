package com.wizzardo.tools.misc;

import java.util.Arrays;

/**
 * Created by wizzardo on 30.03.15.
 */
public class UTF8 {

    private final static char MALFORMED = '�';
    protected byte[] buffer;

    public byte[] toBytes(char[] chars, int offset, int length) {
        if (buffer == null || buffer.length < length * 4)
            buffer = new byte[length * 4];

        return encodeAndTrim(chars, offset, length, buffer);
    }

    public byte[] toBytes(char[] chars) {
        return toBytes(chars, 0, chars.length);
    }

    public static byte[] encode(char[] chars) {
        return encode(chars, 0, chars.length);
    }

    public static byte[] encode(char[] chars, int off, int length) {
        byte[] bytes = new byte[count(chars, off, length)];
        encode(chars, off, length, bytes);
        return bytes;
    }

    public static byte[] encodeAndTrim(char[] chars, int off, int length, byte[] bytes) {
        int l = encode(chars, off, length, bytes);
        return Arrays.copyOf(bytes, l);
    }

    public static int encode(char[] chars, int off, int length, byte[] bytes) {
        int limit = off + length;
        int l = 0;

        int ch;
        while (off < limit) {
            if ((ch = chars[off++]) < 128)
                bytes[l++] = (byte) ch;
            else {
                off--;
                break;
            }
        }

        while (off < limit) {
            int c = chars[off++];
            if (c < 128) {
                bytes[l++] = (byte) c;
            } else if (c < 2048) {
                bytes[l++] = (byte) (192 | c >> 6);
                bytes[l++] = (byte) (128 | c & 63);
            } else if (c >= '\uD800' && c < '\uE000') {//surrogate
                int r = off < limit ? parseSurrogate(c, chars[off]) : -1;
                if (r < 0) {
                    bytes[l++] = '?';
                } else {
                    bytes[l++] = (byte) (240 | r >> 18);
                    bytes[l++] = (byte) (128 | r >> 12 & 63);
                    bytes[l++] = (byte) (128 | r >> 6 & 63);
                    bytes[l++] = (byte) (128 | r & 63);
                    ++off;
                }
            } else {
                bytes[l++] = (byte) (224 | c >> 12);
                bytes[l++] = (byte) (128 | c >> 6 & 63);
                bytes[l++] = (byte) (128 | c & 63);
            }
        }

        return l;
    }

    public static void encode(char[] chars, int off, int length, Supplier<byte[]> bytesSupplier, BytesConsumer bytesConsumer) {
        int limit = off + length;
        while (off < limit) {
            byte[] bytes = bytesSupplier.supply();
            int ll = bytes.length - 4;
            if (ll < 0)
                throw new IllegalArgumentException("bytesSupplier.supply().length must be >= 4");

            int l = 0;
            int ch;
            while (off < limit && l <= ll) {
                if ((ch = chars[off++]) < 128)
                    bytes[l++] = (byte) ch;
                else {
                    off--;
                    break;
                }
            }

            while (off < limit && l <= ll) {
                int c = chars[off++];
                if (c < 128) {
                    bytes[l++] = (byte) c;
                } else if (c < 2048) {
                    bytes[l++] = (byte) (192 | c >> 6);
                    bytes[l++] = (byte) (128 | c & 63);
                } else if (c >= '\uD800' && c < '\uE000') {//surrogate
                    int r = off < limit ? parseSurrogate(c, chars[off]) : -1;
                    if (r < 0) {
                        bytes[l++] = '?';
                    } else {
                        bytes[l++] = (byte) (240 | r >> 18);
                        bytes[l++] = (byte) (128 | r >> 12 & 63);
                        bytes[l++] = (byte) (128 | r >> 6 & 63);
                        bytes[l++] = (byte) (128 | r & 63);
                        ++off;
                    }
                } else {
                    bytes[l++] = (byte) (224 | c >> 12);
                    bytes[l++] = (byte) (128 | c >> 6 & 63);
                    bytes[l++] = (byte) (128 | c & 63);
                }
            }

            bytesConsumer.consume(bytes, 0, l);
        }
    }

    public static int encode(char ch, byte[] bytes, int offset) {
        if (ch < 128) {
            bytes[offset++] = (byte) ch;
            return offset;
        }

        if (ch < 2048) {
            bytes[offset++] = (byte) (192 | ch >> 6);
            bytes[offset++] = (byte) (128 | ch & 63);
        } else if (ch >= '\uD800' && ch < '\uE000') {//surrogate
            bytes[offset++] = '?';
        } else {
            bytes[offset++] = (byte) (224 | ch >> 12);
            bytes[offset++] = (byte) (128 | ch >> 6 & 63);
            bytes[offset++] = (byte) (128 | ch & 63);
        }

        return offset;
    }

    public static int count(char[] chars, int off, int length) {
        int limit = off + length;
        int l = 0;

        while (off < limit) {
            if (chars[off++] < 128)
                l++;
            else {
                off--;
                break;
            }
        }

        while (off < limit) {
            int c = chars[off++];
            if (c < 128) {
                l++;
            } else if (c < 2048) {
                l += 2;
            } else if (c >= '\uD800' && c < '\uE000') {//surrogate
                int r = off < limit ? parseSurrogate(c, chars[off]) : -1;
                if (r < 0) {
                    l++;
                } else {
                    l += 4;
                    ++off;
                }
            } else {
                l += 3;
            }
        }

        return l;
    }

    private static int parseSurrogate(int ch, int ch2) {
        if (ch >= '\uD800' && ch < '\uDC00') {
            if (ch2 >= '\uDC00' && ch2 < '\uE000')
                return ((ch & 1023) << 10 | ch2 & 1023) + 65536;
            else
                return -1;
        } else if (ch >= '\uDC00' && ch < '\uE000')
            return -1;
        else
            return ch;
    }

    public static int decode(byte[] bytes, int offset, int length, char[] chars) {
        int to = offset + length;
        int i = 0;

        int temp;
        while (i < length) {
            if ((temp = bytes[offset++]) >= 0)
                chars[i++] = (char) temp;
            else {
                offset--;
                break;
            }
        }

        while (offset < to) {
            int b = bytes[offset++];
            if (b < 0) {
                int b1;
                if (b >> 5 != -2 || (b & 0x1e) == 0) {
                    int b2;
                    if (b >> 4 == -2) {
                        if (offset + 1 < to) {
                            b1 = bytes[offset++];
                            b2 = bytes[offset++];
                            if (isMalformed3(b, b1, b2)) {
                                chars[i++] = MALFORMED;
                            } else {
                                char ch = (char) (b << 12 ^ b1 << 6 ^ b2 ^ -123008);
                                if (isSurrogate(ch)) {
                                    chars[i++] = MALFORMED;
                                } else {
                                    chars[i++] = ch;
                                }
                            }
                        } else {
                            if (offset >= to || !isMalformed3_2(b, bytes[offset])) {
                                chars[i++] = MALFORMED;
                                return i;
                            }

                            chars[i++] = MALFORMED;
                        }
                    } else if (b >> 3 != -2) {
                        chars[i++] = MALFORMED;
                    } else if (offset + 2 < to) {
                        b1 = bytes[offset++];
                        b2 = bytes[offset++];
                        int b3 = bytes[offset++];
                        int value = b << 18 ^ b1 << 12 ^ b2 << 6 ^ b3 ^ 3678080;
                        if (!isMalformed4(b1, b2, b3) && isSupplementaryCodePoint(value)) {
                            chars[i++] = highSurrogate(value);
                            chars[i++] = lowSurrogate(value);
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    } else {
                        int i1 = b & 0xff;
                        if (i1 <= 244 && (offset >= to || !isMalformed4_2(i1, bytes[offset] & 0xff))) {
                            ++offset;
                            if (offset >= to || !isMalformed4_3(bytes[offset])) {
                                chars[i++] = MALFORMED;
                                return i;
                            }

                            chars[i++] = MALFORMED;
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    }
                } else {
                    if (offset >= to) {
                        chars[i++] = MALFORMED;
                        return i;
                    }

                    b1 = bytes[offset++];
                    if (isNotContinuation(b1)) {
                        chars[i++] = MALFORMED;
                        --offset;
                    } else {
                        chars[i++] = (char) (b << 6 ^ b1 ^ 0xf80);
                    }
                }
            } else {
                chars[i++] = (char) b;
            }
        }

        return i;
    }

    public static class DecodeOffsets {
        public int charsOffset;
        public int bytesOffset;

        public DecodeOffsets() {
        }

        public DecodeOffsets(int charsDecoded, int bytesOffset) {
            this.charsOffset = charsDecoded;
            this.bytesOffset = bytesOffset;
        }

        public DecodeOffsets update(int charsOffset, int bytesOffset) {
            this.charsOffset = charsOffset;
            this.bytesOffset = bytesOffset;
            return this;
        }
    }

    public static DecodeOffsets decode(byte[] bytes, int length, char[] chars, DecodeOffsets offsets) {
        int offset = offsets.bytesOffset;
        int to = offset + length;
        int i = offsets.charsOffset;
        int l = Math.min(length, chars.length - i);

        int temp;
        while (i < l) {
            if ((temp = bytes[offset++]) >= 0)
                chars[i++] = (char) temp;
            else {
                offset--;
                break;
            }
        }

        while (offset < to) {
            int b = bytes[offset++];
            if (b < 0) {
                int b1;
                if (b >> 5 != -2 || (b & 0x1e) == 0) {
                    int b2;
                    if (b >> 4 == -2) {
                        if (offset + 1 < to) {
                            b1 = bytes[offset++];
                            b2 = bytes[offset++];
                            if (isMalformed3(b, b1, b2)) {
                                chars[i++] = MALFORMED;
                            } else {
                                char ch = (char) (b << 12 ^ b1 << 6 ^ b2 ^ -123008);
                                if (isSurrogate(ch)) {
                                    chars[i++] = MALFORMED;
                                } else {
                                    chars[i++] = ch;
                                }
                            }
                        } else {
                            if (offset >= to || !isMalformed3_2(b, bytes[offset])) {
                                return offsets.update(i, offset - 1);
                            }
                            chars[i++] = MALFORMED;
                        }
                    } else if (b >> 3 != -2) {
                        chars[i++] = MALFORMED;
                    } else if (offset + 2 < to) {
                        b1 = bytes[offset++];
                        b2 = bytes[offset++];
                        int b3 = bytes[offset++];
                        int value = b << 18 ^ b1 << 12 ^ b2 << 6 ^ b3 ^ 3678080;
                        if (!isMalformed4(b1, b2, b3) && isSupplementaryCodePoint(value)) {
                            chars[i++] = highSurrogate(value);
                            chars[i++] = lowSurrogate(value);
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    } else {
                        int i1 = b & 0xff;
                        if (i1 <= 244 && (offset >= to || !isMalformed4_2(i1, bytes[offset] & 0xff))) {
                            offset++;
                            if (offset >= to || !isMalformed4_3(bytes[offset]))
                                return offsets.update(i, offset - 2);

                            chars[i++] = MALFORMED;
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    }
                } else {
                    if (offset >= to)
                        return offsets.update(i, offset - 1);

                    b1 = bytes[offset++];
                    if (isNotContinuation(b1)) {
                        chars[i++] = MALFORMED;
                        --offset;
                    } else {
                        chars[i++] = (char) (b << 6 ^ b1 ^ 0xf80);
                    }
                }
            } else {
                chars[i++] = (char) b;
            }
        }

        return offsets.update(i, offset);
    }

    public static class DecodeContext {
        int charsOffset;
        byte[] buffer = new byte[4];
        int length;

        public DecodeContext() {
        }

        public DecodeContext(int charsDecoded) {
            this.charsOffset = charsDecoded;
        }

        public DecodeContext update(int charsOffset, byte[] bytes, int from, int to) {
            this.charsOffset = charsOffset;
            length = to - from;
            if (length == 0) {
                return this;
            } else if (length == 1) {
                buffer[0] = bytes[from];
            } else if (length == 2) {
                buffer[0] = bytes[from];
                buffer[1] = bytes[from + 1];
            } else if (length == 3) {
                buffer[0] = bytes[from];
                buffer[1] = bytes[from + 1];
                buffer[2] = bytes[from + 2];
            } else {
                length = 0;
                throw new IllegalArgumentException("this context can store only 3 bytes");
            }

            return this;
        }
    }

    public static DecodeContext decode(byte[] bytes, int offset, int length, char[] chars, DecodeContext context) {
        int to = offset + length;
        int i = context.charsOffset;
        int l = Math.min(length, chars.length - i);

        if (context.length != 0) {
            context.buffer[context.length] = bytes[offset];
            decode(context.buffer, 0, context.length + 1, chars);
            offset++;
            i++;
        }

        int temp;
        while (i < l) {
            if ((temp = bytes[offset++]) >= 0)
                chars[i++] = (char) temp;
            else {
                offset--;
                break;
            }
        }

        while (offset < to) {
            int b = bytes[offset++];
            if (b < 0) {
                int b1;
                if (b >> 5 != -2 || (b & 0x1e) == 0) {
                    int b2;
                    if (b >> 4 == -2) {
                        if (offset + 1 < to) {
                            b1 = bytes[offset++];
                            b2 = bytes[offset++];
                            if (isMalformed3(b, b1, b2)) {
                                chars[i++] = MALFORMED;
                            } else {
                                char ch = (char) (b << 12 ^ b1 << 6 ^ b2 ^ -123008);
                                if (isSurrogate(ch)) {
                                    chars[i++] = MALFORMED;
                                } else {
                                    chars[i++] = ch;
                                }
                            }
                        } else {
                            if (offset >= to || !isMalformed3_2(b, bytes[offset])) {
                                return context.update(i, bytes, offset - 1, to);
                            }
                            chars[i++] = MALFORMED;
                        }
                    } else if (b >> 3 != -2) {
                        chars[i++] = MALFORMED;
                    } else if (offset + 2 < to) {
                        b1 = bytes[offset++];
                        b2 = bytes[offset++];
                        int b3 = bytes[offset++];
                        int value = b << 18 ^ b1 << 12 ^ b2 << 6 ^ b3 ^ 3678080;
                        if (!isMalformed4(b1, b2, b3) && isSupplementaryCodePoint(value)) {
                            chars[i++] = highSurrogate(value);
                            chars[i++] = lowSurrogate(value);
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    } else {
                        int i1 = b & 0xff;
                        if (i1 <= 244 && (offset >= to || !isMalformed4_2(i1, bytes[offset] & 0xff))) {
                            offset++;
                            if (offset >= to || !isMalformed4_3(bytes[offset]))
                                return context.update(i, bytes, offset - 2, to);

                            chars[i++] = MALFORMED;
                        } else {
                            chars[i++] = MALFORMED;
                        }
                    }
                } else {
                    if (offset >= to)
                        return context.update(i, bytes, offset - 1, to);

                    b1 = bytes[offset++];
                    if (isNotContinuation(b1)) {
                        chars[i++] = MALFORMED;
                        --offset;
                    } else {
                        chars[i++] = (char) (b << 6 ^ b1 ^ 0xf80);
                    }
                }
            } else {
                chars[i++] = (char) b;
            }
        }

        return context.update(i, bytes, offset, to);
    }

    private static boolean isNotContinuation(int b) {
        return (b & 0xc0) != 0x80;
    }

    private static boolean isMalformed3(int b1, int b2, int b3) {
        return b1 == -32 && (b2 & 224) == 128 || (b2 & 192) != 128 || (b3 & 192) != 128;
    }

    private static boolean isMalformed3_2(int b1, int b2) {
        return b1 == -32 && (b2 & 224) == 128 || (b2 & 192) != 128;
    }

    private static boolean isMalformed4(int b1, int b2, int b3) {
        return (b1 & 192) != 128 || (b2 & 192) != 128 || (b3 & 192) != 128;
    }

    private static boolean isMalformed4_2(int b1, int b2) {
        return b1 == 240 && (b2 < 144 || b2 > 191) || b1 == 244 && (b2 & 240) != 128 || (b2 & 192) != 128;
    }

    private static boolean isMalformed4_3(int b1) {
        return (b1 & 192) != 128;
    }

    public static boolean isSurrogate(char ch) {
        return ch >= '\uD800' && ch < '\uE000';
    }

    public static boolean isSupplementaryCodePoint(int codePoint) {
        return codePoint >= 0x010000 && codePoint < 0x110000;
    }

    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10) + 55232);
    }

    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + '\uDC00');
    }

    public interface BytesConsumer {
        void consume(byte[] buffer, int offset, int length);
    }
}
