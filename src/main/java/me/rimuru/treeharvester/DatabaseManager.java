package me.rimuru.treeharvester;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private Connection connection;
    private final TreeHarvester plugin;

    public DatabaseManager(TreeHarvester plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/player_blocks.db";
            connection = DriverManager.getConnection(url);

            createTable();
            plugin.getLogger().info("Database connected successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_placed_blocks (" +
                "world TEXT NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL," +
                "timestamp BIGINT NOT NULL," +
                "PRIMARY KEY (world, x, y, z)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);

            // Create index for faster lookups
            String indexSql = "CREATE INDEX IF NOT EXISTS idx_location ON player_placed_blocks(world, x, y, z);";
            stmt.execute(indexSql);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database disconnected successfully!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to disconnect from database: " + e.getMessage());
        }
    }

    /**
     * Mark a block as player-placed (async)
     */
    public CompletableFuture<Void> markBlockAsPlayerPlaced(Block block) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO player_placed_blocks (world, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                Location loc = block.getLocation();
                pstmt.setString(1, loc.getWorld().getName());
                pstmt.setInt(2, loc.getBlockX());
                pstmt.setInt(3, loc.getBlockY());
                pstmt.setInt(4, loc.getBlockZ());
                pstmt.setLong(5, System.currentTimeMillis());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to mark block as player-placed: " + e.getMessage());
            }
        });
    }

    /**
     * Check if a block was placed by a player (sync - use cached check when possible)
     */
    public boolean isBlockPlayerPlaced(Block block) {
        String sql = "SELECT 1 FROM player_placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ? LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            Location loc = block.getLocation();
            pstmt.setString(1, loc.getWorld().getName());
            pstmt.setInt(2, loc.getBlockX());
            pstmt.setInt(3, loc.getBlockY());
            pstmt.setInt(4, loc.getBlockZ());

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check if block is player-placed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove a block from the database when broken (async)
     */
    public CompletableFuture<Void> removeBlock(Block block) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM player_placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                Location loc = block.getLocation();
                pstmt.setString(1, loc.getWorld().getName());
                pstmt.setInt(2, loc.getBlockX());
                pstmt.setInt(3, loc.getBlockY());
                pstmt.setInt(4, loc.getBlockZ());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to remove block from database: " + e.getMessage());
            }
        });
    }

    /**
     * Get total number of tracked blocks
     */
    public int getTotalTrackedBlocks() {
        String sql = "SELECT COUNT(*) FROM player_placed_blocks";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get total tracked blocks: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Optimize database (vacuum)
     */
    public CompletableFuture<Void> optimizeDatabase() {
        return CompletableFuture.runAsync(() -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("VACUUM");
                plugin.getLogger().info("Database optimized successfully!");
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to optimize database: " + e.getMessage());
            }
        });
    }
}