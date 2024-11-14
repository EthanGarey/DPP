package com.ethannetwork.dPP.commands.subcommands;

import com.ethannetwork.dPP.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Help extends SubCommand {
    @Override
    public List<String> getName() {
        return List.of("help");
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        
    }

    @Override
    public List<String> performTab(CommandSender sender, String[] args) {
        return List.of();
    }
}
