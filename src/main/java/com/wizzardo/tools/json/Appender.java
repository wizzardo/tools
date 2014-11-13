package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;
import com.wizzardo.tools.misc.WrappedException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * @author: wizzardo
 * Date: 16.09.14
 */
abstract class Appender {
    public abstract void append(String s);

    public abstract void append(String s, int from, int to);

    public abstract void append(char[] s, int from, int to);

    public abstract void append(char s);

    public abstract void flush();

    public void append(Object ob) {
        append(String.valueOf(ob));
    }

    public void append(char[] chars) {
        append(chars, 0, chars.length);
    }

    static Appender create() {
        return new StringBuilderAppender();
    }

    static Appender create(StringBuilder sb) {
        return new StringBuilderAppender(sb);
    }

    static Appender create(ExceptionDrivenStringBuilder sb) {
        return new ExceptionDrivenStringBuilderAppender(sb);
    }

    static Appender create(OutputStream out) {
        return new StreamAppender(out);
    }

    private static class StringBuilderAppender extends Appender {
        private StringBuilder sb;

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
        public void flush() {
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static class StreamAppender extends Appender {
        private OutputStreamWriter out;

        StreamAppender(OutputStream out) {
            this.out = new OutputStreamWriter(out, Charset.forName("utf-8"));
        }

        @Override
        public void append(String s) {
            try {
                out.write(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(String s, int from, int to) {
            try {
                out.append(s, from, to);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(char[] s, int from, int to) {
            try {
                out.write(s, from, to - from);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void append(char s) {
            try {
                out.append(s);
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }

        @Override
        public void flush() {
            try {
                out.flush();
            } catch (IOException e) {
                throw new WrappedException(e);
            }
        }
    }
}