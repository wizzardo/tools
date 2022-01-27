package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Predicate;

class ByteCodeParser {
    protected final static int[] EMPTY_INTERFACES = new int[0];
    protected final static ConstantPoolInfo.ConstantInfo[] EMPTY_CONSTANT_POOL = new ConstantPoolInfo.ConstantInfo[1];

    int minorVersion;
    int majorVersion;
    int constantPoolCount = 1;
    ConstantPoolInfo.ConstantInfo[] constantPool = EMPTY_CONSTANT_POOL;
    int accessFlags;
    int thisClass;
    int superClass;
    int interfacesCount;
    int[] interfaces = EMPTY_INTERFACES;
    int fieldsCount;
    FieldInfo[] fields = FieldInfo.EMPTY_FIELDS;
    int methodsCount;
    MethodInfo[] methods = MethodInfo.EMPTY_METHODS;
    int attributesCount;
    AttributeInfo[] attributes = AttributeInfo.EMPTY_ATTRIBUTES;

    byte[] bytes;

    ByteCodeParser() {
    }

    public ByteCodeParser(byte[] bytes) {
        this.bytes = bytes;
    }

    public void read() {
        int position = 0;

        position = 4;

        minorVersion = readInt2(position, bytes);
        System.out.println("minorVersion: " + minorVersion);
        position += 2;

        majorVersion = readInt2(position, bytes);
        System.out.println("majorVersion: " + majorVersion);
        position += 2;

        constantPoolCount = readInt2(position, bytes);
        position += 2;
        constantPool = new ConstantPoolInfo.ConstantInfo[constantPoolCount];
        System.out.println("Constant pool:");
        for (int i = 1; i < constantPool.length; i++) {
            byte tag = bytes[position];
            ConstantPoolInfo.ConstantInfo info = ConstantPoolInfo.byTag(tag);
            if (info == null) {
                throw new IllegalStateException("Unknown ConstantPoolInfo tag: " + tag);
            }
            constantPool[i] = info;
            position = info.read(bytes, position);
            System.out.println("\t" + i + ": " + info);
            if (tag == ConstantPoolInfo.CONSTANT_Long || tag == ConstantPoolInfo.CONSTANT_Double)
                i++;
        }

        accessFlags = readInt2(position, bytes);
        System.out.println("accessFlags: " + accessFlags);
        position += 2;

        thisClass = readInt2(position, bytes);
        System.out.println("thisClass: " + thisClass);
        position += 2;

        superClass = readInt2(position, bytes);
        System.out.println("superClass: " + superClass);
        position += 2;

        interfacesCount = readInt2(position, bytes);
        System.out.println("interfacesCount: " + interfacesCount);
        position += 2;
        interfaces = new int[interfacesCount];
        for (int i = 0; i < interfacesCount; i++) {
            int index = readInt2(position, bytes);
            interfaces[i] = index;
            System.out.println("\t" + index);
            position += 2;
        }


        fieldsCount = readInt2(position, bytes);
        System.out.println("fieldsCount: " + fieldsCount);
        position += 2;

        fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            FieldInfo fi = new FieldInfo();
            fields[i] = fi;

            fi.access_flags = readInt2(position, bytes);
            position += 2;
            fi.name_index = readInt2(position, bytes);
            position += 2;
            fi.descriptor_index = readInt2(position, bytes);
            position += 2;
            fi.attributes_count = readInt2(position, bytes);
            position += 2;

            fi.attributes = new AttributeInfo[fi.attributes_count];
            for (int j = 0; j < fi.attributes_count; j++) {
                AttributeInfo ai = new AttributeInfo();
                fi.attributes[j] = ai;
                position = ai.read(bytes, position);
            }
            System.out.println(fi);
        }

        methodsCount = readInt2(position, bytes);
        System.out.println("methodsCount: " + methodsCount);
        position += 2;

        methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            MethodInfo mi = new MethodInfo();
            methods[i] = mi;

            mi.access_flags = readInt2(position, bytes);
            position += 2;
            mi.name_index = readInt2(position, bytes);
            position += 2;
            mi.descriptor_index = readInt2(position, bytes);
            position += 2;
            mi.attributes_count = readInt2(position, bytes);
            position += 2;

            mi.attributes = new AttributeInfo[mi.attributes_count];
            for (int j = 0; j < mi.attributes_count; j++) {
                int nameIndex = readInt2(position, bytes);
                String name = getName(nameIndex);
                AttributeInfo ai = AttributeInfo.byName(name, this);

                mi.attributes[j] = ai;
                position = ai.read(bytes, position);
            }
            System.out.println(mi);
        }

        attributesCount = readInt2(position, bytes);
        System.out.println("attributesCount: " + attributesCount);
        position += 2;

        attributes = new AttributeInfo[attributesCount];
        for (int j = 0; j < attributesCount; j++) {
            int nameIndex = readInt2(position, bytes);
            String name = getName(nameIndex);
            AttributeInfo ai = AttributeInfo.byName(name, this);

            attributes[j] = ai;
            position = ai.read(bytes, position);

            System.out.println(ai);
        }
    }

    protected String getName(int nameIndex) {
        ConstantPoolInfo.ConstantInfo info = constantPool[nameIndex];
        return new String(((ConstantPoolInfo.CONSTANT_Utf8_info) info).bytes, StandardCharsets.UTF_8);
    }

    public byte[] write() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeInt4(0xCAFEBABE, out);
        writeInt2(minorVersion, out);
        writeInt2(majorVersion, out);
        writeInt2(constantPoolCount, out);
        for (int i = 1; i < constantPool.length; i++) {
            ConstantPoolInfo.ConstantInfo constantInfo = constantPool[i];
            if (constantInfo != null)
                constantInfo.write(out);
        }
        writeInt2(accessFlags, out);
        writeInt2(thisClass, out);
        writeInt2(superClass, out);
        writeInt2(interfacesCount, out);
        for (int anInterface : interfaces) {
            writeInt2(anInterface, out);
        }
        writeInt2(fieldsCount, out);
        for (FieldInfo field : fields) {
            field.write(out);
        }
        writeInt2(methodsCount, out);
        for (MethodInfo method : methods) {
            method.write(out);
        }
        writeInt2(attributesCount, out);
        for (AttributeInfo attribute : attributes) {
            attribute.write(out);
        }
        return out.toByteArray();
    }

    static int readInt2(int position, byte[] bytes) {
        return ((bytes[position] & 0xff) << 8) + (bytes[position + 1] & 0xff);
    }

    static int readInt4(int position, byte[] bytes) {
        return ((bytes[position] & 0xff) << 24)
                + ((bytes[position + 1] & 0xff) << 16)
                + ((bytes[position + 2] & 0xff) << 8)
                + (bytes[position + 3] & 0xff);
    }

    static void writeInt4(int i, ByteArrayOutputStream out) {
        out.write((i >> 24) & 0xff);
        out.write((i >> 16) & 0xff);
        out.write((i >> 8) & 0xff);
        out.write(i & 0xff);
    }

    static void writeInt2(int i, ByteArrayOutputStream out) {
        out.write((i >> 8) & 0xff);
        out.write(i & 0xff);
    }

    static byte[] int2toBytes(int i) {
        return new byte[]{(byte) ((i >> 8) & 0xff), (byte) (i & 0xff)};
    }

    static byte[] int1toBytes(int i) {
        return new byte[]{(byte) (i & 0xff)};
    }

    public int findUtf8ConstantIndex(byte[] bytes) {
        return findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Utf8_info && Arrays.equals(bytes, ((ConstantPoolInfo.CONSTANT_Utf8_info) ci).bytes));
    }

    public int findIntegerConstantIndex(int value) {
        return findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Integer_info && ((ConstantPoolInfo.CONSTANT_Integer_info) ci).value == value);
    }

    public int findLongConstantIndex(long value) {
        return findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Long_info && ((ConstantPoolInfo.CONSTANT_Long_info) ci).value == value);
    }

    public int findFloatConstantIndex(float value) {
        return findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Float_info && ((ConstantPoolInfo.CONSTANT_Float_info) ci).value == value);
    }

    public int findDoubleConstantIndex(double value) {
        return findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Double_info && ((ConstantPoolInfo.CONSTANT_Double_info) ci).value == value);
    }

    public int findConstantIndex(Predicate<ConstantPoolInfo.ConstantInfo> predicate) {
        for (int i = 1; i < constantPool.length; i++) {
            if (predicate.test(constantPool[i])) {
                return i;
            }
        }
        return -1;
    }

    public int findMethodIndex(Predicate<MethodInfo> predicate) {
        for (int i = 0; i < methods.length; i++) {
            if (predicate.test(methods[i])) {
                return i;
            }
        }
        return -1;
    }

    public int findFieldIndex(Predicate<FieldInfo> predicate) {
        for (int i = 0; i < fields.length; i++) {
            if (predicate.test(fields[i])) {
                return i;
            }
        }
        return -1;
    }
}
