package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;

public class Signature_attribute extends AttributeInfo {
    int signature_index;

    @Override
    int read(byte[] bytes, int from) {
        int result = super.read(bytes, from);
        signature_index = ByteCodeParser.readInt2(0, info);
        return result;
    }

    @Override
    public String toString() {
        return "Signature_attribute{" +
                "signature_index=" + signature_index +
                '}';
    }

    @Override
    public void write(ByteArrayOutputStream out) {
        ByteCodeParser.writeInt2(attribute_name_index, out);
        ByteCodeParser.writeInt4(2, out);
        ByteCodeParser.writeInt2(signature_index, out);
    }
}
