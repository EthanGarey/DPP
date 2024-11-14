package com.ethannetwork.dPP.util;

import com.ethannetwork.dPP.DPP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {
    private final String name;
    private final File configFile;
    private FileConfiguration config;

    public Config(String name) {
        this.name = name;
        this.configFile = new File(DPP.instance.getDataFolder(), name);
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void copyDefaults() {

        try (InputStream stream = DPP.instance.getResource(name)) {
            if (stream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
            }
        } catch (IOException e) {
            DPP.instance.getLogger().severe("Failed to load default configuration for " + name + ": " + e.getMessage());
        }

        config.options().copyDefaults(true);
        saveConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            DPP.instance.saveResource(name, false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            DPP.instance.getLogger().severe("Could not save config to " + configFile.getName() + ": " + e.getMessage());
        }
    }
}
