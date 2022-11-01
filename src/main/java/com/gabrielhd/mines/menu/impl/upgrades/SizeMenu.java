package com.gabrielhd.mines.menu.impl.upgrades;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.menu.Menu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.ItemBuilder;
import com.gabrielhd.mines.utils.RomanNumerals;
import com.gabrielhd.mines.utils.TextPlaceholders;
import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

public class SizeMenu extends Menu {

    private final Mine mine;
    private final List<Integer> sizeSlots;

    public SizeMenu(Mine mine) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/upgrades/Size"));

        this.loadMenu();
        this.mine = mine;
        this.sizeSlots = getSlots(this.getCfg(), "Size", this.getCharSlots());
        this.load();
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        if(this.getActions().containsKey(event.getSlot())) {
            int level = this.mine.getUpgrades().getOrDefault(UpgradeType.SIZE, 1);

            if(this.getActions().get(event.getSlot()).equalsIgnoreCase("level:" + (level + 1))) {
                this.mine.getUpgrades().put(UpgradeType.SIZE, level + 1);

                Lang.MINE_SIZE_LEVELUP.send(player, new TextPlaceholders().set("%old-level%", level).set("%level%", (level + 1)));

                this.load();

                this.mine.reset(true);
            }
        }
    }

    private void load() {
        for(int i = 0; i < this.mine.getSize().size(); i++) {
            int slot = this.sizeSlots.get(i);
            int level = i + 1;
            int blocks = this.mine.getBlocks();
            int size = this.mine.getSize().get(level);
            int required = Math.toIntExact(this.mine.getRequired().get(level));

            TextPlaceholders placeholders = new TextPlaceholders();
            placeholders.set("%required%", required);

            int percentage = (int) (((double)blocks / (double)required) * 100);
            int progressMath = (int) (((double)blocks / (double)required) * 30);
            PrivateMines.debug(percentage + "");

            String output = "§a" + StringUtils.repeat(":", progressMath) + "§c" + StringUtils.repeat(":", 30 - progressMath);

            placeholders.set("%level%", RomanNumerals.toRoman(level));
            placeholders.set("%progress%", output);
            placeholders.set("%amount%", blocks);
            placeholders.set("%size%", size + "x" + size);
            placeholders.set("%percentage%", percentage);

            ItemBuilder builder;
            if(level > this.mine.getUpgrades().getOrDefault(UpgradeType.SIZE, 1)) {
                builder = getItemStack("Size.yml", this.getCfg().getConfigurationSection("SizeItem.Locked"), placeholders);
            } else {
                builder = getItemStack("Size.yml", this.getCfg().getConfigurationSection("SizeItem.Unlocked"), placeholders);

                if(this.mine.getUpgrades().getOrDefault(UpgradeType.SIZE, 1) == level) {
                    builder.withEnchant(Enchantment.DURABILITY, 3);
                    builder.withFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            if(percentage >= 100) {
                this.getActions().put(slot, "level:"+level);
            }

            this.setItem(slot, builder.build());
        }
    }
}
