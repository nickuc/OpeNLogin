/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class TextComponentMessage {

    public static void sendPluginChoice(Player player) {
        TextComponent first = new TextComponent("      ");

        TextComponent nlogin = new TextComponent("nLogin");
        nlogin.setColor(ChatColor.YELLOW);
        HoverEvent nloginHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§6nLogin §7is a §6proprietary §7authentication plugin,\n§7updated and maintained by §bnickuc.com§7. This means that you\n§7cannot view and modify the source code of the plugin.\n\n§eIf you still have questions, please contact us:\n§bnickuc.com/discord"));
        ClickEvent nloginClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openlogin nlogin skip");
        nlogin.setHoverEvent(nloginHover);
        nlogin.setClickEvent(nloginClick);
        first.addExtra(nlogin);
        first.addExtra("              ");

        TextComponent openlogin = new TextComponent("OpeNLogin");
        openlogin.setColor(ChatColor.YELLOW);
        HoverEvent openloginHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§bOpeNLogin §7is a §bopen source §7authentication plugin,\n§7updated and maintained by all OpeNLogin contributors.\n\n§cCurrently the plugin does not have as many resources as nLogin."));
        ClickEvent openloginClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openlogin setup");
        openlogin.setHoverEvent(openloginHover);
        openlogin.setClickEvent(openloginClick);
        first.addExtra(openlogin);

        TextComponent second = new TextComponent("  ");

        TextComponent proprietary = new TextComponent("(proprietary)");
        proprietary.setColor(ChatColor.GOLD);
        proprietary.setHoverEvent(nloginHover);
        proprietary.setClickEvent(nloginClick);
        second.addExtra(proprietary);
        second.addExtra("      ");

        TextComponent opensource = new TextComponent("(open source)");
        opensource.setColor(ChatColor.AQUA);
        opensource.setHoverEvent(openloginHover);
        opensource.setClickEvent(openloginClick);
        second.addExtra(opensource);

        Player.Spigot spigot = player.spigot();
        spigot.sendMessage(first);
        spigot.sendMessage(second);
    }

    public static void sendPluginAd(Player player) {
        TextComponent first = new TextComponent("      ");

        TextComponent nlogin = new TextComponent("Migrate to nLogin");
        nlogin.setColor(ChatColor.GREEN);
        HoverEvent nloginHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§6nLogin §7is a §6proprietary §7authentication plugin,\n§7updated and maintained by §bnickuc.com§7. This means that you\n§7cannot view and modify the source code of the plugin.\n\n§eIf you still have questions, please contact us:\n§bnickuc.com/discord"));
        ClickEvent nloginClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openlogin nlogin skip");
        nlogin.setHoverEvent(nloginHover);
        nlogin.setClickEvent(nloginClick);
        first.addExtra(nlogin);
        first.addExtra("              ");

        TextComponent openlogin = new TextComponent("Keep OpeNLogin");
        openlogin.setColor(ChatColor.DARK_GRAY);
        HoverEvent openloginHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Continue using OpeNLogin."));
        ClickEvent openloginClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/openlogin nlogin_ad");
        openlogin.setHoverEvent(openloginHover);
        openlogin.setClickEvent(openloginClick);
        first.addExtra(openlogin);

        TextComponent second = new TextComponent("       ");

        TextComponent proprietary = new TextComponent("(recommended)");
        proprietary.setColor(ChatColor.BLUE);
        proprietary.setHoverEvent(nloginHover);
        proprietary.setClickEvent(nloginClick);
        second.addExtra(proprietary);

        // start
        player.sendMessage("");
        player.sendMessage(" §6nLogin §7is a free proprietary auth plugin with §6more features§7.");
        player.sendMessage(" §7When you click to migrate, the plugin will be installed");
        player.sendMessage(" §7on the next restart. No data will be lost.");
        player.sendMessage("");

        Player.Spigot spigot = player.spigot();
        spigot.sendMessage(first);
        spigot.sendMessage(second);

        player.sendMessage("");
    }

}
