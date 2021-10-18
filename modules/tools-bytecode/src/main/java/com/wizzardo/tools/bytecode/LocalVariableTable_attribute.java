package com.wizzardo.tools.bytecode;

import java.util.Arrays;

class LocalVariableTable_attribute extends AttributeInfo {
    //            int attribute_name_index;
//            int attribute_length; //4
    int local_variable_table_length;
    LocalVariable[] local_variable_table;

    static class LocalVariable {
        int start_pc;
        int length;
        int name_index;
        int descriptor_index;
        int index;

        @Override
        public String toString() {
            return "LocalVariable{" +
                    "start_pc=" + start_pc +
                    ", length=" + length +
                    ", name_index=" + name_index +
                    ", descriptor_index=" + descriptor_index +
                    ", index=" + index +
                    '}';
        }
    }

    @Override
    int read(byte[] bytes, int from) {
        int result = super.read(bytes, from);
        int position = 0;
        local_variable_table_length = ByteCodeParser.readInt2(position, info);
        position += 2;

        local_variable_table = new LocalVariable[local_variable_table_length];
        for (int i = 0; i < local_variable_table_length; i++) {
            LocalVariable lv = new LocalVariable();
            local_variable_table[i] = lv;

            lv.start_pc = ByteCodeParser.readInt2(position, info);
            position += 2;
            lv.length = ByteCodeParser.readInt2(position, info);
            position += 2;
            lv.name_index = ByteCodeParser.readInt2(position, info);
            position += 2;
            lv.descriptor_index = ByteCodeParser.readInt2(position, info);
            position += 2;
            lv.index = ByteCodeParser.readInt2(position, info);
            position += 2;
        }

        return result;
    }

    @Override
    public String toString() {
        return "LocalVariableTable_attribute{" +
                "local_variable_table_length=" + local_variable_table_length +
                ", local_variable_table=" + Arrays.toString(local_variable_table) +
                '}';
    }
}
