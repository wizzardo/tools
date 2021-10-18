package com.wizzardo.tools.bytecode;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

class StackMapTable_attribute extends AttributeInfo {
    //            int attribute_name_index;
//            int attribute_length; //4
    int number_of_entries;
    StackMapFrame[] entries;


    public int updateLength() {
        int entriesLength = Arrays.stream(entries).mapToInt(StackMapFrame::length).sum();
        return 2 + entriesLength;
    }

    static class StackMapFrame {
        int frame_type;

        public int getType() {
            return frame_type;
        }

        int length() {
            return 1;
        }

        public void write(ByteArrayOutputStream out) {
            out.write(frame_type);
        }
    }

    static class same_frame extends StackMapFrame {
    }

    static class same_locals_1_stack_item_frame extends StackMapFrame {
    }

    static class same_locals_1_stack_item_frame_extended extends StackMapFrame {
    }

    static class chop_frame extends StackMapFrame {
    }

    static class same_frame_extended extends StackMapFrame {
    }

    static class append_frame extends StackMapFrame {
        int offset_delta;
        verification_type_info[] locals;

        @Override
        public int getType() {
            return locals.length + 251;
        }

        @Override
        int length() {
            return 1 + 2 + Arrays.stream(locals).mapToInt(it -> 1 + it.additionalBytes).sum();
        }

        @Override
        public void write(ByteArrayOutputStream out) {
            super.write(out);
            ByteCodeParser.writeInt2(offset_delta, out);
            for (verification_type_info local : locals) {
                local.write(out);
            }
        }
    }

    static class full_frame extends StackMapFrame {
    }

    static class verification_type_info {
        int additionalBytes = 0;
        int tag;

        static verification_type_info byTag(int tag) {
            if (tag == 0)
                return new verification_type_info.Top_variable_info();
            if (tag == 1)
                return new verification_type_info.Integer_variable_info();
            if (tag == 2)
                return new verification_type_info.Float_variable_info();
            if (tag == 3)
                return new verification_type_info.Double_variable_info();
            if (tag == 4)
                return new verification_type_info.Long_variable_info();
            if (tag == 5)
                return new verification_type_info.Null_variable_info();
            if (tag == 6)
                return new verification_type_info.UninitializedThis_variable_info();
            if (tag == 7)
                return new verification_type_info.Object_variable_info();
            if (tag == 8)
                return new verification_type_info.Uninitialized_variable_info();

            throw new IllegalArgumentException("Unknown tag " + tag);
        }

        public void read(byte[] info, int position) {
        }

        public void write(ByteArrayOutputStream out) {
            out.write(tag);
        }

        static class Top_variable_info extends verification_type_info {
            {
                tag = 0;
            }
        }

        static class Integer_variable_info extends verification_type_info {
            {
                tag = 1;
            }
        }

        static class Float_variable_info extends verification_type_info {
            {
                tag = 2;
            }
        }

        static class Long_variable_info extends verification_type_info {
            {
                tag = 4;
            }
        }

        static class Double_variable_info extends verification_type_info {
            {
                tag = 3;
            }
        }

        static class Null_variable_info extends verification_type_info {
            {
                tag = 5;
            }
        }

        static class UninitializedThis_variable_info extends verification_type_info {
            {
                tag = 6;
            }
        }

        static class Object_variable_info extends verification_type_info {
            int cpool_index;

            {
                tag = 7;
                additionalBytes = 2;
            }

            public Object_variable_info() {
            }

            public Object_variable_info(int cpool_index) {
                this.cpool_index = cpool_index;
            }

            @Override
            public void read(byte[] info, int position) {
                cpool_index = ByteCodeParser.readInt2(position, info);
            }

            @Override
            public void write(ByteArrayOutputStream out) {
                super.write(out);
                ByteCodeParser.writeInt2(cpool_index, out);
            }
        }

        static class Uninitialized_variable_info extends verification_type_info {
            int offset;

            {
                tag = 8;
                additionalBytes = 2;
            }

            @Override
            public void read(byte[] info, int position) {
                offset = ByteCodeParser.readInt2(position, info);
            }

            @Override
            public void write(ByteArrayOutputStream out) {
                super.write(out);
                ByteCodeParser.writeInt2(offset, out);
            }
        }
    }


    @Override
    int read(byte[] bytes, int from) {
        int result = super.read(bytes, from);
        int position = 0;
        number_of_entries = ByteCodeParser.readInt2(position, info);
        position += 2;

        entries = new StackMapFrame[number_of_entries];
        for (int i = 0; i < number_of_entries; i++) {
            StackMapFrame frame = null;
            int type = info[position] & 0xff;
            position++;
            if (type <= 63) {
                frame = new same_frame();
            } else if (type <= 127) {
                frame = new same_locals_1_stack_item_frame();
                throw new IllegalStateException("Not Implemented yet");
            } else if (type == 247) {
                frame = new same_locals_1_stack_item_frame_extended();
                throw new IllegalStateException("Not Implemented yet");
            } else if (type >= 248 && type <= 250) {
                frame = new chop_frame();
                throw new IllegalStateException("Not Implemented yet");
            } else if (type == 251) {
                frame = new same_frame_extended();
                throw new IllegalStateException("Not Implemented yet");
            } else if (type >= 252 && type <= 254) {
                append_frame f = new append_frame();
                frame = f;
                f.locals = new verification_type_info[type - 251];
                f.offset_delta = ByteCodeParser.readInt2(position, info);
                position += 2;
                for (int j = 0; j < f.locals.length; j++) {
                    int tag = info[position] & 0xff;
                    position++;
                    verification_type_info type_info = verification_type_info.byTag(tag);
                    f.locals[j] = type_info;
                    if (type_info.additionalBytes > 0) {
                        type_info.read(info, position);
                        j += type_info.additionalBytes;
                    }
                }

            } else if (type == 255) {
                frame = new full_frame();
                throw new IllegalStateException("Not Implemented yet");
            }
            frame.frame_type = type;
            entries[i] = frame;

//                    frame.start_pc = readInt2(position, info);
//                    position += 2;

        }

        return result;
    }

    public void write(ByteArrayOutputStream out) {
        ByteCodeParser.writeInt2(attribute_name_index, out);

        attribute_length = updateLength();
        ByteCodeParser.writeInt4(attribute_length, out);
//        if (info != null) {
//            System.out.println(Arrays.toString(info));
//        }
//
//        ByteArrayOutputStream test = new ByteArrayOutputStream();
//        ByteCodeParser.writeInt2(number_of_entries, test);
//        for (StackMapFrame entry : entries) {
//            entry.write(test);
//        }
//        System.out.println(Arrays.toString(test.toByteArray()));
//        System.out.println();


        ByteCodeParser.writeInt2(number_of_entries, out);
        for (StackMapFrame entry : entries) {
            entry.write(out);
        }
    }
}
