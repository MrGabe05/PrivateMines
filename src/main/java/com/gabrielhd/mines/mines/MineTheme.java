package com.gabrielhd.mines.mines;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class MineTheme {

    private final String name;
    private String perms;
    private double price;

    private Location min;
    private Location max;
    private Location minMine;
    private Location maxMine;
    private Location spawnLocation;

    private Map<Integer, Long> required;
    private Map<Integer, Integer> size;

    public MineTheme(String name) {
        this.name = name;
        this.perms = "";

        this.price = 0.0;

        this.size = new HashMap<>();
        this.required = new HashMap<>();

        this.size.put(1, 5);
        this.size.put(2, 10);
        this.size.put(3, 20);

        this.required.put(1, 500L);
        this.required.put(2, 1000L);
        this.required.put(3, 2000L);
    }
}
