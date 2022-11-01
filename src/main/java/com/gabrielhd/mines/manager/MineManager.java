package com.gabrielhd.mines.manager;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.database.Database;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.mines.MineCreator;
import com.gabrielhd.mines.mines.MineTheme;
import com.gabrielhd.mines.utils.LocUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class MineManager {

    private static final Set<Mine> available = new HashSet<>();
    private static final Map<UUID, Mine> mines = new HashMap<>();
    private static final Map<String, MineTheme> themes = new HashMap<>();

    public MineManager() {
        this.loadThemes();
        this.loadMines();
    }

    public static MineTheme of(String name) {
        return themes.get(name.toLowerCase());
    }

    public static void register(MineTheme theme) {
        themes.putIfAbsent(theme.getName().toLowerCase(), theme);

        YamlConfig themeConfig = new YamlConfig(PrivateMines.getInstance(), "themes/" + theme.getName());
        themeConfig.set("Name", theme.getName());
        themeConfig.set("Permission", theme.getPerms());
        themeConfig.set("Price", theme.getPrice());
        themeConfig.set("Max", LocUtils.LocationToString(theme.getMax()));
        themeConfig.set("Min", LocUtils.LocationToString(theme.getMin()));
        themeConfig.set("MaxMine", LocUtils.LocationToString(theme.getMaxMine()));
        themeConfig.set("MinMine", LocUtils.LocationToString(theme.getMinMine()));
        themeConfig.set("Spawn", LocUtils.LocationToString(theme.getSpawnLocation()));

        for(Map.Entry<Integer, Long> costs : theme.getRequired().entrySet()) {
            themeConfig.set("Sizes." + costs.getKey() + ".Amount", costs.getValue());
        }

        for(Map.Entry<Integer, Integer> sizes : theme.getSize().entrySet()) {
            themeConfig.set("Sizes." + sizes.getKey() + ".Size", sizes.getValue());
        }

        themeConfig.save();
    }

    public static void delete(String name) {
        MineTheme mineTheme = of(name);

        if(mineTheme != null) {
            File file = new File(PrivateMines.getInstance().getDataFolder(), "themes/" + mineTheme.getName() + ".yml");
            if (file.exists()) file.delete();

            themes.remove(name.toLowerCase());
        }
    }

    public static Mine of(UUID uuid) {
        return mines.get(uuid);
    }

    public static Mine from(Location loc) {
        return mines.values().stream().filter(mine -> mine.isInRegion(loc)).findFirst().orElse(null);
    }

    public static void create(Player player, MineTheme theme) {
        Lang.MINE_CREATING.send(player);

        if(!available.isEmpty()) {
            Mine mine = available.stream().filter(m -> m.getName().equals(theme.getName())).findFirst().orElse(null);
            if(mine != null) {
                available.removeIf(m -> m.getMineUUID().equals(mine.getMineUUID()));

                Lang.MINE_CREATED.send(player);

                mine.setOwner(player.getUniqueId());
                mine.addMember(player.getUniqueId());
                mine.reset(true);

                mines.put(player.getUniqueId(), mine);

                Lang.MINE_TELEPORTING.send(player);
                Bukkit.getScheduler().runTaskLater(PrivateMines.getInstance(), () -> player.teleport(mine.getSpawnLocation()), 20L);
                return;
            }
        }

        new MineCreator(player.getUniqueId(), theme, result -> {
            mines.put(player.getUniqueId(), result);

            Lang.MINE_CREATED.send(player);

            Lang.MINE_TELEPORTING.send(player);
            Bukkit.getScheduler().runTaskLater(PrivateMines.getInstance(), () -> player.teleport(result.getSpawnLocation()), 40L);
        });
    }

    public static void delete(Player player) {
        Mine mine = of(player.getUniqueId());
        if(mine != null) {
            mine.setOwner(null);

            mine.setBlocks(0);

            mine.getMembers().clear();
            mine.getInvites().clear();
            mine.getUpgrades().clear();

            PrivateMines.getInstance().getWorldEditHook().resetSize(mine);

            mines.remove(player.getUniqueId());

            available.add(mine);
        }

        Lang.MINE_DELETED.send(player);
    }

    public static Collection<MineTheme> getThemes() {
        return themes.values();
    }

    public static Collection<Mine> getMines() {
        List<Mine> minesL = new ArrayList<>(mines.values());
        minesL.addAll(available);

        return minesL;
    }

    private void loadThemes() {
        File directory = new File(PrivateMines.getInstance().getDataFolder(), "themes");
        if(!directory.exists()) directory.mkdirs();

        for(File file : Objects.requireNonNull(directory.listFiles())) {
            YamlConfig themeConfig = new YamlConfig(PrivateMines.getInstance(), file);

            MineTheme mineTheme = new MineTheme(themeConfig.getString("Name"));
            mineTheme.setPerms(themeConfig.getString("Permissions", ""));
            mineTheme.setPrice(themeConfig.getDouble("Price", 0.0));
            mineTheme.setMax(LocUtils.StringToLocation(themeConfig.getString("Max")));
            mineTheme.setMin(LocUtils.StringToLocation(themeConfig.getString("Min")));
            mineTheme.setMaxMine(LocUtils.StringToLocation(themeConfig.getString("MaxMine")));
            mineTheme.setMinMine(LocUtils.StringToLocation(themeConfig.getString("MinMine")));
            mineTheme.setSpawnLocation(LocUtils.StringToLocation(themeConfig.getString("Spawn")));

            Map<Integer, Long> required = new HashMap<>();
            Map<Integer, Integer> sizes = new HashMap<>();
            ConfigurationSection levelSizes = themeConfig.getConfigurationSection("Sizes");
            for(String levelS : levelSizes.getKeys(false)) {
                sizes.put(Integer.parseInt(levelS), levelSizes.getInt(levelS + ".Size"));
                required.put(Integer.parseInt(levelS), levelSizes.getLong(levelS + ".Amount"));
            }

            mineTheme.setSize(sizes);
            mineTheme.setRequired(required);

            themes.putIfAbsent(mineTheme.getName().toLowerCase(), mineTheme);
        }
    }

    private void loadMines() {
        Database.getStorage().loadMines(list -> {
            list.forEach(mine -> {
                mines.put(mine.getOwner(), mine);
            });
        });
    }
}
