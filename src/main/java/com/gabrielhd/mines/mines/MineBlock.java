package com.gabrielhd.mines.mines;

import lombok.Getter;
import org.bukkit.material.MaterialData;

@Getter
public class MineBlock {

    private final MaterialData data;
    private final double chance;

    public MineBlock(MaterialData data, double chance) {
        this.data = data;
        this.chance = chance;
    }
}