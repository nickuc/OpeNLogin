/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nickuc.openlogin.bukkit.reflection;

import com.nickuc.openlogin.bukkit.enums.BukkitVersion;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitReflection {

    private static final Pattern MATCH_VARIABLE = Pattern.compile("\\{([^\\}]+)\\}");

    private static final String OBC_PREFIX = Bukkit.getServer().getClass().getPackage().getName();
    private static final String NMS_PREFIX = OBC_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server");

    public static Class<?> craftPlayerClass, entityPlayerClass, playerConnectionClass;
    private static Method getHandleMethod, sendPacketMethod;
    private static Field playerConnectionField, playerNetworkManagerField;

    static {
        try {
            craftPlayerClass = getClass("{obc}.entity.CraftPlayer");
            entityPlayerClass = getClass("net.minecraft.server.level.EntityPlayer", "{nms}.EntityPlayer");
            playerConnectionClass = getClass("net.minecraft.server.network.PlayerConnection", "{nms}.PlayerConnection");
            playerConnectionField = getField(entityPlayerClass, playerConnectionClass, 0);
            getHandleMethod = getMethod(craftPlayerClass, "getHandle");

            Class<?> packetClass = getClass("net.minecraft.network.protocol.Packet", "{nms}.Packet");

            // send packet method
            if (BukkitVersion.getVersion().isNewerOrEqual(BukkitVersion.v1_18)) {
                Class<?> networkManager = Class.forName("net.minecraft.network.NetworkManager");
                for (Field field : playerConnectionClass.getDeclaredFields()) {
                    if (networkManager.isAssignableFrom(field.getType())) {
                        playerNetworkManagerField = field;
                        break;
                    }
                }

                try {
                    sendPacketMethod = getMethod(networkManager, "sendPacket", packetClass);
                } catch (NoSuchMethodException e) {
                    sendPacketMethod = getMethod(networkManager, "a", packetClass);
                }
            } else {
                sendPacketMethod = getMethod(playerConnectionClass, "sendPacket", packetClass);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
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

    public static void sendPacket(Player player, Object packet) throws InvocationTargetException, IllegalAccessException {
        Object entityPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConnectionField.get(entityPlayer);
        if (playerNetworkManagerField != null) {
            Object networkManager = playerNetworkManagerField.get(playerConnection);
            sendPacketMethod.invoke(networkManager, packet);
        } else {
            sendPacketMethod.invoke(playerConnection, packet);
        }
    }
}
