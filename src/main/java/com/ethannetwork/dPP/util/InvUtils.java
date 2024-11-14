package com.ethannetwork.dPP.util;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class InvUtils {

    public static final ItemStack[] SIZE_54_INV_SURROUNDINGS;
    private static final int INVENTORY_SIZE = 54;
    private static final int ROW_LENGTH = 9;
    private static final ChatColor BORDER_COLOR = ChatColor.DARK_GRAY;

    static {
        SIZE_54_INV_SURROUNDINGS = createSize54InvSurroundings();
    }

    private InvUtils() {
    }

    private static ItemStack[] createSize54InvSurroundings() {
        ItemStack borderStack = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1, BORDER_COLOR + "").build();
        try {
            NBT.modify(borderStack, (Consumer<ReadWriteItemNBT>) readWriteItemNBT -> readWriteItemNBT.setString("custom;dpp", "side"));
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to apply NBT tag to border item: " + e.getMessage());
        }

        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE, "");
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (isBorderSlot(i)) inv.setItem(i, borderStack);
        }

        return inv.getContents();
    }

    private static boolean isBorderSlot(int slotIndex) {
        return (slotIndex < ROW_LENGTH) ||
                (slotIndex >= INVENTORY_SIZE - ROW_LENGTH) ||
                (slotIndex % ROW_LENGTH == 0) ||
                (slotIndex % ROW_LENGTH == ROW_LENGTH - 1);
    }
}
