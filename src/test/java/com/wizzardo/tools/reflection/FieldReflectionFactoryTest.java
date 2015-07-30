package com.wizzardo.tools.reflection;

import com.wizzardo.tools.reflection.field.*;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author: wizzardo
 * Date: 8/10/14
 */
public class FieldReflectionFactoryTest {

    @Test
    public void testUnsafe() {
        Assert.assertTrue(FieldReflectionFactory.putInt);
        Assert.assertTrue(FieldReflectionFactory.putLong);
        Assert.assertTrue(FieldReflectionFactory.putByte);
        Assert.assertTrue(FieldReflectionFactory.putShort);
        Assert.assertTrue(FieldReflectionFactory.putChar);
        Assert.assertTrue(FieldReflectionFactory.putFloat);
        Assert.assertTrue(FieldReflectionFactory.putDouble);
        Assert.assertTrue(FieldReflectionFactory.putBoolean);
        Assert.assertTrue(FieldReflectionFactory.putObject);

        Assert.assertTrue(FieldReflectionFactory.getInt);
        Assert.assertTrue(FieldReflectionFactory.getLong);
        Assert.assertTrue(FieldReflectionFactory.getByte);
        Assert.assertTrue(FieldReflectionFactory.getShort);
        Assert.assertTrue(FieldReflectionFactory.getChar);
        Assert.assertTrue(FieldReflectionFactory.getFloat);
        Assert.assertTrue(FieldReflectionFactory.getDouble);
        Assert.assertTrue(FieldReflectionFactory.getBoolean);
        Assert.assertTrue(FieldReflectionFactory.getObject);
    }

    public static class TestClass {
        public int i = 1;
        public long l = 2l;
        public byte b = 3;
        public short s = 4;
        public char c = 5;
        public float f = 6f;
        public double d = 7d;
        public boolean aBoolean = true;
        public String string = "foo";
    }

    @Test
    public void testSet() throws NoSuchFieldException, IllegalAccessException {
        switchUnsafe(true);
        doTestSet();
        switchUnsafe(false);
        doTestSet();
    }

    private void doTestSet() throws NoSuchFieldException {
        TestClass test = new TestClass();
        FieldReflectionFactory factory = new FieldReflectionFactory();

        factory.create(TestClass.class, "i").setInteger(test, 2);
        factory.create(TestClass.class, "l").setLong(test, 3l);
        factory.create(TestClass.class, "b").setByte(test, (byte) 4);
        factory.create(TestClass.class, "s").setShort(test, (short) 5);
        factory.create(TestClass.class, "c").setChar(test, (char) 6);
        factory.create(TestClass.class, "f").setFloat(test, 7f);
        factory.create(TestClass.class, "d").setDouble(test, 8d);
        factory.create(TestClass.class, "aBoolean").setBoolean(test, false);
        factory.create(TestClass.class, "string").setObject(test, "bar");

        Assert.assertEquals(2, test.i);
        Assert.assertEquals(3l, test.l);
        Assert.assertEquals((byte) 4, test.b);
        Assert.assertEquals((short) 5, test.s);
        Assert.assertEquals((char) 6, test.c);
        Assert.assertEquals(7f, test.f, 0);
        Assert.assertEquals(8d, test.d, 0);
        Assert.assertEquals(false, test.aBoolean);
        Assert.assertEquals("bar", test.string);
    }

    @Test
    public void testGet() throws NoSuchFieldException, IllegalAccessException {
        switchUnsafe(true);
        doTestGet();
        switchUnsafe(false);
        doTestGet();
    }

    private void doTestGet() throws NoSuchFieldException {
        Object test = new TestClass();
        FieldReflectionFactory factory = new FieldReflectionFactory();

        Assert.assertEquals(1, factory.create(TestClass.class, "i").getInteger(test));
        Assert.assertEquals(2l, factory.create(TestClass.class, "l").getLong(test));
        Assert.assertEquals((byte) 3, factory.create(TestClass.class, "b").getByte(test));
        Assert.assertEquals((short) 4, factory.create(TestClass.class, "s").getShort(test));
        Assert.assertEquals((char) 5, factory.create(TestClass.class, "c").getChar(test));
        Assert.assertEquals(6f, factory.create(TestClass.class, "f").getFloat(test), 0);
        Assert.assertEquals(7d, factory.create(TestClass.class, "d").getDouble(test), 0);
        Assert.assertEquals(true, factory.create(TestClass.class, "aBoolean").getBoolean(test));
        Assert.assertEquals("foo", factory.create(TestClass.class, "string").getObject(test));
    }

    @Test
    public void testTypes() throws NoSuchFieldException, IllegalAccessException {
        switchUnsafe(true);
        testUnsafeTypes();
        switchUnsafe(false);
        testReflectionTypes();
    }

    private void testReflectionTypes() throws NoSuchFieldException {
        FieldReflectionFactory factory = new FieldReflectionFactory();

        Assert.assertTrue(factory.create(TestClass.class, "i") instanceof IntegerReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "l") instanceof LongReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "b") instanceof ByteReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "s") instanceof ShortReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "c") instanceof CharReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "f") instanceof FloatReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "d") instanceof DoubleReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "aBoolean") instanceof BooleanReflectionGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "string") instanceof ObjectReflectionGetterSetter);
    }

    private void testUnsafeTypes() throws NoSuchFieldException {
        FieldReflectionFactory factory = new FieldReflectionFactory();

        Assert.assertTrue(factory.create(TestClass.class, "i") instanceof IntegerUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "l") instanceof LongUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "b") instanceof ByteUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "s") instanceof ShortUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "c") instanceof CharUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "f") instanceof FloatUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "d") instanceof DoubleUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "aBoolean") instanceof BooleanUnsafeGetterSetter);
        Assert.assertTrue(factory.create(TestClass.class, "string") instanceof ObjectUnsafeGetterSetter);
    }

    private void switchUnsafe(boolean enabled) throws NoSuchFieldException, IllegalAccessException {
        Field field = FieldReflectionFactory.class.getDeclaredField("unsafe");

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        new FieldReflectionFactory().create(field, true).setObject(null, enabled ? UnsafeTools.getUnsafe() : null);
    }
}
