package me.rimuru.treeharvester;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TreeHarvesterCommand implements CommandExecutor, TabCompleter {

    private final TreeHarvester plugin;

    public TreeHarvesterCommand(TreeHarvester plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            send(sender, "command.help-header");
            send(sender, "command.help-reload");
            send(sender, "command.help-info");
            send(sender, "command.help-stats");
            send(sender, "command.help-optimize");
            send(sender, "command.help-footer");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("treeharvester.admin")) {
                    send(sender, "command.no-permission");
                    return true;
                }
                plugin.reloadPluginConfig();
                send(sender, "command.reloaded");
                return true;

            case "info":
                send(sender, "command.help-header");
                send(sender, "command.info-version");
                send(sender, "command.info-supports");
                send(sender, "command.info-author");
                sender.sendMessage("");
                send(sender, "command.info-features");
                send(sender, "command.info-natural-detection");
                send(sender, "command.info-auto-replant");
                send(sender, "command.info-silk-touch");
                send(sender, "command.info-durability");
                send(sender, "command.info-database");
                send(sender, "command.info-big-tree");
                send(sender, "command.info-mushroom");
                send(sender, "command.info-all-trees");
                send(sender, "command.help-footer");
                return true;

            case "stats":
                if (!sender.hasPermission("treeharvester.admin")) {
                    send(sender, "command.no-permission");
                    return true;
                }
                int totalBlocks = plugin.getDatabaseManager().getTotalTrackedBlocks();
                send(sender, "command.stats-header");
                send(sender, "command.stats-total", "{count}", String.valueOf(totalBlocks));
                send(sender, "command.stats-database");
                send(sender, "command.stats-footer");
                return true;

            case "optimize":
                if (!sender.hasPermission("treeharvester.admin")) {
                    send(sender, "command.no-permission");
                    return true;
                }
                send(sender, "command.optimizing");
                plugin.getDatabaseManager().optimizeDatabase().thenRun(() -> {
                    send(sender, "command.optimized");
                });
                return true;

            default:
                send(sender, "command.unknown");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("info");
            if (sender.hasPermission("treeharvester.admin")) {
                completions.add("stats");
                completions.add("optimize");
            }
        }

        return completions;
    }

    private void send(CommandSender sender, String path) {
        sender.sendMessage(plugin.getLanguageManager().get(path));
    }

    private void send(CommandSender sender, String path, String placeholder, String value) {
        sender.sendMessage(plugin.getLanguageManager().get(path).replace(placeholder, value));
    }
}
