package com.wizzardo.tools.reflection;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author: wizzardo
 * Date: 8/10/14
 */
public class FieldReflectionTest {
    @Test
    public void testUnsafe() {
        Assert.assertTrue(FieldReflection.putInt);
        Assert.assertTrue(FieldReflection.putLong);
        Assert.assertTrue(FieldReflection.putByte);
        Assert.assertTrue(FieldReflection.putShort);
        Assert.assertTrue(FieldReflection.putChar);
        Assert.assertTrue(FieldReflection.putFloat);
        Assert.assertTrue(FieldReflection.putDouble);
        Assert.assertTrue(FieldReflection.putBoolean);
        Assert.assertTrue(FieldReflection.putObject);

        Assert.assertTrue(FieldReflection.getInt);
        Assert.assertTrue(FieldReflection.getLong);
        Assert.assertTrue(FieldReflection.getByte);
        Assert.assertTrue(FieldReflection.getShort);
        Assert.assertTrue(FieldReflection.getChar);
        Assert.assertTrue(FieldReflection.getFloat);
        Assert.assertTrue(FieldReflection.getDouble);
        Assert.assertTrue(FieldReflection.getBoolean);
        Assert.assertTrue(FieldReflection.getObject);
    }

    static class TestClass {
        int i = 1;
        long l = 2l;
        byte b = 3;
        short s = 4;
        char c = 5;
        float f = 6f;
        double d = 7d;
        boolean aBoolean = true;
        String string = "foo";
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
        new FieldReflection(TestClass.class, "i").setInteger(test, 2);
        new FieldReflection(TestClass.class, "l").setLong(test, 3l);
        new FieldReflection(TestClass.class, "b").setByte(test, (byte) 4);
        new FieldReflection(TestClass.class, "s").setShort(test, (short) 5);
        new FieldReflection(TestClass.class, "c").setChar(test, (char) 6);
        new FieldReflection(TestClass.class, "f").setFloat(test, 7f);
        new FieldReflection(TestClass.class, "d").setDouble(test, 8d);
        new FieldReflection(TestClass.class, "aBoolean").setBoolean(test, false);
        new FieldReflection(TestClass.class, "string").setObject(test, "bar");

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

        Assert.assertEquals(1, new FieldReflection(TestClass.class, "i").getInteger(test));
        Assert.assertEquals(2l, new FieldReflection(TestClass.class, "l").getLong(test));
        Assert.assertEquals((byte) 3, new FieldReflection(TestClass.class, "b").getByte(test));
        Assert.assertEquals((short) 4, new FieldReflection(TestClass.class, "s").getShort(test));
        Assert.assertEquals((char) 5, new FieldReflection(TestClass.class, "c").getChar(test));
        Assert.assertEquals(6f, new FieldReflection(TestClass.class, "f").getFloat(test), 0);
        Assert.assertEquals(7d, new FieldReflection(TestClass.class, "d").getDouble(test), 0);
        Assert.assertEquals(true, new FieldReflection(TestClass.class, "aBoolean").getBoolean(test));
        Assert.assertEquals("foo", new FieldReflection(TestClass.class, "string").getObject(test));
    }

    private void switchUnsafe(boolean enabled) throws NoSuchFieldException, IllegalAccessException {
        Field field = FieldReflection.class.getDeclaredField("unsafe");

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        new FieldReflection(field).setObject(null, enabled ? UnsafeTools.getUnsafe() : null);
    }
}
