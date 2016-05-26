package org.spongebot.bot.rs.reflection;

import org.spongebot.loader.Loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {

    public static Object getStaticField(String className, String fieldName) {
        try {
            Class<?> c = Loader.rsClassLoader.defined.get(className);
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setStaticField(String className, String fieldName, Object value) {
        try {
            Class<?> c = Loader.rsClassLoader.defined.get(className);
            Field field = c.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeStatic(String className, String methodName, Object[] args, Class<?>[] argTypes) {
        try {
            Class<?> c = Loader.rsClassLoader.defined.get(className);
            Method m = c.getDeclaredMethod(methodName, argTypes);
            m.setAccessible(true);
            m.invoke(null, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeStatic(String className, String methodName) {
        try {
            Class<?> c = Loader.rsClassLoader.defined.get(className);
            Method m = c.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invoke(Object inst, String methodName, Object[] args, Class<?>[] argTypes) {
        try {
            Method m = inst.getClass().getSuperclass().getDeclaredMethod(methodName, argTypes);
            m.setAccessible(true);
            m.invoke(inst, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
