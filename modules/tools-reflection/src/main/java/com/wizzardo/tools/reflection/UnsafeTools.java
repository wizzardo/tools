package com.wizzardo.tools.reflection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 6/11/14
 */
public class UnsafeTools {
    private static Unsafe unsafe;

    static {
        init();
    }

    private static void init() {
        Field f = null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        if (unsafe == null)
            try {
                f = Unsafe.class.getDeclaredField("THE_ONE"); // android
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalAccessException ignored) {
            }

//        if (unsafe == null)
//            throw new IllegalStateException("can't get instance of sun.misc.Unsafe");
    }

    public static Unsafe getUnsafe() {
        return unsafe;
    }
}
