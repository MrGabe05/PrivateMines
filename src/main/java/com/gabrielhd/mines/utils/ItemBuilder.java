package com.gabrielhd.mines.utils;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public final class ItemBuilder implements Cloneable {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private boolean textured = false;

    public ItemBuilder(Material type, int data) {
        itemStack = new ItemStack(type, 1, (short) data);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder asSkullOf(OfflinePlayer player) {
        if(itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;

            skullMeta.setOwner(player.getName());
            textured = true;
        }
        return this;
    }

    public ItemBuilder withName(String name){
        if(name != null)
            itemMeta.setDisplayName(Utils.Color(name));
        return this;
    }

    public ItemBuilder withLore(List<String> lore){
        if(lore != null)
            itemMeta.setLore(lore.stream().map(Utils::Color).collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchant, int level){
        itemMeta.addEnchant(enchant, level, true);
        return this;
    }

    public ItemBuilder withFlags(ItemFlag... itemFlags){
        itemMeta.addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder setUnbreakable() {
        itemMeta.spigot().setUnbreakable(true);
        return this;
    }

    public ItemBuilder applyPlaceholders(TextPlaceholders placeholders) {
        if(itemMeta.hasDisplayName()) {
            withName(placeholders.parse(itemMeta.getDisplayName()));
        }

        if(itemMeta.hasLore()) {
            withLore(itemMeta.getLore().stream().map(placeholders::parse).collect(Collectors.toList()));
        }
        return this;
    }

    public ItemStack build(OfflinePlayer offlinePlayer) {
        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", (offlinePlayer.isOnline() ? "&a" + offlinePlayer.getName() : "&c" + offlinePlayer.getName()));

        if(itemStack.getType() == Material.SKULL && !textured) {
            asSkullOf(offlinePlayer);
        }

        if(itemMeta.hasDisplayName()) {
            withName(PlaceholderAPI.setPlaceholders(offlinePlayer, placeholders.parse(itemMeta.getDisplayName())));
        }

        if(itemMeta.hasLore()) {
            withLore(itemMeta.getLore().stream().map(line -> PlaceholderAPI.setPlaceholders(offlinePlayer, line)).map(placeholders::parse).collect(Collectors.toList()));
        }

        return build();
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemBuilder clone(){
        try {
            ItemBuilder itemBuilder = (ItemBuilder) super.clone();
            itemBuilder.itemStack = itemStack.clone();
            itemBuilder.itemMeta = itemMeta.clone();
            itemBuilder.textured = textured;
            return itemBuilder;
        }catch(Exception ex){
            throw new NullPointerException(ex.getMessage());
        }
    }
}
