package com.wizzardo.tools.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author: wizzardo
 * Date: 16.09.14
 */
public abstract class Appender {

    private static final char[] CHARS_TRUE = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] CHARS_FALSE = new char[]{'f', 'a', 'l', 's', 'e'};
    private static final char[] CHARS_NULL = new char[]{'n', 'u', 'l', 'l'};

    public static final Charset UTF8 = Charset.forName("utf-8");

    public abstract void append(String s);

    public abstract void append(String s, int from, int to);

    public abstract void append(char[] s, int from, int to);

    public abstract void append(char s);

    public abstract void append(int s);

    public abstract void append(long s);

    public abstract void append(boolean s);

    public abstract void append(Date d);

    public void append(float s) {
        append(String.valueOf(s));
    }

    public void append(double s) {
        append(String.valueOf(s));
    }

    public abstract void flush();

    public void append(Object ob) {
        append(String.valueOf(ob));
    }

    public void append(char[] chars) {
        append(chars, 0, chars.length);
    }

    public static Appender create() {
        return new StringBuilderAppender();
    }

    public static Appender create(StringBuilder sb) {
        return new StringBuilderAppender(sb);
    }

    public static Appender create(ExceptionDrivenStringBuilder sb) {
        return new ExceptionDrivenStringBuilderAppender(sb);
    }

    public static Appender create(OutputStream out) {
        return new UTF8WriterAppender(out);
    }

    public static Appender create(Writer out) {
        return new WriterAppender(out);
    }

    private static class StringBuilderAppender extends Appender {
        private StringBuilder sb;
        protected char[] buffer = new char[28];

        StringBuilderAppender() {
            this(new StringBuilder());
        }

        StringBuilderAppender(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public void append(String s) {
            sb.append(s);
        }

        @Override
        public void append(String s, int from, int to) {
            sb.append(s, from, to);
        }

        @Override
        public void append(char[] s, int from, int to) {
            sb.append(s, from, to - from);
        }

        @Override
        public void append(char s) {
            sb.append(s);
        }

        @Override
        public void append(int s) {
            sb.append(s);
        }

        @Override
        public void append(long s) {
            sb.append(s);
        }

        @Override
        public void append(boolean s) {
            sb.append(s);
        }

        @Override
        public void append(Date d) {
            int l = DateIso8601.formatToChars(d, buffer, 0);
            sb.append(buffer, 0, l);
        }

        @Override
        public void append(float s) {
            sb.append(s);
        }

        @Override
        public void append(double s) {
            sb.append(s);
        }

        @Override
        public void flush() {
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static class ExceptionDrivenStringBuilderAppender extends Appender {
        private ExceptionDrivenStringBuilder sb;

        ExceptionDrivenStringBuilderAppender(ExceptionDrivenStringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public void append(String s) {
            sb.append(s);
        }

        @Override
        public void append(String s, int from, int to) {
            sb.append(s, from, to);
        }

        @Override
        public void append(char[] s, int from, int to) {
            sb.append(s, from, to - from);
        }

        @Override
        public void append(char s) {
            sb.append(s);
        }

        @Override
        public void append(int s) {
            sb.append(s);
        }

        @Override
        public void append(long s) {
            sb.append(s);
        }

        @Override
        public void append(boolean s) {
            sb.append(s);
        }

        @Override
        public void append(Date d) {
            sb.append(d);
        }

        @Override
        public void append(float s) {
            sb.append(s);
        }

        @Override
        public void append(double s) {
            sb.append(s);
        }

        @Override
        public void flush() {
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static class WriterAppender<T extends Writer> extends Appender {
        protected T out;
        protected char[] buffer = new char[28];

        WriterAppender(T out) {
            this.out = out;
        }

        @Override
        public void append(int i) {
            int l = NumberToChars.toChars(i, buffer, 0);
            append(buffer, 0, l);
        }

        @Override
        public void append(long s) {
            int l = NumberToChars.toChars(s, buffer, 0);
            append(buffer, 0, l);
        }

        @Override
        public void append(boolean s) {
            if (s)
                append(CHARS_TRUE, 0, 4);
            else
                append(CHARS_FALSE, 0, 5);
        }

        @Override
        public void append(Date d) {
            int l = DateIso8601.formatToChars(d, buffer, 0);
            append(buffer, 0, l);
        }

        @Override
        public void append(Object ob) {
            if (ob == null)
                append(CHARS_NULL, 0, 4);
            else
                super.append(ob);
        }

        @Override
        public void append(String s) {
            if (s == null)
                append(CHARS_NULL, 0, 4);
            else
                try {
                    out.write(s);
                } catch (IOException e) {
                    throw Unchecked.rethrow(e);
                }
        }

        @Override
        public void append(String s, int from, int to) {
            try {
                out.write(s, from, to - from);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void append(char[] s, int from, int to) {
            try {
                out.write(s, from, to - from);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void append(char s) {
            try {
                out.append(s);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void flush() {
            try {
                out.flush();
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }

    private static class UTF8WriterAppender extends WriterAppender<UTF8Writer> {
        UTF8WriterAppender(OutputStream out) {
            super(new UTF8Writer(out));
        }

        @Override
        public void append(int i) {
            try {
                out.write(i);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void append(long l) {
            try {
                out.write(l);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void append(String s) {
            try {
                out.write(s);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }

        @Override
        public void append(boolean b) {
            try {
                out.write(b);
            } catch (IOException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }
}