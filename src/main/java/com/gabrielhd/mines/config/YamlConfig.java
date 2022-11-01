package com.gabrielhd.mines.config;

import com.gabrielhd.mines.PrivateMines;
import lombok.Getter;
import org.bukkit.configuration.file.*;
import java.io.*;

@Getter
public class YamlConfig extends YamlConfiguration {

    private final File file;
    private final String path;
    private final PrivateMines plugin;

    public YamlConfig(PrivateMines plugin, String path) {
        this.plugin = plugin;

        this.path = path + ".yml";
        this.file = new File(plugin.getDataFolder(), this.path);

        this.saveDefault();
        this.reload();
    }

    public YamlConfig(PrivateMines plugin, File file) {
        this.plugin = plugin;

        this.path = file.getName() + ".yml";
        this.file = file;

        this.reload();
    }

    public void reload() {
        try {
            super.load(this.file);
        }
        catch (Exception ignored) {}
    }

    public void save() {
        try {
            super.save(this.file);
        }
        catch (Exception ignored) {}
    }

    public void saveDefault() {
        try {
            if (!this.file.exists()) {
                if (plugin.getResource(this.path) != null) {
                    plugin.saveResource(this.path, false);
                }
                else {
                    this.file.createNewFile();
                }
            }
        }
        catch (Exception ignored) {}
    }
}