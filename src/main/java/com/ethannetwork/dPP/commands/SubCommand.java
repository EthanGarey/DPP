package com.ethannetwork.dPP.commands;


import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {

    public abstract List<String> getName();

    public abstract void perform(CommandSender sender, String[] args);

    public abstract List<String> performTab(CommandSender sender, String[] args);

}

