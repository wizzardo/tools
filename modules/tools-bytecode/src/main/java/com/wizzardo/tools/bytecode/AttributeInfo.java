package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

class AttributeInfo {
    public static final AttributeInfo[] EMPTY_ATTRIBUTES = new AttributeInfo[0];

    int attribute_name_index;
    int attribute_length; //4
    byte[] info;

    int read(byte[] bytes, int from) {
        attribute_name_index = ByteCodeParser.readInt2(from, bytes);
        from += 2;

        attribute_length = ByteCodeParser.readInt4(from, bytes);
        from += 4;

        info = new byte[attribute_length];
        System.arraycopy(bytes, from, info, 0, attribute_length);
        from += attribute_length;
        return from;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "attribute_name_index=" + attribute_name_index +
                ", attribute_length=" + attribute_length +
                ", info=" + Arrays.toString(info) +
                '}';
    }

    public void write(ByteArrayOutputStream out) {
        ByteCodeParser.writeInt2(attribute_name_index, out);
        ByteCodeParser.writeInt4(attribute_length, out);
        out.write(info, 0, info.length);
    }

    static AttributeInfo byName(String name, ByteCodeParser reader) {
        if (name.equals("Code"))
            return new Code_attribute(reader);
        if (name.equals("LocalVariableTable"))
            return new LocalVariableTable_attribute();
        if (name.equals("StackMapTable"))
            return new StackMapTable_attribute();
        if (name.equals("Signature"))
            return new Signature_attribute();

        return new AttributeInfo();
    }
}
