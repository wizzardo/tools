package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Code_attribute extends AttributeInfo {
    //            u2 attribute_name_index;
    //            u4 attribute_length;
    int max_stack;
    int max_locals;
    int code_length; //4
    byte[] code;
    int exception_table_length;
    ExceptionInfo[] exception_table = ExceptionInfo.EMPTY_EXCEPTION_TABLE;
    int attributes_count;
    AttributeInfo[] attributes = EMPTY_ATTRIBUTES;

    final ByteCodeParser reader;

    Code_attribute(ByteCodeParser reader) {
        this.reader = reader;
    }

    public int updateLength() {
        return 2 + 2 + 4 + code.length + 2 + 8 * exception_table.length + 2 + Arrays.stream(attributes).mapToInt(it -> it.attribute_length + 6).sum();
    }

    static class ExceptionInfo {
        public final static ExceptionInfo[] EMPTY_EXCEPTION_TABLE = new ExceptionInfo[0];

        int start_pc;
        int end_pc;
        int handler_pc;
        int catch_type;
    }

    @Override
    int read(byte[] bytes, int from) {
        int result = super.read(bytes, from);
        int position = 0;
        max_stack = ByteCodeParser.readInt2(position, info);
        position += 2;
        max_locals = ByteCodeParser.readInt2(position, info);
        position += 2;
        code_length = ByteCodeParser.readInt4(position, info);
        position += 4;

        code = new byte[code_length];
        System.arraycopy(info, position, code, 0, code_length);
        position += code_length;

        exception_table_length = ByteCodeParser.readInt2(position, info);
        position += 2;
        exception_table = new ExceptionInfo[exception_table_length];
        for (int i = 0; i < exception_table_length; i++) {
            ExceptionInfo ei = new ExceptionInfo();
            exception_table[i] = ei;

            ei.start_pc = ByteCodeParser.readInt2(position, info);
            position += 2;
            ei.end_pc = ByteCodeParser.readInt2(position, info);
            position += 2;
            ei.handler_pc = ByteCodeParser.readInt2(position, info);
            position += 2;
            ei.catch_type = ByteCodeParser.readInt2(position, info);
            position += 2;
        }

        attributes_count = ByteCodeParser.readInt2(position, info);
        position += 2;
        attributes = new AttributeInfo[attributes_count];
        for (int j = 0; j < attributes_count; j++) {
            int nameIndex = ByteCodeParser.readInt2(position, info);
            AttributeInfo ai = AttributeInfo.byName(reader.getName(nameIndex), reader);

            attributes[j] = ai;
            position = ai.read(info, position);
        }

        return result;
    }

    @Override
    public void write(ByteArrayOutputStream out) {
        ByteCodeParser.writeInt2(attribute_name_index, out);
        ByteCodeParser.writeInt4(attribute_length, out);
//                out.write(info, 0, info.length);
        ByteCodeParser.writeInt2(max_stack, out);
        ByteCodeParser.writeInt2(max_locals, out);
        ByteCodeParser.writeInt4(code_length, out);
        out.write(code, 0, code.length);
        ByteCodeParser.writeInt2(exception_table_length, out);
        for (ExceptionInfo ei : exception_table) {
            ByteCodeParser.writeInt2(ei.start_pc, out);
            ByteCodeParser.writeInt2(ei.end_pc, out);
            ByteCodeParser.writeInt2(ei.handler_pc, out);
            ByteCodeParser.writeInt2(ei.catch_type, out);
        }
        ByteCodeParser.writeInt2(attributes_count, out);
        for (AttributeInfo a : attributes) {
            a.write(out);
        }
    }

    @Override
    public String toString() {
        return "Code_attribute{" +
                "attribute_name_index=" + attribute_name_index +
                ", max_stack=" + max_stack +
                ", max_locals=" + max_locals +
                ", code_length=" + code_length +
                ", code=" + Arrays.toString(code) +
                "\n" + readCode() + "\n" +
                ", exception_table_length=" + exception_table_length +
                ", exception_table=" + Arrays.toString(exception_table) +
                ", attributes_count=" + attributes_count +
                ", attributes=" + Arrays.toString(attributes) +
                '}';
    }

    private String readCode() {
        List<Operation> list = new ArrayList<>();
        for (int i = 0; i < code.length; i++) {
            byte b = code[i];
            Instruction instruction = Instruction.byCode(b & 0xff);
            if (instruction.operands == 0)
                list.add(new Operation(instruction));
            else {
                byte[] args = new byte[instruction.operands];
                System.arraycopy(code, i + 1, args, 0, args.length);
                i += args.length;
                list.add(new Operation(instruction, args));
            }
        }
        return list.stream().map(it -> it.toString()).collect(Collectors.joining("\n"));
    }
}
