package com.gabrielhd.mines.mines;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.upgrades.UpgradeType;
import com.gabrielhd.mines.utils.TextPlaceholders;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.math.IntRange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;

import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
public class Mine extends MineTheme {

    private final UUID mineUUID;

    private UUID owner;

    private int blocks;

    private int valueLevel;
    private int blocksLevel;

    private boolean close;

    private Set<UUID> members;
    private Set<UUID> invites;
    private Map<UpgradeType, Integer> upgrades;

    public Mine(UUID owner, MineTheme theme) {
        this(owner, UUID.randomUUID(), theme);
    }

    public Mine(UUID owner, UUID mineUUID, MineTheme theme) {
        super(theme.getName());

        this.mineUUID = mineUUID;
        this.owner = owner;

        this.close = true;

        this.members = new HashSet<>();
        this.invites = new HashSet<>();
        this.upgrades = new HashMap<>();

        this.blocks = 0;
        this.valueLevel = 1;
        this.blocksLevel = 1;

        if(owner != null) this.members.add(owner);
    }

    public boolean isOwner(UUID uuid) {
        return this.owner.equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        return this.members.contains(uuid);
    }

    public boolean addMember(UUID uuid) {
        return this.members.add(uuid);
    }

    public boolean removeMember(UUID uuid) {
        return this.members.remove(uuid);
    }

    public boolean hasInvite(UUID uuid) {
        return this.invites.contains(uuid);
    }

    public boolean addInvite(UUID uuid) {
        return this.invites.add(uuid);
    }

    public boolean removeInvite(UUID uuid) {
        return this.invites.remove(uuid);
    }

    public boolean isResetRequired() {
        int blockAmount = 0;
        int airBlocks = 0;

        int topBlockX = this.getMax().getBlockX();
        int bottomBlockX = this.getMin().getBlockX();
        int topBlockY = this.getMax().getBlockY();
        int bottomBlockY = this.getMin().getBlockY();
        int topBlockZ = this.getMax().getBlockZ();
        int bottomBlockZ = this.getMin().getBlockZ();

        for (int x = bottomBlockX; x <= topBlockX; ++x) {
            for (int z = bottomBlockZ; z <= topBlockZ; ++z) {
                for (int y = bottomBlockY; y <= topBlockY; ++y) {
                    blockAmount++;

                    Block block = this.getMax().getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) {
                        airBlocks++;
                    }
                }
            }
        }

        return airBlocks >= blockAmount;
    }

    public boolean isInMine(Location loc) {
        return new IntRange(this.getMaxMine().getBlockX() + 0.5, this.getMinMine().getBlockX() + 0.5).containsInteger(loc.getBlockX()) &&
                new IntRange(this.getMaxMine().getBlockY(), this.getMinMine().getBlockY()).containsInteger(loc.getBlockY()) &&
                new IntRange(this.getMaxMine().getBlockZ() + 0.5, this.getMinMine().getBlockZ() + 0.5).containsInteger(loc.getBlockZ());
    }

    public boolean isInRegion(Location loc) {
        return new IntRange(this.getMax().getBlockX(), this.getMin().getBlockX()).containsInteger(loc.getBlockX()) &&
                new IntRange(this.getMax().getBlockY(), this.getMin().getBlockY()).containsInteger(loc.getBlockY()) &&
                new IntRange(this.getMax().getBlockZ(), this.getMin().getBlockZ()).containsInteger(loc.getBlockZ());
    }

    public void teleportPlayers() {
        for(OfflinePlayer offlinePlayer : this.getPlayers()) {
            if(!offlinePlayer.isOnline()) continue;

            Location loc = offlinePlayer.getPlayer().getLocation();

            if(isInMine(loc)) {
                Location to = PrivateMines.getInstance().getWorldEditHook().getCenter(this);
                to.setY(this.getMaxMine().getY() + 2);

                offlinePlayer.getPlayer().teleport(to);
            }
        }
    }

    public void reset(boolean force) {
        if(!force && !isResetRequired()) return;

        PrivateMines.getInstance().getWorldEditHook().resetMine(this);

        this.teleportPlayers();
    }

    public List<OfflinePlayer> getPlayers() {
        return this.members.stream().map(Bukkit::getOfflinePlayer).filter(Objects::nonNull).filter(OfflinePlayer::hasPlayedBefore).collect(Collectors.toList());
    }

    public void sendMessage(Lang message, TextPlaceholders textPlaceholders) {
        this.getPlayers().stream().filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).forEach(player -> message.send(player, textPlaceholders));
    }
}
