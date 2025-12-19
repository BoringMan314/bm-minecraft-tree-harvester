package me.rimuru.treeharvester;

import org.bukkit.Material;

import java.util.*;

public class MaterialsHelper {

    private static final Set<Material> LOG_TYPES = new HashSet<>();
    private static final Set<Material> LEAF_TYPES = new HashSet<>();
    private static final Map<Material, Material> LOG_TO_SAPLING = new HashMap<>();

    static {
        initializeLogs();
        initializeLeaves();
        initializeSaplingMapping();
    }

    private static void initializeLogs() {
        // Standard logs (1.13+)
        LOG_TYPES.add(Material.OAK_LOG);
        LOG_TYPES.add(Material.BIRCH_LOG);
        LOG_TYPES.add(Material.SPRUCE_LOG);
        LOG_TYPES.add(Material.JUNGLE_LOG);
        LOG_TYPES.add(Material.ACACIA_LOG);
        LOG_TYPES.add(Material.DARK_OAK_LOG);

        // 1.16+ logs
        tryAddMaterial(LOG_TYPES, "CRIMSON_STEM");
        tryAddMaterial(LOG_TYPES, "WARPED_STEM");

        // 1.19+ logs
        tryAddMaterial(LOG_TYPES, "MANGROVE_LOG");

        // 1.20+ logs
        tryAddMaterial(LOG_TYPES, "CHERRY_LOG");

        // 1.21+ logs
        tryAddMaterial(LOG_TYPES, "PALE_OAK_LOG");
    }

    private static void initializeLeaves() {
        // Standard leaves (1.13+)
        LEAF_TYPES.add(Material.OAK_LEAVES);
        LEAF_TYPES.add(Material.BIRCH_LEAVES);
        LEAF_TYPES.add(Material.SPRUCE_LEAVES);
        LEAF_TYPES.add(Material.JUNGLE_LEAVES);
        LEAF_TYPES.add(Material.ACACIA_LEAVES);
        LEAF_TYPES.add(Material.DARK_OAK_LEAVES);

        // 1.16+ leaves (nether "leaves")
        tryAddMaterial(LEAF_TYPES, "NETHER_WART_BLOCK");
        tryAddMaterial(LEAF_TYPES, "WARPED_WART_BLOCK");

        // 1.19+ leaves
        tryAddMaterial(LEAF_TYPES, "MANGROVE_LEAVES");

        // 1.20+ leaves
        tryAddMaterial(LEAF_TYPES, "CHERRY_LEAVES");

        // 1.21+ leaves
        tryAddMaterial(LEAF_TYPES, "PALE_OAK_LEAVES");
    }

    private static void initializeSaplingMapping() {
        // Standard saplings
        LOG_TO_SAPLING.put(Material.OAK_LOG, Material.OAK_SAPLING);
        LOG_TO_SAPLING.put(Material.BIRCH_LOG, Material.BIRCH_SAPLING);
        LOG_TO_SAPLING.put(Material.SPRUCE_LOG, Material.SPRUCE_SAPLING);
        LOG_TO_SAPLING.put(Material.JUNGLE_LOG, Material.JUNGLE_SAPLING);
        LOG_TO_SAPLING.put(Material.ACACIA_LOG, Material.ACACIA_SAPLING);
        LOG_TO_SAPLING.put(Material.DARK_OAK_LOG, Material.DARK_OAK_SAPLING);

        // Nether fungus (1.16+)
        tryAddSaplingMapping("CRIMSON_STEM", "CRIMSON_FUNGUS");
        tryAddSaplingMapping("WARPED_STEM", "WARPED_FUNGUS");

        // 1.19+ saplings
        tryAddSaplingMapping("MANGROVE_LOG", "MANGROVE_PROPAGULE");

        // 1.20+ saplings
        tryAddSaplingMapping("CHERRY_LOG", "CHERRY_SAPLING");

        // 1.21+ saplings
        tryAddSaplingMapping("PALE_OAK_LOG", "PALE_OAK_SAPLING");
    }

    private static void tryAddMaterial(Set<Material> set, String materialName) {
        try {
            Material mat = Material.valueOf(materialName);
            set.add(mat);
        } catch (IllegalArgumentException e) {
        }
    }

    private static void tryAddSaplingMapping(String logName, String saplingName) {
        try {
            Material log = Material.valueOf(logName);
            Material sapling = Material.valueOf(saplingName);
            LOG_TO_SAPLING.put(log, sapling);
        } catch (IllegalArgumentException e) {
        }
    }

    public static Set<Material> getLogTypes() {
        Set<Material> result = new HashSet<>();
        result.addAll(LOG_TYPES);
        return result;
    }

    public static Set<Material> getLeafTypes() {
        Set<Material> result = new HashSet<>();
        result.addAll(LEAF_TYPES);
        return result;
    }

    public static Set<Material> getAllTreeBlocks() {
        Set<Material> all = new HashSet<>();
        all.addAll(LOG_TYPES);
        return all;
    }

    public static boolean isLog(Material material) {
        return LOG_TYPES.contains(material);
    }

    public static boolean isLeaf(Material material) {
        return LEAF_TYPES.contains(material);
    }

    public static Material getSaplingForLog(Material log) {
        return LOG_TO_SAPLING.get(log);
    }

    public static boolean isAxe(Material material) {
        if (material == null) {
            return false;
        }

        String name = material.name();
        return material == Material.WOODEN_AXE ||
                material == Material.STONE_AXE ||
                material == Material.IRON_AXE ||
                material == Material.GOLDEN_AXE ||
                material == Material.DIAMOND_AXE ||
                name.equals("NETHERITE_AXE");
    }
}