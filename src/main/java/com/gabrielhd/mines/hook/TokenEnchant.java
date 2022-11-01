package com.gabrielhd.mines.hook;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.mines.Mine;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TokenEnchant implements Listener {

    @Getter private static TokenEnchant Api;

    public TokenEnchant() {
        Api = this;

        Bukkit.getPluginManager().registerEvents(this, PrivateMines.getInstance());
    }

    public static double getPlayerTokens(Player player) {
        return TokenEnchantAPI.getInstance().getTokens(player);
    }

    public static void removePlayerTokens(Player player, double amount) {
        TokenEnchantAPI.getInstance().removeTokens(player, amount);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTEBlockExplode(TEBlockExplodeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Mine mine = MineManager.from(block.getLocation());
        if(mine == null) return;

        if(!mine.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            Lang.MINE_NOT_MEMBER.send(player);
            return;
        }

        if(!mine.isInMine(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        event.blockList().removeIf(b -> !mine.isInMine(b.getLocation()));
        mine.setBlocks(mine.getBlocks() + event.blockList().size());
        mine.reset(false);
    }
}
