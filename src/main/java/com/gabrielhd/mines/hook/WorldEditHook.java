package com.gabrielhd.mines.hook;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.Config;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.mines.MineBlock;
import com.gabrielhd.mines.mines.MineCreator;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.Utils;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WorldEditHook {

    public Location getCenter(Mine mine) {
        BukkitWorld w = new BukkitWorld(mine.getSpawnLocation().getWorld());
        CuboidRegion region = new CuboidRegion(w, new BlockVector(mine.getMaxMine().getX(), mine.getMaxMine().getY(), mine.getMaxMine().getZ()), new BlockVector(mine.getMinMine().getX(), mine.getMinMine().getY(), mine.getMinMine().getZ()));

        return BukkitUtil.toLocation(mine.getSpawnLocation().getWorld(), region.getCenter());
    }

    public void resetSize(Mine mine) {
        TaskManager.IMP.async(() -> {
            BukkitWorld w = new BukkitWorld(mine.getSpawnLocation().getWorld());
            CuboidRegion region = new CuboidRegion(w, new BlockVector(mine.getMaxMine().getX(), mine.getMaxMine().getY(), mine.getMaxMine().getZ()), new BlockVector(mine.getMinMine().getX(), mine.getMinMine().getY(), mine.getMinMine().getZ()));

            EditSession session = new EditSession(w, -1);
            session.setBlocks(region, new BaseBlock(7));
            session.flushQueue();
        });
    }

    public void resetMine(Mine mine) {
        List<BlockChance> blocksChance = new ArrayList<>();

        Map<MaterialData, Float> blocksLevel = new HashMap<>();
        for(Map.Entry<String, Map<Integer, Float>> value : Config.VALUE.entrySet()) {
            float chance = value.getValue().getOrDefault(mine.getValueLevel(), 0.0f);
            if(value.getKey().equalsIgnoreCase("Sponge")) {
                if(chance > 0.0f) blocksLevel.put(new MaterialData(Material.SPONGE), chance);
            }
            if(value.getKey().equalsIgnoreCase("Note")) {
                if(chance > 0.0f) blocksLevel.put(new MaterialData(Material.NOTE_BLOCK), chance);
            }
            if(value.getKey().equalsIgnoreCase("SeaLantern")) {
                if(chance > 0.0f) blocksLevel.put(new MaterialData(Material.SEA_LANTERN), chance);
            }
        }
        blocksLevel.put(Config.BLOCKS.get(mine.getBlocksLevel()), 100.0f);

        int sizeLevel = mine.getUpgrades().getOrDefault(UpgradeType.SIZE, 1);
        int size = mine.getSize().get(sizeLevel);

        TaskManager.IMP.async(() -> {
            BukkitWorld w = new BukkitWorld(mine.getSpawnLocation().getWorld());
            CuboidRegion region = new CuboidRegion(w, new BlockVector(mine.getMaxMine().getX(), mine.getMaxMine().getY(), mine.getMaxMine().getZ()), new BlockVector(mine.getMinMine().getX(), mine.getMinMine().getY(), mine.getMinMine().getZ()));

            double maxX = region.getCenter().getX() + size;
            double maxZ = region.getCenter().getZ() + size;
            double minX = region.getCenter().getX() - size;
            double minZ = region.getCenter().getZ() - size;

            if(maxX >= mine.getMaxMine().getBlockX()) {
                maxX = mine.getMaxMine().getBlockX() - 2;
            }
            if(maxZ >= mine.getMaxMine().getBlockZ()) {
                maxZ = mine.getMaxMine().getBlockZ() - 2;
            }
            if(minX <= mine.getMinMine().getBlockX()) {
                minX = mine.getMinMine().getBlockX() + 2;
            }
            if(minZ <= mine.getMinMine().getBlockZ()) {
                minZ = mine.getMinMine().getBlockZ() + 2;
            }

            BlockVector maxVector = new BlockVector(maxX, mine.getMaxMine().getY() - 1, maxZ);
            BlockVector minVector = new BlockVector(minX, mine.getMinMine().getY() + 1, minZ);

            CuboidRegion emptyRegion = new CuboidRegion(w, new BlockVector(maxVector).add(1, 1, 1), new BlockVector(minVector).subtract(1, 1, 1));

            EditSession emptySession = new EditSession(w, -1);

            int e = emptySession.setBlocks(emptyRegion, new BaseBlock(0));
            emptySession.flushQueue();
            PrivateMines.debug("Ready empty = " + e);

            TaskManager.IMP.async(() -> {
                CuboidRegion miningRegion = new CuboidRegion(w, maxVector, minVector);

                List<MineBlock> probabilityMap = Utils.mapComposition(blocksLevel);
                EditSession session = new EditSession(w, -1);
                for (MineBlock ce : probabilityMap) {
                    BaseBlock baseBlock = new BaseBlock(ce.getData().getItemTypeId(), ce.getData().getData());
                    BlockChance blockChance = new BlockChance(baseBlock, ce.getChance());
                    blocksChance.add(blockChance);
                    PrivateMines.debug(blockChance + " Added");
                }
                RandomFillPattern fillPattern = new RandomFillPattern(blocksChance);

                PrivateMines.debug("Session Apply...");

                int i = session.setBlocks(miningRegion, fillPattern);
                session.flushQueue();
                PrivateMines.debug("Ready blocks = " + i);
            });
        });
    }

    public void setBlocks(MineCreator mineCreator, Map<Location, Block> b) {
        ConcurrentMap<Location, Block> blocks = new ConcurrentHashMap<>(b);
        Iterator<Location> iterator = blocks.keySet().iterator();

        if(blocks.isEmpty() || !iterator.hasNext()) {
            mineCreator.getMineConsumer().accept(null);
            return;
        }

        TaskManager.IMP.async(() -> {
            EditSession editSession = new EditSessionBuilder(mineCreator.getWorld().getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            Iterator<Map.Entry<Location, Block>> itera = blocks.entrySet().iterator();
            Map.Entry<Location, Block> entry;
            while (itera.hasNext()) {
                entry = itera.next();
                try {
                    editSession.setBlock(new Vector(entry.getKey().getBlockX(), entry.getKey().getBlockY(), entry.getKey().getZ()), new BaseBlock(entry.getValue().getTypeId(), entry.getValue().getData()));
                }
                catch (MaxChangedBlocksException ignored) {}
            }
            editSession.flushQueue();

            mineCreator.onComplete();
            TaskManager.IMP.task(blocks::clear);
        });
    }

    public Location getPos1(Player player) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

        Location pos1 = null;
        try {
            pos1 = BukkitUtil.toLocation(player.getWorld(), worldEdit.getSession(player).getSelection(new BukkitWorld(player.getWorld())).getMaximumPoint());
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }
        return pos1;
    }

    public Location getPos2(Player player) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

        Location pos2 = null;
        try {
            pos2 = BukkitUtil.toLocation(player.getWorld(), worldEdit.getSession(player).getSelection(new BukkitWorld(player.getWorld())).getMinimumPoint());
        } catch (IncompleteRegionException e) {
            e.printStackTrace();
        }

        return pos2;
    }
}
