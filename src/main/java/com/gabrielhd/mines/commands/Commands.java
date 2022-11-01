package com.gabrielhd.mines.commands;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.database.Database;
import com.gabrielhd.mines.lang.Lang;
import com.gabrielhd.mines.manager.MineManager;
import com.gabrielhd.mines.menu.impl.ConfirmMenu;
import com.gabrielhd.mines.menu.impl.MineMenu;
import com.gabrielhd.mines.menu.impl.ThemesMenu;
import com.gabrielhd.mines.mines.Mine;
import com.gabrielhd.mines.mines.MineTheme;
import com.gabrielhd.mines.utils.Clickable;
import com.gabrielhd.mines.utils.TextPlaceholders;
import com.gabrielhd.mines.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Commands implements CommandExecutor {

    private final Map<UUID, MineTheme> themeCreator = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            Mine mine = MineManager.of(player.getUniqueId());
            if(args.length == 0) {
                if(mine == null) {
                    new ThemesMenu().open(player);
                    return true;
                }

                new MineMenu(mine).open(player);
                return true;
            }

            if(args.length == 1) {
                if(args[0].equalsIgnoreCase("create")) {
                    if(mine != null) {
                        Lang.ALREADY_HAVE_MINE.send(player);
                        return true;
                    }

                    new ThemesMenu().open(player);
                    return true;
                }

                if(args[0].equalsIgnoreCase("delete")) {
                    if(mine == null) {
                        Lang.MINE_NOT_EXISTS.send(player);
                        return true;
                    }

                    new ConfirmMenu(yes -> {
                        if(yes) {
                            MineManager.delete(player);
                        }
                    });
                    return true;
                }

                if(args[0].equalsIgnoreCase("reset")) {
                    if(!player.hasPermission("privatemine.forcereset")) return true;

                    if(mine == null) {
                        Lang.MINE_NOT_EXISTS.send(player);
                        return true;
                    }

                    mine.reset(true);
                    return true;
                }

                if(args[0].equalsIgnoreCase("go")) {
                    if(mine == null) {
                        Lang.MINE_NOT_EXISTS.send(player);
                        return true;
                    }

                    Lang.MINE_TELEPORTING.send(player);
                    player.teleport(mine.getSpawnLocation());
                    return true;
                }

                if(args[0].equalsIgnoreCase("theme")) {
                    if (!player.hasPermission("privatemines.theme")) {
                        Lang.PLAYER_NOT_PERMISSIONS.send(player);
                        return true;
                    }

                    this.sendMessages(player,
                            "&f",
                            "&a/mine theme create (Name) &8- &7Create a new theme.",
                            "&a/mine theme delete (Name) &8- &7Delete an existing theme.",
                            "&a/mine theme setRegion &8- &7You set the region of the schematic.",
                            "&a/mine theme setMine &8- &7You set the region of the mining area.",
                            "&a/mine theme setSpawn &8- &7You set the mine spawn.",
                            "&f");
                    return true;
                }
            }

            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("teleport")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if(!offlinePlayer.hasPlayedBefore()) {
                        Lang.PLAYER_NOT_EXISTS.send(player);
                        return true;
                    }

                    Mine targetMine = MineManager.of(offlinePlayer.getUniqueId());
                    if(targetMine == null) {
                        Lang.PLAYER_NOT_HAVE_MINE.send(player, new TextPlaceholders().set("%player%", offlinePlayer.getName()));
                        return true;
                    }

                    if(targetMine.isClose() && !targetMine.isMember(player.getUniqueId()) && !player.hasPermission("privatemines.teleport")) {
                        Lang.PLAYER_CANT_JOIN_MINE.send(player, new TextPlaceholders().set("%player%", offlinePlayer.getName()));
                        return true;
                    }

                    Lang.MINE_TELEPORTING.send(player);
                    player.teleport(targetMine.getSpawnLocation());
                    return true;
                }

                if(args[0].equalsIgnoreCase("invite")) {
                    if(mine == null) {
                        Lang.MINE_NOT_EXISTS.send(player);
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);
                    if(target == null) {
                        Lang.PLAYER_NOT_ONLINE.send(player);
                        return true;
                    }

                    if(target.getUniqueId().equals(player.getUniqueId())) {
                        Lang.MINE_SAME_PLAYER.send(player);
                        return true;
                    }

                    if(!mine.addInvite(target.getUniqueId())) {
                        Lang.MINE_ALREADY_INVITE.send(player, new TextPlaceholders().set("%player%", target.getName()));
                        return true;
                    }

                    Lang.MINE_MEMBER_INVITED.send(player, new TextPlaceholders().set("%player%", target.getName()));

                    Clickable invite = new Clickable(Lang.PLAYER_INVITED_TO_MINE.get(target, new TextPlaceholders().set("%player%", player.getName())), Lang.PLAYER_INVITED_TO_MINE_HOVER.get(target), "/mine join " + player.getName());
                    invite.sendToPlayer(target);

                    Bukkit.getScheduler().runTaskLaterAsynchronously(PrivateMines.getInstance(), () -> mine.removeInvite(target.getUniqueId()), 1200);
                    return true;
                }

                if(args[0].equalsIgnoreCase("leave")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if(!offlinePlayer.hasPlayedBefore()) {
                        Lang.PLAYER_NOT_EXISTS.send(player);
                        return true;
                    }

                    Mine targetMine = MineManager.of(offlinePlayer.getUniqueId());
                    if(targetMine == null) {
                        Lang.PLAYER_NOT_HAVE_MINE.send(player, new TextPlaceholders().set("%player%", offlinePlayer.getName()));
                        return true;
                    }

                    if(!targetMine.removeMember(player.getUniqueId())) {
                        Lang.MINE_NOT_MEMBER.send(player);
                        return true;
                    }

                    Lang.MINE_LEFT_MEMBER.send(player);

                    targetMine.sendMessage(Lang.MINE_MEMBER_QUIT, new TextPlaceholders().set("%player%", player.getName()));
                    return true;
                }

                if(args[0].equalsIgnoreCase("join")) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if(!offlinePlayer.hasPlayedBefore()) {
                        Lang.PLAYER_NOT_EXISTS.send(player);
                        return true;
                    }

                    Mine targetMine = MineManager.of(offlinePlayer.getUniqueId());
                    if(targetMine == null) {
                        Lang.PLAYER_NOT_HAVE_MINE.send(player, new TextPlaceholders().set("%player%", offlinePlayer.getName()));
                        return true;
                    }

                    if(!targetMine.hasInvite(player.getUniqueId())) {
                        Lang.PLAYER_NOT_INVITE.send(player);
                        return true;
                    }

                    targetMine.sendMessage(Lang.MINE_MEMBER_JOIN, new TextPlaceholders().set("%player%", player));
                    if(targetMine.addMember(player.getUniqueId())) {
                        targetMine.removeInvite(player.getUniqueId());

                        Lang.MINE_ENTER_MEMBER.send(player);
                    }
                    return true;
                }

                if(args[0].equalsIgnoreCase("kick")) {
                    if(mine == null) {
                        Lang.MINE_NOT_EXISTS.send(player);
                        return true;
                    }

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                    if(!offlinePlayer.hasPlayedBefore()) {
                        Lang.PLAYER_NOT_EXISTS.send(player);
                        return true;
                    }

                    if(offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                        Lang.MINE_SAME_PLAYER.send(player);
                        return true;
                    }

                    if(!mine.removeMember(offlinePlayer.getUniqueId())) {
                        Lang.MINE_PLAYER_NOT_MEMBER.send(player);
                        return true;
                    }

                    Lang.MINE_MEMBER_KICK.send(player, new TextPlaceholders().set("%player%", offlinePlayer.getName()));
                    return true;
                }

                if(args[0].equalsIgnoreCase("theme")) {
                    if (!player.hasPermission("privatemines.theme")) {
                        Lang.PLAYER_NOT_PERMISSIONS.send(player);
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("setregion")) {
                        MineTheme theme = themeCreator.get(player.getUniqueId());
                        if(theme == null) {
                            this.sendMessages(player, "&cFirst start creating a new theme using /mine theme create (name)");
                            return true;
                        }

                        Location pos1 = PrivateMines.getInstance().getWorldEditHook().getPos1(player);
                        Location pos2 = PrivateMines.getInstance().getWorldEditHook().getPos2(player);
                        if(pos1 == null || pos2 == null) {
                            this.sendMessages(player,
                                    (pos1 == null ? "&cSelect position #1 with the wand (From worldedit)" : "&aPosition #1 already selected correctly"),
                                    (pos2 == null ? "&cSelect position #2 with the wand (From worldedit)" : "&aPosition #2 already selected correctly"));
                            return true;
                        }

                        theme.setMax(new Location(pos1.getWorld(), Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ())));
                        theme.setMin(new Location(pos1.getWorld(), Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ())));

                        this.sendMessages(player, "&aRegion select correctly!",
                                "&f",
                                "&aNow select the min and max, then use",
                                "&a&l/mine theme setmine &ato set the mining zone.");
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("setmine")) {
                        MineTheme theme = themeCreator.get(player.getUniqueId());
                        if(theme == null) {
                            this.sendMessages(player, "&cFirst start creating a new theme using /mine theme create (name)");
                            return true;
                        }

                        Location pos1 = PrivateMines.getInstance().getWorldEditHook().getPos1(player);
                        Location pos2 = PrivateMines.getInstance().getWorldEditHook().getPos2(player);
                        if(pos1 == null || pos2 == null) {
                            this.sendMessages(player,
                                    (pos1 == null ? "&cSelect position #1 with the wand (From worldedit)" : "&aPosition #1 already selected correctly"),
                                    (pos2 == null ? "&cSelect position #2 with the wand (From worldedit)" : "&aPosition #2 already selected correctly"));
                            return true;
                        }

                        double maxX = Math.max(pos1.getX(), pos2.getX());
                        double minX = Math.min(pos1.getX(), pos2.getX());
                        double maxY = Math.max(pos1.getY(), pos2.getY());
                        double minY = Math.min(pos1.getY(), pos2.getY());
                        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
                        double minZ = Math.min(pos1.getZ(), pos2.getZ());

                        Location max = new Location(pos1.getWorld(), maxX - 1, maxY, maxZ - 1);
                        Location min = new Location(pos1.getWorld(), minX + 1, minY, minZ + 1);
                        theme.setMaxMine(max);
                        theme.setMinMine(min);

                        this.sendMessages(player, "&aRegion Mine select correctly!",
                                "&f",
                                "&aNow use &l/mine theme setspawn &ato set mine spawn.");
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("setspawn")) {
                        MineTheme theme = themeCreator.get(player.getUniqueId());
                        if(theme == null) {
                            this.sendMessages(player, "&cFirst start creating a new theme using /mine theme create (name)");
                            return true;
                        }

                        theme.setSpawnLocation(player.getLocation());

                        this.sendMessages(player, "&aSpawn setted correctly!",
                                "&f",
                                "&aNow use &l/mine theme finish &ato register the new Theme.");
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("finish")) {
                        MineTheme theme = themeCreator.get(player.getUniqueId());
                        if(theme == null) {
                            this.sendMessages(player, "&cFirst start creating a new theme using &l/mine theme create (name)");
                            return true;
                        }

                        if(theme.getMax() == null || theme.getMin() == null) {
                            this.sendMessages(player, "&cSet the region of the map, select the max and min then run &l/mine theme setregion");
                            return true;
                        }

                        if(theme.getMaxMine() == null || theme.getMinMine() == null) {
                            this.sendMessages(player, "&cSet the mine region, select the max and min then run &l/mine theme setmine");
                            return true;
                        }

                        if(theme.getSpawnLocation() == null) {
                            this.sendMessages(player, "&cSet mine spawn, use &l/mine theme setspawn &ccommand for that");
                            return true;
                        }

                        MineManager.register(theme);
                        themeCreator.remove(player.getUniqueId());

                        this.sendMessages(player, "&aTheme created successfully!");
                        return true;
                    }

                    this.sendMessages(player,
                            "&f",
                            "&a/mine theme create (Name) &8- &7Create a new theme.",
                            "&a/mine theme delete (Name) &8- &7Delete an existing theme.",
                            "&a/mine theme setRegion &8- &7You set the region of the schematic.",
                            "&a/mine theme setMine &8- &7You set the region of the mining area.",
                            "&a/mine theme setSpawn &8- &7You set the mine spawn.",
                            "&f");
                    return true;
                }
            }

            if(args.length == 3) {
                if(args[0].equalsIgnoreCase("theme")) {
                    if (!player.hasPermission("privatemines.theme")) {
                        Lang.PLAYER_NOT_PERMISSIONS.send(player);
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("create")) {
                        if(this.themeCreator.containsKey(player.getUniqueId())) {
                            this.sendMessages(player, "&cYou are already creating a Theme,",
                                    "&cfinish it or delete it with /mine theme delete " + this.themeCreator.get(player.getUniqueId()).getName());
                            return true;
                        }

                        String name = args[2];

                        if(MineManager.of(name) != null) {
                            this.sendMessages(player, "&cThis Theme already exists.");
                            return true;
                        }

                        this.themeCreator.put(player.getUniqueId(), new MineTheme(name));

                        player.getInventory().addItem(new ItemStack(Material.WOOD_AXE, 1));
                        this.sendMessages(player,
                                "&aYou have created a new theme,",
                                "&anow select the max and min of the mine build",
                                "&aand then run &l/mine theme setregion &acommand.");
                        return true;
                    }

                    if(args[1].equalsIgnoreCase("delete")) {
                        String name = args[2];

                        MineTheme theme = MineManager.of(name);
                        if(theme == null) {
                            theme = this.themeCreator.get(player.getUniqueId());
                        }

                        if(theme == null) {
                            this.sendMessages(player, "&cThere is no Theme with that name");
                            return true;
                        }

                        MineTheme finalTheme = theme;
                        new ConfirmMenu(yes -> {
                            if(finalTheme.getName().equalsIgnoreCase(name)) {
                                this.themeCreator.remove(player.getUniqueId());
                            }

                            MineManager.delete(name);

                            this.sendMessages(player, "&cTheme removed successfully!");
                        }).open(player);
                        return true;
                    }

                    this.sendMessages(player,
                            "&f",
                            "&a/mine theme create (Name) &8- &7Create a new theme.",
                            "&a/mine theme delete (Name) &8- &7Delete an existing theme.",
                            "&a/mine theme setRegion &8- &7You set the region of the schematic.",
                            "&a/mine theme setMine &8- &7You set the region of the mining area.",
                            "&a/mine theme setSpawn &8- &7You set the mine spawn.",
                            "&f");
                    return true;
                }
            }

            Lang.MINE_COMMANDS_HELP.send(player);
            return true;
        }

        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("checkdb")) {
                Database.getStorage().printRegisteredMines();
                return true;
            }
        }
        return false;
    }

    private void sendMessages(Player player, String... messages) {
        Arrays.stream(messages).map(Utils::Color).forEach(player::sendMessage);
    }
}
