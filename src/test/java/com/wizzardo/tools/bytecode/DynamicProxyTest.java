package com.wizzardo.tools.bytecode;

import com.wizzardo.tools.bytecode.fields.IntFieldSetter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicProxyTest {

    public static class SuperClassNoArgsReturnString {
        public String test() {
            return "test";
        }

        public String toString() {
            return "SuperClass";
        }
    }

    @Test
    public void test_no_args_return_String() {
        SuperClassNoArgsReturnString o = DynamicProxy.create(
                SuperClassNoArgsReturnString.class,
                (that, method, args) -> method.toUpperCase()
        );
        Assert.assertEquals("test", new SuperClassNoArgsReturnString().test());
        Assert.assertEquals("TEST", o.test());

        Assert.assertEquals("SuperClass", new SuperClassNoArgsReturnString().toString());
        Assert.assertEquals("TOSTRING", o.toString());
    }

    public static class SuperClassNoArgsReturnInt {
        public int test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_int() {
        SuperClassNoArgsReturnInt o = DynamicProxy.create(SuperClassNoArgsReturnInt.class, (that, method, args) -> 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnInt().test());
        Assert.assertEquals(2, o.test());
    }

    public static class SuperClassNoArgsReturnByte {
        public byte test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_byte() {
        SuperClassNoArgsReturnByte o = DynamicProxy.create(SuperClassNoArgsReturnByte.class, (that, method, args) -> (byte) 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnByte().test());
        Assert.assertEquals(2, o.test());
    }

    public static class SuperClassNoArgsReturnShort {
        public short test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_short() {
        SuperClassNoArgsReturnShort o = DynamicProxy.create(SuperClassNoArgsReturnShort.class, (that, method, args) -> (short) 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnShort().test());
        Assert.assertEquals(2, o.test());
    }

    public static class SuperClassNoArgsReturnLong {
        public long test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_long() {
        SuperClassNoArgsReturnLong o = DynamicProxy.create(SuperClassNoArgsReturnLong.class, (that, method, args) -> (long) 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnLong().test());
        Assert.assertEquals(2, o.test());
    }


    public static class SuperClassNoArgsReturnFloat {
        public float test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_float() {
        SuperClassNoArgsReturnFloat o = DynamicProxy.create(SuperClassNoArgsReturnFloat.class, (that, method, args) -> (float) 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnFloat().test(), 0);
        Assert.assertEquals(2, o.test(), 0);
    }

    public static class SuperClassNoArgsReturnDouble {
        public double test() {
            return 1;
        }
    }

    @Test
    public void test_no_args_return_double() {
        SuperClassNoArgsReturnDouble o = DynamicProxy.create(SuperClassNoArgsReturnDouble.class, (that, method, args) -> (double) 2);
        Assert.assertEquals(1, new SuperClassNoArgsReturnDouble().test(), 0);
        Assert.assertEquals(2, o.test(), 0);
    }

    public static class SuperClassNoArgsReturnBoolean {
        public boolean test() {
            return true;
        }
    }

    @Test
    public void test_no_args_return_boolean() {
        SuperClassNoArgsReturnBoolean o = DynamicProxy.create(SuperClassNoArgsReturnBoolean.class, (that, method, args) -> false);
        Assert.assertEquals(true, new SuperClassNoArgsReturnBoolean().test());
        Assert.assertEquals(false, o.test());
    }


    public static class SuperClassNoArgsReturnChar {
        public char test() {
            return 'a';
        }
    }

    @Test
    public void test_no_args_return_char() {
        SuperClassNoArgsReturnChar o = DynamicProxy.create(SuperClassNoArgsReturnChar.class, (that, method, args) -> 'b');
        Assert.assertEquals('a', new SuperClassNoArgsReturnChar().test());
        Assert.assertEquals('b', o.test());
    }

    public static class SuperClassNoArgsReturnVoid {
        public void test() {
        }
    }

    @Test
    public void test_no_args_return_void() {
        AtomicInteger counter = new AtomicInteger(0);
        SuperClassNoArgsReturnVoid o = DynamicProxy.create(SuperClassNoArgsReturnVoid.class, (that, method, args) -> {
            counter.incrementAndGet();
            return null;
        });

        o.test();
        Assert.assertEquals(1, counter.get());
    }

    public static class SuperClassArgsIntReturnInt {
        public int test(int i) {
            return i;
        }
    }

    @Test
    public void test_args_int_return_int() {
        SuperClassArgsIntReturnInt o = DynamicProxy.create(SuperClassArgsIntReturnInt.class, (that, method, args) -> ((Integer) args[0]) * 2);
        Assert.assertEquals(1, new SuperClassArgsIntReturnInt().test(1));
        Assert.assertEquals(2, o.test(1));
    }


    public static class SuperClassArgsLongReturnLong {
        public long test(long i) {
            return i;
        }
    }

    @Test
    public void test_args_long_return_long() {
        SuperClassArgsLongReturnLong o = DynamicProxy.create(SuperClassArgsLongReturnLong.class, (that, method, args) -> ((Long) args[0]) * 2l);
        Assert.assertEquals(1, new SuperClassArgsLongReturnLong().test(1));
        Assert.assertEquals(2, o.test(1));
    }

    public static class SuperClassArgsByteReturnByte {
        public byte test(byte i) {
            return i;
        }
    }

    @Test
    public void test_args_byte_return_byte() {
        SuperClassArgsByteReturnByte o = DynamicProxy.create(SuperClassArgsByteReturnByte.class, (that, method, args) -> (byte) (((Byte) args[0]) * 2));
        Assert.assertEquals(1, new SuperClassArgsByteReturnByte().test((byte) 1));
        Assert.assertEquals(2, o.test((byte) 1));
    }

    public static class SuperClassArgsShortReturnShort {
        public short test(short i) {
            return i;
        }
    }

    @Test
    public void test_args_short_return_short() {
        SuperClassArgsShortReturnShort o = DynamicProxy.create(SuperClassArgsShortReturnShort.class, (that, method, args) -> (short) (((Short) args[0]) * 2));
        Assert.assertEquals(1, new SuperClassArgsShortReturnShort().test((short) 1));
        Assert.assertEquals(2, o.test((short) 1));
    }

    public static class SuperClassArgsBooleanReturnBoolean {
        public boolean test(boolean i) {
            return i;
        }
    }

    @Test
    public void test_args_boolean_return_boolean() {
        SuperClassArgsBooleanReturnBoolean o = DynamicProxy.create(SuperClassArgsBooleanReturnBoolean.class, (that, method, args) -> !((Boolean) args[0]));
        Assert.assertEquals(true, new SuperClassArgsBooleanReturnBoolean().test(true));
        Assert.assertEquals(false, o.test(true));
    }

    public static class SuperClassArgsCharReturnChar {
        public char test(char i) {
            return i;
        }
    }

    @Test
    public void test_args_char_return_char() {
        SuperClassArgsCharReturnChar o = DynamicProxy.create(SuperClassArgsCharReturnChar.class, (that, method, args) -> ((Character) args[0]).toString().toUpperCase().charAt(0));
        Assert.assertEquals('a', new SuperClassArgsCharReturnChar().test('a'));
        Assert.assertEquals('A', o.test('a'));
    }

    public static class SuperClassArgsFloatReturnFloat {
        public float test(float i) {
            return i;
        }
    }

    @Test
    public void test_args_float_return_float() {
        SuperClassArgsFloatReturnFloat o = DynamicProxy.create(SuperClassArgsFloatReturnFloat.class, (that, method, args) -> 2 * ((Float) args[0]));
        Assert.assertEquals(1f, new SuperClassArgsFloatReturnFloat().test(1f), 0);
        Assert.assertEquals(2f, o.test(1f), 0);
    }

    public static class SuperClassArgsDoubleReturnDouble {
        public double test(double i) {
            return i;
        }
    }

    @Test
    public void test_args_double_return_double() {
        SuperClassArgsDoubleReturnDouble o = DynamicProxy.create(SuperClassArgsDoubleReturnDouble.class, (that, method, args) -> 2 * ((Double) args[0]));
        Assert.assertEquals(1d, new SuperClassArgsDoubleReturnDouble().test(1d), 0);
        Assert.assertEquals(2d, o.test(1d), 0);
    }

    public static class SuperClassArgsStringReturnString {
        public String test(String i) {
            return i;
        }
    }

    @Test
    public void test_args_String_return_String() {
        SuperClassArgsStringReturnString o = DynamicProxy.create(SuperClassArgsStringReturnString.class, (that, method, args) -> ((String) args[0]).toUpperCase());
        Assert.assertEquals("test", new SuperClassArgsStringReturnString().test("test"));
        Assert.assertEquals("TEST", o.test("test"));
    }


    public static class SuperClassArgsLongLongReturnLong {
        public long test(long a, long b) {
            return a + b;
        }
    }

    @Test
    public void test_args_long_long_return_long() {
        SuperClassArgsLongLongReturnLong o = DynamicProxy.create(SuperClassArgsLongLongReturnLong.class, (that, method, args) -> ((Long) args[0]) * 2l + ((Long) args[1]) * 2l);
        Assert.assertEquals(3, new SuperClassArgsLongLongReturnLong().test(1, 2));
        Assert.assertEquals(6, o.test(1, 2));
    }


    public static class SuperClassConstructorArgsInt {
        int a;

        public SuperClassConstructorArgsInt(int a) {
            this.a = a;
        }
    }

    @Test
    public void test_constructor_args_int() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String name = SuperClassConstructorArgsInt.class.getSimpleName() + "Extension";
        ClassBuilder builder = DynamicProxyFactory.createBuilder(name, SuperClassConstructorArgsInt.class);
        Class<?> clazz = DynamicProxyFactory.loadClass(name, builder.build());
        Constructor<?> constructor = clazz.getConstructor(int.class);
        SuperClassConstructorArgsInt o = (SuperClassConstructorArgsInt) constructor.newInstance(1);
        Assert.assertEquals(name, o.getClass().getSimpleName());
        Assert.assertEquals(1, o.a);
    }


    public static class IntHolder {
        public int a;

        public IntHolder(int a) {
            this.a = a;
        }
    }

    @Test
    public void test_int_setter() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        IntHolder holder = new IntHolder(1);
        Assert.assertEquals(1, holder.a);

        Class<IntFieldSetter> aClass = DynamicProxyFactory.createFieldSetter(IntHolder.class, "a", IntFieldSetter.class);
        IntFieldSetter<IntHolder> instance = aClass.newInstance();
        instance.set(holder, 2);
        Assert.assertEquals(2, holder.a);
    }

    public static class IntHolderWithSetter {
        private int a;

        public IntHolderWithSetter(int a) {
            this.a = a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }
    }

    @Test
    public void test_int_setter_use_setter() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        IntHolderWithSetter holder = new IntHolderWithSetter(1);
        Assert.assertEquals(1, holder.a);

        Class<IntFieldSetter> aClass = DynamicProxyFactory.createFieldSetter(IntHolderWithSetter.class, "a", IntFieldSetter.class);
        IntFieldSetter<IntHolderWithSetter> instance = aClass.newInstance();
        instance.set(holder, 2);
        Assert.assertEquals(2, holder.a);
    }

    public static class IntHolderWithDefault extends IntHolderParent {
        public int a = IntConstants.getI();
        public TestEnum e = TestEnum.ONE;

        public IntHolderWithDefault() {
        }

        public IntHolderWithDefault(int a) {
            this.a = a;
        }
    }

    public static class IntHolderParent {
        public int initA() {
            return 42;
        }
    }

    public static class IntConstants {
        public static final int I = 42;

        public static int getI() {
            return I;
        }

        public static int getValue(int value) {
            return value;
        }
    }

    public enum TestEnum {
        ONE, TWO
    }

    @Test
    public void test_proxy_field_int_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "IntHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", int.class, 42)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(42, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_int_default_value_invoke_static() throws NoSuchFieldException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String name = "IntHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name);

        builder.fieldCallMethod("a", int.class, IntConstants.class.getDeclaredMethod("getI"))
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(42, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_int_default_value_invoke_static_with_param() throws NoSuchFieldException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String name = "IntHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name);

        builder.fieldCallMethod("a", int.class, IntConstants.class.getDeclaredMethod("getValue", int.class), 42)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(42, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_list_default_value_invoke_new_with_param() throws NoSuchFieldException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        String name = "ListHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name);

        builder.fieldCallConstructor("l", List.class, ArrayList.class.getDeclaredConstructor(int.class), 10)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("l");
        Assert.assertEquals(ArrayList.class, field.get(aClass.newInstance()).getClass());
    }

    @Test
    public void test_proxy_field_long_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "LongHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", long.class, 1234567890123456l)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(1234567890123456l, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_float_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "FloatHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", float.class, Float.MAX_VALUE / 42f)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(Float.MAX_VALUE / 42f, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_double_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "DoubleHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", double.class, Float.MAX_VALUE / 42d)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Field field = aClass.getDeclaredField("a");
        Assert.assertEquals(Float.MAX_VALUE / 42d, field.get(aClass.newInstance()));
    }

    @Test
    public void test_proxy_field_boolean_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "BooleanHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", boolean.class, true)
                .field("b", boolean.class, false)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Object instance = aClass.newInstance();
        Assert.assertEquals(true, aClass.getDeclaredField("a").get(instance));
        Assert.assertEquals(false, aClass.getDeclaredField("b").get(instance));
    }

    @Test
    public void test_proxy_field_enum_default_value_constant() throws NoSuchFieldException, InstantiationException, IllegalAccessException {
        String name = "EnumHolderWithDefault";
        ClassBuilder builder = new ClassBuilder()
                .setSuperClass(Object.class)
                .setClassFullName(name)
                .field("a", TestEnum.class, TestEnum.ONE)
                .withDefaultConstructor();

        byte[] bytes = builder.build();
//        FileTools.bytes("/tmp/" + name + ".class", bytes);

        Class<?> aClass = DynamicProxyFactory.loadClass(name, bytes);
        Object instance = aClass.newInstance();
        Assert.assertEquals(TestEnum.ONE, aClass.getDeclaredField("a").get(instance));
    }
}
