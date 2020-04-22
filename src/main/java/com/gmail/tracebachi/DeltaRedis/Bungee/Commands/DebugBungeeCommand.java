package com.gmail.tracebachi.DeltaRedis.Bungee.Commands;

import com.gmail.tracebachi.DeltaRedis.Bungee.DeltaRedis;
import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class DebugBungeeCommand extends Command implements Registerable, Shutdownable {
    private DeltaRedis plugin;

    public DebugBungeeCommand(DeltaRedis deltaRedis) {
        super("deltaredisbungeedebug");

        this.plugin = deltaRedis;
    }

    @Override
    public void register() {
        plugin.getProxy().getPluginManager().registerCommand(plugin, this);
    }

    @Override
    public void unregister() {
        plugin.getProxy().getPluginManager().unregisterCommand(this);
    }

    @Override
    public void shutdown() {
        unregister();
        plugin = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("DeltaRedis.Debug")) {
            sender.sendMessage("You do not have permission to run this command.");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage("/deltaredisbungeedebug <on|off>");
            return;
        }

        if (args[0].equalsIgnoreCase("on")) {
            plugin.setDebugEnabled(true);
        } else if (args[0].equalsIgnoreCase("off")) {
            plugin.setDebugEnabled(true);
        } else {
            sender.sendMessage("/deltaredisbungeedebug <on|off>");
        }
    }
}