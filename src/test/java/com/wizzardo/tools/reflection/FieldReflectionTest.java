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

}
