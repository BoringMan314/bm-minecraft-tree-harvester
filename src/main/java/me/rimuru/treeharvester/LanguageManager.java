package me.rimuru.treeharvester;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Loads player-facing messages from the configured language file. */
public class LanguageManager {

    private static final String DEFAULT_LANGUAGE = "en_US";

    private final TreeHarvester plugin;
    private YamlConfiguration messages;

    public LanguageManager(TreeHarvester plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        String language = getBundledLanguage();

        File languageDirectory = new File(plugin.getDataFolder(), "lang");
        if (!languageDirectory.exists() && !languageDirectory.mkdirs()) {
            plugin.getLogger().warning("Could not create language directory.");
        }

        saveLanguageFile(language);

        File selectedFile = new File(languageDirectory, language + ".yml");
        if (!selectedFile.isFile()) {
            plugin.getLogger().warning("Language '" + language + "' was not found; using " + DEFAULT_LANGUAGE + ".");
            saveLanguageFile(DEFAULT_LANGUAGE);
            selectedFile = new File(languageDirectory, DEFAULT_LANGUAGE + ".yml");
        }
        messages = YamlConfiguration.loadConfiguration(selectedFile);
    }

    public String get(String path) {
        String message = messages.getString(path);
        if (message == null) {
            message = path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void saveLanguageFile(String language) {
        String resourcePath = "lang/" + language + ".yml";
        if (plugin.getResource(resourcePath) == null) {
            return;
        }

        File destination = new File(plugin.getDataFolder(), resourcePath);
        if (!destination.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private String getBundledLanguage() {
        try (InputStream input = plugin.getResource("active-language.yml")) {
            if (input == null) {
                return DEFAULT_LANGUAGE;
            }
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            return normalizeLanguage(configuration.getString("language", DEFAULT_LANGUAGE));
        } catch (Exception exception) {
            plugin.getLogger().warning("Could not load the bundled language setting; using " + DEFAULT_LANGUAGE + ".");
            return DEFAULT_LANGUAGE;
        }
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return DEFAULT_LANGUAGE;
        }
        String[] parts = language.trim().replace('-', '_').split("_", 2);
        return parts.length == 2
                ? parts[0].toLowerCase(Locale.ROOT) + "_" + parts[1].toUpperCase(Locale.ROOT)
                : language.trim();
    }
}
