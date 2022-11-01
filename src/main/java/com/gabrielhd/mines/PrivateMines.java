package com.gabrielhd.mines;

import com.gabrielhd.mines.commands.Commands;
import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.database.Database;
import com.gabrielhd.mines.hook.RevRankup;
import com.gabrielhd.mines.hook.TokenEnchant;
import com.gabrielhd.mines.hook.VaultHook;
import com.gabrielhd.mines.hook.WorldEditHook;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.listeners.MineListeners;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.tasks.ResetTask;
import com.gabrielhd.mines.tasks.SaveTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;

@Getter
public class PrivateMines extends JavaPlugin {

    @Getter private static PrivateMines instance;

    private WorldEditHook WorldEditHook;
    private int version;

    @Override
    public void onEnable() {
        instance = this;

        if(!VaultHook.setupEconomy()) {
            info("Vault not found...");
            return;
        } else {
            info("Vault hooked correctly.");
        }

        if(!loadWEProvider()) {
            info("WorldEdit or FastAsyncWorldEdit not installed, deactivating plugin...");

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        info("WorldEdit and FAWE loaded correctly.");

        this.loadFiles();

        new Config();
        new Database();
        new MineManager();

        if(Bukkit.getPluginManager().isPluginEnabled("TokenEnchant")) {
            new TokenEnchant();
        }

        if(Bukkit.getPluginManager().isPluginEnabled("RevRankup")) {
            new RevRankup();
        }

        Lang.loadLangs();

        this.getCommand("mine").setExecutor(new Commands());
        this.getServer().getPluginManager().registerEvents(new MineListeners(), this);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new SaveTask(), 1200L, 1200L);
        this.getServer().getScheduler().runTaskTimer(this, new ResetTask(), 20L * 60L * Config.TIME_RESET, 20L * 60L * Config.TIME_RESET);

        this.createWorld();
    }

    private boolean loadWEProvider() {
        String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].toLowerCase(Locale.ROOT);
        this.version = Integer.parseInt(internalsName.split("_")[1]);

        if(!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit") || !Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) return false;

        this.WorldEditHook = new WorldEditHook();
        return true;
    }

    private void loadFiles() {
        new YamlConfig(this, "lang/Lang_en");

        new YamlConfig(this, "Upgrades");
        new YamlConfig(this, "Settings");

        new YamlConfig(this, "menus/Mine");
        new YamlConfig(this, "menus/Themes");
        new YamlConfig(this, "menus/Confirm");
        new YamlConfig(this, "menus/Members");
        new YamlConfig(this, "menus/Public");
        new YamlConfig(this, "menus/Settings");
        new YamlConfig(this, "menus/Upgrades");
        new YamlConfig(this, "menus/upgrades/Size");
        new YamlConfig(this, "menus/upgrades/Value");
        new YamlConfig(this, "menus/upgrades/Blocks");

        File directory = new File(this.getDataFolder(), "themes/");
        if(!directory.exists()) directory.mkdirs();
    }

    private void createWorld() {
        World world = Bukkit.getWorld("MineWorld");
        if(world == null) {
            WorldCreator wc = new WorldCreator("MineWorld");
            wc.type(WorldType.FLAT);
            wc.generatorSettings("2;0;1;");
            world = wc.createWorld();
        }

        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
    }

    public static void debug(String message) {
        if(Config.DEBUG) {
            instance.getLogger().log(Level.INFO, message);
        }
    }

    public static void info(String message) {
        instance.getLogger().log(Level.INFO, message);
    }

    public static void error(String message) {
        instance.getLogger().log(Level.SEVERE, message);
    }
}
