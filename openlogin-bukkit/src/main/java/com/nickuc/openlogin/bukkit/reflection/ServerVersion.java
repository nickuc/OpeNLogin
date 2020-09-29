/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;

/**
 * @author rush
 */

@AllArgsConstructor @Getter
public enum ServerVersion {

    v1_17("1.17"),
    v1_16("1.16"),
    v1_15("1.15"),
    v1_14("1.14"),
    v1_13("1.13"),
    v1_12("1.12"),
    v1_11("1.11"),
    v1_10("1.10"),
    v1_9("1.9"),
    v1_8("1.8"),
    v1_7("1.7"),
    v1_6("1.6"),
    v1_5("1.5"),
    DESCONHECIDA("???");
    
	private final String check;
	@Getter private static ServerVersion serverVersion = DESCONHECIDA;

	static {
        String ver = Bukkit.getVersion();
        for (ServerVersion serverVersion : ServerVersion.values()) {
            if (ver.contains(serverVersion.check)) {
                ServerVersion.serverVersion = serverVersion;
                break;
            }
        }
    }

}