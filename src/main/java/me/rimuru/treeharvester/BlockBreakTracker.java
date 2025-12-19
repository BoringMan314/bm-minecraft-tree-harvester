package me.rimuru.treeharvester;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


public class BlockBreakTracker implements Listener {

    private final TreeHarvester plugin;

    public BlockBreakTracker(TreeHarvester plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // Remove logs and leaves from database when broken (async)
        if (MaterialsHelper.isLog(event.getBlock().getType()) ||
                MaterialsHelper.isLeaf(event.getBlock().getType())) {
            plugin.getDatabaseManager().removeBlock(event.getBlock());
        }
    }
}