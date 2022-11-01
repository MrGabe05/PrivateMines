package com.gabrielhd.mines.lang;

import com.gabrielhd.mines.PrivateMines;
import com.gabrielhd.mines.config.YamlConfig;
import com.gabrielhd.mines.utils.TextPlaceholders;
import com.gabrielhd.mines.utils.Utils;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class Lang {

    private final Map<String, Object> langs = new HashMap<>();

    public static Lang
            ALREADY_HAVE_MINE,
            MINE_CREATING,
            MINE_CREATED,
            MINE_DELETED,
            MINE_TELEPORTING,
            MINE_LEFT_MEMBER,
            MINE_ENTER_MEMBER,
            MINE_MEMBER_JOIN,
            MINE_MEMBER_QUIT,
            MINE_MEMBER_KICK,
            MINE_MEMBER_INVITED,
            MINE_ALREADY_INVITE,
            MINE_COMMANDS_HELP,
            MINE_SIZE_LEVELUP,
            MINE_BLOCKS_LEVELUP,
            MINE_VALUE_LEVELUP,
            MINE_LOCKED,
            MINE_NOT_MEMBER,
            MINE_NOT_PERMISSIONS,
            MINE_PLAYER_NOT_MEMBER,
            MINE_SAME_PLAYER,
            MINE_ALREADY_BLOCK_SELECT,
            MINE_ALREADY_VALUE_SELECT,
            MINE_BLOCK_SELECTED,
            MINE_VALUE_SELECTED,
            MINE_NOT_EXISTS,
            PLAYER_NOT_EXISTS,
            PLAYER_NOT_ONLINE,
            PLAYER_NOT_HAVE_MINE,
            PLAYER_NOT_INVITE,
            PLAYER_INVITED_TO_MINE,
            PLAYER_INVITED_TO_MINE_HOVER,
            PLAYER_NOT_PERMISSIONS,
            PLAYER_CANT_JOIN_MINE,
            PLAYER_INSUFFICIENT_MONEY,
            PLAYER_INSUFFICIENT_TOKENS,
            PLAYER_INSUFFICIENT_PRESTIGE,
            THEME_NOT_CONFIGURED;

    public void addLang(String lang, String... text) {
        for(String s : text) {
            langs.put(lang.toLowerCase(Locale.ROOT), Utils.Color(s));
        }
    }

    public void send(Player player) {
        send(player, new TextPlaceholders());
    }

    public void send(Player player, TextPlaceholders textPlaceholders) {
        String lang = player.spigot().getLocale().split("_")[0];

        Object obj = langs.getOrDefault(lang.toLowerCase(Locale.ROOT), langs.values().stream().findFirst().orElse(this.getClass().getName() + " value not set"));

        if(obj instanceof List) {
            for(String s : (List<String>) obj) {
                s = textPlaceholders.parse(s);

                player.sendMessage(Utils.Color(s));
            }
            return;
        }

        player.sendMessage(Utils.Color(textPlaceholders.parse((String) obj)));
    }

    public String get(Player player) {
        return get(player, new TextPlaceholders());
    }

    public String get(Player player, TextPlaceholders textPlaceholders) {
        String lang = player.spigot().getLocale().split("_")[0];

        Object obj = langs.getOrDefault(lang.toLowerCase(Locale.ROOT), langs.values().stream().findFirst().orElse(this.getClass().getName() + " value not set"));

        return Utils.Color(textPlaceholders.parse((String) obj));
    }

    public static void loadLangs() {
        File langFolder = new File(PrivateMines.getInstance().getDataFolder(), "/lang/");
        if(!langFolder.exists()) langFolder.mkdir();

        Arrays.stream(Objects.requireNonNull(langFolder.listFiles())).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig langConfig = new YamlConfig(PrivateMines.getInstance(), file);

            String lang = file.getName().split("_")[1].replace(".yml", "");

            for(Field field : Lang.class.getFields()) {
                field.setAccessible(true);

                if(field.getType() == Lang.class) {
                    if(!langConfig.isSet(field.getName().toLowerCase(Locale.ROOT))) langConfig.set(field.getName().toLowerCase(Locale.ROOT), field.getName().toLowerCase(Locale.ROOT) + " value not set");

                    try {
                        if(field.get(null) == null) {
                            field.set(field, new Lang());
                        }

                        Lang obj = (Lang) field.get(null);

                        if(langConfig.isList(field.getName().toLowerCase(Locale.ROOT))) {
                            obj.addLang(lang, langConfig.getStringList(field.getName().toLowerCase(Locale.ROOT)).toArray(new String[0]));
                        } else {
                            obj.addLang(lang, langConfig.getString(field.getName().toLowerCase(Locale.ROOT)));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            langConfig.save();

            PrivateMines.getInstance().getLogger().info("Lang " + lang + " loaded.");
        });
    }
}
