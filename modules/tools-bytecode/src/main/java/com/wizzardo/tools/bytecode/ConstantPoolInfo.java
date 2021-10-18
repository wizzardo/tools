package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

class ConstantPoolInfo {
    final static byte CONSTANT_Utf8 = 1;
    final static byte CONSTANT_Integer = 3;
    final static byte CONSTANT_Float = 4;
    final static byte CONSTANT_Long = 5;
    final static byte CONSTANT_Double = 6;
    final static byte CONSTANT_Class = 7;
    final static byte CONSTANT_String = 8;
    final static byte CONSTANT_Fieldref = 9;
    final static byte CONSTANT_Methodref = 10;
    final static byte CONSTANT_InterfaceMethodref = 11;
    final static byte CONSTANT_NameAndType = 12;
    final static byte CONSTANT_MethodHandle = 15;
    final static byte CONSTANT_MethodType = 16;
    final static byte CONSTANT_InvokeDynamic = 18;

    static ConstantInfo byTag(byte tag) {
        if (tag == CONSTANT_Utf8)
            return new CONSTANT_Utf8_info();
        if (tag == CONSTANT_Integer)
            return null;
        if (tag == CONSTANT_Float)
            return null;
        if (tag == CONSTANT_Long)
            return null;
        if (tag == CONSTANT_Double)
            return null;
        if (tag == CONSTANT_Class)
            return new CONSTANT_Class_info();
        if (tag == CONSTANT_String)
            return new CONSTANT_String_info();
        if (tag == CONSTANT_Fieldref)
            return new CONSTANT_Fieldref_info();
        if (tag == CONSTANT_Methodref)
            return new CONSTANT_Methodref_info();
        if (tag == CONSTANT_InterfaceMethodref)
            return new CONSTANT_InterfaceMethodref_info();
        if (tag == CONSTANT_NameAndType)
            return new CONSTANT_NameAndType_info();
        if (tag == CONSTANT_MethodHandle)
            return null;
        if (tag == CONSTANT_MethodType)
            return null;
        if (tag == CONSTANT_InvokeDynamic)
            return null;

        return null;
    }

    public interface ConstantInfo {
        int read(byte[] bytes, int from);

        void write(ByteArrayOutputStream out);
    }

    public static class CONSTANT_String_info implements ConstantInfo {
        final byte tag = CONSTANT_String;
        int string_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_String");

            from++;
            string_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(string_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_String_info{" +
                    "tag=" + tag +
                    ", string_index=" + string_index +
                    '}';
        }
    }

    public static class CONSTANT_Class_info implements ConstantInfo {
        final byte tag = CONSTANT_Class;
        int name_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_Class");

            from++;
            name_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(name_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_Class_info{" +
                    "tag=" + tag +
                    ", name_index=" + name_index +
                    '}';
        }
    }

    public static class CONSTANT_Fieldref_info implements ConstantInfo {
        final byte tag = CONSTANT_Fieldref;
        int class_index;
        int name_and_type_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_Fieldref");

            from++;
            class_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            name_and_type_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(class_index, out);
            ByteCodeParser.writeInt2(name_and_type_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_Fieldref_info{" +
                    "tag=" + tag +
                    ", class_index=" + class_index +
                    ", name_and_type_index=" + name_and_type_index +
                    '}';
        }
    }

    public static class CONSTANT_NameAndType_info implements ConstantInfo {
        final byte tag = CONSTANT_NameAndType;
        int name_index;
        int descriptor_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_NameAndType");

            from++;
            name_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            descriptor_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(name_index, out);
            ByteCodeParser.writeInt2(descriptor_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_NameAndType_info{" +
                    "tag=" + tag +
                    ", name_index=" + name_index +
                    ", descriptor_index=" + descriptor_index +
                    '}';
        }
    }

    public static class CONSTANT_Utf8_info implements ConstantInfo {
        final byte tag = CONSTANT_Utf8;
        int length;
        byte[] bytes;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_Utf8");

            from++;
            length = ByteCodeParser.readInt2(from, bytes);
            from += 2;

            this.bytes = new byte[length];
            System.arraycopy(bytes, from, this.bytes, 0, length);
            from += length;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(length, out);
            out.write(bytes, 0, length);
        }

        @Override
        public String toString() {
            return "CONSTANT_Utf8_info{" +
                    "tag=" + tag +
                    ", length=" + length +
                    ", bytes=" + new String(bytes, StandardCharsets.UTF_8) +
                    '}';
        }
    }

    public static class CONSTANT_Methodref_info implements ConstantInfo {
        final byte tag = CONSTANT_Methodref;
        int class_index;
        int name_and_type_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_Methodref");

            from++;
            class_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            name_and_type_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(class_index, out);
            ByteCodeParser.writeInt2(name_and_type_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_Methodref_info{" +
                    "tag=" + tag +
                    ", class_index=" + class_index +
                    ", name_and_type_index=" + name_and_type_index +
                    '}';
        }
    }

    public static class CONSTANT_InterfaceMethodref_info implements ConstantInfo {
        final byte tag = CONSTANT_InterfaceMethodref;
        int class_index;
        int name_and_type_index;

        @Override
        public int read(byte[] bytes, int from) {
            if (bytes[from] != tag)
                throw new IllegalStateException(bytes[from] + " != " + tag + " CONSTANT_InterfaceMethodref");

            from++;
            class_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            name_and_type_index = ByteCodeParser.readInt2(from, bytes);
            from += 2;
            return from;
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            out.write(tag);
            ByteCodeParser.writeInt2(class_index, out);
            ByteCodeParser.writeInt2(name_and_type_index, out);
        }

        @Override
        public String toString() {
            return "CONSTANT_InterfaceMethodref_info{" +
                    "tag=" + tag +
                    ", class_index=" + class_index +
                    ", name_and_type_index=" + name_and_type_index +
                    '}';
        }
    }
}
