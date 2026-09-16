package me.rimuru.treeharvester;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class TreeHarvester extends JavaPlugin {

    private static TreeHarvester instance;
    private FileConfiguration config;
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();
        config = getConfig();
        languageManager = new LanguageManager(this);

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        // Register events
        getServer().getPluginManager().registerEvents(new BlockBreakTracker(this), this);
        getServer().getPluginManager().registerEvents(new TreeBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);

        // Register command
        getCommand("treeharvester").setExecutor(new TreeHarvesterCommand(this));

        getLogger().info("TreeHarvester has been enabled!");
        getLogger().info("Tracking " + databaseManager.getTotalTrackedBlocks() + " player-placed blocks.");
    }

    @Override
    public void onDisable() {
        // Disconnect database
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("TreeHarvester has been disabled!");
    }

    public static TreeHarvester getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        languageManager.reload();
    }
}
