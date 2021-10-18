package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.misc.With;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

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
                .withDefaultConstructor()
                .implement(DynamicProxy.class)
                .field("handler", Handler.class)
                .fieldSetter("handler");

        for (Method method : clazz.getDeclaredMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            builder.method(method.getName(), parameterTypes, returnType, cb -> {
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

                int methodNameIndex = cb.getOrCreateStringConstant(method.getName());
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
                code.append(Instruction.astore, int1toBytes(ca.max_locals - 1))
                        .append(Instruction.aload, int1toBytes(ca.max_locals - 1))
                        .append(Instruction.getstatic, cb.getOrCreateFieldRefBytes(HANDLER_CALL_SUPER))
                        .append(Instruction.if_acmpne, With.with(new CodeBuilder(), b -> {
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
                                })
                        );
                //call super.method(args..)
                code.append(Instruction.aload_0);
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
                code.append(Instruction.invokespecial, cb.getOrCreateMethodRefBytes(method))
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

                ca.attribute_length = ca.updateLength();
                return ca;
            });
        }

        byte[] bytes = builder.build();
        Class<?> proxy = Unchecked.call(() -> new ClassLoader() {

            @Override
            public Class findClass(String name) {
                byte[] b = bytes;
                return defineClass(name, b, 0, b.length);
            }
        }.loadClass(classFullName.replace('/', '.')));

        return Unchecked.call(() -> (T) proxy.newInstance());
    }

//    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
////        String s1 = FileTools.text("core/build/classes/java/main/com/wizzardo/newsorpigal/compiler/test/TestEmpty.class");
////        String s2 = FileTools.text("core/build/classes/java/main/com/wizzardo/newsorpigal/compiler/test/TestEmpty2.class");
////        System.out.println(s1);
////        System.out.println(s2);
////        byte[] bytes = FileTools.bytes("core/build/classes/java/main/com/wizzardo/newsorpigal/compiler/test/TestEmpty.class");
//        byte[] bytes = FileTools.bytes("core/build/classes/java/main/com/wizzardo/newsorpigal/compiler/test/ExtendProx.class");
//        ByteCodeReader reader = new ByteCodeReader(bytes);
//        reader.read();
//        byte[] result = reader.write();
//        System.out.println(Arrays.equals(bytes, result));
////        System.out.println("reader.minorVersion " + reader.minorVersion);
////        System.out.println("reader.majorVersion " + reader.majorVersion);
////        System.out.println("reader.constantPoolCount " + reader.constantPoolCount);
//        System.out.println();
//        System.out.println();
//        System.out.println();
//        System.out.println();
//
//
//        new ByteCodeReader(myClass).read();
//
////        FileTools.bytes("core/build/classes/java/main/com/wizzardo/newsorpigal/compiler/test/Qwerty.class", myClass);
//        Class<?> aClass = new ClassLoader() {
//
//            @Override
//            public Class findClass(String name) throws ClassNotFoundException {
////                byte[] b = loadClassFromFile(name);
//                System.out.println("findClass " + name);
//                byte[] b = myClass;
//                return defineClass(name, b, 0, b.length);
//            }
//        }.loadClass("com.wizzardo.newsorpigal.compiler.test.Qwerty");
//        System.out.println(aClass.getCanonicalName());
//        System.out.println(aClass.getSuperclass());
//        SuperClass instance = (SuperClass) aClass.newInstance();
//        System.out.println(instance);
//        System.out.println("instance instanceof DynamicProxy: " + (instance instanceof DynamicProxy));
//        DynamicProxy proxy = (DynamicProxy) instance;
//        proxy.setHandler((that, method, args1) -> {
//            System.out.println(that.getClass() + "." + method + " " + Arrays.toString(args1));
//            if(true)
//                return "qwe";
//            return Handler.CALL_SUPER;
//        });
//        System.out.println("instance.test(): " + instance.test());
//    }
}
