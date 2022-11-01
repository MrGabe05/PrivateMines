package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.ItemBuilder;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PublicsMenu extends Menu {

    private final List<Integer> minesSlots;

    public PublicsMenu() {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Public"));
        this.loadMenu();
        this.minesSlots = getSlots(this.getCfg(), "Mines", this.getCharSlots());
        this.load();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            if(this.getActions().get(event.getSlot()).equalsIgnoreCase("back") && MineManager.of(player.getUniqueId()) != null) {
                new MineMenu(MineManager.of(player.getUniqueId())).open(player);
                return;
            }

            UUID uuid = UUID.fromString(this.getActions().get(event.getSlot()));
            Mine mine = MineManager.of(uuid);
            if(mine != null) {
                player.teleport(mine.getSpawnLocation());
            }
        }
    }

    private void load() {
        if(this.minesSlots == null || this.minesSlots.isEmpty()) {
            return;
        }

        List<Mine> mines = MineManager.getMines().stream().filter(mine -> !mine.isClose()).collect(Collectors.toList());
        if(mines.isEmpty()) return;

        for(int i = 0; i < this.minesSlots.size(); i++) {
            if(mines.size() <= i) return;

            int slot = this.minesSlots.get(i);
            Mine mine = mines.get(i);

            TextPlaceholders placeholders = new TextPlaceholders();
            placeholders.set("%player%", Bukkit.getOfflinePlayer(mine.getOwner()).getName());

            int sizeLevel = mine.getUpgrades().getOrDefault(UpgradeType.SIZE, 1);
            int size = mine.getSize().get(sizeLevel) + 1;
            placeholders.set("%size%", size + "x" + size);

            ItemBuilder itemBuilder = getItemStack("menus/Public.yml", this.getCfg().getConfigurationSection("MinesItem"), placeholders);
            if(itemBuilder != null) {
                this.setItem(slot, itemBuilder.build());

                this.getActions().put(slot, mine.getOwner().toString());
            }
        }
    }
}
