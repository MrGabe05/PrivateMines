package com.gabrielhd.mines.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocUtils {

    public static String LocationToString(Location location) {
        return location.getWorld().getName() + "@" + location.getX() + "@" + location.getY() + "@" + location.getZ() + "@" + location.getYaw() + "@" + location.getPitch();
    }
    
    public static Location StringToLocation(String string) {
        String[] split = string.split("@");
        World world = Bukkit.getWorld(split[0]);
        if (world != null) {
            return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
        }
        return new Location(Bukkit.getWorld("world"), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }
}
