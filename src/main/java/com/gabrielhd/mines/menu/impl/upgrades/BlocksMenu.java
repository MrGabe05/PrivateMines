package com.gabrielhd.mines.menu.impl.upgrades;

import com.cryptomorin.xseries.XMaterial;
import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.hook.RevRankup;
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
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlocksMenu extends Menu {

    private final Mine mine;
    private final List<Integer> blockSlots;

    public BlocksMenu(Mine mine, Player player) {
        super(new YamlConfig(PrivateMines.getInstance(), "menus/upgrades/Blocks"));

        this.loadMenu();
        this.mine = mine;
        this.blockSlots = getSlots(this.getCfg(), "Block", this.getCharSlots());
        this.load(player);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        int level = event.getSlot() + 1;

        if(!Config.BLOCKS.containsKey(level)) return;

        if(this.mine.getUpgrades().getOrDefault(UpgradeType.BLOCKS, 1) >= level) {
            if (this.mine.getBlocksLevel() == level) {
                Lang.MINE_ALREADY_BLOCK_SELECT.send(player);
                return;
            }

            this.mine.setBlocksLevel(level);
            Lang.MINE_BLOCK_SELECTED.send(player);

            this.mine.reset(true);

            player.closeInventory();
            return;
        }

        long price = Config.BLOCKS_COST.get(level);
        if(RevRankup.getApi() != null && RevRankup.getPrestige(player) < price) {
            Lang.PLAYER_INSUFFICIENT_PRESTIGE.send(player);
            return;
        }

        this.mine.getUpgrades().put(UpgradeType.BLOCKS, level);
        Lang.MINE_BLOCKS_LEVELUP.send(player);

        this.load(player);
    }

    private void load(Player player) {
        this.getInventory().clear();

        List<MaterialData> blocks = new ArrayList<>(Config.BLOCKS.values());
        for(int i = 0; i < Config.BLOCKS.size(); i++) {
            int slot = this.blockSlots.get(i);
            int level = i + 1;

            MaterialData materialData = blocks.get(i);
            if(materialData == null) continue;

            TextPlaceholders placeholders = new TextPlaceholders();
            placeholders.set("%level%", RomanNumerals.toRoman(level));
            placeholders.set("%required%", Config.BLOCKS_COST.get(level));
            placeholders.set("%prestige%", (RevRankup.getApi() != null ? RevRankup.getPrestige(player) : 0L));
            placeholders.set("%type_name%", Objects.requireNonNull(XMaterial.matchXMaterial(materialData.getItemTypeId(), materialData.getData()).orElse(null)).name().replace("_", ""));

            ItemBuilder builder;
            if(level > this.mine.getUpgrades().getOrDefault(UpgradeType.BLOCKS, 1)) {
                builder = getItemStack("Blocks.yml", this.getCfg().getConfigurationSection("BlockItem.Locked"), placeholders);
            } else {
                builder = getItemStack("Blocks.yml", materialData, this.getCfg().getConfigurationSection("BlockItem.Unlocked"), placeholders);

                if(this.mine.getBlocksLevel() == level) {
                    builder.withEnchant(Enchantment.DURABILITY, 3);
                    builder.withFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            this.setItem(slot, builder.build());
        }
    }
}
