package com.gabrielhd.mines.menu.impl;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.utils.ItemBuilder;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;

public class MembersMenu extends Menu {

    private final Mine mine;
    private final List<Integer> memberSlots;

    public MembersMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/Members"));

        this.loadMenu();
        this.mine = mine;
        this.memberSlots = getSlots(this.getCfg(), "Members", this.getCharSlots());
        this.load();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            if(this.getActions().get(event.getSlot()).equalsIgnoreCase("back")) {
                new MineMenu(this.mine).open(player);
                return;
            }

            UUID uuid = UUID.fromString(this.getActions().get(event.getSlot()));

            new ConfirmMenu(yes -> {
                if(yes && this.mine.removeMember(uuid)) {
                    Lang.MINE_MEMBER_KICK.send(player, new TextPlaceholders().set("%player%", Bukkit.getOfflinePlayer(uuid).getName()));
                }
            });
        }
    }

    private void load() {
        List<OfflinePlayer> players = this.mine.getPlayers();

        for(int member = 0; member < this.memberSlots.size(); member++) {
            if(players.size() <= member) {
                return;
            }

            int slot = this.memberSlots.get(member);

            OfflinePlayer player = players.get(member);

            ConfigurationSection section = this.getCfg().getConfigurationSection("MembersItem");

            ItemBuilder itemBuilder = getItemStack("Members.yml", section);
            if(itemBuilder != null) {
                this.setItem(slot, itemBuilder.asSkullOf(player).build(player));

                this.getActions().put(slot, player.getUniqueId().toString());
            }
        }
    }
}
