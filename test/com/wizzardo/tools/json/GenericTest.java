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
public class GenericTest {

    static public class Wrapper<T> {
        public T value;
    }

    static public class ListWrapper<E> extends Wrapper<List<E>> {
    }

    static public class AnotherWrapper extends ListWrapper<Wrapper<String>> {
    }

    @Test
    public void firstTest() throws NoSuchFieldException {
        Generic generic = new Generic(AnotherWrapper.class);

        Assert.assertEquals(AnotherWrapper.class.getSimpleName(), generic.toString());

        Assert.assertEquals(AnotherWrapper.class, generic.clazz);
        Assert.assertEquals(ListWrapper.class, generic.parent.clazz);
        Assert.assertEquals(Wrapper.class, generic.parent.parent.clazz);
        Assert.assertEquals(Object.class, generic.parent.parent.parent.clazz);
        Assert.assertEquals(null, generic.parent.parent.parent.parent);

        Generic type = generic.getGenericType(Wrapper.class.getDeclaredField("value"));
        Assert.assertEquals(List.class, type.clazz);
        Assert.assertEquals(Wrapper.class, type.typeParameters[0].clazz);
        Assert.assertEquals(String.class, type.typeParameters[0].typeParameters[0].clazz);
    }

    static class Child {
        Map<Integer, ListWrapper<String>> map;
    }

    @Test
    public void secondTest() throws NoSuchFieldException {
        Generic generic = new Generic(Child.class.getDeclaredField("map").getGenericType());

        Assert.assertEquals(Map.class, generic.clazz);
        Assert.assertEquals(2, generic.typeParameters.length);
        Assert.assertEquals(Integer.class, generic.typeParameters[0].clazz);
        Assert.assertEquals(ListWrapper.class, generic.typeParameters[1].clazz);
        Assert.assertEquals(String.class, generic.typeParameters[1].typeParameters[0].clazz);
        Assert.assertEquals(Wrapper.class, generic.typeParameters[1].parent.clazz);
        Assert.assertEquals(List.class, generic.typeParameters[1].parent.typeParameters[0].clazz);
        Assert.assertEquals(String.class, generic.typeParameters[1].parent.typeParameters[0].typeParameters[0].clazz);
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
        Generic generic = new Generic(KeyValueStrings.class);

        Assert.assertEquals(KeyValueStrings.class, generic.clazz);
        Assert.assertEquals(KeyValueWrapper.class, generic.parent.clazz);
        Assert.assertEquals(KeyValue.class, generic.parent.parent.clazz);
        Assert.assertEquals(Object.class, generic.parent.parent.parent.clazz);
        Assert.assertEquals(null, generic.parent.parent.parent.parent);

        Generic type = generic.getGenericType(KeyValue.class.getDeclaredField("key"));
        Assert.assertEquals(String.class, type.clazz);
    }

    static class ArrayHolder {
        int[] array;
        List<int[]> list;
    }

    @Test
    public void arrayTest() throws NoSuchFieldException {
        Generic generic = new Generic(ArrayHolder.class.getDeclaredField("array").getGenericType());

        Assert.assertEquals(Array.class, generic.clazz);
        Assert.assertEquals(null, generic.parent);
        Assert.assertEquals(1, generic.typeParameters.length);
        Assert.assertEquals(int.class, generic.typeParameters[0].clazz);

        generic = new Generic(ArrayHolder.class.getDeclaredField("list").getGenericType());

        Assert.assertEquals(List.class, generic.clazz);
        Assert.assertEquals(null, generic.parent);
        Assert.assertEquals(1, generic.typeParameters.length);

        generic = generic.typeParameters[0];
        Assert.assertEquals(Array.class, generic.clazz);
        Assert.assertEquals(null, generic.parent);
        Assert.assertEquals(1, generic.typeParameters.length);
        Assert.assertEquals(int.class, generic.typeParameters[0].clazz);
    }

    @Test
    public void nullTest() throws NoSuchFieldException {
        Assert.assertEquals(0, new Generic(Object.class, (Generic[]) null).typeParameters.length);
        Assert.assertEquals(0, new Generic(Object.class, (Class[]) null).typeParameters.length);
    }

    @Test
    public void fieldsTest() throws NoSuchFieldException {
        Generic generic = new Generic(ArrayHolder.class);
        Map<String, FieldInfo> map = generic.getFields();

        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey("array"));
        Assert.assertTrue(map.containsKey("list"));
        Assert.assertTrue(map == generic.getFields());
    }
}
