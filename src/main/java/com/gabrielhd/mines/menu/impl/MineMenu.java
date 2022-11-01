package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class MineMenu extends Menu {

    private final Mine mine;

    public MineMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Mine"), new TextPlaceholders().set("%player%", Bukkit.getOfflinePlayer(mine.getOwner()).getName()));

        this.mine = mine;

        this.loadMenu();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            switch (this.getActions().get(event.getSlot()).toLowerCase(Locale.ROOT)) {
                case "teleport": {
                    player.teleport(this.mine.getSpawnLocation());
                    return;
                }
                case "members": {
                    new MembersMenu(this.mine).open(player);
                    return;
                }
                case "settings": {
                    new SettingsMenu(this.mine).open(player);
                    return;
                }
                case "upgrades": {
                    new UpgradesMenu(this.mine).open(player);
                    return;
                }
                case "public": {
                    new PublicsMenu().open(player);
                }
            }
        }
    }
}
