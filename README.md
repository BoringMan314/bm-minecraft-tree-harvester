🌲 TreeHarvester
A smart Minecraft tree harvesting plugin that automatically chops down entire trees with one break while intelligently detecting natural vs player-placed trees.
✨ Features

🌳 One-Click Tree Harvesting - Break the bottom log and watch the entire tree fall
🧠 Smart Natural Tree Detection - Only harvests naturally generated trees, not player-built structures
📊 SQLite Database Tracking - Tracks all player-placed logs and leaves to prevent exploitation
🌱 Auto-Replanting - Automatically replants saplings after harvesting
🍂 Instant Leaf Decay - Leaves decay automatically after a configurable delay
🪓 Durability Management - Properly calculates and applies tool durability (respects Unbreaking enchantment)
🎮 Game Mode Support - Works in Survival, Creative, or any configured game mode
🛡️ Protection Plugin Support - Respects WorldGuard, GriefPrevention, and other protection plugins
🌲 Universal Tree Support - All tree types from Oak to Pale Oak (1.13-1.21+)
📏 2x2 Tree Support - Handles mega trees (Jungle, Spruce, Dark Oak)
🍄 Nether Tree Support - Works with Crimson and Warped stems
⚡ Performance Optimized - Async database operations, efficient tree detection algorithms

📦 Supported Tree Types
Standard Trees (1.13+)

Oak, Birch, Spruce, Jungle, Acacia, Dark Oak

Nether Trees (1.16+)

Crimson Stem, Warped Stem

Modern Trees

Mangrove (1.19+)
Cherry (1.20+)
Pale Oak (1.21+)

Special Features

✅ 2x2 thick trees (Mega Spruce, Jungle, Dark Oak)
✅ All tree variants and orientations
✅ Natural vs player-placed detection
✅ Structure block detection (prevents harvesting village/mansion trees)

🚀 Installation

Download the latest TreeHarvester.jar from Releases
Place the jar file in your server's plugins/ folder
Restart your server or use a plugin manager to load it
Configure settings in plugins/TreeHarvester/config.yml
Reload with /treeharvester reload

🎮 Commands
CommandDescriptionPermission/treeharvesterShow help menutreeharvester.command/treeharvester reloadReload configurationtreeharvester.admin/treeharvester infoShow plugin informationtreeharvester.command/treeharvester statsShow database statisticstreeharvester.admin/treeharvester optimizeOptimize databasetreeharvester.admin
Aliases: /th, /treeharvest
🔑 Permissions
PermissionDescriptionDefaulttreeharvester.useUse tree harvesting featuretruetreeharvester.commandUse basic commandstruetreeharvester.adminUse admin commandsop
⚙️ Configuration
yaml# Enable or disable the plugin
enabled: true

# Require permission to use tree harvester
require-permission: false

# Game modes where tree harvesting works
allowed-game-modes:
  - survival
  - creative

# Automatically replant saplings
auto-replant: true

# Delay before leaves decay (in ticks, 20 ticks = 1 second)
leaf-decay-delay-ticks: 40

# Respect protection plugins (WorldGuard, GriefPrevention, etc.)
respect-protection-plugins: true

### Language

`build.bat` produces four fixed-language JARs in `dist/`. Install the one that matches your server language.

```text
TreeHarvester_26.2_1.1-en_US.jar
TreeHarvester_26.2_1.1-zh_TW.jar
TreeHarvester_26.2_1.1-zh_CN.jar
TreeHarvester_26.2_1.1-ja_JP.jar
```

The selected JAR extracts its language file to `plugins/TreeHarvester/lang/` on first startup; you can customize its messages there.

📋 Requirements

Minecraft Version: 1.13 - 1.21+
Server Software: Spigot, Paper, or any Bukkit-based server
Java Version: 8+

# Maximum tree size (prevents performance issues)
max-tree-blocks: 1000
See config.yml for full configuration options and detailed comments.
