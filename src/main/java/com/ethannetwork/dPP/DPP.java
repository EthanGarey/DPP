package com.ethannetwork.dPP;

import com.ethannetwork.dPP.commands.MainCommand;
import com.ethannetwork.dPP.commands.subcommands.Kits;
import com.ethannetwork.dPP.util.Config;
import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public final class DPP extends JavaPlugin implements Listener {

    public static DPP instance;
    private final MainCommand mainCommand = new MainCommand();
    public Config mainConfig;
    public Config kitsConfig;
    private BukkitAudiences adventure;

    private static Component translate(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(
                ChatColor.translateAlternateColorCodes('&',
                        MiniMessage.miniMessage().serialize(
                                MiniMessage.miniMessage().deserialize(text))));
    }

    public static Component translateSection(String path) {
        FileConfiguration config = instance.mainConfig.getConfig();
        List<String> configStringList = config.getStringList(path);
        String configString = config.getString(path);

        if (!configStringList.isEmpty()) {
            return configStringList.stream()
                    .filter(Objects::nonNull)
                    .map(DPP::translate)
                    .reduce((comp1, comp2) -> comp1.append(Component.newline()).append(comp2))
                    .orElse(Component.empty());
        } else if (configString != null && !configString.isEmpty()) {
            return translate(configString);
        } else {
            return translate("");
        }
    }

    public BukkitAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return adventure;
    }

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);

        sendConsoleMessage("""
                \s
                &7██████╗ ██████╗ ██╗   ██╗██████╗
                &7██╔══██╗██╔══██╗██║   ██║██╔══██╗
                &7██║  ██║██████╔╝██║   ██║██████╔╝  &c- Made by EthanGarey
                &7██║  ██║██╔═══╝ ╚██╗ ██╔╝██╔═══╝
                &7██████╔╝██║      ╚████╔╝ ██║
                &7╚═════╝ ╚═╝  &c❤    &7╚═══╝  ╚═╝
                \s
                &7&lThank you for using DPVP! Version:\s""" + getDescription().getVersion());

        mainConfig = new Config("config.yml");
        mainConfig.copyDefaults();
        kitsConfig = new Config("kits.yml");
        if (!loadDependencies() || !registerCommands()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        registerListeners();
    }

    private void sendConsoleMessage(String message) {
        adventure.sender(Bukkit.getConsoleSender()).sendMessage(translate(message));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new Kits.KitsListener(), this);
    }

    private boolean loadDependencies() {
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin.");
            return false;
        }
        return true;
    }

    public boolean registerCommands() {
        if (getCommand("dpp") == null) {
            adventure.sender(Bukkit.getConsoleSender()).sendMessage(translateSection("Messages.BasicErrors.CommandCannotBeRegistered").replaceText(builder -> builder.matchLiteral("{0}").replacement("DPP")));
            return false;
        }

        getCommand("dpp").setAliases(mainConfig.getConfig().getStringList("maincommand.aliases"));
        getCommand("dpp").setPermission(mainConfig.getConfig().getString("maincommand.permission"));
        getCommand("dpp").setExecutor(mainCommand);
        getCommand("dpp").setTabCompleter(mainCommand);
        return true;
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }
}
