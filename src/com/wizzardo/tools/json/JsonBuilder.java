package com.wizzardo.tools.json;

/**
 * @author: moxa
 * Date: 4/26/13
 */
public abstract class JsonBuilder {

    protected boolean end = false;
    protected JsonBuilder innerBuilder;
    //    protected StringBuilder sb;
    protected CharBuffer sb;
    protected boolean inString = false;
    protected char lastChar;

    private JsonBuilder() {
//        sb = new StringBuilder();
        sb = new CharBuffer();
    }

    protected boolean handleInString(char ch) {
        if (inString) {
            if (ch == '"' && lastChar != '\\') {
                inString = false;
            } else {
                sb.append(ch);
            }
            lastChar = ch;
            return true;
        }
        return false;
    }

    private static class JsonObjectBuilder extends JsonBuilder {
        private JsonObject json = new JsonObject();
        private String key;

        private JsonObjectBuilder(CharBuffer sb) {
            this.sb = sb;
        }

        private JsonObjectBuilder() {
            super();
        }

        @Override
        public JsonItem get() {
            return new JsonItem(json);
        }

        protected boolean handleInnerBuilder(char ch) {
            if (innerBuilder != null) {
                if (innerBuilder.isComplete()) {
                    json.put(key, innerBuilder.get());
                    key = null;
                    innerBuilder = null;
                } else {
                    innerBuilder.append(ch);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void append(char ch) {
            if (handleInnerBuilder(ch)) return;
            if (handleInString(ch)) return;
//            if (innerBuilder != null) {
//                if (innerBuilder.isComplete()) {
//                    json.put(key, innerBuilder.get());
//                    key = null;
//                    innerBuilder = null;
//                } else {
//                    innerBuilder.append(ch);
//                    return;
//                }
//            }
//            if (inString) {
//                if (ch == '"' && lastChar != '\\') {
//                    inString = false;
//                } else {
//                    sb.append(ch);
//                }
//                lastChar = ch;
//                return;
//            }
            switch (ch) {
                case '"': {
                    inString = lastChar != '\\';
                    break;
                }
                case ':': {
//                    key = parseKey(sb.toString());
                    key = parseKey(sb.buffer, sb.from, sb.to);
                    sb.setLength(0);
                    break;
                }
                case ',': {
                    if (key == null) break;
//                    parseValue(json, key, sb.toString());
                    parseValue(json, key, sb.buffer, sb.from, sb.to);
                    sb.setLength(0);
                    break;
                }
                case '{': {
                    innerBuilder = new JsonObjectBuilder(sb);
                    break;
                }
                case '}': {
                    end = true;
                    if (sb.length() > 0) {
//                        parseValue(json, key, sb.toString());
                        parseValue(json, key, sb.buffer, sb.from, sb.to);
                        sb.setLength(0);
                    }
                    break;
                }
                case '[': {
                    innerBuilder = new JsonArrayBuilder(sb);
                    break;
                }
                default: {
                    sb.append(ch);
                    break;
                }
            }
            lastChar = ch;
        }

        private static void parseValue(JsonObject json, String key, char[] s, int from, int to) {
            if (from == to) {
                return;
            }
            while ((from < to) && (s[from] <= ' ')) {
                from++;
            }
            while ((from < to) && (s[to - 1] <= ' ')) {
                to--;
            }
            if (from == to) {
                return;
            }

            if (s[from] == '"' && s[to - 1] == '"') {
                from++;
                to--;
                String value = new String(s, from, to - from);
                json.put(key, new JsonItem(value));
                return;
            }
            String value = new String(s, from, to - from);
            if (value.equals("null")) {
                json.put(key, null);
            } else if (value.equals("true")) {
                json.put(key, new JsonItem(true));
            } else if (value.equals("false")) {
                json.put(key, new JsonItem(false));
            } else {
                json.put(key, new JsonItem(value));
            }
        }

        private static String parseKey(char[] s, int from, int to) {
            while ((from < to) && (s[from] <= ' ')) {
                from++;
            }
            while ((from < to) && (s[to] <= ' ') && s[to] != 0) {
                to--;
            }
            if (s[from] == '"' && s[to - 1] == '"') {
                from++;
                to--;
            }
            String key = new String(s, from, to - from);
//            if (key.charAt(0) == '"' && key.charAt(key.length() - 1) == '"') {
//                key = key.substring(1, key.length() - 1);
//            }
            return key;
        }

        private static String parseKey(String s) {
            checkMaxLengthString(s);

            String key = s.trim();
            if (key.charAt(0) == '"' && key.charAt(key.length() - 1) == '"') {
                key = key.substring(1, key.length() - 1);
            }
            return key;
        }

        private static void parseValue(JsonObject json, String key, String s) {
            checkMaxLengthString(s);
            checkMaxLengthString(key);

            String value = s.trim();
            if (value.length() > 0 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                value = value.substring(1, value.length() - 1);
            }
            if (value.equals("null")) {
                json.put(key, null);
            } else if (value.equals("true")) {
                json.put(key, new JsonItem(true));
            } else if (value.equals("false")) {
                json.put(key, new JsonItem(false));
            } else {
                json.put(key, new JsonItem(value));
            }
        }
    }

    private static class JsonArrayBuilder extends JsonBuilder {
        private JsonArray json = new JsonArray();

        private JsonArrayBuilder(CharBuffer sb) {
            this.sb = sb;
        }

        private JsonArrayBuilder() {
            super();
        }

        @Override
        public JsonItem get() {
            return new JsonItem(json);
        }

        protected boolean handleInnerBuilder(char ch) {
            if (innerBuilder != null) {
                if (innerBuilder.isComplete()) {
                    json.add(innerBuilder.get());
                    innerBuilder = null;
                } else {
                    innerBuilder.append(ch);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void append(char ch) {
            if (handleInnerBuilder(ch)) return;
            if (handleInString(ch)) return;
//            if (innerBuilder != null) {
//                if (innerBuilder.isComplete()) {
//                    json.add(innerBuilder.get());
//                    innerBuilder = null;
//                } else {
//                    innerBuilder.append(ch);
//                    return;
//                }
//            }
//            if (inString) {
//                if (ch == '"' && lastChar != '\\') {
//                    inString = false;
//                } else {
//                    sb.append(ch);
//                }
//                lastChar = ch;
//                return;
//            }
            switch (ch) {
                case '"': {
                    inString = lastChar != '\\';
                    break;
                }
                case ',': {
                    if (sb.length() > 0) {
//                        parseValue(json, sb.toString());
                        parseValue(json, sb.buffer, sb.from, sb.to);
                        sb.setLength(0);
                    }
                    break;
                }
                case '{': {
                    innerBuilder = new JsonObjectBuilder(sb);
                    break;
                }
                case '[': {
                    innerBuilder = new JsonArrayBuilder(sb);
                    break;
                }
                case ']': {
                    end = true;
                    if (sb.length() > 0) {
//                        parseValue(json, sb.toString());
                        parseValue(json, sb.buffer, sb.from, sb.to);
                        sb.setLength(0);
                    }
                    break;
                }
                default: {
                    sb.append(ch);
                    break;
                }
            }
            lastChar = ch;
        }

        private static void parseValue(JsonArray json, char[] s, int from, int to) {
            if (from == to) {
                return;
            }
            while ((from < to) && (s[from] <= ' ')) {
                from++;
            }
            while ((from < to) && (s[to - 1] <= ' ')) {
                to--;
            }
            if (from == to) {
                return;
            }
            String value = new String(s, from, to - from);

            if (s[from] == '"' && s[to - 1] == '"') {
                value = value.substring(1, value.length() - 1);
                json.add(new JsonItem(value));
            } else if (value.equals("null")) {
                json.add(null);
            } else if (value.equals("true")) {
                json.add(new JsonItem(true));
            } else if (value.equals("false")) {
                json.add(new JsonItem(false));
            } else {
                json.add(new JsonItem(value));
            }
        }

        private static void parseValue(JsonArray json, String s) {
            checkMaxLengthString(s);

            String value = s.trim();
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                value = value.substring(1, value.length() - 1);
                json.add(new JsonItem(value));
            } else if (value.equals("null")) {
                json.add(null);
            } else if (value.equals("true")) {
                json.add(new JsonItem(true));
            } else if (value.equals("false")) {
                json.add(new JsonItem(false));
            } else {
                json.add(new JsonItem(value));
            }
        }
    }

    static int max = 0;

    protected static void checkMaxLengthString(String s) {
        max = Math.max(max, s.length());
    }

    protected boolean isComplete() {
        return end;
    }


    public static JsonItem parse(String s) {
        s = s.trim();
        int length = s.length();

        JsonBuilder builder = JsonBuilder.createBuilder(s.charAt(0));
        char[] data = s.toCharArray();
        for (int i = 1; i < length; i++) {
//            builder.append(s.charAt(i));
            builder.append(data[i]);
        }
//       new BufferedReader(new StringReader(s).)
//        return parse(s.toCharArray());

        return builder.get();
    }

    private static JsonBuilder createBuilder(char c) {
        if (c == '{') {
            return new JsonObjectBuilder();
        }
        if (c == '[') {
            return new JsonArrayBuilder();
        }
        throw new IllegalStateException("unknown json type: " + c);
    }

    public abstract JsonItem get();

    public abstract void append(char c);

    public static class CharBuffer {
        char[] buffer = new char[256];
        int from = 0;
        int to = 0;

        public void append(char c) {
            buffer[to++] = c;
        }

        public int length() {
            return to - from;
        }

        public void setLength(int i) {
            from = 0;
            to = i;
        }
    }
}
