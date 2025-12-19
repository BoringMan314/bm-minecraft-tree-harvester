package me.rimuru.treeharvester;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class TreeDetector {

    private static final int MAX_TREE_BLOCKS = 1000;

    /**
     * Checks if a tree is naturally generated
     */
    public static boolean isNaturalTree(Block startLog) {
        DatabaseManager db = TreeHarvester.getInstance().getDatabaseManager();

        // First check: if the starting block is marked as player-placed, reject immediately
        if (db.isBlockPlayerPlaced(startLog)) {
            return false;
        }

        Set<Block> logs = getTreeBlocks(startLog);

        // Check if tree size is reasonable for natural generation
        if (logs.isEmpty() || logs.size() > MAX_TREE_BLOCKS) {
            return false;
        }

        // Check if ANY log in the tree is marked as player-placed
        for (Block log : logs) {
            if (db.isBlockPlayerPlaced(log)) {
                return false;
            }
        }

        // Check if tree is connected to ground
        Block lowestLog = getLowestBlock(logs);
        if (lowestLog == null) return false;

        Block ground = lowestLog.getLocation().subtract(0, 1, 0).getBlock();
        if (!isValidGroundBlock(ground.getType())) {
            return false;
        }

        // Check for structure blocks nearby (villages, mansions, etc.)
        if (hasStructureBlocksNearby(logs)) {
            return false;
        }

        // Check tree structure consistency
        return hasConsistentStructure(logs);
    }

    /**
     * Gets all connected log blocks of a tree
     * Supports 2x2 thick trees (mega spruce, jungle, dark oak) by checking horizontally connected logs
     */
    public static Set<Block> getTreeBlocks(Block startBlock) {
        Set<Block> treeBlocks = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock);

        Material logType = startBlock.getType();

        while (!queue.isEmpty() && treeBlocks.size() < MAX_TREE_BLOCKS) {
            Block current = queue.poll();

            if (!MaterialsHelper.isLog(current.getType())) continue;
            if (current.getType() != logType) continue; // Same tree type only

            treeBlocks.add(current);

            // Check all 26 adjacent blocks (including diagonals) for connected logs
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block adjacent = current.getRelative(x, y, z);

                        if (!visited.contains(adjacent)) {
                            visited.add(adjacent);
                            if (MaterialsHelper.isLog(adjacent.getType()) && adjacent.getType() == logType) {
                                queue.add(adjacent);
                            }
                        }
                    }
                }
            }
        }

        return treeBlocks;
    }

    private static boolean isValidGroundBlock(Material type) {
        return type == Material.GRASS_BLOCK ||
                type == Material.DIRT ||
                type == Material.PODZOL ||
                type == Material.COARSE_DIRT ||
                type == Material.STONE ||
                type == Material.NETHERRACK ||
                type == Material.SOUL_SAND ||
                type == Material.MYCELIUM ||
                type.name().equals("SOUL_SOIL") ||
                type.name().equals("ROOTED_DIRT") ||
                type.name().equals("MUD") ||
                type.name().equals("MUDDY_MANGROVE_ROOTS") ||
                type.name().equals("MOSS_BLOCK");
    }

    private static boolean hasConsistentStructure(Set<Block> logs) {
        if (logs.isEmpty()) return false;

        // Check if logs form a reasonable tree structure
        Block lowest = getLowestBlock(logs);
        Block highest = getHighestBlock(logs);

        if (lowest == null || highest == null) return false;

        int height = highest.getY() - lowest.getY() + 1;

        // Natural trees shouldn't be too short or too tall
        if (height < 3 || height > 50) return false;

        // Check horizontal spread isn't too large (prevents structures)
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Block log : logs) {
            minX = Math.min(minX, log.getX());
            maxX = Math.max(maxX, log.getX());
            minZ = Math.min(minZ, log.getZ());
            maxZ = Math.max(maxZ, log.getZ());
        }

        int spreadX = maxX - minX + 1;
        int spreadZ = maxZ - minZ + 1;

        // Check if it's a 2x2 thick tree pattern
        boolean is2x2Tree = is2x2ThickTree(logs, minX, minZ, maxX, maxZ);

        if (is2x2Tree) {
            // For 2x2 thick trees, allow up to 2 blocks spread horizontally
            return spreadX <= 2 && spreadZ <= 2;
        } else {
            // For regular trees, allow limited spread
            if (spreadX > 6 || spreadZ > 6) {
                int area = spreadX * spreadZ;
                double density = (double) logs.size() / area;

                // Natural trees have high density, structures have low density
                if (density < 0.3) {
                    return false;
                }
            }

            return spreadX <= 12 && spreadZ <= 12;
        }
    }


    private static boolean is2x2ThickTree(Set<Block> logs, int minX, int minZ, int maxX, int maxZ) {
        if ((maxX - minX + 1) != 2 || (maxZ - minZ + 1) != 2) {
            return false;
        }

        Map<Integer, Set<String>> logsByHeight = new HashMap<>();

        for (Block log : logs) {
            int y = log.getY();
            logsByHeight.putIfAbsent(y, new HashSet<>());
            logsByHeight.get(y).add(log.getX() + "," + log.getZ());
        }

        int levelsWithFourLogs = 0;
        for (Set<String> logsAtHeight : logsByHeight.values()) {
            if (logsAtHeight.size() == 4) {
                levelsWithFourLogs++;
            }
        }

        return levelsWithFourLogs >= 3;
    }

    /**
     * Checks if there are structure blocks nearby (planks, stone, etc.)
     */
    private static boolean hasStructureBlocksNearby(Set<Block> logs) {
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

        int structureBlockCount = 0;
        int totalBlocksChecked = 0;

        for (int x = minX - 3; x <= maxX + 3; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ - 3; z <= maxZ + 3; z++) {
                    Block block = logs.iterator().next().getWorld().getBlockAt(x, y, z);
                    totalBlocksChecked++;

                    if (isStructureBlock(block.getType())) {
                        structureBlockCount++;
                    }
                }
            }
        }

        double structureRatio = (double) structureBlockCount / totalBlocksChecked;
        return structureRatio > 0.10;
    }

    private static boolean isStructureBlock(Material type) {
        String name = type.name();

        return type == Material.COBBLESTONE ||
                type == Material.STONE_BRICKS ||
                type == Material.BRICKS ||
                type == Material.STONE ||
                type == Material.SMOOTH_STONE ||
                name.contains("PLANKS") ||
                name.contains("FENCE") ||
                name.contains("STAIRS") ||
                name.contains("SLAB") ||
                name.contains("DOOR") ||
                name.contains("GLASS") ||
                name.contains("WOOL") ||
                name.contains("CARPET") ||
                name.contains("CONCRETE") ||
                name.contains("TERRACOTTA") ||
                type == Material.TORCH ||
                type == Material.COBWEB ||
                type == Material.BOOKSHELF ||
                type == Material.CHEST ||
                name.equals("WALL_TORCH") ||
                name.equals("LANTERN") ||
                name.equals("IRON_BARS") ||
                name.equals("CHAIN");
    }

    private static Block getLowestBlock(Set<Block> blocks) {
        Block lowest = null;
        for (Block block : blocks) {
            if (lowest == null || block.getY() < lowest.getY()) {
                lowest = block;
            }
        }
        return lowest;
    }

    private static Block getHighestBlock(Set<Block> blocks) {
        Block highest = null;
        for (Block block : blocks) {
            if (highest == null || block.getY() > highest.getY()) {
                highest = block;
            }
        }
        return highest;
    }
}