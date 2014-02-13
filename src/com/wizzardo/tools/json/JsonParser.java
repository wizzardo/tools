package com.wizzardo.tools.json;


import java.util.HashMap;
import java.util.Set;

/**
 * @author: wizzardo
 * Date: 2/3/14
 */
public class JsonParser {
    private byte[] buffer;
    private int bufferLength = 0;
    private int bufferOffset = 0;
    private Object result;
    private JsonBuilderItem builder;

    private boolean inString = false;
    private byte quote = 0;

    public int parse(byte[] b, int offset, int length) {
        return parse(b, offset, length, null);
    }

    public int parse(byte[] b, int offset, int length, Class clazz) {
        if (length <= 0)
            return -1;

        byte ch;
        int ll = offset + length;
        int from = -1;

        for (int i = offset; i < ll && (builder == null || !builder.finished); i++) {
            ch = b[i];
            if (inString) {
                if (ch == quote && b[i - 1] != '\\') {
                    inString = false;
                }
                continue;
            }
            switch (ch) {
                case '"': {
                    inString = b[i - 1] != '\\';
                    quote = '"';
                    break;
                }
                case '\'': {
                    inString = b[i - 1] != '\\';
                    quote = '\'';
                    break;
                }
                case ':': {
                    builder.key = parseKey(b, from >= 0 ? from : offset, i);
                    from = i + 1;
                    break;
                }
                case ',': {
                    if (builder.object != null)
                        parseValue(builder.object, builder.key, b, from >= 0 ? from : offset, i);
                    else
                        parseValue(builder.array, b, from >= 0 ? from : offset, i);
                    from = i + 1;
                    break;
                }
                case '{': {
                    builder = new JsonBuilderItem(builder, Binder.getObjectBinder(builder, clazz));
                    from = i + 1;
                    break;
                }
                case '}': {
                    if (builder.object != null) {
                        parseValue(builder.object, builder.key, b, from >= 0 ? from : offset, i);
                        builder.finished = true;
                        if (builder.parent != null) {
                            if (builder.parent.object != null)
                                builder.parent.object.put(builder.parent.key, builder.object.getObject());
                            else
                                builder.parent.array.add(builder.object.getObject());

                            builder = builder.parent;
                        }
                        from = i + 1;
                    } else
                        throw new IllegalStateException("we are in json array, but find closing character for object!");
                    break;
                }
                case ']': {
                    if (builder.array != null) {
                        parseValue(builder.array, b, from >= 0 ? from : offset, i);
                        builder.finished = true;
                        if (builder.parent != null) {
                            if (builder.parent.object != null)
                                builder.parent.object.put(builder.parent.key, builder.array.getObject());
                            else
                                builder.parent.array.add(new JsonItem(builder.array));

                            builder = builder.parent;
                        }
                        from = i + 1;
                    } else
                        throw new IllegalStateException("we are in json object, but find closing character for array!");
                    break;
                }
                case '[': {
                    builder = new JsonBuilderItem(builder, Binder.getArrayBinder(builder, clazz));
                    from = i + 1;
                    break;
                }
            }
        }

        if (!builder.finished) {
            toBuffer(b, from >= 0 ? from : offset, ll);
        }

        return -1;
    }

    private void parseValue(ObjectBinder json, String key, byte[] s, int from, int to) {
        byte[] bytes = fromBuffer(s, from, to);
        JsonItem item = parseItem(bytes, bufferOffset, bufferOffset + bufferLength);
        bufferLength = 0;
        bufferOffset = 0;
        if (item != null)
            json.put(key, item);
    }

    private void parseValue(ArrayBinder json, byte[] s, int from, int to) {
        byte[] bytes = fromBuffer(s, from, to);
        JsonItem item = parseItem(bytes, bufferOffset, bufferOffset + bufferLength);
        bufferLength = 0;
        bufferOffset = 0;
        if (item != null)
            json.add(item);
    }

    private JsonItem parseItem(byte[] s, int from, int to) {
        if (from == to) {
            return null;
        }
        while ((from < to) && (s[from] <= ' ')) {
            from++;
        }
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        if (from == to) {
            return null;
        }

        if ((s[from] == '"' && s[to - 1] == '"') || (s[from] == '\'' && s[to - 1] == '\'')) {
            from++;
            to--;
            return new JsonItem(unescape(s, from, to));
        } else if (to - from == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l') {
            return new JsonItem(null);
        } else if (to - from == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e') {
            return new JsonItem(true);
        } else if (to - from == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e') {
            return new JsonItem(false);
        } else {
            return new JsonItem(unescape(s, from, to));
        }
    }

    private String unescape(byte[] s, int from, int to) {
        byte[] result = null;
        int length = 0;
        byte ch, prev = 0;
        for (int i = from; i < to; i++) {
            ch = s[i];
            if (ch < 0 && ch >= -64) {
                if (ch >= -16) // 4 bytes
                    i += 3;
                else if (ch > -32) // 3 bytes
                    i += 2;
                else  // 2 bytes
                    i++;
                continue;
            }

            if (prev == '\\') {
                if (result == null) {
                    result = new byte[to - from - 1];
                    length = i - from - 1;
                    System.arraycopy(s, from, result, 0, length);
                } else {
                    System.arraycopy(s, from, result, length, i - from - 1);
                    length += i - from - 1;
                }

                switch (ch) {
                    case '"':
                        result[length++] = '"';
                        break;
                    case '\\':
                        result[length++] = '\\';
                        break;
                    case 'b':
                        result[length++] = '\b';
                        break;
                    case 'f':
                        result[length++] = '\f';
                        break;
                    case 'n':
                        result[length++] = '\n';
                        break;
                    case 'r':
                        result[length++] = '\r';
                        break;
                    case 't':
                        result[length++] = '\t';
                        break;
                    case '/':
                        result[length++] = '/';
                        break;
                    case 'u':
                        if (to <= i + 5)
                            throw new IndexOutOfBoundsException("can't decode unicode character");
                        int c = fromUnicode(s, i + 1);
                        if (c < 256) {
                            result[length++] = (byte) c;
                        } else {
                            result[length++] = (byte) ((c & 0xFF00) >> 8);
                            result[length++] = (byte) (c & 0x00FF);
                        }
                        i += 4;
                        break;
                }

                from = i + 1;
            }
            prev = ch;
        }
        if (from < to) {
            if (result != null) {
                System.arraycopy(s, from, result, length, to - from);
                length += to - from;
            } else
                return createString(s, from, to - from);
        }

        return result == null ? createString(s, from, to - from) : createString(result, 0, length);
    }

    private static HashMap<ByteArray, String> stringCache = new HashMap<ByteArray, String>();

    private static class ByteArray {
        byte[] bytes;
        int offset, length;
        private int hash;

        private ByteArray(byte[] bytes, int offset, int length) {
            this.bytes = bytes;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ByteArray byteArray = (ByteArray) o;

            if (length != byteArray.length) return false;

            for (int i = 0; i < length; i++)
                if (bytes[i + offset] != byteArray.bytes[i + byteArray.offset])
                    return false;

            return true;
        }

        @Override
        public int hashCode() {
            if (hash != 0)
                return hash;

            int result = 1;

            for (int i = offset; i < offset + length; i++)
                result = 31 * result + bytes[i];

            result = 31 * result + length;
            hash = result;
            return result;
        }

        public void save() {
            byte[] b = new byte[length];
            System.arraycopy(bytes, offset, b, 0, length);
            offset = 0;
            bytes = b;
        }
    }

    private String createString(byte[] b, int offset, int length) {
        ByteArray ba = new ByteArray(b, offset, length);
        String s = stringCache.get(ba);
        if (s == null) {
            ba.save();
            s = new String(b, offset, length);
            stringCache.put(ba, s);
        }
        return s;
    }

    private int fromUnicode(byte[] b, int i) {
        int r = 0;
        int k = 4096;
        for (int j = 0; j < 4; j++) {
            byte bb = b[i + j];
            if (bb >= 48 && bb <= 57) { //0 - 9
                r += (bb - 48) * k;
            } else if (bb >= 65 && bb <= 70) { // A - F
                r += (bb - 55) * k;
            } else if (bb >= 97 && bb <= 102) { // a - f
                r += (bb - 87) * k;
            }
            k /= 16;
        }

        return r;
    }

    private String parseKey(byte[] b, int from, int to) {
        String s = getString(fromBuffer(b, from, to), bufferOffset, bufferOffset + bufferLength);
        bufferLength = 0;
        bufferOffset = 0;
        return s;
    }

    private String getString(byte[] b, int from, int to) {
        while ((from < to) && (b[from] <= ' ')) {
            from++;
        }
        while ((from < to) && (b[to - 1] <= ' ')) {
            to--;
        }
        if ((b[from] == '"' && b[to - 1] == '"') || (b[from] == '\'' && b[to - 1] == '\'')) {
            from++;
            to--;
        }
        return new String(b, from, to - from);
    }

    private void toBuffer(byte[] b, int from, int to) {
        if (from >= to)
            return;

        int oldSize = bufferLength;
        bufferLength = to - from + oldSize;
        if (buffer == null || buffer.length < bufferLength) {
            byte[] bytes = new byte[bufferLength];
            if (buffer != null)
                System.arraycopy(buffer, 0, bytes, 0, oldSize);
            buffer = bytes;
        }

        System.arraycopy(b, from, buffer, oldSize, to - from);
    }

    private byte[] fromBuffer(byte[] b, int from, int to) {
        if (bufferLength > 0) {
            if (from == to)
                return buffer;

            byte[] bytes = new byte[bufferLength + to - from];
            System.arraycopy(buffer, 0, bytes, 0, bufferLength);
            System.arraycopy(b, from, bytes, bufferLength, to - from);

            buffer = bytes;
            bufferLength = buffer.length;
            bufferOffset = 0;
            return buffer;
        } else {
            bufferLength = to - from;
            bufferOffset = from;
            return b;
        }
    }

    public JsonItem parse(byte[] b) {
        parse(b, 0, b.length, null);
        return getResult();
    }

    public <T> T parse(byte[] b, Class<T> clazz) {
        parse(b, 0, b.length, clazz);
        return getResult();
    }

    public <T> T getResult() {
        if (builder == null || builder.parent != null || !builder.finished)
            throw new IllegalStateException("parsing not finished yet");

        if (result == null) {
            result = builder.object != null ? builder.object.getObject() : builder.array;
        }

        return (T) result;
    }


    public static JsonItem parse(String json) {
        return JsonObject.parse(json);
    }

    static class JsonBuilderItem {
        JsonBuilderItem parent;
        ObjectBinder object;
        ArrayBinder array;
        boolean finished = false;
        String key;

        private JsonBuilderItem(JsonBuilderItem parent, ObjectBinder object) {
            this.parent = parent;
            this.object = object;
        }

        private JsonBuilderItem(ObjectBinder object) {
            this.object = object;
        }

        private JsonBuilderItem(JsonBuilderItem parent, ArrayBinder array) {
            this.parent = parent;
            this.array = array;
        }

        private JsonBuilderItem(ArrayBinder array) {
            this.array = array;
        }
    }

    public static class TestClass {
        String field;
        int i;
        boolean b;
        TestClass child;
        double d;
        int[] array;
        Set<TestClass> children;
    }

    public static void main(String[] args) {
        JsonParser parser = new JsonParser();

        TestClass tc = parser.parse("{field:value, array:[1,2,3], i:1, d:2.5 ,b:true,child:{field:'qwerty',i:2,b:true},children:[{field:'foo',i:3,b:true},{field:'bar',i:4,b:true}]}".getBytes(), TestClass.class);
        System.out.println(tc);
        System.out.println(tc.field);
        System.out.println(new JsonParser().parse("{field:value}".getBytes()).asJsonObject().get("field").asString());
    }
}
