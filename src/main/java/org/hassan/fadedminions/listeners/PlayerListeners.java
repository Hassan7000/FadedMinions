package org.hassan.fadedminions.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.menus.menu.MinionMenu;
import org.hassan.fadedminions.menus.menu.SubMinionMenu;
import org.hassan.fadedminions.menus.menu.TierMinionMenu;
import org.hassan.fadedminions.utils.PagedPane;

public class PlayerListeners implements Listener {

    private FadedMinions plugin;

    public PlayerListeners(FadedMinions plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();

        if(plugin.getMinionPlayerDataManager().hasMinionPlayerData(player)) return;

        plugin.getMinionPlayerDataManager().saveMinionPlayerData(player);

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof MinionMenu) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) {
                return;
            }
            if(e.getClickedInventory() == null) return;

            MinionMenu menu = (MinionMenu) holder;
            menu.handleMenu(e);
        }
        if (holder instanceof SubMinionMenu) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) {
                return;
            }
            if(e.getClickedInventory() == null) return;

            SubMinionMenu menu = (SubMinionMenu) holder;
            menu.handleMenu(e);
        }
        if (holder instanceof TierMinionMenu) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) {
                return;
            }
            if(e.getClickedInventory() == null) return;

            TierMinionMenu menu = (TierMinionMenu) holder;
            menu.handleMenu(e);
        }
        if (holder instanceof PagedPane) {
            ((PagedPane) holder).onClick(e);
        }

    }
}
