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
            sender.sendMessage("§6§l════════ TreeHarvester ════════");
            sender.sendMessage("§e/treeharvester reload §7- Reload configuration");
            sender.sendMessage("§e/treeharvester info §7- Show plugin info");
            sender.sendMessage("§e/treeharvester stats §7- Show database statistics");
            sender.sendMessage("§e/treeharvester optimize §7- Optimize database");
            sender.sendMessage("§6§l═══════════════════════════════");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("treeharvester.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                plugin.reloadPluginConfig();
                sender.sendMessage("§aTreeHarvester configuration reloaded!");
                return true;

            case "info":
                sender.sendMessage("§6§l════════ TreeHarvester ════════");
                sender.sendMessage("§7Version: §e1.0");
                sender.sendMessage("§7Supports: §e1.13-1.21+");
                sender.sendMessage("§7Author: §eRimuru");
                sender.sendMessage("");
                sender.sendMessage("§6Features:");
                sender.sendMessage("§e✓ §7Natural tree detection");
                sender.sendMessage("§e✓ §7Auto replanting");
                sender.sendMessage("§e✓ §7Silk touch support");
                sender.sendMessage("§e✓ §7Durability management");
                sender.sendMessage("§e✓ §7SQLite database tracking");
                sender.sendMessage("§e✓ §7Big tree support (2x2 thick)");
                sender.sendMessage("§e✓ §7Mushroom tree support");
                sender.sendMessage("§e✓ §7All tree types (Oak → Pale Oak)");
                sender.sendMessage("§6§l═══════════════════════════════");
                return true;

            case "stats":
                if (!sender.hasPermission("treeharvester.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                int totalBlocks = plugin.getDatabaseManager().getTotalTrackedBlocks();
                sender.sendMessage("§6§l═══ Database Statistics ═══");
                sender.sendMessage("§7Total tracked blocks: §e" + totalBlocks);
                sender.sendMessage("§7Database: §eplayer_blocks.db");
                sender.sendMessage("§6§l═════════════════════════");
                return true;

            case "optimize":
                if (!sender.hasPermission("treeharvester.admin")) {
                    sender.sendMessage("§cYou don't have permission to use this command!");
                    return true;
                }
                sender.sendMessage("§7Optimizing database...");
                plugin.getDatabaseManager().optimizeDatabase().thenRun(() -> {
                    sender.sendMessage("§aDatabase optimization completed!");
                });
                return true;

            default:
                sender.sendMessage("§cUnknown command. Use /treeharvester for help.");
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
}