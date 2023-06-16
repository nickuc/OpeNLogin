/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listener;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.manager.LoginManagement;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

@RequiredArgsConstructor
public class PlayerGeneralListeners implements Listener {

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();
        loginManagement.cleanup(name);
        LoginQueue.removeFromQueue(name);
        TitleAPI.getApi().reset(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();
        String message = e.getMessage().toLowerCase();
        String command = message.split(" ")[0];
        if (!plugin.getLoginManagement().isAuthenticated(name) && !plugin.getCommandManagement().isAllowedCommand(command)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;

        Player player = e.getPlayer();
        String name = player.getName();
        if (plugin.getLoginManagement().isAuthenticated(name)) return;

        Location to = e.getTo();
        if (to != null && e.getFrom().getY() > to.getY()) return;

        player.teleport(e.getFrom());
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.SUICIDE) return;
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player)) return;

        Player player = ((Player) e.getEntity());
        if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String name = e.getWhoClicked().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.isCancelled()) return;

        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.SUICIDE) return;
        if (e.isCancelled()) return;

        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
                e.setCancelled(true);
            }
        }

        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        String name = e.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) e.setCancelled(true);
    }


}
