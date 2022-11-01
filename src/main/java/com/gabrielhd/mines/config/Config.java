package com.gabrielhd.mines.config;

import com.gabrielhd.mines.PrivateMines;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static boolean DEBUG;

    public static String TYPE, HOST, PORT, DATABASE, USERNAME, PASSWORD;

    public static int MINE_MEMBER_LIMITS, TIME_RESET;

    public static Map<Integer, Long> BLOCKS_COST;
    public static Map<Integer, MaterialData> BLOCKS;
    public static Map<String, Map<Integer, Float>> VALUE;
    public static Map<String, Map<Integer, Double>> VALUE_COST;
    public static Map<String, Map<Integer, List<String>>> VALUE_REWARDS;

    public Config() {
        YamlConfig config = new YamlConfig(PrivateMines.getInstance(), "Settings");

        DEBUG = config.getBoolean("Settings.Debug", false);
        TIME_RESET = config.getInt("Settings.TimeReset", 5);
        MINE_MEMBER_LIMITS = config.getInt("Settings.MineMemberLimits", 3);
        TYPE = config.getString("StorageType", "sqlite");
        HOST = config.getString("MySQL.Host", "localhost");
        PORT = config.getString("MySQL.Port", "3306");
        DATABASE = config.getString("MySQL.Database", "privatemines");
        USERNAME = config.getString("MySQL.Username", "root");
        PASSWORD = config.getString("MySQL.Password", "pass1234");

        YamlConfig upgradesConfig = new YamlConfig(PrivateMines.getInstance(), "Upgrades");

        Map<String, Map<Integer, Float>> value = new HashMap<>();
        Map<String, Map<Integer, Double>> value_cost = new HashMap<>();
        Map<String, Map<Integer, List<String>>> value_rewards = new HashMap<>();

        ConfigurationSection levelValue = upgradesConfig.getConfigurationSection("Value");
        for(String valueType : levelValue.getKeys(false)) {
            Map<Integer, Float> values = new HashMap<>();
            Map<Integer, Double> valueCosts = new HashMap<>();
            Map<Integer, List<String>> valueRewards = new HashMap<>();

            ConfigurationSection valueSection = levelValue.getConfigurationSection(valueType);
            for(String levelS : valueSection.getKeys(false)) {
                int level = Integer.parseInt(levelS);
                float chance = Float.parseFloat(valueSection.getString(levelS + ".Chance"));
                double price = Double.parseDouble(valueSection.getString(levelS + ".Price"));

                values.putIfAbsent(level, chance);
                valueCosts.putIfAbsent(level, price);
                valueRewards.putIfAbsent(level, valueSection.getStringList(levelS + ".Rewards"));

                PrivateMines.debug("Added new value block Level " + level + " Chance " + chance + " Price " + price);
            }

            value_rewards.put(valueType.toLowerCase(), valueRewards);
            value_cost.put(valueType.toLowerCase(), valueCosts);
            value.put(valueType.toLowerCase(), values);
        }
        VALUE_REWARDS = value_rewards;
        VALUE_COST = value_cost;
        VALUE = value;

        Map<Integer, Long> blocks_costs = new HashMap<>();
        Map<Integer, MaterialData> blocks = new HashMap<>();

        ConfigurationSection levelBlocks = upgradesConfig.getConfigurationSection("Blocks");
        for(String levelS : levelBlocks.getKeys(false)) {
            Material material = Material.getMaterial(levelBlocks.getString(levelS + ".Block"));
            if(material != null) {
                int level = Integer.parseInt(levelS);
                byte data = Byte.parseByte(levelBlocks.getString(levelS + ".Data"));
                long amount = levelBlocks.getLong(levelS + ".Amount", 1000L);

                blocks.put(level, new MaterialData(material, data));
                blocks_costs.put(level, amount);

                PrivateMines.debug("Added new block " + material.name() + " Level " + level + " Amount " + amount);
            }
        }
        BLOCKS = blocks;
        BLOCKS_COST = blocks_costs;
    }
}
