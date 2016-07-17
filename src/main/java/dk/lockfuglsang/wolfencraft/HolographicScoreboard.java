package dk.lockfuglsang.wolfencraft;

import dk.lockfuglsang.wolfencraft.commands.HGSCommand;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.intercept.PacketInterceptor;
import dk.lockfuglsang.wolfencraft.stats.CommandPlotter;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
    public static PacketInterceptor interceptor;

    @Override
    public void onEnable() {
        getCommand("holographicscoreboard").setExecutor(new HGSCommand(this));
        if (!isDependenciesFulfilled()) {
            getLogger().severe(rm.format("log.missing.dependencies"));
            return;
        }
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
        interceptor = new PacketInterceptor(this);
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                loadScoreboards();
            }
        }, TimeUtil.getTimeAsTicks(getConfig().getString("delayAfterEnable", "30s")));
    }

    @Override
    public void onDisable() {
        saveConfig();
        removeAllBoards();
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

    public boolean isDependenciesFulfilled() {
        return Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")
               && (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")
                || Bukkit.getPluginManager().isPluginEnabled("Holograms"));
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
        scheduleAll();
    }

    private void scheduleAll() {
        for (Scoreboard scoreboard : scoreboards) {
            scheduleUpdater(scoreboard);
        }
    }

    public void scheduleUpdater(final Scoreboard scoreboard) {
        final String scoreBoardId = scoreboard.getId();
        BukkitTask oldTask = tasks.put(scoreBoardId, Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
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
        if (oldTask != null) {
            oldTask.cancel();
        }
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

    public boolean refreshAll(CommandSender sender) {
        for (Scoreboard scoreboard : scoreboards) {
            scoreboard.refreshView(this);
        }
        sender.sendMessage(rm.format("msg.scoreboard.refresh.all"));
        return true;
    }

    public boolean removeScoreboard(Scoreboard scoreboard) {
        synchronized (scoreboards) {
            BukkitTask bukkitTask = tasks.remove(scoreboard.getId());
            if (bukkitTask != null) {
                bukkitTask.cancel();
            }
            scoreboard.removeView();
            return scoreboards.remove(scoreboard);
        }
    }

    public Scoreboard getScoreboard(String scoreName) {
        for (Scoreboard scoreboard : scoreboards) {
            if (scoreboard.getId().equals(scoreName)) {
                return scoreboard;
            }
        }
        return null;
    }

    public void addScoreboard(Scoreboard scoreboard) {
        scoreboards.add(scoreboard);
    }

    public ResourceManager getRM() {
        return rm;
    }

    public static Set<Scoreboard> getScoreboards() {
        return scoreboards;
    }
}
