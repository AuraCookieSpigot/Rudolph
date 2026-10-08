
package com.auracookiespigot.rudolph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class RudolphPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Rudolph enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {

        if (!command.getName().equalsIgnoreCase("rudolph")) {
            return false;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("rudolph.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

            reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Rudolph reloaded!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can open the menu.");
            return true;
        }

        openMenu((Player) sender);
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private void openMenu(Player player) {
        int size = getConfig().getInt("menu.size", 27);
        if (size < 9 || size > 54 || size % 9 != 0) {
            size = 27;
        }

        String title = color(getConfig().getString(
                "menu.title", "&cRudolph Menu"));

        MenuHolder holder = new MenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.inventory = inventory;

        ConfigurationSection items = getConfig()
                .getConfigurationSection("items");

        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection item = items.getConfigurationSection(key);
                if (item == null) continue;

                int slot = item.getInt("slot", -1);
                if (slot < 0 || slot >= size) continue;

                Material material = Material.matchMaterial(
                        item.getString("material", "STONE"));

                if (material == null || !material.isItem()
                        || material == Material.AIR) {
                    material = Material.STONE;
                }

                ItemStack stack = new ItemStack(material);
                ItemMeta meta = stack.getItemMeta();

                if (meta != null) {
                    meta.setDisplayName(color(
                            item.getString("name", key)));
                    stack.setItemMeta(meta);
                }

                inventory.setItem(slot, stack);
                holder.commands.put(slot,
                        item.getString("command", ""));
            }
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder()
                instanceof MenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getClickedInventory()
                != event.getView().getTopInventory()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        MenuHolder holder = (MenuHolder)
                event.getView().getTopInventory().getHolder();

        String action = holder.commands.get(event.getRawSlot());
        if (action == null || action.isEmpty()) return;

        player.closeInventory();

        if (action.equalsIgnoreCase("close")) return;

        // Run a command as the player.
        // Leading slashes are optional.
        String playerCommand = action.startsWith("/")
                ? action.substring(1) : action;

        Bukkit.getScheduler().runTask(this,
                () -> player.performCommand(playerCommand));
    }

    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;
        private final Map<Integer, String> commands = new HashMap<>();

        @Override
        public Inventory getInventory() {
            return Objects.requireNonNull(inventory);
        }
    }
}
