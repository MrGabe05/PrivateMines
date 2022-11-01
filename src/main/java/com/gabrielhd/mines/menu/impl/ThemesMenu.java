package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.hook.VaultHook;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.MineTheme;
import com.gabrielhd.mines.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ThemesMenu extends Menu {

    public ThemesMenu() {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Themes"));

        this.loadMenu();
        this.load();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot()) && event.getCurrentItem() != null && this.getFillItems().get(event.getSlot()).build().isSimilar(event.getCurrentItem())) {
            MineTheme theme = MineManager.of(this.getActions().get(event.getSlot()));
            if(theme == null) {
                Lang.THEME_NOT_CONFIGURED.send(player);
                player.closeInventory();
                return;
            }

            if(!player.hasPermission(theme.getPerms())) {
                Lang.PLAYER_NOT_PERMISSIONS.send(player);
                player.closeInventory();
                return;
            }

            if(theme.getPrice() > 0.0 && VaultHook.getEconomy() != null) {
                if(!VaultHook.getEconomy().has(player, theme.getPrice())) {
                    Lang.PLAYER_INSUFFICIENT_MONEY.send(player);
                    return;
                }

                VaultHook.getEconomy().has(player, theme.getPrice());
            }

            player.closeInventory();
            MineManager.create(player, theme);
        }
    }

    private void load() {
        List<MineTheme> mines = new ArrayList<>(MineManager.getThemes());
        for(int i = 0; i < this.getInventory().getSize(); i++) {
            if(mines.size() <= i) return;
            MineTheme theme = mines.get(i);

            ItemBuilder itemBuilder = getItemStack("Themes.yml", this.getCfg().getConfigurationSection("Themes." + theme.getName()));
            if(itemBuilder != null) {
                this.getActions().put(i, theme.getName().toLowerCase());
                this.getFillItems().put(i, itemBuilder);

                this.setItem(i, itemBuilder.build());
            }
        }
    }
}
