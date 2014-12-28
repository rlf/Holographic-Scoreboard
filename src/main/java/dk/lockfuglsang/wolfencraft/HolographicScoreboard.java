package dk.lockfuglsang.wolfencraft;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.stats.CommandPlotter;
import dk.lockfuglsang.wolfencraft.util.LocationUtil;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The Main Bukkit Plugin Entry Point
 */
public final class HolographicScoreboard extends JavaPlugin {
    public static final String CMD_HGS = "holographicscoreboard";
    private static final Set<Scoreboard> scoreboards = new CopyOnWriteArraySet<>();
    private static final Map<String, BukkitTask> tasks = new HashMap<>();

    private Map<String, CommandPlotter> plotters = new HashMap<>();
    private Metrics metrics;
    private Metrics.Graph cmdGraph;
    private final ResourceManager rm = ResourceManager.getRM();

    @Override
    public void onEnable() {
        if (!isDependenciesFulfilled()) {
            getLogger().severe(rm.format("log.missing.dependencies"));
            this.setEnabled(false);
            return;
        }
        loadScoreboards();
        try {
            metrics = new Metrics(this);
            cmdGraph = metrics.createGraph("Commands");
            if (metrics.start()) {
                getLogger().info(rm.format("log.mcstats.enabled"));
            } else {
                getLogger().warning("MCStats was not enabled for HolographicScoreboard");
                getLogger().info("- isOptOut ; " + metrics.isOptOut());
                getLogger().info("- config : " + metrics.getConfigFile());
            }
        } catch (IOException e) {
            getLogger().severe(rm.format("log.mcstats.failed", e.getMessage()));
        }
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
        return Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");// && Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
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
        scoreboards.addAll(ConfigWriter.load(getConfig()));
        for (Scoreboard scoreboard : scoreboards) {
            scheduleUpdater(scoreboard);
        }
    }

    private void scheduleUpdater(final Scoreboard scoreboard) {
        final String scoreBoardId = scoreboard.getId();
        tasks.put(scoreBoardId, Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                Scoreboard board = getScoreboard(scoreBoardId);
                if (board != null) {
                    board.refreshView(HolographicScoreboard.this);
                } else {
                    // TODO: Somehow cleanup?
                    BukkitTask task = tasks.remove(scoreBoardId);
                    if (task != null) {
                        task.cancel();
                    }
                }
            }
        }, 0, scoreboard.getRefreshTicks()));
        // TODO: Do stats
        getPlotter(scoreboard.getCommand()).inc();
    }

    private synchronized CommandPlotter getPlotter(String command) {
        if (!plotters.containsKey(command)) {
            CommandPlotter plotter = new CommandPlotter();
            plotters.put(command, plotter);
            if (cmdGraph != null) {
                cmdGraph.addPlotter(plotter);
            }
            // TODO: is metrics.start() required here again?
        }
        return plotters.get(command);
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
        super.saveConfig();
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
                        sender.sendMessage(rm.format("msg.cleaned.holograms", removeAllHolograms()));
                        return true;
                    case "refresh":
                        return refreshScoreboards(sender, args);
                    case "move":
                        return moveScoreboard(sender, args);
                    case "edit":
                        return editScoreboard(sender, args);
                }
            } else {
                sender.sendMessage(rm.format("error.noaccess"));
                return true;
            }
        }
        return false;
    }

    private boolean editScoreboard(CommandSender sender, String[] args) {
        String scoreboardId = args.length > 1 ? args[1] : null;
        String key = args.length > 2 ? args[2] : null;
        String value = args.length > 3 ? args[3] : null;
        if (args.length > 4) {
            for (int i = 4; i < args.length; i++) {
                value += " " + args[i];
            }
        }
        Scoreboard scoreboard = getScoreboard(scoreboardId);
        if (scoreboard != null && key != null) {
            if (key.equals("filter")) {
                scoreboard.setFilter(value);
            } else if (key.equals("location")) {
                Location location = LocationUtil.getLocation(value);
                if (location == null) {
                    sender.sendMessage(rm.format("error.wrong.location"));
                    return false;
                } else {
                    scoreboard.setLocation(location);
                }
            } else if (key.equals("delay") || key.equals("interval")) {
                int time = TimeUtil.getTimeAsTicks(value);
                if (time == 0) {
                    sender.sendMessage(rm.format("error.wrong." + key));
                    return false;
                } else if (key.equals("delay")) {
                    scoreboard.setDelay(value);
                } else if (key.equals("interval")) {
                    scoreboard.setInterval(value);
                }
            } else {
                return false;
            }
            sender.sendMessage(rm.format("msg.scoreboard.edit", key, scoreboardId, value));
            return true;
        }
        return false;
    }

    private boolean isAllowed(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("holographicscoreboard.admin") || sender.isOp();
    }

    private boolean moveScoreboard(CommandSender sender, String[] args) {
        Scoreboard scoreboard = getScoreboard(args[1]);
        if (scoreboard != null) {
            Location location = null;
            try {
                if (args.length == 2 && sender instanceof Player) {
                    location = ((Player) sender).getEyeLocation();
                } else if (args.length == 3) {
                    location = LocationUtil.getLocation(args[2]);
                }
                if (location == null) {
                    throw new IllegalArgumentException("No valid location was found");
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(rm.format("msg.usage.move"));
                return true;
            }
            scoreboard.setLocation(location);
            scoreboard.refreshView(this);
            sender.sendMessage(rm.format("msg.scoreboard.moved", args[1], location.getX(), location.getY(), location.getZ(), location.getWorld().getName()));
            return true;
        }
        sender.sendMessage(rm.format("error.scoreboard.notfound", args[1]));
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
        // TODO: Refactor this to be "smarter"
        if (command.getName().equalsIgnoreCase(CMD_HGS)) {
            List<String> suggestions = new ArrayList<>();
            if (args.length == 1) {
                // Complete on the first-level commands
                String arg = args[0].toLowerCase();
                suggestions.addAll(getSuggestions(arg, Arrays.asList("list", "create", "remove", "info", "save", "reload", "cleanup", "refresh", "move", "edit")));
            } else if (args.length == 2) {
                // Complete on 2nd level
                if ("remove".equals(args[0]) || "move".equals(args[0]) || "edit".equals(args[0])) {
                    suggestions.addAll(getScoreboards(args[1]));
                } else if ("refresh".equals(args[0])) {
                    suggestions.addAll(getScoreboards(args[1]));
                }
            } else if (args.length == 3) {
                if ("create".equals(args[0])) {
                    // interval - we could suggest something here...
                    suggestions.addAll(getSuggestions(args[2].toLowerCase(), Arrays.asList("10s", "30s", "5m", "10m", "30m", "1h")));
                } else if ("edit".equals(args[0])) {
                    suggestions.addAll(Arrays.asList("filter", "delay", "interval", "location"));
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
                return refreshAll(sender);
            } else {
                Scoreboard scoreboard = getScoreboard(args[1]);
                if (scoreboard != null) {
                    scoreboard.refreshView(this);
                    sender.sendMessage(rm.format("msg.scoreboard.refresh", args[1]));
                    return true;
                } else {
                    sender.sendMessage(rm.format("error.scoreboard.notfound", args[1]));
                    return false;
                }
            }
        } else if (args.length == 1) {
            return refreshAll(sender);
        }
        return false;
    }

    private boolean refreshAll(CommandSender sender) {
        for (Scoreboard scoreboard : scoreboards) {
            scoreboard.refreshView(this);
        }
        sender.sendMessage(rm.format("msg.scoreboard.refresh.all"));
        return true;
    }

    private int removeAllHolograms() {
        Collection<Hologram> holograms = HologramsAPI.getHolograms(this);
        for (Hologram hologram : holograms) {
            hologram.delete();
        }
        return holograms.size();
    }

    private boolean reloadConfig(CommandSender sender) {
        reloadConfig();
        sender.sendMessage(rm.format("msg.reload"));
        return true;
    }

    private boolean saveConfig(CommandSender sender) {
        saveConfig();
        sender.sendMessage(rm.format("msg.saved"));
        return true;
    }

    private boolean removeScoreboard(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(rm.format("msg.usage.remove"));
            return true;
        }
        String scoreName = args[1];
        Scoreboard scoreboard = getScoreboard(scoreName);
        if (scoreboard == null) {
            sender.sendMessage(rm.format("error.scoreboard.notfound", scoreName));
            return false;
        }
        if (removeScoreboard(scoreboard)) {
            sender.sendMessage(rm.format("msg.scoreboard.removed", scoreName));
            saveConfig();
            return true;
        }
        return false;
    }

    private boolean removeScoreboard(Scoreboard scoreboard) {
        synchronized (scoreboards) {
            BukkitTask bukkitTask = tasks.remove(scoreboard.getId());
            if (bukkitTask != null) {
                bukkitTask.cancel();
            }
            scoreboard.removeView();
            return scoreboards.remove(scoreboard);
        }
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
            sb.append(rm.format("msg.scoreboards.empty"));
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
            sender.sendMessage(rm.format("msg.usage.create"));
            return true;
        }
        String id = args[1];
        String refresh = args[2];
        int interval = TimeUtil.getTimeAsTicks(refresh);
        if (interval < 200) {
            sender.sendMessage(rm.format("error.wrong.interval"));
            return false;
        }
        Scoreboard.Sender senderType;
        try {
            senderType = Scoreboard.Sender.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException ignored) {
            sender.sendMessage(rm.format("error.wrong.sender"));
            return false;
        }
        int ix = 4;
        Location location = LocationUtil.getLocation(args[ix]);
        if (location == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(rm.format("msg.usage.create.console"));
                return true;
            } else {
                Player player = (Player) sender;
                location = player.getEyeLocation();
            }
        } else {
            ix++;
        }
        String cmd = args[ix++];
        for (int i = ix; i < args.length; i++) {
            cmd += " " + args[i];
        }
        Scoreboard scoreboard = getScoreboard(id);
        if (scoreboard != null) {
            removeScoreboard(scoreboard);
        }
        scoreboard = new Scoreboard(id, refresh, senderType, cmd, location);
        scoreboards.add(scoreboard);
        YamlConfiguration config = new YamlConfiguration();
        ConfigWriter.save(config, scoreboard);
        sender.sendMessage(rm.format("msg.scoreboard.created", id, config.saveToString()).split("\n"));
        scheduleUpdater(scoreboard);
        saveConfig();
        return true;
    }
}
