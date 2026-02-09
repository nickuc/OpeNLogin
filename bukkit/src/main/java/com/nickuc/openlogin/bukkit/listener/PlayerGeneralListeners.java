/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 - 2026 - OpenLogin Contributors
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
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();
        loginManagement.cleanup(name);
        LoginQueue.removeFromQueue(name);
        TitleAPI.getApi().reset(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        String message = event.getMessage().toLowerCase();
        String command = message.split(" ")[0];
        if (!plugin.getLoginManagement().isAuthenticated(name) && !plugin.getCommandManagement().isAllowedCommand(command)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String name = player.getName();
        if (plugin.getLoginManagement().isAuthenticated(name)) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && from.getY() > to.getY()) return;

        // Fix "too many packets" disconnect by using PlayerMoveEvent#setTo instead of Player#teleport
        event.setTo(from);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUICIDE) return;
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = ((Player) event.getEntity());
        if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String name = event.getWhoClicked().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;

        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.SUICIDE) return;
        if (event.isCancelled()) return;

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (!plugin.getLoginManagement().isAuthenticated(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        String name = event.getPlayer().getName();
        if (!plugin.getLoginManagement().isAuthenticated(name)) event.setCancelled(true);
    }
}
