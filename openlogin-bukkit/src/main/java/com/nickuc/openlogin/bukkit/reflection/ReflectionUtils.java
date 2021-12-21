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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {

    private static final Pattern MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");

    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

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

    public static Field getField(Class<?> clasz, int index) throws NoSuchFieldException {
        return getField(clasz, null, index);
    }

    public static Field getField(Class<?> clasz, Class<?> type, int index) throws NoSuchFieldException {
        if (index < 0) {
            throw new IllegalArgumentException("Negative index!" + index);
        }

        Field[] fields = clasz.getDeclaredFields();
        if (fields.length > 0) {
            int i = 0;
            for (Field field : fields) {
                if (type != null) {
                    Class<?> fieldType = field.getType();
                    if (type == Object.class) {
                        if (fieldType != Object.class) {
                            continue;
                        }
                    } else if (fieldType == Object.class || !fieldType.isAssignableFrom(type)) {
                        continue;
                    }
                }

                if (i == index) {
                    field.setAccessible(true);
                    return field;
                }
                i++;
            }
        }
        throw new NoSuchFieldException("Cannot find field assignable from " + type + " in class " + clasz);
    }

    public static Class<?> getClass(String... names) throws ClassNotFoundException, NoClassDefFoundError {
        for (int i = 0; i < names.length; i++) {
            names[i] = expandVariables(names[i]);
        }
        return getCanonicalClass(names);
    }

    private static Class<?> getCanonicalClass(String... names) throws ClassNotFoundException, NoClassDefFoundError {
        if (names.length == 0) {
            throw new IllegalArgumentException("Empty canonical names!");
        }
        for (int i = 0; i < names.length - 1; i++) {
            try {
                return Class.forName(names[i]);
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            }
        }
        String canonicalName = names[names.length - 1];
        return Class.forName(canonicalName);
    }

    private static String expandVariables(String name) {
        StringBuffer output = new StringBuffer();
        Matcher matcher = MATCH_VARIABLE.matcher(name);

        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = "";

            // Expand all detected variables
            switch (variable) {
                case "nms":
                    replacement = NMS_PREFIX;
                    break;
                case "obc":
                    replacement = OBC_PREFIX;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown variable: " + variable);
            }

            // Assume the expanded variables are all packages, and append a dot
            if (!replacement.isEmpty() && matcher.end() < name.length() && name.charAt(matcher.end()) != '.') {
                replacement += ".";
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(output);
        return output.toString();
    }

}
