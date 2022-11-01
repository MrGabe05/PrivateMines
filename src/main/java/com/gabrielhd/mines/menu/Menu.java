package com.gabrielhd.mines.menu;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.utils.ItemBuilder;
import com.gabrielhd.mines.utils.TextPlaceholders;
import com.gabrielhd.mines.utils.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Getter
public abstract class Menu implements Listener {

    private final YamlConfig cfg;
    private final Inventory inventory;

    private Map<Character, List<Integer>> charSlots;

    private final Map<Integer, String> actions = new HashMap<>();
    private final Map<Integer, ItemBuilder> fillItems = new HashMap<>();

    public Menu(YamlConfig cfg) {
        this(cfg, new TextPlaceholders());
    }

    public Menu(YamlConfig cfg, TextPlaceholders placeholders) {
        this.cfg = cfg;

        String title = placeholders.parse(Utils.Color(cfg.getString("Title")));

        InventoryType type = InventoryType.valueOf(cfg.getString("InventoryType", "CHEST"));
        if(type == InventoryType.CHEST || type == InventoryType.PLAYER) {
            this.inventory = Bukkit.createInventory(null, 9 * cfg.getStringList("Pattern").size(), title);
        } else {
            this.inventory = Bukkit.createInventory(null, type, title);
        }

        Bukkit.getPluginManager().registerEvents(this, PrivateMines.getInstance());
    }

    public void setItem(int i, ItemStack stack) {
        this.inventory.setItem(i, stack);
    }

    public void open(Player p) {
        p.openInventory(this.inventory);
    }

    protected void loadMenu() {
        this.loadMenu(new TextPlaceholders());
    }

    protected void fillItems(Player player) {
        for(Map.Entry<Integer, ItemBuilder> itemStackEntry : this.fillItems.entrySet()) {
            ItemBuilder itemBuilder = itemStackEntry.getValue().clone();
            if(itemStackEntry.getKey() >= 0) {
                inventory.setItem(itemStackEntry.getKey(), itemBuilder.build(player));
            }
        }
    }

    protected void loadMenu(TextPlaceholders textPlaceholders) {
        this.getFillItems().clear();
        this.getActions().clear();

        Map<Character, List<Integer>> charSlots = new HashMap<>();

        List<String> pattern = cfg.getStringList("Pattern");
        for(int row = 0; row < pattern.size(); row++) {
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for (int i = 0; i < patternLine.length(); i++) {
                char ch = patternLine.charAt(i);
                if (ch != ' ') {
                    ConfigurationSection section = cfg.getConfigurationSection("Items." + ch);
                    ItemBuilder itemBuilder = getItemStack(cfg.getFile().getName(), section);
                    if (itemBuilder != null) {
                        itemBuilder.applyPlaceholders(textPlaceholders);

                        this.fillItems.put(slot, itemBuilder);

                        if (section.isSet("Action")) this.actions.put(slot, section.getString("Action"));
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.put(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        this.charSlots = charSlots;
    }

    protected static ItemBuilder getItemStack(String fileName, MaterialData materialData, ConfigurationSection section, TextPlaceholders textPlaceholders) {
        Material type = materialData.getItemType();
        int data = materialData.getData();

        ItemBuilder itemBuilder = new ItemBuilder(type, data);

        if(section.contains("Name")) itemBuilder.withName(textPlaceholders.parse(section.getString("Name")));
        if(section.contains("Lore")) itemBuilder.withLore(section.getStringList("Lore").stream().map(textPlaceholders::parse).collect(Collectors.toList()));
        if(section.contains("Enchants")) {
            for(String _enchantment : section.getConfigurationSection("Enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    PrivateMines.getInstance().getLogger().info("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("Enchants." + _enchantment));
            }
        }

        if(section.contains("Flags")) {
            for(String flag : section.getStringList("Flags")) itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.getBoolean("Unbreakable", false)) itemBuilder.setUnbreakable();

        return itemBuilder;
    }

    protected static ItemBuilder getItemStack(String fileName, ConfigurationSection section) {
        return getItemStack(fileName, section, new TextPlaceholders());
    }

    protected static ItemBuilder getItemStack(String fileName, ConfigurationSection section, TextPlaceholders textPlaceholders) {
        if(section == null) return null;

        Material type;
        int data = section.getInt("Data", 0);
        try {
            type = Material.valueOf(section.getString("Type", "BEDROCK"));
        } catch (Exception ex) {
            PrivateMines.getInstance().getLogger().log(Level.SEVERE, "[" + fileName + "] Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type sections!");

            type = Material.BEDROCK;
        }

        ItemBuilder itemBuilder = new ItemBuilder(type, data);

        if(section.contains("Name")) itemBuilder.withName(textPlaceholders.parse(section.getString("Name")));
        if(section.contains("Lore")) itemBuilder.withLore(section.getStringList("Lore").stream().map(textPlaceholders::parse).collect(Collectors.toList()));
        if(section.contains("Enchants")) {
            for(String _enchantment : section.getConfigurationSection("Enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    PrivateMines.getInstance().getLogger().info("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("Enchants." + _enchantment));
            }
        }

        if(section.contains("Flags")) {
            for(String flag : section.getStringList("Flags")) itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.getBoolean("Unbreakable", false)) itemBuilder.setUnbreakable();

        return itemBuilder;
    }

    protected static List<Integer> getSlots(YamlConfig section, String key, Map<Character, List<Integer>> charSlots) {
        if(!section.contains(key))
            return new ArrayList<>();

        List<Character> chars = new ArrayList<>();

        for(char ch : section.getString(key).toCharArray())
            chars.add(ch);

        List<Integer> slots = new ArrayList<>();

        chars.stream().filter(charSlots::containsKey).forEach(ch -> slots.addAll(charSlots.get(ch)));

        return slots.isEmpty() ? Collections.singletonList(-1) : slots;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().equals(this.inventory) && event.getPlayer() instanceof Player) {
            this.fillItems((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void itemMove(InventoryMoveItemEvent event) {
        if (event.getDestination().equals(this.inventory) || event.getSource().equals(this.inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(this.inventory) && event.getCurrentItem() != null && event.getWhoClicked() instanceof Player) {
            this.onClick((Player) event.getWhoClicked(), event);

            event.setCancelled(true);
        }
    }

    public abstract void onClick(Player player, InventoryClickEvent event);
}
