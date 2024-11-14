package com.ethannetwork.dPP.commands.subcommands;

import com.ethannetwork.dPP.DPP;
import com.ethannetwork.dPP.commands.SubCommand;
import com.ethannetwork.dPP.util.InvUtils;
import com.ethannetwork.dPP.util.ItemBuilder;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Kits extends SubCommand {

    private static final String KIT_MANAGER_TITLE = ChatColor.translateAlternateColorCodes('&', "&b&l----- &3&lKits Manager&b&l -----");
    private static final String KIT_SETTINGS_TITLE = ChatColor.translateAlternateColorCodes('&', "&b&l----- &3&lKit Settings&b&l -----");
    private static final String VIEW_ITEMS_TITLE = ChatColor.translateAlternateColorCodes('&', "&b&l----- &3&lView Kit Items&b&l -----");
    private static final String KIT_NBT_TAG = "kitName";
    private static final String CREATE_KIT_TAG = "createKit";
    private static final int ITEMS_PER_PAGE = 28;
    private static final ConcurrentHashMap<UUID, Integer> playerPages = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> creatingKits = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, String> selectedKit = new ConcurrentHashMap<>();

    public static void openKitsGUI(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
        Inventory inventory = Bukkit.createInventory(null, 54, KIT_MANAGER_TITLE);
        inventory.setContents(InvUtils.SIZE_54_INV_SURROUNDINGS);

        List<String> kits = new ArrayList<>();
        var kitsSection = DPP.instance.kitsConfig.getConfig().getConfigurationSection("kits");
        if (kitsSection != null) {
            kits.addAll(kitsSection.getKeys(false));
        }

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, kits.size());

        for (int i = start; i < end; i++) {
            String kitName = kits.get(i);
            ItemStack kitItem = new ItemBuilder(Material.CHEST, 1, ChatColor.GOLD + kitName).build();
            NBT.modify(kitItem, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, kitName));
            inventory.addItem(kitItem);
        }

        // "Create Kit" button at slot 49
        ItemStack createKitItem = new ItemBuilder(Material.NETHER_STAR, 1, ChatColor.YELLOW + "Create Kit").build();
        NBT.modify(createKitItem, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, CREATE_KIT_TAG));
        inventory.setItem(49, createKitItem);

        // "Next Page" button at slot 53 if there are more kits
        if (end < kits.size()) {
            ItemStack nextPageItem = new ItemBuilder(Material.ARROW, 1, ChatColor.GREEN + "Next Page").build();
            NBT.modify(nextPageItem, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, "nextPage"));
            inventory.setItem(53, nextPageItem);
        }

        // "Previous Page" button at slot 45 if it's not the first page
        if (page > 0) {
            ItemStack prevPageItem = new ItemBuilder(Material.ARROW, 1, ChatColor.GREEN + "Previous Page").build();
            NBT.modify(prevPageItem, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, "prevPage"));
            inventory.setItem(45, prevPageItem);
        }

        player.openInventory(inventory);
    }

    public static void openKitSettings(Player player, String kitName) {
        Inventory settingsMenu = Bukkit.createInventory(null, 27, KIT_SETTINGS_TITLE);
        selectedKit.put(player.getUniqueId(), kitName);

        // "View Kit Items" option
        ItemStack viewItems = new ItemBuilder(Material.DIAMOND, 1, ChatColor.AQUA + "View Kit Items").build();
        NBT.modify(viewItems, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, "viewItems"));
        settingsMenu.setItem(11, viewItems); // Placed in the settings menu

        // "Back to Kit Manager" option
        ItemStack backToManager = new ItemBuilder(Material.ARROW, 1, ChatColor.GREEN + "Back to Kit Manager").build();
        NBT.modify(backToManager, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString(KIT_NBT_TAG, "backToManager"));
        settingsMenu.setItem(15, backToManager);

        player.openInventory(settingsMenu);
    }

    public static void openViewKitItems(Player player, String kitName) {
        Inventory viewItemsMenu = Bukkit.createInventory(null, 54, VIEW_ITEMS_TITLE);

        // Load items from the kit
        var kitSection = DPP.instance.kitsConfig.getConfig().getConfigurationSection("kits." + kitName);
        if (kitSection != null) {
            for (String key : kitSection.getKeys(false)) {
                if (key.startsWith("slot")) {
                    int slot = Integer.parseInt(key.substring(4));
                    ItemStack item = DPP.instance.kitsConfig.getConfig().getItemStack("kits." + kitName + "." + key);
                    if (item != null) viewItemsMenu.setItem(slot, item);
                }
            }
        }

        player.openInventory(viewItemsMenu);
    }

    @Override
    public List<String> getName() {
        return List.of("kits", "kit");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            openKitsGUI(player, 0);
        } else {
            DPP.instance.adventure().sender(sender).sendMessage(DPP.translateSection("Messages.MustBePlayer"));
        }
    }

    @Override
    public List<String> performTab(CommandSender sender, String[] args) {
        return List.of();
    }

    public static class KitsListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String title = event.getView().getTitle();
            if (title.equals(KIT_MANAGER_TITLE)) {
                event.setCancelled(true);
                NBT.get(clickedItem, nbt -> {
                    String kitIdentifier = nbt.getString(KIT_NBT_TAG);
                    if ("nextPage".equals(kitIdentifier)) {
                        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                        player.closeInventory();
                        openKitsGUI(player, currentPage + 1);
                    } else if ("prevPage".equals(kitIdentifier)) {
                        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
                        player.closeInventory();
                        openKitsGUI(player, currentPage - 1);
                    } else if (CREATE_KIT_TAG.equals(kitIdentifier)) {
                        player.closeInventory();
                        startKitCreationProcess(player);
                    } else if (kitIdentifier != null && !kitIdentifier.isEmpty()) {
                        player.closeInventory();
                        openKitSettings(player, kitIdentifier);
                    }
                });
            } else if (title.equals(KIT_SETTINGS_TITLE)) {
                event.setCancelled(true);
                NBT.get(clickedItem, nbt -> {
                    String action = nbt.getString(KIT_NBT_TAG);
                    if ("viewItems".equals(action)) {
                        openViewKitItems(player, selectedKit.get(player.getUniqueId()));
                    } else if ("backToManager".equals(action)) {
                        openKitsGUI(player, playerPages.getOrDefault(player.getUniqueId(), 0));
                    }
                });
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event) {
            try {
                Player player = (Player) event.getPlayer();
                String title = event.getView().getTitle();

                if (title.equals(VIEW_ITEMS_TITLE)) {
                    String kitName = selectedKit.get(player.getUniqueId());
                    Inventory inventory = event.getInventory();

                    // Save the items
                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        ItemStack item = inventory.getItem(slot);
                        DPP.instance.kitsConfig.getConfig().set("kits." + kitName + ".slot" + slot, item);
                    }
                    DPP.instance.kitsConfig.saveConfig();

                    player.closeInventory();
                    // Reopen the Kit Settings GUI
                    openKitSettings(player, kitName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                DPP.instance.getLogger().severe("Error in onInventoryClose: " + e.getMessage());
            }
        }

        private void startKitCreationProcess(Player player) {
            UUID playerId = player.getUniqueId();
            creatingKits.put(playerId, true);
            player.closeInventory();
            DPP.instance.adventure().player(player).showTitle(
                    net.kyori.adventure.title.Title.title(
                            Component.text("Enter Kit Name"),
                            Component.text("Type 'cancel' to exit")
                    )
            );
            DPP.instance.getLogger().info("[DEBUG] Kit creation mode activated for player: " + player.getName());
        }

        @EventHandler
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();

            if (creatingKits.containsKey(playerId)) {
                event.setCancelled(true);
                String input = event.getMessage().trim();
                DPP.instance.getLogger().info("[DEBUG] Received chat input from " + player.getName() + ": " + input);

                if ("cancel".equalsIgnoreCase(input)) {
                    creatingKits.remove(playerId);
                    DPP.instance.adventure().player(player).sendMessage(Component.text("Kit creation cancelled."));
                    DPP.instance.getLogger().info("[DEBUG] Kit creation cancelled for player: " + player.getName());
                } else {
                    saveKitToFile(player, input);
                }
            }
        }

        private void saveKitToFile(Player player, String kitName) {
            String kitPath = "kits." + kitName;

            // Confirm kit does not already exist
            if (DPP.instance.kitsConfig.getConfig().contains(kitPath)) {
                DPP.instance.adventure().player(player).sendMessage(
                        Component.text("A kit with that name already exists! Choose a different name.")
                                .color(net.kyori.adventure.text.format.NamedTextColor.RED)
                );
                DPP.instance.getLogger().warning("[ERROR] Kit creation failed: Kit name '" + kitName + "' already exists for player " + player.getName());
                return;
            }

            // Attempt to save the kit items
            try {
                for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                    ItemStack item = player.getInventory().getItem(slot);
                    if (item != null) {
                        DPP.instance.kitsConfig.getConfig().set(kitPath + ".slot" + slot, item);
                    }
                }

                DPP.instance.kitsConfig.saveConfig();
                DPP.instance.adventure().player(player).sendMessage(
                        Component.text("Kit '" + kitName + "' created successfully!")
                                .color(net.kyori.adventure.text.format.NamedTextColor.GREEN)
                );
                DPP.instance.getLogger().info("[DEBUG] Kit '" + kitName + "' saved successfully for player " + player.getName());
                creatingKits.remove(player.getUniqueId());

            } catch (Exception e) {
                DPP.instance.getLogger().severe("[ERROR] Failed to save kit '" + kitName + "' for player " + player.getName() + ": " + e.getMessage());
            }
        }

    }
}
