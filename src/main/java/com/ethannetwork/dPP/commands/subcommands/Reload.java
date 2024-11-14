package com.ethannetwork.dPP.commands.subcommands;

import com.ethannetwork.dPP.DPP;
import com.ethannetwork.dPP.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class Reload extends SubCommand {

    public static void doReload() {
        DPP.instance.mainConfig.loadConfig();
        DPP.instance.mainConfig.copyDefaults();
        DPP.instance.kitsConfig.loadConfig();
        DPP.instance.registerCommands();
    }

    @Override
    public List<String> getName() {
        return List.of("reload");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        doReload();
        sendReloadMessage(sender);
    }

    private void sendReloadMessage(CommandSender sender) {
        DPP.instance.adventure().sender(sender).sendMessage(DPP.translateSection("Messages.ReloadMessage"));
        if (!(sender instanceof ConsoleCommandSender)) {
            DPP.instance.adventure().sender(Bukkit.getConsoleSender()).sendMessage(DPP.translateSection("Messages.ReloadMessage"));
        }
    }

    @Override
    public List<String> performTab(CommandSender sender, String[] args) {
        return List.of();
    }
}
