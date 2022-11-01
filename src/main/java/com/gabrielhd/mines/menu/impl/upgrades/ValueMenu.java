package com.gabrielhd.mines.menu.impl.upgrades;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.hook.TokenEnchant;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.ItemBuilder;
import com.gabrielhd.mines.utils.RomanNumerals;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Map;

public class ValueMenu extends Menu {

    private final Mine mine;
    private final List<Integer> noteSlots;
    private final List<Integer> spongeSlots;
    private final List<Integer> lenternSlots;

    public ValueMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/upgrades/Value"));

        this.loadMenu();

        this.mine = mine;
        this.noteSlots = getSlots(this.getCfg(), "Note", this.getCharSlots());
        this.spongeSlots = getSlots(this.getCfg(), "Sponge", this.getCharSlots());
        this.lenternSlots = getSlots(this.getCfg(), "SeaLantern", this.getCharSlots());

        this.load();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            String s = this.getActions().get(event.getSlot());

            int currentLevel = this.mine.getUpgrades().getOrDefault(UpgradeType.VALUE, 1);
            int nextLevel = Integer.parseInt(s.split(":")[1]);

            if(currentLevel >= nextLevel) {
                if(this.mine.getValueLevel() == nextLevel) {
                    Lang.MINE_ALREADY_VALUE_SELECT.send(player);
                    return;
                }

                this.mine.setValueLevel(nextLevel);
                Lang.MINE_VALUE_SELECTED.send(player);

                this.load();
                return;
            }

            double price = Config.VALUE_COST.get(s.split(":")[0]).get(nextLevel);
            if(TokenEnchant.getApi() != null) {
                if(TokenEnchant.getPlayerTokens(player) >= price) {
                    Lang.PLAYER_INSUFFICIENT_TOKENS.send(player);
                    return;
                }

                TokenEnchant.removePlayerTokens(player, price);
            }

            this.mine.getUpgrades().put(UpgradeType.VALUE, nextLevel);
            this.mine.setValueLevel(nextLevel);

            Lang.MINE_VALUE_LEVELUP.send(player);

            this.load();
        }
    }

    private void load() {
        Map<Integer, Float> noteBlocks = Config.VALUE.get("note");
        if(!noteBlocks.isEmpty()) {
            int s = 0;
            for(int level : noteBlocks.keySet()) {
                if(s > this.noteSlots.size()) break;
                int slot = this.noteSlots.get(s++);

                TextPlaceholders placeholders = new TextPlaceholders();
                placeholders.set("%level%", RomanNumerals.toRoman(level));
                placeholders.set("%amount%", Config.VALUE_COST.get("note").get(level));
                placeholders.set("%chance%", noteBlocks.get(level));

                ItemBuilder builder;
                if (level > this.mine.getUpgrades().getOrDefault(UpgradeType.VALUE, 1)) {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("NoteItem.Locked"), placeholders);
                } else {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("NoteItem.Unlocked"), placeholders);

                    if(this.mine.getValueLevel() == level) {
                        builder.withEnchant(Enchantment.DURABILITY, 3);
                        builder.withFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                this.setItem(slot, builder.build());
                this.getActions().put(slot, "note:"+level);
            }
        }

        Map<Integer, Float> spongeBlocks = Config.VALUE.get("sponge");
        if(!spongeBlocks.isEmpty()) {
            int s = 0;
            for(int level : spongeBlocks.keySet()) {
                if(s > this.spongeSlots.size()) break;
                int slot = this.spongeSlots.get(s++);

                TextPlaceholders placeholders = new TextPlaceholders();
                placeholders.set("%level%", RomanNumerals.toRoman(level));
                placeholders.set("%amount%", Config.VALUE_COST.get("sponge").get(level));
                placeholders.set("%chance%", spongeBlocks.get(level));

                ItemBuilder builder;
                if (level > this.mine.getUpgrades().getOrDefault(UpgradeType.VALUE, 1)) {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("SpongeItem.Locked"), placeholders);
                } else {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("SpongeItem.Unlocked"), placeholders);

                    if(this.mine.getValueLevel() == level) {
                        builder.withEnchant(Enchantment.DURABILITY, 3);
                        builder.withFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                this.setItem(slot, builder.build());
                this.getActions().put(slot, "sponge:"+level);
            }
        }

        Map<Integer, Float> seaBlocks = Config.VALUE.get("sealantern");
        if(!seaBlocks.isEmpty()) {
            int s = 0;
            for(int level : seaBlocks.keySet()) {
                if(s > this.lenternSlots.size()) break;
                int slot = this.lenternSlots.get(s++);

                TextPlaceholders placeholders = new TextPlaceholders();
                placeholders.set("%level%", RomanNumerals.toRoman(level));
                placeholders.set("%amount%", Config.VALUE_COST.get("sealantern").get(level));
                placeholders.set("%chance%", seaBlocks.get(level));

                ItemBuilder builder;
                if (level > this.mine.getUpgrades().getOrDefault(UpgradeType.VALUE, 1)) {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("SeaLanternItem.Locked"), placeholders);
                } else {
                    builder = getItemStack("Value.yml", this.getCfg().getConfigurationSection("SeaLanternItem.Unlocked"), placeholders);

                    if(this.mine.getValueLevel() == level) {
                        builder.withEnchant(Enchantment.DURABILITY, 3);
                        builder.withFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                this.setItem(slot, builder.build());
                this.getActions().put(slot, "sealantern:"+level);
            }
        }
    }
}
