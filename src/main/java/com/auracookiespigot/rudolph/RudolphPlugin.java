
package com.auracookiespigot.rudolph;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class RudolphPlugin extends JavaPlugin implements Listener {

    private final String menuTitle = "Rudolph | Server Menu";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Rudolph has been enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open Rudolph.");
            return true;
        }

        Player player = (Player) sender;
        openMenu(player);
        return true;
    }

    private void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, menuTitle);

        menu.setItem(10, createItem(Material.COMPASS, "Spawn"));
        menu.setItem(12, createItem(Material.RED_BED, "Home"));
        menu.setItem(14, createItem(Material.BOOK, "Rules"));
        menu.setItem(16, createItem(Material.BARRIER, "Close"));

        player.openInventory(menu);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(menuTitle)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getClickedInventory() != event.getView().getTopInventory()) return;

        Player player = (Player) event.getWhoClicked();

        switch (event.getRawSlot()) {
            case 10:
                player.closeInventory();
                player.performCommand("spawn");
                break;
            case 12:
                player.closeInventory();
                player.performCommand("home");
                break;
            case 14:
                player.closeInventory();
                player.performCommand("rules");
                break;
            case 16:
                player.closeInventory();
                break;
            default:
                break;
        }
    }
}
