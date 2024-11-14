package com.ethannetwork.dPP.commands;

import com.ethannetwork.dPP.DPP;
import com.ethannetwork.dPP.commands.subcommands.Kits;
import com.ethannetwork.dPP.commands.subcommands.Reload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final List<SubCommand> subcommands = new ArrayList<>();

    public MainCommand() {
        subcommands.add(new Reload());
        subcommands.add(new Kits());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length < 1) {
            DPP.instance.adventure().sender(sender).sendMessage(DPP.translateSection("Messages.Usage"));
            return true;
        }

        SubCommand subCommand = findSubCommand(args[0]);
        if (subCommand == null) {
            DPP.instance.adventure().sender(sender).sendMessage(DPP.translateSection("Messages.InvalidSubCommand"));
            return true;
        }

        String permission = DPP.instance.mainConfig.getConfig().getString("subcommands." + subCommand.getName().getFirst() + ".permission");
        if (permission == null || !sender.hasPermission(permission)) {
            DPP.instance.adventure().sender(sender).sendMessage(DPP.translateSection("subcommands." + subCommand.getName().getFirst() + ".permission-message"));
            return true;
        }

        subCommand.perform(sender, args);
        return true;
    }

    private SubCommand findSubCommand(String name) {
        return subcommands.stream()
                .filter(cmd -> cmd.getName().contains(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> subCommandNames = new ArrayList<>();
            subcommands.forEach(subCommand -> subCommandNames.addAll(subCommand.getName()));
            return subCommandNames;
        } else if (args.length >= 2) {
            SubCommand subCommand = findSubCommand(args[0]);
            if (subCommand != null) {
                return subCommand.performTab(sender, args);
            }
        }
        return List.of();
    }

    public List<SubCommand> getSubcommands() {
        return subcommands;
    }
}
