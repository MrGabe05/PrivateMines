package com.gabrielhd.mines.mines;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.database.Database;
import com.gabrielhd.mines.manager.MineManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public class MineCreator {

    private final Consumer<Mine> mineConsumer;

    private final UUID owner;
    private final MineTheme copiedMine;

    private final World world;

    private final int incrementX;
    private final int incrementZ;

    private int offsetX;
    private int offsetZ;

    private Map<Location, Block> paste;

    public MineCreator(UUID owner, MineTheme copiedMine, Consumer<Mine> mine) {
        this.owner = owner;
        this.mineConsumer = mine;

        this.copiedMine = copiedMine;
        this.world = Bukkit.getWorld("MineWorld");

        this.offsetX = 1000;
        this.offsetZ = 1000;
        this.incrementX = 500;
        this.incrementZ = 500;

        this.run();
    }

    public void run() {
        if (this.paste == null) {
            Map<Location, Block> copy = this.blocksFromTwoPoints(this.copiedMine.getMin(), this.copiedMine.getMax());

            this.paste = new HashMap<>();
            for (Location loc : copy.keySet()) {
                if (copy.get(loc).getType() != Material.AIR) {
                    this.paste.put(loc.clone().add(this.offsetX, 0.0, this.offsetZ), copy.get(loc));
                }
            }
            copy.clear();
        } else {
            Map<Location, Block> newPaste = new HashMap<>();
            for (Location loc : this.paste.keySet()) {
                if (this.paste.get(loc).getType() != Material.AIR) {
                    newPaste.put(loc.clone().add(this.incrementX, 0.0, this.incrementZ), this.paste.get(loc));
                }
            }
            this.paste.clear();
            this.paste.putAll(newPaste);
        }

        boolean safe = true;
        for (Location loc : this.paste.keySet()) {
            if(MineManager.from(loc) != null) {
                safe = false;
                break;
            }
        }

        if (!safe) {
            this.offsetX += this.incrementX;
            this.offsetZ += this.incrementZ;

            this.run();
            return;
        }

        PrivateMines.getInstance().getWorldEditHook().setBlocks(this, this.paste);
    }

    public void onComplete() {
        double minX = this.copiedMine.getMin().getX() + this.getOffsetX();
        double minZ = this.copiedMine.getMin().getZ() + this.getOffsetZ();
        double maxX = this.copiedMine.getMax().getX() + this.getOffsetX();
        double maxZ = this.copiedMine.getMax().getZ() + this.getOffsetZ();

        double minMineX = this.copiedMine.getMinMine().getX() + this.getOffsetX();
        double minMineZ = this.copiedMine.getMinMine().getZ() + this.getOffsetZ();
        double maxMineX = this.copiedMine.getMaxMine().getX() + this.getOffsetX();
        double maxMineZ = this.copiedMine.getMaxMine().getZ() + this.getOffsetZ();

        double spawnX = this.copiedMine.getSpawnLocation().getX() + this.getOffsetX();
        double spawnZ = this.copiedMine.getSpawnLocation().getZ() + this.getOffsetZ();

        Location min = new Location(this.world, minX, this.copiedMine.getMin().getY(), minZ);
        Location max = new Location(this.world, maxX, this.copiedMine.getMax().getY(), maxZ);
        Location minMine = new Location(this.world, minMineX, this.copiedMine.getMinMine().getY(), minMineZ);
        Location maxMine = new Location(this.world, maxMineX, this.copiedMine.getMaxMine().getY(), maxMineZ);
        Location spawn = new Location(this.world, spawnX, this.copiedMine.getSpawnLocation().getY(), spawnZ, this.copiedMine.getSpawnLocation().getYaw(), this.copiedMine.getSpawnLocation().getPitch());

        Mine mine = new Mine(this.owner, this.copiedMine);
        mine.setMin(min);
        mine.setMax(max);
        mine.setMinMine(minMine);
        mine.setMaxMine(maxMine);
        mine.setSpawnLocation(spawn);

        mine.reset(true);

        Database.getStorage().saveNewMine(mine);

        this.mineConsumer.accept(mine);
    }

    public Map<Location, Block> blocksFromTwoPoints(Location loc1, Location loc2) {
        Map<Location, Block> blocks = new HashMap<>();

        int topBlockX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int bottomBlockX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int topBlockY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int bottomBlockY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int topBlockZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        int bottomBlockZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        for (int x = bottomBlockX; x <= topBlockX; ++x) {
            for (int z = bottomBlockZ; z <= topBlockZ; ++z) {
                for (int y = bottomBlockY; y <= topBlockY; ++y) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                    if (block != null && block.getType() != Material.AIR) {
                        blocks.put(new Location(loc1.getWorld(), x, y, z), block);
                    }
                }
            }
        }
        return blocks;
    }
}
