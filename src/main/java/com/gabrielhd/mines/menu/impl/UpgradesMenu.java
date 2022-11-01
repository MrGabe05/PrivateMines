package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.menu.impl.upgrades.BlocksMenu;
import com.gabrielhd.mines.menu.impl.upgrades.SizeMenu;
import com.gabrielhd.mines.menu.impl.upgrades.ValueMenu;
import com.gabrielhd.mines.mines.Mine;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class UpgradesMenu extends Menu {

    private final Mine mine;

    public UpgradesMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Upgrades"));

        this.mine = mine;

        this.loadMenu();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            switch (this.getActions().get(event.getSlot()).toLowerCase(Locale.ROOT)) {
                case "size": {
                    new SizeMenu(this.mine).open(player);
                    return;
                }
                case "value": {
                    new ValueMenu(this.mine).open(player);
                    return;
                }
                case "blocks": {
                    new BlocksMenu(this.mine, player).open(player);
                    return;
                }
                case "back": {
                    new MineMenu(this.mine).open(player);
                }
            }
        }
    }
}
