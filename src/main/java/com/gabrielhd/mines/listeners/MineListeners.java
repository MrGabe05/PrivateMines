package com.gabrielhd.mines.listeners;

import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.mines.Mine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MineListeners implements Listener {

    @EventHandler
    public void onMineBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        Mine mine = MineManager.from(block.getLocation());
        if(mine == null || player.hasPermission("privatemines.build.bypass")) return;

        event.setCancelled(true);
        Lang.MINE_NOT_PERMISSIONS.send(player);
    }

    @EventHandler
    public void onMineBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Mine mine = MineManager.from(block.getLocation());
        if(mine == null) return;

        if(mine.isClose() && !mine.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            Lang.MINE_NOT_MEMBER.send(player);
            return;
        }

        if(!mine.isInMine(block.getLocation()) && !player.hasPermission("privatemines.build.bypass")) {
            event.setCancelled(true);

            Lang.MINE_NOT_PERMISSIONS.send(player);
            return;
        }

        if(isValueBlock(block)) {
            int valueLevel = mine.getValueLevel();

            String value = "note";
            switch (block.getType()) {
                case NOTE_BLOCK:
                    value = "note";
                    break;
                case SPONGE:
                    value = "sponge";
                    break;
                case SEA_LANTERN:
                    value = "sealantern";
                    break;
            }

            Config.VALUE_REWARDS.get(value).get(valueLevel).stream().map(cmd -> cmd.replaceAll("%player%", player.getName())).forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }

        mine.setBlocks(mine.getBlocks() + 1);
        mine.reset(false);
    }

    private boolean isValueBlock(Block block) {
        return block.getType() == Material.NOTE_BLOCK || block.getType() == Material.SPONGE || block.getType() == Material.SEA_LANTERN;
    }
}
