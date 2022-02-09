package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.interfaces.Consumer;
import com.wizzardo.tools.interfaces.Mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.wizzardo.tools.bytecode.ByteCodeParser.int1toBytes;
import static com.wizzardo.tools.bytecode.ByteCodeParser.int2toBytes;

public class ClassBuilder {
    public static final Class<?>[] EMPTY_ARGS = new Class[0];
    protected ByteCodeParser reader = new ByteCodeParser();
    protected List<FieldDescription> fields = new ArrayList<>();

    static class FieldDescription<T> {
        final String name;
        final Class<T> type;
        final Consumer<CodeBuilder> initializer;

        FieldDescription(String name, Class<T> type, Consumer<CodeBuilder> initializer) {
            this.name = name;
            this.type = type;
            this.initializer = initializer;
        }
    }

    {
        reader.majorVersion = 52;
        reader.accessFlags = AccessFlags.ACC_PUBLIC;
    }

    public ClassBuilder setClassFullName(String name) {
        if (reader.thisClass != 0)
            throw new IllegalStateException("thisClass is already set");

        reader.thisClass = getOrCreateClassConstant(name);
        return this;
    }

    public ClassBuilder setSuperClassFullName(String name) {
        if (reader.superClass != 0)
            throw new IllegalStateException("superClass is already set");

        reader.superClass = getOrCreateClassConstant(name);
        return this;
    }

    public ClassBuilder setSuperClass(Class<?> clazz) {
        return setSuperClassFullName(clazz.getTypeName());
    }

    public ClassBuilder withDefaultConstructor() {
        if (reader.thisClass == 0)
            throw new IllegalStateException("thisClass is not set");
        if (reader.superClass == 0)
            throw new IllegalStateException("superClass is not set");

        int nameIndex = getOrCreateUtf8Constant("<init>");
        int descriptorIndex = getOrCreateUtf8Constant("()V");
        if (reader.findMethodIndex(m -> m.name_index == nameIndex && m.descriptor_index == descriptorIndex) != -1)
            throw new IllegalStateException("Default constructor is already set");

        MethodInfo methodInfo = new MethodInfo();
        methodInfo.name_index = nameIndex;
        methodInfo.descriptor_index = descriptorIndex;
        methodInfo.access_flags = AccessFlags.ACC_PUBLIC;
        methodInfo.attributes_count = 1;
        methodInfo.attributes = new AttributeInfo[1];

        Code_attribute codeAttribute = new Code_attribute(reader);
        methodInfo.attributes[0] = codeAttribute;

        codeAttribute.max_locals = 2;
        codeAttribute.max_stack = 6;
        codeAttribute.attribute_name_index = getOrCreateUtf8Constant("Code");

        int superConstructorNameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);

        int superConstructorIndex = getOrCreateMethodRef(reader.superClass, superConstructorNameAndTypeIndex);

        CodeBuilder code = new CodeBuilder()
                .append(Instruction.aload_0)
                .append(Instruction.invokespecial, ByteCodeParser.int2toBytes(superConstructorIndex));

        for (FieldDescription<?> field : fields) {
            if (field.initializer != null) {
                field.initializer.consume(code);
            }
        }

        code.append(Instruction.return_);

        codeAttribute.code = code.build();
        codeAttribute.code_length = codeAttribute.code.length;
        codeAttribute.attribute_length = codeAttribute.updateLength();

//            ByteCodeReader.LocalVariableTable_attribute localVariableTableAttribute = new ByteCodeReader.LocalVariableTable_attribute();
//            codeAttribute.attributes[0] = localVariableTableAttribute;
//            localVariableTableAttribute.local_variable_table_length = 1;
//            localVariableTableAttribute.local_variable_table = new ByteCodeReader.LocalVariableTable_attribute.LocalVariable[1];
//
//            ByteCodeReader.LocalVariableTable_attribute.LocalVariable localVariable = new ByteCodeReader.LocalVariableTable_attribute.LocalVariable();
//            localVariableTableAttribute.local_variable_table[0] = localVariable;
//            localVariable.index = 0;
//            localVariable.name_index = getOrCreateUtf8Constant("this");
//
//            ByteCodeReader.ConstantPoolInfo.CONSTANT_Class_info thisClass = (ByteCodeReader.ConstantPoolInfo.CONSTANT_Class_info) reader.constantPool[reader.thisClass];
//            ByteCodeReader.ConstantPoolInfo.CONSTANT_Utf8_info className = (ByteCodeReader.ConstantPoolInfo.CONSTANT_Utf8_info) reader.constantPool[thisClass.name_index];
//            localVariable.descriptor_index = getOrCreateClassConstant("L" + new String(className.bytes, StandardCharsets.UTF_8) + ";");

        append(methodInfo);
        return this;
    }


    public void withSuperConstructor(Class<?>[] parameterTypes) {
        if (reader.thisClass == 0)
            throw new IllegalStateException("thisClass is not set");
        if (reader.superClass == 0)
            throw new IllegalStateException("superClass is not set");

        method("<init>", parameterTypes, void.class, b -> {
            Code_attribute codeAttribute = new Code_attribute(reader);
            codeAttribute.max_locals = 2 + (parameterTypes.length == 0 ? 0 : Arrays.stream(parameterTypes).mapToInt(it -> it == long.class || it == double.class ? 2 : 1).sum());
            codeAttribute.max_stack = 4 + parameterTypes.length * 2; // not optimal but should be ok
            codeAttribute.attribute_name_index = getOrCreateUtf8Constant("Code");

            int nameIndex = getOrCreateUtf8Constant("<init>");
            int descriptorIndex = getOrCreateUtf8Constant(getMethodDescriptor(parameterTypes, void.class));

            int superConstructorNameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);

            int superConstructorIndex = getOrCreateMethodRef(reader.superClass, superConstructorNameAndTypeIndex);

            CodeBuilder code = new CodeBuilder()
                    .append(Instruction.aload_0);

            if (parameterTypes.length != 0) {
                int localVarIndexOffset = 1;
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    if (parameterType.isPrimitive()) {
                        if (parameterType == int.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                        } else if (parameterType == long.class) {
                            code.append(Instruction.lload, int1toBytes(i + localVarIndexOffset++));
                        } else if (parameterType == byte.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                        } else if (parameterType == short.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                        } else if (parameterType == float.class) {
                            code.append(Instruction.fload, int1toBytes(i + localVarIndexOffset));
                        } else if (parameterType == double.class) {
                            code.append(Instruction.dload, int1toBytes(i + localVarIndexOffset++));
                        } else if (parameterType == boolean.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                        } else if (parameterType == char.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                        }
                    } else {
                        code.append(Instruction.aload, int1toBytes(i + 1));
                    }
                }
            }

            code.append(Instruction.invokespecial, ByteCodeParser.int2toBytes(superConstructorIndex));

            for (FieldDescription<?> field : fields) {
                if (field.initializer != null) {
                    field.initializer.consume(code);
                }
            }

            code.append(Instruction.return_);
            codeAttribute.code = code.build();
            codeAttribute.code_length = codeAttribute.code.length;
            codeAttribute.attribute_length = codeAttribute.updateLength();
            return codeAttribute;
        });
    }

    public ClassBuilder implement(Class<?>... interfaces) {
        if (interfaces != null)
            for (Class<?> anInterface : interfaces) {
                if (!anInterface.isInterface())
                    throw new IllegalArgumentException("Class " + anInterface.getCanonicalName() + " is not an interface");

                int index = getOrCreateClassConstant(anInterface);
                appendInterface(index);
            }
        return this;
    }

    public ClassBuilder field(String name, Class<?> type) {
        return field(name, type, null);
    }

    public <T> ClassBuilder field(String name, Class<T> type, T initValue) {
        String description = getFieldDescription(type);
        int nameIndex = getOrCreateUtf8Constant(name);
        int descriptorIndex = getOrCreateUtf8Constant(description);
        if (reader.findFieldIndex(fi -> fi.name_index == nameIndex && fi.descriptor_index == descriptorIndex) != -1)
            return this;

        fields.add(new FieldDescription<>(name, type, initValue == null ? null : code -> {
            code.append(Instruction.aload_0);
            addLoadConstant(code, initValue, type);
            code.append(Instruction.putfield, getOrCreateFieldRefBytes(name));
        }));

        FieldInfo fi = new FieldInfo();
        fi.access_flags = AccessFlags.ACC_PUBLIC;
        fi.name_index = nameIndex;
        fi.descriptor_index = descriptorIndex;
        append(fi);
        return this;
    }

    public <T> ClassBuilder fieldCallMethod(String name, Class<T> type, Method method, Object... args) {
        int methodRef = getOrCreateMethodRef(method);
        Class<?>[] types = method.getParameterTypes();
        return fieldCallMethod(name, type, code -> {
            code.append(Instruction.aload_0);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    Class<?> argType = types[i];
                    addLoadConstant(code, arg, argType);
                }
            }

            if ((method.getModifiers() & Modifier.STATIC) != 0)
                code.append(Instruction.invokestatic, ByteCodeParser.int2toBytes(methodRef));
            else
                code.append(Instruction.invokevirtual, ByteCodeParser.int2toBytes(methodRef));

            code.append(Instruction.putfield, getOrCreateFieldRefBytes(name));
        });
    }

    public <T> ClassBuilder fieldCallConstructor(String name, Class<T> type, Constructor constructor, Object... args) {
        int methodRef = getOrCreateMethodRef(constructor);
        Class<?>[] types = constructor.getParameterTypes();
        if (types.length != (args == null ? 0 : args.length))
            throw new IllegalArgumentException("Cannot invoke constructor " + constructor + ", args.length does not match");

        return fieldCallMethod(name, type, code -> {
            code.append(Instruction.aload_0);
            code.append(Instruction.new_, int2toBytes(getOrCreateClassConstant(constructor.getDeclaringClass())));
            code.append(Instruction.dup);

            for (int i = 0; i < types.length; i++) {
                Object arg = args[i];
                Class<?> argType = types[i];
                addLoadConstant(code, arg, argType);
            }

            code.append(Instruction.invokespecial, ByteCodeParser.int2toBytes(methodRef));
            code.append(Instruction.putfield, getOrCreateFieldRefBytes(name));
        });
    }

    private void addLoadConstant(CodeBuilder code, Object value, Class<?> type) {
        if (type.isPrimitive()) {
            int constant;
            if (type == int.class) {
                constant = getOrCreateIntegerConstant(((Number) value).intValue());
            } else if (type == char.class) {
                constant = getOrCreateIntegerConstant((Character) value);
            } else if (type == byte.class) {
                constant = getOrCreateIntegerConstant(((Number) value).intValue());
            } else if (type == short.class) {
                constant = getOrCreateIntegerConstant(((Number) value).intValue());
            } else if (type == long.class) {
                constant = getOrCreateLongConstant(((Number) value).longValue());
                code.append(Instruction.ldc2_w, int2toBytes(constant));
                return;
            } else if (type == float.class) {
                constant = getOrCreateFloatConstant(((Number) value).floatValue());
            } else if (type == double.class) {
                constant = getOrCreateDoubleConstant(((Number) value).doubleValue());
                code.append(Instruction.ldc2_w, int2toBytes(constant));
                return;
            } else if (type == boolean.class) {
                if (((Boolean) value)) {
                    code.append(Instruction.iconst_1);
                } else {
                    code.append(Instruction.iconst_0);
                }
                return;
            } else
                throw new IllegalStateException();

            if (constant < 255)
                code.append(Instruction.ldc, int1toBytes(constant));
            else
                code.append(Instruction.ldc_w, int2toBytes(constant));
        } else if (type.isEnum()) {
            code.append(Instruction.getstatic, int2toBytes(getOrCreateFieldRef((Enum) value)));
        } else if (type == String.class) {
            int constant = getOrCreateStringConstant(((String) value));
            if (constant < 255)
                code.append(Instruction.ldc, int1toBytes(constant));
            else
                code.append(Instruction.ldc_w, int2toBytes(constant));
        } else if (type == Object.class && (value instanceof Number || value instanceof Boolean)) {
            int constant = 0;
            boolean longDoubleConst = false;
            String methodName = "valueOf";
            Class boxed = null;
            Class primitive = null;
            if (value instanceof Integer) {
                constant = getOrCreateIntegerConstant((Integer) value);
                boxed = Integer.class;
                primitive = int.class;
            } else if (value instanceof Short) {
                constant = getOrCreateIntegerConstant((Short) value);
                boxed = Short.class;
                primitive = short.class;
            } else if (value instanceof Byte) {
                constant = getOrCreateIntegerConstant((Byte) value);
                boxed = Byte.class;
                primitive = byte.class;
            } else if (value instanceof Boolean) {
                constant = getOrCreateIntegerConstant((Boolean) value ? 1 : 0);
                boxed = Boolean.class;
                primitive = boolean.class;
            } else if (value instanceof Long) {
                constant = getOrCreateLongConstant((Long) value);
                boxed = Long.class;
                primitive = long.class;
                longDoubleConst = true;
            } else if (value instanceof Float) {
                constant = getOrCreateFloatConstant((Float) value);
                boxed = Float.class;
                primitive = float.class;
            } else if (value instanceof Double) {
                constant = getOrCreateDoubleConstant((Double) value);
                boxed = Double.class;
                primitive = double.class;
                longDoubleConst = true;
            }

            if (longDoubleConst) {
                code.append(Instruction.ldc2_w, int2toBytes(constant));
            } else if (constant < 255)
                code.append(Instruction.ldc, int1toBytes(constant));
            else
                code.append(Instruction.ldc_w, int2toBytes(constant));
            int methodRef = getOrCreateMethodRef(getOrCreateClassConstant(boxed), methodName, new Class[]{primitive}, boxed);
            code.append(Instruction.invokestatic, ByteCodeParser.int2toBytes(methodRef));
        } else
            throw new IllegalStateException();
    }

    protected <T> ClassBuilder fieldCallMethod(String name, Class<T> type, Consumer<CodeBuilder> initializer) {
        String description = getFieldDescription(type);
        int nameIndex = getOrCreateUtf8Constant(name);
        int descriptorIndex = getOrCreateUtf8Constant(description);
        if (reader.findFieldIndex(fi -> fi.name_index == nameIndex && fi.descriptor_index == descriptorIndex) != -1)
            return this;

        fields.add(new FieldDescription<>(name, type, initializer));

        FieldInfo fi = new FieldInfo();
        fi.access_flags = AccessFlags.ACC_PUBLIC;
        fi.name_index = nameIndex;
        fi.descriptor_index = descriptorIndex;
        append(fi);
        return this;
    }

    public byte[] getOrCreateFieldRefBytes(String name) {
        return ByteCodeParser.int2toBytes(getOrCreateFieldRef(name));
    }

    public int getOrCreateFieldRef(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int nameIndex = reader.findUtf8ConstantIndex(nameBytes);
        if (nameIndex == -1)
            throw new IllegalArgumentException("There is no field with name '" + name + "'");

        int fieldIndex = reader.findFieldIndex(fi -> fi.name_index == nameIndex);
        if (fieldIndex == -1)
            throw new IllegalArgumentException("There is no field with name '" + name + "'");

        FieldInfo field = reader.fields[fieldIndex];

        int fieldNameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, field.descriptor_index);

        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Fieldref_info
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).class_index == reader.thisClass
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).name_and_type_index == fieldNameAndTypeIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Fieldref_info fieldrefInfo = new ConstantPoolInfo.CONSTANT_Fieldref_info();
        fieldrefInfo.class_index = reader.thisClass;
        fieldrefInfo.name_and_type_index = fieldNameAndTypeIndex;
        return append(fieldrefInfo);
    }

    public byte[] getOrCreateFieldRefBytes(Class<?> clazz, String name) throws NoSuchFieldException {
        return ByteCodeParser.int2toBytes(getOrCreateFieldRef(clazz, name));
    }

    public byte[] getOrCreateFieldRefBytes(Field field) {
        return ByteCodeParser.int2toBytes(getOrCreateFieldRef(field));
    }

    public int getOrCreateFieldRef(Class<?> clazz, String name) throws NoSuchFieldException {
        return getOrCreateFieldRef(clazz.getDeclaredField(name));
    }

    public int getOrCreateFieldRef(Field field) {
        int classIndex = getOrCreateClassConstant(field.getDeclaringClass());
        int nameIndex = getOrCreateUtf8Constant(field.getName());
        int descriptorIndex = getOrCreateUtf8Constant(getFieldDescription(field.getType()));

        int fieldNameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);

        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Fieldref_info
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).class_index == classIndex
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).name_and_type_index == fieldNameAndTypeIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Fieldref_info fieldrefInfo = new ConstantPoolInfo.CONSTANT_Fieldref_info();
        fieldrefInfo.class_index = classIndex;
        fieldrefInfo.name_and_type_index = fieldNameAndTypeIndex;
        return append(fieldrefInfo);
    }

    public <E extends Enum<E>> int getOrCreateFieldRef(E e) {
        int classIndex = getOrCreateClassConstant(e.getDeclaringClass());
        int nameIndex = getOrCreateUtf8Constant(e.name());
        int descriptorIndex = getOrCreateUtf8Constant(getFieldDescription(e.getDeclaringClass()));

        int fieldNameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);

        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Fieldref_info
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).class_index == classIndex
                && ((ConstantPoolInfo.CONSTANT_Fieldref_info) ci).name_and_type_index == fieldNameAndTypeIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Fieldref_info fieldrefInfo = new ConstantPoolInfo.CONSTANT_Fieldref_info();
        fieldrefInfo.class_index = classIndex;
        fieldrefInfo.name_and_type_index = fieldNameAndTypeIndex;
        return append(fieldrefInfo);
    }

    public byte[] getOrCreateInterfaceMethodRefBytes(Class<?> clazz, String name, Class<?>[] args) throws NoSuchMethodException {
        return getOrCreateInterfaceMethodRefBytes(clazz.getDeclaredMethod(name, args));
    }

    public byte[] getOrCreateInterfaceMethodRefBytes(Method method) {
        int methodRef = getOrCreateInterfaceMethodRef(method);
        return new byte[]{(byte) ((methodRef >> 8) & 0xff), (byte) (methodRef & 0xff), (byte) (method.getParameterCount() + 1), 0};
    }

    public int getOrCreateInterfaceMethodRef(Method method) {
        if (!method.getDeclaringClass().isInterface())
            throw new IllegalArgumentException("DeclaringClass of the method " + method + " is not an interface");

        int classIndex = getOrCreateClassConstant(method.getDeclaringClass());
        int nameIndex = getOrCreateUtf8Constant(method.getName());
        String descriptor = getMethodDescriptor(method);
        int descriptorIndex = getOrCreateUtf8Constant(descriptor);

        int nameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);
        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_InterfaceMethodref_info
                && ((ConstantPoolInfo.CONSTANT_InterfaceMethodref_info) ci).class_index == classIndex
                && ((ConstantPoolInfo.CONSTANT_InterfaceMethodref_info) ci).name_and_type_index == nameAndTypeIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_InterfaceMethodref_info c = new ConstantPoolInfo.CONSTANT_InterfaceMethodref_info();
        c.class_index = classIndex;
        c.name_and_type_index = nameAndTypeIndex;
        return append(c);
    }

    public byte[] getOrCreateMethodRefBytes(Class<?> clazz, String name, Class<?>[] args) throws NoSuchMethodException {
        return getOrCreateMethodRefBytes(clazz.getDeclaredMethod(name, args));
    }

    public byte[] getOrCreateMethodRefBytes(Method method) {
        return ByteCodeParser.int2toBytes(getOrCreateMethodRef(method));
    }

    public int getOrCreateMethodRef(Method method) {
        if (method.getDeclaringClass().isInterface())
            throw new IllegalArgumentException("DeclaringClass of the method " + method + " is an interface!");

        int classIndex = getOrCreateClassConstant(method.getDeclaringClass());
        return getOrCreateMethodRef(classIndex, method.getName(), method.getParameterTypes(), method.getReturnType());
    }

    public int getOrCreateMethodRef(Constructor constructor) {
        int classIndex = getOrCreateClassConstant(constructor.getDeclaringClass());
        return getOrCreateMethodRef(classIndex, "<init>", constructor.getParameterTypes(), void.class);
    }

    public int getOrCreateMethodRef(int classIndex, String name, Class<?>[] args, Class<?> returnType) {
        int nameIndex = getOrCreateUtf8Constant(name);
        String descriptor = getMethodDescriptor(args, returnType);
        int descriptorIndex = getOrCreateUtf8Constant(descriptor);
        int nameAndTypeIndex = getOrCreateNameAndTypeConstant(nameIndex, descriptorIndex);
        return getOrCreateMethodRef(classIndex, nameAndTypeIndex);
    }

    public int getOrCreateMethodRef(int classIndex, int nameAndTypeIndex) {
        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Methodref_info
                && ((ConstantPoolInfo.CONSTANT_Methodref_info) ci).class_index == classIndex
                && ((ConstantPoolInfo.CONSTANT_Methodref_info) ci).name_and_type_index == nameAndTypeIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Methodref_info c = new ConstantPoolInfo.CONSTANT_Methodref_info();
        c.class_index = classIndex;
        c.name_and_type_index = nameAndTypeIndex;
        return append(c);
    }

    public ClassBuilder fieldSetter(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        int nameIndex = reader.findUtf8ConstantIndex(nameBytes);
        if (nameIndex == -1)
            throw new IllegalArgumentException("There is no field with name '" + name + "'");

        String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        int setterNameIndex = getOrCreateUtf8Constant(setterName);

        int fieldIndex = reader.findFieldIndex(fi -> fi.name_index == nameIndex);
        if (fieldIndex == -1)
            throw new IllegalArgumentException("There is no field with name '" + name + "'");

        FieldInfo field = reader.fields[fieldIndex];
        ConstantPoolInfo.CONSTANT_Utf8_info fieldDescriptor = (ConstantPoolInfo.CONSTANT_Utf8_info) reader.constantPool[field.descriptor_index];

        String setterDescriptor = "(" + new String(fieldDescriptor.bytes, StandardCharsets.UTF_8) + ")V";
        int setterDescriptorIndex = getOrCreateUtf8Constant(setterDescriptor);

        if (reader.findMethodIndex(mi -> mi.name_index == setterNameIndex && mi.descriptor_index == setterDescriptorIndex) != -1)
            return this;

        MethodInfo mi = new MethodInfo();
        mi.access_flags = AccessFlags.ACC_PUBLIC;
        mi.name_index = setterNameIndex;
        mi.descriptor_index = setterDescriptorIndex;
        mi.attributes_count = 1;
        mi.attributes = new AttributeInfo[1];

        Code_attribute ca = new Code_attribute(reader);
        mi.attributes[0] = ca;
        ca.max_locals = 2;
        ca.max_stack = 2;
        ca.attribute_name_index = getOrCreateUtf8Constant("Code");
        ca.code = new CodeBuilder()
                .append(Instruction.aload_0)
                .append(Instruction.aload_1)
                .append(Instruction.putfield, getOrCreateFieldRefBytes(name))
                .append(Instruction.return_)
                .build();
        ca.code_length = ca.code.length;
        ca.attribute_length = ca.updateLength();

        append(mi);

        return this;
    }

    public ClassBuilder method(Method method, Mapper<ClassBuilder, Code_attribute> codeBuilderMapper) {
        return method(method.getName(), method.getParameterTypes(), method.getReturnType(), codeBuilderMapper);
    }

    public ClassBuilder method(String name, Class<?> returnType, Mapper<ClassBuilder, Code_attribute> codeBuilderMapper) {
        return method(name, EMPTY_ARGS, returnType, codeBuilderMapper);
    }

    public ClassBuilder method(String name, Class<?>[] args, Class<?> returnType, Mapper<ClassBuilder, Code_attribute> codeBuilderMapper) {
        return method(name, args, returnType, AccessFlags.ACC_PUBLIC, codeBuilderMapper);
    }

    public ClassBuilder method(String name, Class<?>[] args, Class<?> returnType, int accessFlags, Mapper<ClassBuilder, Code_attribute> codeBuilderMapper) {
        int nameIndex = getOrCreateUtf8Constant(name);
        String descriptor = getMethodDescriptor(args, returnType);
        int descriptorIndex = getOrCreateUtf8Constant(descriptor);

        MethodInfo mi = new MethodInfo();
        mi.access_flags = accessFlags;
        mi.name_index = nameIndex;
        mi.descriptor_index = descriptorIndex;
        mi.attributes_count = 1;
        mi.attributes = new AttributeInfo[1];
        mi.attributes[0] = codeBuilderMapper.map(this);

        append(mi);

        return this;
    }

    public boolean hasMethod(Method method) {
        return hasMethod(method.getName(), method.getParameterTypes(), method.getReturnType());
    }

    public boolean hasMethod(String name, Class<?>[] args, Class<?> returnType) {
        int nameIndex = reader.findUtf8ConstantIndex(name.getBytes(StandardCharsets.UTF_8));
        if (nameIndex == -1)
            return false;

        String descriptor = getMethodDescriptor(args, returnType);
        int descriptorIndex = reader.findUtf8ConstantIndex(descriptor.getBytes(StandardCharsets.UTF_8));
        return descriptorIndex != -1;
    }

    public String getMethodDescriptor(Method method) {
        return getMethodDescriptor(method.getParameterTypes(), method.getReturnType());
    }

    public String getMethodDescriptor(Class<?>[] args, Class<?> returnType) {
        return "(" + (Arrays.stream(args).map(this::getFieldDescription).collect(Collectors.joining(""))) + ")" + getFieldDescription(returnType);
    }

    public String getFieldDescription(Class type) {
        if (type == null)
            throw new NullPointerException("Field type should not be null");

        if (type.isPrimitive()) {
            if (type == int.class)
                return "I";
            else if (type == byte.class)
                return "B";
            else if (type == char.class)
                return "C";
            else if (type == double.class)
                return "D";
            else if (type == float.class)
                return "F";
            else if (type == long.class)
                return "J";
            else if (type == short.class)
                return "S";
            else if (type == boolean.class)
                return "Z";
        }
        if (type == void.class)
            return "V";

        if (type.isArray())
            return "[" + getFieldDescription(type.getComponentType());

        return "L" + type.getTypeName().replace('.', '/') + ";";
    }

    protected int getOrCreateUtf8Constant(String s) {
        return getOrCreateUtf8Constant(s.getBytes(StandardCharsets.UTF_8));
    }

    protected int getOrCreateUtf8Constant(byte[] bytes) {
        int index = reader.findUtf8ConstantIndex(bytes);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Utf8_info c = new ConstantPoolInfo.CONSTANT_Utf8_info();
        c.bytes = bytes;
        c.length = bytes.length;
        return append(c);
    }

    protected int getOrCreateClassConstant(int nameIndex) {
        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_Class_info && ((ConstantPoolInfo.CONSTANT_Class_info) ci).name_index == nameIndex);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Class_info c = new ConstantPoolInfo.CONSTANT_Class_info();
        c.name_index = nameIndex;
        return append(c);
    }

    protected int getOrCreateNameAndTypeConstant(int nameIndex, int descriptorIndex) {
        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_NameAndType_info
                && ((ConstantPoolInfo.CONSTANT_NameAndType_info) ci).name_index == nameIndex
                && ((ConstantPoolInfo.CONSTANT_NameAndType_info) ci).descriptor_index == descriptorIndex
        );
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_NameAndType_info nameAndTypeInfo = new ConstantPoolInfo.CONSTANT_NameAndType_info();
        nameAndTypeInfo.name_index = nameIndex;
        nameAndTypeInfo.descriptor_index = descriptorIndex;
        return append(nameAndTypeInfo);
    }

    protected int getOrCreateClassConstant(String name) {
        name = name.replace('.', '/');
        int nameIndex = getOrCreateUtf8Constant(name.getBytes(StandardCharsets.UTF_8));
        return getOrCreateClassConstant(nameIndex);
    }

    protected int getOrCreateClassConstant(Class<?> clazz) {
        return getOrCreateClassConstant(clazz.getTypeName());
    }

    protected void appendInterface(int index) {
        if (Arrays.stream(reader.interfaces).anyMatch(value -> value == index))
            return;

        reader.interfacesCount++;
        int[] arr = new int[reader.interfaces.length + 1];
        System.arraycopy(reader.interfaces, 0, arr, 0, reader.interfaces.length);
        reader.interfaces = arr;
        arr[arr.length - 1] = index;
    }

    protected int append(ConstantPoolInfo.ConstantInfo constantInfo) {
        reader.constantPoolCount++;
        ConstantPoolInfo.ConstantInfo[] infos = new ConstantPoolInfo.ConstantInfo[reader.constantPoolCount];
        System.arraycopy(reader.constantPool, 0, infos, 0, reader.constantPool.length);
        reader.constantPool = infos;
        int index = reader.constantPoolCount - 1;
        infos[index] = constantInfo;
        if (constantInfo.tag() == ConstantPoolInfo.CONSTANT_Long || constantInfo.tag() == ConstantPoolInfo.CONSTANT_Double) {
            reader.constantPoolCount++;
            infos = new ConstantPoolInfo.ConstantInfo[reader.constantPoolCount];
            System.arraycopy(reader.constantPool, 0, infos, 0, reader.constantPool.length);
            reader.constantPool = infos;
        }
        return index;
    }

    protected int append(MethodInfo methodInfo) {
        reader.methodsCount++;
        MethodInfo[] infos = new MethodInfo[reader.methodsCount];
        System.arraycopy(reader.methods, 0, infos, 0, reader.methods.length);
        reader.methods = infos;
        infos[infos.length - 1] = methodInfo;
        return infos.length - 1;
    }

    protected int append(FieldInfo fieldInfo) {
        reader.fieldsCount++;
        FieldInfo[] infos = new FieldInfo[reader.fieldsCount];
        System.arraycopy(reader.fields, 0, infos, 0, reader.fields.length);
        reader.fields = infos;
        infos[infos.length - 1] = fieldInfo;
        return infos.length - 1;
    }

    protected int append(AttributeInfo fieldInfo) {
        reader.attributesCount++;
        AttributeInfo[] infos = new AttributeInfo[reader.attributesCount];
        System.arraycopy(reader.attributes, 0, infos, 0, reader.attributes.length);
        reader.attributes = infos;
        infos[infos.length - 1] = fieldInfo;
        return infos.length - 1;
    }

    public byte[] build() {
        return reader.write();
    }

    public byte[] getOrCreateStringConstantBytes(String text) {
        return ByteCodeParser.int2toBytes(getOrCreateStringConstant(text));
    }

    public int getOrCreateStringConstant(String text) {
        int stringIndex = getOrCreateUtf8Constant(text);

        int index = reader.findConstantIndex(ci -> ci instanceof ConstantPoolInfo.CONSTANT_String_info && ((ConstantPoolInfo.CONSTANT_String_info) ci).string_index == stringIndex);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_String_info c = new ConstantPoolInfo.CONSTANT_String_info();
        c.string_index = stringIndex;
        return append(c);
    }

    public int getOrCreateIntegerConstant(int value) {
        int index = reader.findIntegerConstantIndex(value);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Integer_info c = new ConstantPoolInfo.CONSTANT_Integer_info();
        c.value = value;
        return append(c);
    }

    public int getOrCreateLongConstant(long value) {
        int index = reader.findLongConstantIndex(value);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Long_info c = new ConstantPoolInfo.CONSTANT_Long_info();
        c.value = value;
        return append(c);
    }

    public int getOrCreateFloatConstant(float value) {
        int index = reader.findFloatConstantIndex(value);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Float_info c = new ConstantPoolInfo.CONSTANT_Float_info();
        c.value = value;
        return append(c);
    }

    public int getOrCreateDoubleConstant(double value) {
        int index = reader.findDoubleConstantIndex(value);
        if (index != -1)
            return index;

        ConstantPoolInfo.CONSTANT_Double_info c = new ConstantPoolInfo.CONSTANT_Double_info();
        c.value = value;
        return append(c);
    }
}
