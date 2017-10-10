package com.wizzardo.tools.reflection;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
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
        Fields map = generic.getFields();

        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey("array"));
        Assert.assertTrue(map.containsKey("list"));
        Assert.assertTrue(map == generic.getFields());
    }

    @Test
    public void interfacesTest() {
        Generic generic = new Generic(String.class);
        Assert.assertEquals(String.class, generic.clazz);
        Assert.assertEquals(3, generic.interfaces.length);

        for (int i = 0; i < generic.interfaces.length; i++) {
            Generic g = generic.interfaces[i];
            if (g.clazz == Serializable.class)
                continue;

            if (g.clazz == CharSequence.class)
                continue;

            if (g.clazz == Comparable.class) {
                Assert.assertEquals(1, g.typesCount());
                Generic type = g.type(0);
                Assert.assertEquals(String.class, type.clazz);
                Assert.assertSame(generic.interfaces, type.interfaces);
                continue;
            }

            Assert.assertTrue(false);
        }
    }


    public interface SuperInterface<T> {
        T get();
    }

    public interface ParentInterface<A> extends SuperInterface<A> {
        A get();
    }

    public interface ChildInterface extends ParentInterface<String> {
    }

    @Test
    public void interfacesTest_2() {
        Generic generic = new Generic(ChildInterface.class);
        Assert.assertEquals(ChildInterface.class, generic.clazz);
        Assert.assertEquals(1, generic.interfaces.length);

        Generic parent = generic.interfaces[0];
        Assert.assertEquals(ParentInterface.class, parent.clazz);
        Assert.assertEquals(String.class, parent.type(0).clazz);
        Assert.assertEquals(1, parent.interfaces.length);

        Generic supr = parent.interfaces[0];
        Assert.assertEquals(SuperInterface.class, supr.clazz);
        Assert.assertEquals(String.class, supr.type(0).clazz);
    }

    @Test
    public void methodsTest() {
        Generic<ChildInterface, Fields, Generic> generic = Generic.of(ChildInterface.class);
        List<GenericMethod> methods = generic.methods();

        GenericMethod method = methods.get(0);
        Assert.assertEquals("get", method.method.getName());
        Assert.assertEquals(String.class, method.returnType.clazz);
        Assert.assertEquals(0, method.args.size());
    }

    public static class WildcardTypeTestPojo {
        List<? super Number> superNumbers;
        List<? extends Number> extendsNumbers;
    }

    @Test
    public void wildcardTypeTest() {
        Fields<FieldInfo> fields = new Fields<FieldInfo>(WildcardTypeTestPojo.class);
        Assert.assertEquals(2,fields.size());

        FieldInfo fieldInfo;
        fieldInfo = fields.get("superNumbers");
        Assert.assertEquals(List.class, fieldInfo.generic.clazz);
        Assert.assertEquals(1, fieldInfo.generic.typesCount());
        Assert.assertEquals(Number.class, fieldInfo.generic.type(0).clazz);
        Assert.assertEquals(0, fieldInfo.generic.type(0).typesCount());

        fieldInfo = fields.get("extendsNumbers");
        Assert.assertEquals(List.class, fieldInfo.generic.clazz);
        Assert.assertEquals(1, fieldInfo.generic.typesCount());
        Assert.assertEquals(Number.class, fieldInfo.generic.type(0).clazz);
        Assert.assertEquals(0, fieldInfo.generic.type(0).typesCount());
    }
}
