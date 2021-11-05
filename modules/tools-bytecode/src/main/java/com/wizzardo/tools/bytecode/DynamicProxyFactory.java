package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.bytecode.fields.FieldSetter;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.misc.With;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Optional;

import static com.wizzardo.tools.bytecode.ByteCodeParser.int1toBytes;
import static com.wizzardo.tools.bytecode.ByteCodeParser.int2toBytes;

public class DynamicProxyFactory {
    private final static Method UNBOXING_INTEGER;
    private final static Method UNBOXING_LONG;
    private final static Method UNBOXING_BYTE;
    private final static Method UNBOXING_SHORT;
    private final static Method UNBOXING_FLOAT;
    private final static Method UNBOXING_DOABLE;
    private final static Method UNBOXING_BOOLEAN;
    private final static Method UNBOXING_CHARACTER;

    private final static Method BOXING_INTEGER;
    private final static Method BOXING_LONG;
    private final static Method BOXING_BYTE;
    private final static Method BOXING_SHORT;
    private final static Method BOXING_FLOAT;
    private final static Method BOXING_DOABLE;
    private final static Method BOXING_BOOLEAN;
    private final static Method BOXING_CHARACTER;

    private final static Method HANDLER_INVOKE;
    private final static Field HANDLER_EMPTY_ARGS;
    private final static Field HANDLER_CALL_SUPER;

    static {
        UNBOXING_INTEGER = Unchecked.call(() -> Integer.class.getMethod("intValue"));
        UNBOXING_LONG = Unchecked.call(() -> Long.class.getMethod("longValue"));
        UNBOXING_BYTE = Unchecked.call(() -> Byte.class.getMethod("byteValue"));
        UNBOXING_SHORT = Unchecked.call(() -> Short.class.getMethod("shortValue"));
        UNBOXING_FLOAT = Unchecked.call(() -> Float.class.getMethod("floatValue"));
        UNBOXING_DOABLE = Unchecked.call(() -> Double.class.getMethod("doubleValue"));
        UNBOXING_BOOLEAN = Unchecked.call(() -> Boolean.class.getMethod("booleanValue"));
        UNBOXING_CHARACTER = Unchecked.call(() -> Character.class.getMethod("charValue"));

        BOXING_INTEGER = Unchecked.call(() -> Integer.class.getMethod("valueOf", int.class));
        BOXING_LONG = Unchecked.call(() -> Long.class.getMethod("valueOf", long.class));
        BOXING_BYTE = Unchecked.call(() -> Byte.class.getMethod("valueOf", byte.class));
        BOXING_SHORT = Unchecked.call(() -> Short.class.getMethod("valueOf", short.class));
        BOXING_FLOAT = Unchecked.call(() -> Float.class.getMethod("valueOf", float.class));
        BOXING_DOABLE = Unchecked.call(() -> Double.class.getMethod("valueOf", double.class));
        BOXING_BOOLEAN = Unchecked.call(() -> Boolean.class.getMethod("valueOf", boolean.class));
        BOXING_CHARACTER = Unchecked.call(() -> Character.class.getMethod("valueOf", char.class));

        HANDLER_INVOKE = Unchecked.call(() -> Handler.class.getMethod("invoke", Object.class, String.class, Object[].class));
        HANDLER_EMPTY_ARGS = Unchecked.call(() -> Handler.class.getField("EMPTY_ARGS"));
        HANDLER_CALL_SUPER = Unchecked.call(() -> Handler.class.getField("CALL_SUPER"));
    }

    public static <T> T create(Class<T> clazz) {
        String classFullName = "com/wizzardo/tools/bytecode/generated/Generated" + System.nanoTime();
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(clazz)
                .setClassFullName(classFullName)
                .implement(DynamicProxy.class)
                .field("handler", Handler.class)
                .fieldSetter("handler");

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            builder.withSuperConstructor(constructor.getParameterTypes());
        }

        for (Method method : clazz.getDeclaredMethods()) {
            addHandlerCallForMethod(builder, method);
        }

        byte[] bytes = builder.build();
        Class<?> proxy = loadClass(classFullName, bytes);

        return Unchecked.call(() -> (T) proxy.newInstance());
    }

    public static Class<?> loadClass(String classFullName, byte[] bytes) {
        String name = classFullName.replace('/', '.');
        try {
            Optional<Method> defineHiddenClass = Arrays.stream(MethodHandles.Lookup.class.getMethods()).filter(it -> it.getName().equals("defineHiddenClass")).findFirst();
            if (defineHiddenClass.isPresent()) {
                Class<?> classOptionClass = ClassLoader.getSystemClassLoader().loadClass("java.lang.invoke.MethodHandles$Lookup$ClassOption");
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) defineHiddenClass.get().invoke(MethodHandles.lookup(), bytes, true, Array.newInstance(classOptionClass, 0));
                return lookup.lookupClass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            ClassLoader classLoader = DynamicProxyFactory.class.getClassLoader();
//            Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
//            method.setAccessible(true);
//            Object result = method.invoke(classLoader, name, bytes, 0, bytes.length);
//            return (Class<?>) result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return Unchecked.call(() -> new ClassLoader() {
            @Override
            public Class<?> findClass(String name) {
                return defineClass(name, bytes, 0, bytes.length);
            }
        }.loadClass(name));
    }

    public static ClassBuilder createBuilder(String fullName, Class<?> superClass) {
        return createBuilder(fullName, superClass, null);
    }

    public static ClassBuilder createBuilder(String fullName, Class<?> superClass, Class<?>[] interfaces) {
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(superClass)
                .setClassFullName(fullName)
                .implement(DynamicProxy.class)
                .field("handler", Handler.class)
                .fieldSetter("handler");

        Constructor<?>[] constructors = superClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            builder.withSuperConstructor(constructor.getParameterTypes());
        }

        if (interfaces != null)
            for (Class<?> anInterface : interfaces) {
                builder.implement(anInterface);
            }
        return builder;
    }

    public static <T extends FieldSetter> Class<T> createFieldSetter(Class<?> clazz, String fieldName, Class<T> setterInterface) throws NoSuchFieldException {
        String classFullName = "com/wizzardo/tools/bytecode/Generated_" + clazz.getSimpleName() + "_" + fieldName + "_setter" + System.nanoTime();

        Field field = clazz.getDeclaredField(fieldName);
        ClassBuilder builder = new ClassBuilder()
                .setClassFullName(classFullName)
                .setSuperClass(Object.class)
                .implement(setterInterface)
                .withDefaultConstructor();

        Optional<Method> optionalSetter = Optional.empty();
        if (!Modifier.isPublic(field.getModifiers())) {
            optionalSetter = Arrays.stream(clazz.getMethods())
                    .filter(it -> it.getParameterCount() == 1
                            && it.getParameterTypes()[0] == field.getType()
                            && it.getName().equalsIgnoreCase("set" + fieldName))
                    .findFirst();

            if (!optionalSetter.isPresent())
                throw new IllegalArgumentException("Cannot create setter, field is not public and there is no setter for it");
        }

        Method setter = optionalSetter.orElse(null);


        Signature_attribute attribute = new Signature_attribute();
        attribute.attribute_name_index = builder.getOrCreateUtf8Constant("Signature");
        String setterType = builder.getFieldDescription(setterInterface);
        setterType = setterType.substring(0, setterType.length() - 1) + "<" + builder.getFieldDescription(clazz) + ">;";
        String signature = builder.getFieldDescription(Object.class) + setterType;
        attribute.signature_index = builder.getOrCreateUtf8Constant(signature);
        builder.append(attribute);

        for (Method method : setterInterface.getDeclaredMethods()) {
            Class<?>[] parameterTypes = new Class[]{clazz, method.getParameterTypes()[1]};
            builder.method(method.getName(), parameterTypes, void.class, cb -> {
                Code_attribute ca = new Code_attribute(cb.reader);
                ca.max_locals = 1 + Arrays.stream(parameterTypes).mapToInt(it -> it == long.class || it == double.class ? 2 : 1).sum();
                ca.max_stack = 3;
                ca.attribute_name_index = cb.getOrCreateUtf8Constant("Code");

                CodeBuilder code = new CodeBuilder().append(Instruction.aload_1);
                addLoadArg(code, 1, parameterTypes[1], 1);
                if (Modifier.isPublic(field.getModifiers())) {
                    code.append(Instruction.putfield, cb.getOrCreateFieldRefBytes(field));
                } else if (setter != null) {
                    int methodRef = builder.getOrCreateMethodRef(setter);
                    code.append(Instruction.invokevirtual, ByteCodeParser.int2toBytes(methodRef));
                } else
                    throw new IllegalArgumentException("Cannot create setter, field is not public and there is no setter for it");

                code.append(Instruction.return_);

                ca.code = code.build();
                ca.code_length = ca.code.length;

                ca.attribute_length = ca.updateLength();
                return ca;
            });

//            String descriptor = builder.getMethodDescriptor(parameterTypes, void.class);
//            int descriptorIndex = builder.getOrCreateUtf8Constant(descriptor);

            int methodRef = builder.getOrCreateMethodRef(builder.reader.thisClass, method.getName(), parameterTypes, void.class);

            builder.method(method.getName(), new Class[]{Object.class, parameterTypes[1]}, void.class,
                    AccessFlags.ACC_PUBLIC | AccessFlags.ACC_SYNTHETIC | AccessFlags.ACC_BRIDGE
                    , cb -> {
                        Code_attribute ca = new Code_attribute(cb.reader);
                        ca.max_locals = 1 + Arrays.stream(parameterTypes).mapToInt(it -> it == long.class || it == double.class ? 2 : 1).sum();
                        ca.max_stack = 3;
                        ca.attribute_name_index = cb.getOrCreateUtf8Constant("Code");

                        CodeBuilder code = new CodeBuilder()
                                .append(Instruction.aload_0)
                                .append(Instruction.aload_1)
                                .append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(clazz)));
                        addLoadArg(code, 1, parameterTypes[1], 1);
//                        code.append(Instruction.putfield, cb.getOrCreateFieldRefBytes(field));
                        code.append(Instruction.invokevirtual, ByteCodeParser.int2toBytes(methodRef));
                        code.append(Instruction.return_);

                        ca.code = code.build();
                        ca.code_length = ca.code.length;
                        return ca;
                    });
        }

        byte[] build = builder.build();
        return (Class<T>) loadClass(classFullName, build);
    }

    private static int addLoadArg(CodeBuilder code, int i, Class<?> parameterType, int localVarIndexOffset) {
        if (parameterType.isPrimitive()) {
            if (parameterType == int.class) {
                if (i + localVarIndexOffset == 1)
                    code.append(Instruction.iload_1);
                else if (i + localVarIndexOffset == 2)
                    code.append(Instruction.iload_2);
                else if (i + localVarIndexOffset == 3)
                    code.append(Instruction.iload_3);
                else
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
        return localVarIndexOffset;
    }

    public static void addHandlerCallForMethod(ClassBuilder builder, Method method) {
        addHandlerCallForMethod(builder, method, true);
    }

    public static void addHandlerCallForMethod(ClassBuilder builder, Method method, boolean callSuper) {
        int superMethodIndex = callSuper && !Modifier.isAbstract(method.getModifiers()) ? builder.getOrCreateMethodRef(method) : 0;
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        String name = method.getName();
        addHandlerCallForMethod(builder, name, parameterTypes, returnType, superMethodIndex);
    }

    public static void addHandlerCallForMethod(ClassBuilder builder, String name, Class<?>[] parameterTypes, Class<?> returnType) {
        addHandlerCallForMethod(builder, name, parameterTypes, returnType, 0);
    }

    public static void addHandlerCallForMethod(ClassBuilder builder, String name, Class<?>[] parameterTypes, Class<?> returnType, int superMethodIndex) {
        builder.method(name, parameterTypes, returnType, cb -> {
            Code_attribute ca = new Code_attribute(cb.reader);
            ca.max_locals = 2 + (parameterTypes.length == 0 ? 0 : Arrays.stream(parameterTypes).mapToInt(it -> it == long.class || it == double.class ? 2 : 1).sum());
            ca.max_stack = 4 + parameterTypes.length * 2 + 2; // not optimal but should be ok
            ca.attribute_name_index = cb.getOrCreateUtf8Constant("Code");
            Instruction returnInstruction;
            if (returnType == int.class || returnType == byte.class || returnType == short.class || returnType == boolean.class || returnType == char.class) {
                returnInstruction = Instruction.ireturn;
            } else if (returnType == long.class) {
                returnInstruction = Instruction.lreturn;
            } else if (returnType == float.class) {
                returnInstruction = Instruction.freturn;
            } else if (returnType == double.class) {
                returnInstruction = Instruction.dreturn;
            } else if (returnType == void.class) {
                returnInstruction = Instruction.return_;
            } else {
                returnInstruction = Instruction.areturn;
            }

            CodeBuilder code = new CodeBuilder()
                    .append(Instruction.aload_0)
                    .append(Instruction.getfield, cb.getOrCreateFieldRefBytes("handler"))
                    .append(Instruction.aload_0);

            int methodNameIndex = cb.getOrCreateStringConstant(name);
            if (methodNameIndex < 255)
                code.append(Instruction.ldc, int1toBytes(methodNameIndex));
            else
                code.append(Instruction.ldc_w, int2toBytes(methodNameIndex));

            if (parameterTypes.length == 0)
                code.append(Instruction.getstatic, cb.getOrCreateFieldRefBytes(HANDLER_EMPTY_ARGS));
            else {
                code.append(Instruction.bipush, int1toBytes(parameterTypes.length));
                code.append(Instruction.anewarray, int2toBytes(cb.getOrCreateClassConstant(Object.class)));
                int localVarIndexOffset = 1;
                for (int i = 0; i < parameterTypes.length; i++) {
                    code.append(Instruction.dup);
                    code.append(Instruction.bipush, int1toBytes(i));

                    Class<?> parameterType = parameterTypes[i];
                    if (parameterType.isPrimitive()) {
                        if (parameterType == int.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_INTEGER));
                        } else if (parameterType == long.class) {
                            code.append(Instruction.lload, int1toBytes(i + localVarIndexOffset++));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_LONG));
                        } else if (parameterType == byte.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_BYTE));
                        } else if (parameterType == short.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_SHORT));
                        } else if (parameterType == float.class) {
                            code.append(Instruction.fload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_FLOAT));
                        } else if (parameterType == double.class) {
                            code.append(Instruction.dload, int1toBytes(i + localVarIndexOffset++));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_DOABLE));
                        } else if (parameterType == boolean.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_BOOLEAN));
                        } else if (parameterType == char.class) {
                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
                            code.append(Instruction.invokestatic, cb.getOrCreateMethodRefBytes(BOXING_CHARACTER));
                        }
                    } else {
                        code.append(Instruction.aload, int1toBytes(i + 1));
                    }
                    code.append(Instruction.aastore);
                }
            }

            code.append(Instruction.invokeinterface, cb.getOrCreateInterfaceMethodRefBytes(HANDLER_INVOKE));
            code.append(Instruction.astore, int1toBytes(ca.max_locals - 1));

            CodeBuilder returnResultFromHandler = With.with(new CodeBuilder(), b -> {
                b.append(Instruction.aload, int1toBytes(ca.max_locals - 1));
                if (returnType.isPrimitive()) {
                    if (returnType == int.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Integer.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_INTEGER));
                    } else if (returnType == long.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Long.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_LONG));
                    } else if (returnType == byte.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Byte.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_BYTE));
                    } else if (returnType == short.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Short.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_SHORT));
                    } else if (returnType == float.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Float.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_FLOAT));
                    } else if (returnType == double.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Double.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_DOABLE));
                    } else if (returnType == boolean.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Boolean.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_BOOLEAN));
                    } else if (returnType == char.class) {
                        b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(Character.class)));
                        b.append(Instruction.invokevirtual, cb.getOrCreateMethodRefBytes(UNBOXING_CHARACTER));
                    }
                } else if (returnType != void.class) {
                    b.append(Instruction.checkcast, int2toBytes(cb.getOrCreateClassConstant(returnType)));
                }
                b.append(returnInstruction);
            });

            if (superMethodIndex != 0) {
                code.append(Instruction.aload, int1toBytes(ca.max_locals - 1))
                        .append(Instruction.getstatic, cb.getOrCreateFieldRefBytes(HANDLER_CALL_SUPER))
                        .append(Instruction.if_acmpne, returnResultFromHandler);
                //call super.method(args..)
                code.append(Instruction.aload_0);
                int localVarIndexOffset = 1;
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    localVarIndexOffset = addLoadArg(code, i, parameterType, localVarIndexOffset);
//                    if (parameterType.isPrimitive()) {
//                        if (parameterType == int.class) {
//                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
//                        } else if (parameterType == long.class) {
//                            code.append(Instruction.lload, int1toBytes(i + localVarIndexOffset++));
//                        } else if (parameterType == byte.class) {
//                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
//                        } else if (parameterType == short.class) {
//                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
//                        } else if (parameterType == float.class) {
//                            code.append(Instruction.fload, int1toBytes(i + localVarIndexOffset));
//                        } else if (parameterType == double.class) {
//                            code.append(Instruction.dload, int1toBytes(i + localVarIndexOffset++));
//                        } else if (parameterType == boolean.class) {
//                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
//                        } else if (parameterType == char.class) {
//                            code.append(Instruction.iload, int1toBytes(i + localVarIndexOffset));
//                        }
//                    } else {
//                        code.append(Instruction.aload, int1toBytes(i + 1));
//                    }
                }
                code.append(Instruction.invokespecial, int2toBytes(superMethodIndex))
                        .append(returnInstruction);

                ca.code = code.build();
                ca.code_length = ca.code.length;
                ca.attributes_count = 1;
                ca.attributes = new AttributeInfo[1];

                BranchingOperation branchingOperation = (BranchingOperation) code.operations.stream().filter(it -> it.instruction == Instruction.if_acmpne).findFirst().get();
                StackMapTable_attribute smt = new StackMapTable_attribute();
                smt.attribute_name_index = cb.getOrCreateUtf8Constant("StackMapTable");
                ca.attributes[0] = smt;
                smt.number_of_entries = 1;
                smt.entries = new StackMapTable_attribute.StackMapFrame[1];
                StackMapTable_attribute.append_frame frame = new StackMapTable_attribute.append_frame();
                smt.entries[0] = frame;
                frame.offset_delta = branchingOperation.to;
                frame.locals = new StackMapTable_attribute.verification_type_info[1];
                frame.locals[0] = new StackMapTable_attribute.verification_type_info.Object_variable_info(cb.getOrCreateClassConstant(Object.class));
                frame.frame_type = frame.getType();
                smt.attribute_length = smt.updateLength();
            } else {
                code.append(returnResultFromHandler);
                ca.code = code.build();
                ca.code_length = ca.code.length;
            }

            ca.attribute_length = ca.updateLength();
            return ca;
        });
    }

    private static boolean isAndroid() {
        try {
            Class.forName("android.os.Build");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isSupported() {
        return !isAndroid();
    }
}
