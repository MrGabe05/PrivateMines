package com.gabrielhd.mines.database;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.mines.MineTheme;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.LocUtils;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class DataHandler {

    protected abstract Connection getConnection();

    private static final String TABLE = "privatemines_data_";

    private final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE + " (uuid VARCHAR(40), owner VARCHAR(40), theme TEXT, blocks int, blockLevel int, valueLevel int, close boolean, members TEXT, upgrades TEXT, region TEXT, mine TEXT, spawn TEXT, PRIMARY KEY ('uuid'));";

    private final String INSERT_CLAIM = "INSERT INTO " + TABLE + " (uuid, owner, theme, blocks, blockLevel, valueLevel, close, members, upgrades, region, mine, spawn) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');";
    private final String UPDATE_CLAIM = "UPDATE " + TABLE + " SET theme='%s', blocks='%s', blockLevel='%s', valueLevel='%s', close='%s', members='%s', upgrades='%s', region='%s', mine='%s', spawn='%s' WHERE uuid='%s';";

    private final String SELECT_ALL_MINES = "SELECT * FROM " + TABLE + ";";
    private final String SELECT_MINE = "SELECT * FROM " + TABLE + " WHERE uuid='%s';";

    protected synchronized void setupTable() {
        try {
            this.execute(CREATE_TABLE);
        } catch (SQLException e) {
            PrivateMines.getInstance().getLogger().log(Level.SEVERE, "Error inserting columns! Please check your configuration!");
            PrivateMines.getInstance().getLogger().log(Level.SEVERE, "If this error persists, please report it to the developer!");

            e.printStackTrace();
        }
    }

    protected void execute(String sql, Object... replacements) throws SQLException {
        Connection connection = this.getConnection();
        try(PreparedStatement statement = connection.prepareStatement(String.format(sql, replacements))) {
            statement.execute();
        }
    }

    public void saveNewMine(Mine mine) {
        Bukkit.getScheduler().runTaskAsynchronously(PrivateMines.getInstance(), () -> {
            if(Config.DEBUG) PrivateMines.getInstance().getLogger().log(Level.INFO, "Saving new mine " + mine.getMineUUID().toString());
            try {
                UUID uuid = mine.getMineUUID();
                UUID owner = mine.getOwner();
                String theme = mine.getName();
                int blocks = mine.getBlocks();
                int blockLevel = mine.getBlocksLevel();
                int valueLevel = mine.getValueLevel();
                boolean close = mine.isClose();
                String members = getSetToString(mine.getMembers());
                String upgrades = getMapToString(mine.getUpgrades());

                String region = LocUtils.LocationToString(mine.getMax()) + ";" + LocUtils.LocationToString(mine.getMin());
                String mineRegion = LocUtils.LocationToString(mine.getMaxMine()) + ";" + LocUtils.LocationToString(mine.getMinMine());
                String spawn = LocUtils.LocationToString(mine.getSpawnLocation());

                this.execute(INSERT_CLAIM, uuid.toString(), owner.toString(), theme, blocks, blockLevel, valueLevel, close, members, upgrades, region, mineRegion, spawn);

                if(Config.DEBUG) PrivateMines.getInstance().getLogger().log(Level.INFO, "Saved claim " + uuid);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public void saveMine(Mine mine) {
        Bukkit.getScheduler().runTaskAsynchronously(PrivateMines.getInstance(), () -> {
            if(Config.DEBUG) PrivateMines.getInstance().getLogger().log(Level.INFO, "Saving mine " + mine.getMineUUID().toString());

            Connection connection = this.getConnection();
            try(PreparedStatement statement = connection.prepareStatement(String.format(SELECT_MINE, mine.getMineUUID().toString()))) {
                ResultSet rs = statement.executeQuery();
                if(rs != null && rs.next()) {
                    UUID uuid = mine.getMineUUID();
                    String theme = mine.getName();
                    int blocks = mine.getBlocks();
                    int blockLevel = mine.getBlocksLevel();
                    int valueLevel = mine.getValueLevel();
                    boolean close = mine.isClose();
                    String members = getSetToString(mine.getMembers());
                    String upgrades = getMapToString(mine.getUpgrades());

                    String region = LocUtils.LocationToString(mine.getMax()) + ";" + LocUtils.LocationToString(mine.getMin());
                    String mineRegion = LocUtils.LocationToString(mine.getMaxMine()) + ";" + LocUtils.LocationToString(mine.getMinMine());
                    String spawn = LocUtils.LocationToString(mine.getSpawnLocation());

                    this.execute(UPDATE_CLAIM, theme, blocks, blockLevel, valueLevel, close, members, upgrades, region, mineRegion, spawn, uuid.toString());
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void printRegisteredMines() {
        Bukkit.getScheduler().runTaskAsynchronously(PrivateMines.getInstance(), () -> {
            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_MINES)) {
                ResultSet rs = statement.executeQuery();
                if(rs == null) {
                    PrivateMines.debug("ResultSet null");
                    return;
                }

                while (rs.next()) {
                    PrivateMines.debug(" ");
                    PrivateMines.debug("Owner: " + Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("owner"))));
                    PrivateMines.debug("Mine UUID: " + rs.getString("uuid"));
                    PrivateMines.debug("Owner UUID: " + rs.getString("owner"));
                    PrivateMines.debug("Theme Mine: " + rs.getString("theme"));
                    PrivateMines.debug("Region Locs: " + rs.getString("region"));
                    PrivateMines.debug("Spawn Mine: " + rs.getString("spawn"));
                }
            } catch (SQLException throwables) {
                PrivateMines.error(throwables.getMessage());

                throwables.printStackTrace();
            }
        });
    }

    public void loadMines(Consumer<List<Mine>> action) {
        Bukkit.getScheduler().runTaskAsynchronously(PrivateMines.getInstance(), () -> {
            List<Mine> mines = new ArrayList<>();

            Connection connection = this.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL_MINES)) {
                ResultSet rs = statement.executeQuery();
                if(rs == null) {
                    action.accept(mines);
                    return;
                }

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    MineTheme theme = MineManager.of(rs.getString("theme"));

                    Mine mine = new Mine(owner, uuid, theme);
                    mine.setBlocks(rs.getInt("blocks"));
                    mine.setClose(rs.getBoolean("close"));
                    mine.setMembers(getStringToList(rs.getString("members")));
                    mine.setUpgrades(getStringToMap(rs.getString("upgrades")));
                    mine.setBlocksLevel(rs.getInt("blockLevel"));
                    mine.setValueLevel(rs.getInt("valueLevel"));

                    String[] regions = rs.getString("region").split(";");
                    mine.setMax(LocUtils.StringToLocation(regions[0]));
                    mine.setMin(LocUtils.StringToLocation(regions[1]));

                    String[] mineRegions = rs.getString("mine").split(";");
                    mine.setMaxMine(LocUtils.StringToLocation(mineRegions[0]));
                    mine.setMinMine(LocUtils.StringToLocation(mineRegions[1]));

                    mine.setSpawnLocation(LocUtils.StringToLocation(rs.getString("spawn")));

                    mines.add(mine);

                    PrivateMines.debug("Mine " + mine.getMineUUID().toString() + " Loaded");
                }
            } catch (SQLException throwables) {
                PrivateMines.error(throwables.getMessage());

                throwables.printStackTrace();
            }
            action.accept(mines);
        });
    }

    private String getSetToString(Set<UUID> list) {
        StringBuilder builder = new StringBuilder();
        for(UUID obj : list) {
            if(builder.length() > 0) builder.append(";");

            builder.append(obj.toString());
        }
        return builder.toString();
    }

    private Set<UUID> getStringToList(String results) {
        if(results.isEmpty()) return new HashSet<>();

        String[] resultsSplit = results.split(";");

        return Arrays.stream(resultsSplit).map(UUID::fromString).collect(Collectors.toSet());
    }

    private String getMapToString(Map<UpgradeType, Integer> map) {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<UpgradeType, Integer> value : map.entrySet()) {
            if(builder.length() > 0) builder.append(";");

            builder.append(value.getKey().name()).append(":").append(value.getValue());
        }
        return builder.toString();
    }

    private Map<UpgradeType, Integer> getStringToMap(String results) {
        Map<UpgradeType, Integer> map = new HashMap<>();

        if(results.isEmpty()) return map;

        String[] resultsSplit = results.split(";");
        for(String s : resultsSplit) {
            String[] split = s.split(":");

            map.put(UpgradeType.valueOf(split[0].toUpperCase()), Integer.parseInt(split[1]));
        }

        return map;
    }
}
