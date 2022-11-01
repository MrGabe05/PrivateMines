package com.gabrielhd.mines.hook;

import lombok.Getter;
import me.revils.rankup.api.RevPrestigeApi;
import org.bukkit.entity.Player;

public class RevRankup {

    @Getter private static RevRankup Api;

    public RevRankup() {
        Api = this;
    }

    public static long getPrestige(Player player) {
        return RevPrestigeApi.getPrestige(player);
    }
}
