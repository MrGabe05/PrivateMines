package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ConfirmMenu extends Menu {

    private final Consumer<Boolean> action;

    public ConfirmMenu(Consumer<Boolean> action) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Confirm"));

        this.action = action;
        this.loadMenu();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            if(Utils.parseBoolean(this.getActions().get(event.getSlot()))) {
                this.action.accept(true);
            }

            player.closeInventory();
        }
    }
}
