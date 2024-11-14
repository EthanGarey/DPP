package com.ethannetwork.dPP.util;

import com.google.gson.Gson;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private final List<ItemFlag> flags = new ArrayList<>();
    private ItemStack item;
    private ItemMeta meta;
    private Material material;
    private int amount = 1;
    private short damage = 0;
    private String displayname;
    private List<String> lore = new ArrayList<>();
    private boolean andSymbol = true;
    private boolean unsafeStackSize = false;

    public ItemBuilder(Material material) {
        this.material = material != null ? material : Material.AIR;
        this.item = new ItemStack(this.material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this(material);
        this.amount = Math.max(1, Math.min(amount, material.getMaxStackSize()));
    }

    public ItemBuilder(Material material, int amount, String displayname) {
        this(material, amount);
        this.displayname = displayname;
    }

    public ItemBuilder(ItemStack item) {
        this.item = item != null ? item : new ItemStack(Material.AIR);
        this.meta = this.item.getItemMeta();
        this.material = item.getType();
        this.amount = item.getAmount();
        this.damage = item.getDurability();
        this.enchantments.putAll(item.getEnchantments());
        if (meta != null) {
            this.displayname = meta.getDisplayName();
            this.lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            this.flags.addAll(meta.getItemFlags());
        }
    }

    public ItemBuilder(FileConfiguration cfg, String path) {
        this(Objects.requireNonNull(cfg.getItemStack(path)));
    }

    // Static utilities for JSON and config serialization
    public static void toConfig(FileConfiguration cfg, String path, ItemBuilder builder) {
        cfg.set(path, builder.build());
    }

    public static String toJson(ItemBuilder builder) {
        return new Gson().toJson(builder);
    }

    public static ItemBuilder fromJson(String json) {
        return new Gson().fromJson(json, ItemBuilder.class);
    }

    // Builder methods
    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(amount, unsafeStackSize ? 64 : material.getMaxStackSize()));
        return this;
    }

    public ItemBuilder durability(short damage) {
        this.damage = damage;
        return this;
    }

    public ItemBuilder material(Material material) {
        this.material = material != null ? material : Material.AIR;
        return this;
    }

    public ItemBuilder enchant(Enchantment enchant, int level) {
        if (enchant != null && level > 0) {
            enchantments.put(enchant, level);
        }
        return this;
    }

    public ItemBuilder displayname(String displayname) {
        this.displayname = andSymbol ? ChatColor.translateAlternateColorCodes('&', displayname) : displayname;
        return this;
    }

    public ItemBuilder lore(String line) {
        if (line != null) {
            lore.add(andSymbol ? ChatColor.translateAlternateColorCodes('&', line) : line);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        this.lore = lore != null ? lore : new ArrayList<>();
        return this;
    }

    public ItemBuilder flag(ItemFlag flag) {
        if (flag != null) {
            flags.add(flag);
        }
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemBuilder glow() {
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
        }
        return this;
    }

    public ItemBuilder unsafeStackSize(boolean allow) {
        this.unsafeStackSize = allow;
        return this;
    }

    public ItemBuilder replaceAndSymbol(boolean replace) {
        this.andSymbol = replace;
        return this;
    }

    public ItemBuilder owner(String user) {
        if (meta instanceof SkullMeta && (material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD)) {
            ((SkullMeta) meta).setOwner(user);
        }
        return this;
    }

    public String getDisplayname() {
        return displayname;
    }

    public int getAmount() {
        return amount;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public List<String> getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }

    public ItemMeta getMeta() {
        return meta;
    }

    public ItemStack build() {
        item.setType(material);
        item.setAmount(amount);
        item.setDurability(damage);

        if (meta != null) {
            if (displayname != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayname));
            }
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList()));
            }
            if (!flags.isEmpty()) {
                flags.forEach(meta::addItemFlags);
            }
            item.setItemMeta(meta);
        }

        item.addUnsafeEnchantments(enchantments);
        return item;
    }

    public ItemBuilder reset() {
        this.item = new ItemStack(Material.AIR);
        this.meta = item.getItemMeta();
        this.material = Material.AIR;
        this.amount = 1;
        this.damage = 0;
        this.enchantments.clear();
        this.displayname = null;
        this.lore.clear();
        this.flags.clear();
        this.andSymbol = true;
        this.unsafeStackSize = false;
        return this;
    }
}
