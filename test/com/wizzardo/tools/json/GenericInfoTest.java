package com.wizzardo.tools.json;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/**
 * @author: wizzardo
 * Date: 2/21/14
 */
public class GenericInfoTest {

    static public class Wrapper<T> {
        public T value;
    }

    static public class ListWrapper<E> extends Wrapper<List<E>> {
    }

    static public class AnotherWrapper extends ListWrapper<Wrapper<String>> {
    }

    @Test
    public void firstTest() throws NoSuchFieldException {
        GenericInfo gi = new GenericInfo(AnotherWrapper.class);

        Assert.assertEquals(AnotherWrapper.class, gi.clazz);
        Assert.assertEquals(ListWrapper.class, gi.parent.clazz);
        Assert.assertEquals(Wrapper.class, gi.parent.parent.clazz);
        Assert.assertEquals(Object.class, gi.parent.parent.parent.clazz);
        Assert.assertEquals(null, gi.parent.parent.parent.parent);

        GenericInfo type = gi.getGenericType(Wrapper.class.getDeclaredField("value"));
        Assert.assertEquals(List.class, type.clazz);
        Assert.assertEquals(Wrapper.class, type.typeParameters[0].clazz);
        Assert.assertEquals(String.class, type.typeParameters[0].typeParameters[0].clazz);
    }

    static class Child {
        Map<Integer, ListWrapper<String>> map;
    }

    @Test
    public void secondTest() throws NoSuchFieldException {
        GenericInfo gi = new GenericInfo(Child.class.getDeclaredField("map").getGenericType());

        Assert.assertEquals(Map.class, gi.clazz);
        Assert.assertEquals(2, gi.typeParameters.length);
        Assert.assertEquals(Integer.class, gi.typeParameters[0].clazz);
        Assert.assertEquals(ListWrapper.class, gi.typeParameters[1].clazz);
        Assert.assertEquals(String.class, gi.typeParameters[1].typeParameters[0].clazz);
        Assert.assertEquals(Wrapper.class, gi.typeParameters[1].parent.clazz);
        Assert.assertEquals(List.class, gi.typeParameters[1].parent.typeParameters[0].clazz);
        Assert.assertEquals(String.class, gi.typeParameters[1].parent.typeParameters[0].typeParameters[0].clazz);
    }

    static class KeyValue<K, V> {
        K key;
        V value;
    }

    static class KeyValueWrapper<T> extends KeyValue<T, ListWrapper<T>> {
    }

    static class KeyValueStrings extends KeyValueWrapper<String> {
    }

    @Test
    public void thirdTest() throws NoSuchFieldException {
        GenericInfo gi = new GenericInfo(KeyValueStrings.class);

        Assert.assertEquals(KeyValueStrings.class, gi.clazz);
        Assert.assertEquals(KeyValueWrapper.class, gi.parent.clazz);
        Assert.assertEquals(KeyValue.class, gi.parent.parent.clazz);
        Assert.assertEquals(Object.class, gi.parent.parent.parent.clazz);
        Assert.assertEquals(null, gi.parent.parent.parent.parent);

        GenericInfo type = gi.getGenericType(KeyValue.class.getDeclaredField("key"));
        Assert.assertEquals(String.class, type.clazz);
    }

    static class ArrayHolder {
        int[] array;
        List<int[]> list;
    }

    @Test
    public void arrayTest() throws NoSuchFieldException {
        GenericInfo gi = new GenericInfo(ArrayHolder.class.getDeclaredField("array").getGenericType());

        Assert.assertEquals(Array.class, gi.clazz);
        Assert.assertEquals(null, gi.parent);
        Assert.assertEquals(1, gi.typeParameters.length);
        Assert.assertEquals(int.class, gi.typeParameters[0].clazz);

        gi = new GenericInfo(ArrayHolder.class.getDeclaredField("list").getGenericType());

        Assert.assertEquals(List.class, gi.clazz);
        Assert.assertEquals(null, gi.parent);
        Assert.assertEquals(1, gi.typeParameters.length);

        gi = gi.typeParameters[0];
        Assert.assertEquals(Array.class, gi.clazz);
        Assert.assertEquals(null, gi.parent);
        Assert.assertEquals(1, gi.typeParameters.length);
        Assert.assertEquals(int.class, gi.typeParameters[0].clazz);
    }
}
