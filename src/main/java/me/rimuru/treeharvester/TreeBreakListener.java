package me.rimuru.treeharvester;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TreeBreakListener implements Listener {

    private final TreeHarvester plugin;

    public TreeBreakListener(TreeHarvester plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("enabled", true)) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check game mode restrictions
        GameMode playerMode = player.getGameMode();
        if (!isGameModeAllowed(playerMode)) return;

        // Check if block is a log
        if (!MaterialsHelper.isLog(block.getType())) return;

        // Check if player is using an axe
        if (!MaterialsHelper.isAxe(tool.getType())) return;

        // Check if tree is natural
        if (!TreeDetector.isNaturalTree(block)) return;

        // Check WorldGuard/protection plugins if enabled
        if (plugin.getConfig().getBoolean("respect-protection-plugins", true)) {
            if (!canBreakBlock(player, block)) return;
        }

        // Check if player has permission (if required)
        if (plugin.getConfig().getBoolean("require-permission", false)) {
            if (!player.hasPermission("treeharvester.use")) {
                String message = plugin.getConfig().getString("messages.no-permission",
                        "&cYou don't have permission to use TreeHarvester!");
                player.sendMessage(message.replace("&", "§"));
                return;
            }
        }

        // Get all tree blocks (logs only)
        Set<Block> treeBlocks = TreeDetector.getTreeBlocks(block);

        // Calculate durability damage
        int logCount = treeBlocks.size();

        // Check if tool has enough durability (only in survival)
        if (playerMode == GameMode.SURVIVAL && !hasSufficientDurability(tool, logCount)) {
            player.sendMessage("§cYour axe doesn't have enough durability to chop this tree!");
            return;
        }

        // Cancel the original break event
        event.setCancelled(true);

        // Break all logs
        Material logType = block.getType();

        // Get base blocks for replanting
        Set<Block> baseBlocks = getBaseBlocks(treeBlocks);

        // Find all leaves connected to this tree BEFORE breaking logs
        Set<Block> connectedLeaves = findConnectedLeaves(treeBlocks);

        // Break all logs
        for (Block log : treeBlocks) {
            log.breakNaturally(tool);
        }

        // Apply durability damage (only in survival)
        if (playerMode == GameMode.SURVIVAL) {
            damageTool(tool, player, logCount);
        }

        // Schedule instant leaf decay after 2 seconds (40 ticks)
        int decayDelay = plugin.getConfig().getInt("leaf-decay-delay-ticks", 40);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block leaf : connectedLeaves) {
                // Check if the block is still a leaf (in case player broke it manually)
                if (MaterialsHelper.isLeaf(leaf.getType())) {
                    leaf.breakNaturally(); // Drops items naturally
                }
            }
        }, decayDelay);

        // Auto-replant saplings based on base block count
        if (plugin.getConfig().getBoolean("auto-replant", true)) {
            Material sapling = MaterialsHelper.getSaplingForLog(logType);
            if (sapling != null && !baseBlocks.isEmpty()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Block baseBlock : baseBlocks) {
                        Block ground = baseBlock.getLocation().subtract(0, 1, 0).getBlock();
                        if (baseBlock.getType() == Material.AIR && canPlaceSapling(ground)) {
                            baseBlock.setType(sapling);
                        }
                    }
                }, decayDelay + 5L); // Plant after leaves decay
            }
        }
    }


    private Set<Block> findConnectedLeaves(Set<Block> logs) {
        Set<Block> leaves = new HashSet<>();

        // Calculate tree boundaries
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block log : logs) {
            minX = Math.min(minX, log.getX());
            maxX = Math.max(maxX, log.getX());
            minY = Math.min(minY, log.getY());
            maxY = Math.max(maxY, log.getY());
            minZ = Math.min(minZ, log.getZ());
            maxZ = Math.max(maxZ, log.getZ());
        }


        int searchRadius = 3; // 3 blocks in each direction

        for (int x = minX - searchRadius; x <= maxX + searchRadius; x++) {
            for (int y = minY; y <= maxY + searchRadius; y++) {
                for (int z = minZ - searchRadius; z <= maxZ + searchRadius; z++) {
                    Block block = logs.iterator().next().getWorld().getBlockAt(x, y, z);

                    if (MaterialsHelper.isLeaf(block.getType())) {
                        // Check if this leaf is reasonably close to any log
                        if (isLeafConnectedToTree(block, logs, searchRadius)) {
                            leaves.add(block);
                        }
                    }
                }
            }
        }

        return leaves;
    }

    /**
     * Check if a leaf block is connected to this tree
     * A leaf is connected if it's within reasonable distance of any log
     */
    private boolean isLeafConnectedToTree(Block leaf, Set<Block> logs, int maxDistance) {
        for (Block log : logs) {
            double distance = leaf.getLocation().distance(log.getLocation());
            if (distance <= maxDistance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the current game mode is allowed based on config
     */
    private boolean isGameModeAllowed(GameMode mode) {
        List<String> allowedModes = plugin.getConfig().getStringList("allowed-game-modes");

        // If list is empty or contains "all", allow all modes
        if (allowedModes.isEmpty() || allowedModes.contains("all")) {
            return true;
        }

        String modeName = mode.name().toLowerCase();
        return allowedModes.contains(modeName);
    }

    /**
     * Get all base blocks (lowest Y level logs) for replanting
     * For 2x2 trees, this returns up to 4 blocks
     */
    private Set<Block> getBaseBlocks(Set<Block> treeBlocks) {
        if (treeBlocks.isEmpty()) return new HashSet<>();

        // Find the lowest Y level
        int lowestY = Integer.MAX_VALUE;
        for (Block log : treeBlocks) {
            if (log.getY() < lowestY) {
                lowestY = log.getY();
            }
        }

        // Get all blocks at the lowest Y level
        Set<Block> baseBlocks = new HashSet<>();
        for (Block log : treeBlocks) {
            if (log.getY() == lowestY) {
                baseBlocks.add(log);
            }
        }

        return baseBlocks;
    }

    private boolean hasSufficientDurability(ItemStack tool, int logsCount) {
        if (tool.getType().getMaxDurability() == 0) return true; // Unbreakable

        ItemMeta meta = tool.getItemMeta();
        if (meta != null && meta.isUnbreakable()) return true;

        int currentDurability = tool.getType().getMaxDurability() - getDamage(tool);
        return currentDurability >= logsCount;
    }

    private int getDamage(ItemStack item) {
        try {
            // 1.13+ method
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                return ((org.bukkit.inventory.meta.Damageable) meta).getDamage();
            }
        } catch (Exception e) {
            // Fallback for older versions
            return item.getDurability();
        }
        return 0;
    }

    private void damageTool(ItemStack tool, Player player, int amount) {
        if (tool.getType().getMaxDurability() == 0) return;

        ItemMeta meta = tool.getItemMeta();
        if (meta != null && meta.isUnbreakable()) return;

        // Get Unbreaking level
        int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);

        // Apply damage for each log with Unbreaking chance
        int actualDamage = 0;
        for (int i = 0; i < amount; i++) {
            if (shouldDamageWithUnbreaking(unbreakingLevel)) {
                actualDamage++;
            }
        }

        // If no damage should be applied, return early
        if (actualDamage == 0) return;

        try {
            // 1.13+ method
            if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                org.bukkit.inventory.meta.Damageable damageable = (org.bukkit.inventory.meta.Damageable) meta;
                int newDamage = damageable.getDamage() + actualDamage;

                if (newDamage >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    damageable.setDamage(newDamage);
                    tool.setItemMeta(meta);
                }
            }
        } catch (Exception e) {
            // Fallback
            short newDurability = (short) (tool.getDurability() + actualDamage);
            if (newDurability >= tool.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(null);
            } else {
                tool.setDurability(newDurability);
            }
        }
    }

    /**
     * Determines if the tool should take damage based on Unbreaking enchantment
     * Unbreaking formula: (level + 1) chance to not take damage
     * For example, Unbreaking III = 75% chance to not take damage (1 in 4 chance to damage)
     */
    private boolean shouldDamageWithUnbreaking(int unbreakingLevel) {
        if (unbreakingLevel <= 0) {
            return true; // No unbreaking, always damage
        }

        // Unbreaking chance: 1 / (level + 1) chance to take damage
        // Unbreaking I: 50% (1/2), Unbreaking II: 33% (1/3), Unbreaking III: 25% (1/4)
        int chance = unbreakingLevel + 1;
        return Math.random() < (1.0 / chance);
    }

    private boolean canPlaceSapling(Block ground) {
        Material type = ground.getType();
        return type == Material.GRASS_BLOCK ||
                type == Material.DIRT ||
                type == Material.PODZOL ||
                type == Material.COARSE_DIRT ||
                type == Material.MYCELIUM ||
                type.name().equals("ROOTED_DIRT") ||
                type.name().equals("MUD") ||
                type.name().equals("MOSS_BLOCK");
    }

    private boolean canBreakBlock(Player player, Block block) {
        return player.hasPermission("treeharvester.use");
    }
}