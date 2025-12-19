package me.rimuru.treeharvester;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final TreeHarvester plugin;

    public BlockPlaceListener(TreeHarvester plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Mark logs and leaves as player-placed in database (async)
        if (MaterialsHelper.isLog(event.getBlock().getType()) ||
                MaterialsHelper.isLeaf(event.getBlock().getType())) {
            plugin.getDatabaseManager().markBlockAsPlayerPlaced(event.getBlock());
        }
    }
}