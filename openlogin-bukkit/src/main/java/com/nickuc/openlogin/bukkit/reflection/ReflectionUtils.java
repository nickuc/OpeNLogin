/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection;

import lombok.NonNull;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> getNMS(@NonNull String clasz) throws ClassNotFoundException {
        return Class.forName(NMS_PREFIX + "." + clasz);
    }

    public static Class<?> getNSNMS(@NonNull String newClass, @NonNull String clasz) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft." + newClass);
        } catch (ClassNotFoundException e) {
            return Class.forName(NMS_PREFIX + "." + clasz);
        }
    }

    public static Class<?> getOBC(@NonNull String clasz) throws ClassNotFoundException {
        return Class.forName(OBC_PREFIX + "." + clasz);
    }

    public static Method getMethod(@NonNull Class<?> clasz, @NonNull String methodName, @NonNull Class<?>... classes) throws NoSuchMethodException {
        Method method = clasz.getDeclaredMethod(methodName, classes);
        method.setAccessible(true);
        return method;
    }

    public static Field getField(@NonNull Class<?> clasz, @NonNull String fieldName) throws NoSuchFieldException {
        Field field = clasz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

}
