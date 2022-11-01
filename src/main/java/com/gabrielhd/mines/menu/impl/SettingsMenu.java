package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;

public class SettingsMenu extends Menu {

    private final Mine mine;

    public SettingsMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Settings"));

        this.mine = mine;

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%status%", (this.mine.isClose() ? "&c&lCLOSED" : "&a&lOPEN"));

        this.loadMenu(placeholders);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            switch (this.getActions().get(event.getSlot()).toLowerCase(Locale.ROOT)) {
                case "lock": {
                    this.mine.setClose(!this.mine.isClose());

                    TextPlaceholders placeholders = new TextPlaceholders();
                    placeholders.set("%status%", (this.mine.isClose() ? "&c&lCLOSED" : "&a&lOPEN"));

                    Lang.MINE_LOCKED.send(player, placeholders);

                    this.loadMenu(placeholders);
                    this.fillItems(player);
                    break;
                }
                case "delete": {
                    new ConfirmMenu(yes -> {
                        if(yes) MineManager.delete(player);
                    }).open(player);
                    return;
                }
                case "kick_all": {
                    new ConfirmMenu(yes -> {
                        if(yes) this.mine.getMembers().removeIf(member -> !this.mine.isOwner(member));
                    }).open(player);
                    return;
                }
                case "back": {
                    new MineMenu(this.mine).open(player);
                    break;
                }
            }
        }
    }
}
