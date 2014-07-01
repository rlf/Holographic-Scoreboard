package dk.lockfuglsang.wolfencraft;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Main Bukkit Plugin Entry Point
 */
public final class HolographicScoreboard extends JavaPlugin {
    public static final String CMD_HGS = "holographicscoreboard";
    private List<Scoreboard> scoreboards = new CopyOnWriteArrayList<>();
    private Map<String, BukkitTask> tasks = new HashMap<>();

    @Override
    public void onEnable() {
        if (!isDependenciesFulfilled()) {
            getLogger().severe("*** HolographicDisplays and ProtocolLib is required! ***");
            getLogger().severe("=> HolographicScoreboard will be disabled!!");
            this.setEnabled(false);
            return;
        }
        loadScoreboards();
    }

    @Override
    public void onDisable() {
        saveConfig();
        removeAllBoards();
        removeAllHolograms();
        super.onDisable();
    }

    private void removeAllBoards() {
        if (scoreboards != null) {
            // To avoid concurrentmodificationexception
            ArrayList<Scoreboard> copy = new ArrayList<>(scoreboards);
            for (Scoreboard scoreboard : copy) {
                removeScoreboard(scoreboard);
            }
        }
    }

    private boolean isDependenciesFulfilled() {
        return Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadScoreboards();
    }

    private void loadScoreboards() {
        if (scoreboards != null) {
            removeAllBoards();
        }
        try {
            getConfig().load("scoreboards.yml");
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().info("No configuration found for Holographic Scoreboards!");
        }
        scoreboards = ConfigWriter.load(getConfig());
        for (Scoreboard scoreboard : scoreboards) {
            scheduleUpdater(scoreboard);
        }
    }

    private void scheduleUpdater(final Scoreboard scoreboard) {
        tasks.put(scoreboard.getId(), Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                scoreboard.refreshHologram(HolographicScoreboard.this);
            }
        }, 0, scoreboard.getRefreshTicks()));
    }

    @Override
    public FileConfiguration getConfig() {
        FileConfiguration config = super.getConfig();
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        return config;
    }

    @Override
    public void saveConfig() {
        ConfigWriter.save(getConfig(), scoreboards);
        try {
            getConfig().save("scoreboards.yml");
        } catch (IOException e) {
            getLogger().severe("Unable to save configuration!");
        }
    }

    @java.lang.Override
    public boolean onCommand(CommandSender sender, Command command, java.lang.String label, java.lang.String[] args) {
        if (command.getName().equalsIgnoreCase(CMD_HGS)) {
            if (isAllowed(sender)) {
                if (args.length == 0) {
                    return false;
                }
                switch (args[0]) {
                    case "list":
                        return showList(sender);
                    case "create":
                        return createScoreboard(sender, args);
                    case "remove":
                        return removeScoreboard(sender, args);
                    case "save":
                        return saveConfig(sender);
                    case "reload":
                        return reloadConfig(sender);
                    case "cleanup":
                        sender.sendMessage("§3Cleaned " + removeAllHolograms() + " holograms!");
                        return true;
                    case "refresh":
                        return refreshScoreboards(sender, args);
                    case "move":
                        return moveScoreboard(sender, args);
                }
            } else {
                sender.sendMessage("§4You do not have permission to Holographic Scoreboards!");
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("holographicscoreboard.admin") || sender.isOp();
    }

    private boolean moveScoreboard(CommandSender sender, String[] args) {
        Scoreboard scoreboard = getScoreboard(args[1]);
        if (scoreboard != null && sender instanceof Player) {
            scoreboard.setLocation(((Player)sender).getLocation());
            scoreboard.refreshHologram(this);
            sender.sendMessage("§3Moved scoreboard §4" + args[1]);
            return true;
        }
        sender.sendMessage("§4No valid scoreboard §2" + args[1] + "§4 found!");
        return false;
    }

    private List<String> getSuggestions(String cmd, List<String> possibleSuggestions) {
        List<String> suggestions = new ArrayList<>();
        for (String suggestion : possibleSuggestions) {
            if (suggestion.startsWith(cmd)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase(CMD_HGS)) {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 1) {
                // Complete on the first-level commands
                String arg = args[0].toLowerCase();
                suggestions.addAll(getSuggestions(arg, Arrays.asList("list", "create", "remove", "info", "save", "reload", "cleanup", "refresh", "move")));
            } else if (args.length == 2) {
                // Complete on 2nd level
                if ("remove".equals(args[0]) || "move".equals(args[0])) {
                    suggestions.addAll(getScoreboards(args[1]));
                } else if ("refresh".equals(args[0])) {
                    suggestions.addAll(getScoreboards(args[1]));
                    suggestions.addAll(getSuggestions(args[1], Arrays.asList("*")));
                }
            } else if (args.length == 3) {
                if ("create".equals(args[0])) {
                    // interval - we could suggest something here...
                    getSuggestions(args[2].toLowerCase(), Arrays.asList("10s", "30s", "5m", "10m", "30m", "1h"));
                }
            } else if (args.length == 4) {
                if ("create".equals(args[0])) {
                    suggestions.addAll(getSuggestions(args[3].toLowerCase(), Arrays.asList("player", "console")));
                }
            }
            return suggestions;
        }
        return null;
    }

    private List<String> getScoreboards(String arg) {
        String lowerArg = arg.toLowerCase();
        List<String> suggestions = new ArrayList<>();
        for (Scoreboard scoreboard : scoreboards) {
            if (scoreboard.getId().toLowerCase().startsWith(lowerArg)) {
                suggestions.add(scoreboard.getId());
            }
        }
        return suggestions;
    }

    private boolean refreshScoreboards(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[1].equals("*")) {
                for (Scoreboard scoreboard : scoreboards) {
                    scoreboard.refreshHologram(this);
                }
                sender.sendMessage("§2Refreshed all scoreboards!");
                return true;
            } else {
                Scoreboard scoreboard = getScoreboard(args[1]);
                if (scoreboard != null) {
                    scoreboard.refreshHologram(this);
                    sender.sendMessage("§2Refreshed " + args[1]);
                    return true;
                }
            }
        }
        sender.sendMessage("§4You have to provide a valid scoreboard-id!");
        return false;
    }

    private int removeAllHolograms() {
        Hologram[] holograms = HolographicDisplaysAPI.getHolograms(this);
        for (Hologram hologram : holograms) {
            hologram.delete();
        }
        return holograms.length;
    }

    private boolean reloadConfig(CommandSender sender) {
        reloadConfig();
        sender.sendMessage("§3Configuration reloaded!");
        return true;
    }

    private boolean saveConfig(CommandSender sender) {
        saveConfig();
        sender.sendMessage("§3Configuration saved!");
        return true;
    }

    private boolean removeScoreboard(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("§4Wrong number of arguments for remove!");
            return false;
        }
        String scoreName = args[1];
        Scoreboard scoreboard = getScoreboard(scoreName);
        if (scoreboard == null) {
            sender.sendMessage("§4No scoreboard with id " + scoreName + " was found!");
            return false;
        }
        if (removeScoreboard(scoreboard)) {
            sender.sendMessage("§2Scoreboard §3" + scoreName + "§2 was removed");
            saveConfig();
            return true;
        }
        return false;
    }

    private boolean removeScoreboard(Scoreboard scoreboard) {
        BukkitTask bukkitTask = tasks.remove(scoreboard.getId());
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        scoreboard.removeHologram();
        return scoreboards.remove(scoreboard);
    }

    private Scoreboard getScoreboard(String scoreName) {
        for (Scoreboard scoreboard : scoreboards) {
            if (scoreboard.getId().equals(scoreName)) {
                return scoreboard;
            }
        }
        return null;
    }

    private boolean showList(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        if (scoreboards == null || scoreboards.isEmpty()) {
            sb.append("§3No scoreboards located!");
        } else {
            YamlConfiguration config = new YamlConfiguration();
            ConfigWriter.save(config, scoreboards);
            sb.append(config.saveToString());
        }
        sender.sendMessage(sb.toString().split("\n"));
        return true;
    }

    private boolean createScoreboard(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage("§4Wrong number of arguments for create!");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§4Only a player can create scoreboards!");
            return false;
        }
        Player player = (Player) sender;
        String id = args[1];
        String refresh = args[2];
        int interval = TimeUtil.getTimeAsTicks(refresh);
        if (interval <= 0) {
            sender.sendMessage("§4Invalid interval, must be <num>[h|m|s]");
            return false;
        }
        Scoreboard.Sender senderType = Scoreboard.Sender.CONSOLE;
        try {
            senderType = Scoreboard.Sender.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException ignored) {
            sender.sendMessage("§4Invalid sender, only player or console is allowed!");
            return false;
        }
        String cmd = args[4];
        for (int i = 5; i < args.length; i++) {
            cmd += " " + args[i];
        }
        Scoreboard scoreboard = getScoreboard(id);
        if (scoreboard != null) {
            removeScoreboard(scoreboard);
        }
        scoreboard = new Scoreboard(id, refresh, senderType, cmd, player.getLocation());
        scoreboards.add(scoreboard);
        YamlConfiguration config = new YamlConfiguration();
        ConfigWriter.save(config, scoreboard);
        sender.sendMessage(("Created scoreboard: " + id + "\n" + config.saveToString()).split("\n"));
        scheduleUpdater(scoreboard);
        saveConfig();
        return true;
    }
}
