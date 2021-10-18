package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

class MethodInfo {
    public static final MethodInfo[] EMPTY_METHODS = new MethodInfo[0];

    int access_flags;
    int name_index;
    int descriptor_index;
    int attributes_count;
    AttributeInfo[] attributes = AttributeInfo.EMPTY_ATTRIBUTES;

    @Override
    public String toString() {
        return "MethodInfo{" +
                "access_flags=" + access_flags +
                ", name_index=" + name_index +
                ", descriptor_index=" + descriptor_index +
                ", attributes_count=" + attributes_count +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    public void write(ByteArrayOutputStream out) {
        ByteCodeParser.writeInt2(access_flags, out);
        ByteCodeParser.writeInt2(name_index, out);
        ByteCodeParser.writeInt2(descriptor_index, out);
        ByteCodeParser.writeInt2(attributes_count, out);
        for (AttributeInfo attribute : attributes) {
            attribute.write(out);
        }
    }
}
