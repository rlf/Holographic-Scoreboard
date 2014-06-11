package dk.lockfuglsang.wolfencraft;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * TODO: Rasmus javadoc
 */
public final class HolographicScoreboard  extends JavaPlugin {
    private List<Scoreboard> scoreboards = new ArrayList<>();
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
        super.onDisable();
        removeAllBoards();
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
        super.saveConfig();
        ConfigWriter.save(getConfig(), scoreboards);
        try {
            getConfig().save("scoreboards.yml");
        } catch (IOException e) {
            getLogger().severe("Unable to save configuration!");
        }
    }

    @java.lang.Override
    public boolean onCommand(CommandSender sender, Command command, java.lang.String label, java.lang.String[] args) {
        if (command.getName().equalsIgnoreCase("holographicscoreboard")) {
            if (args.length > 0) {
                switch (args[0]) {
                    case "list": return showList(sender);
                    case "create": return createScoreboard(sender, args);
                    case "remove": return removeScoreboard(sender, args);
                    case "info": return printInfo(sender);
                    case "save": return saveConfig(sender);
                    case "reload": return reloadConfig(sender);
                    case "cleanup": return removeAllHolograms(sender);
                }
            }
        }
        return false;
    }

    private boolean removeAllHolograms(CommandSender sender) {
        Hologram[] holograms = HolographicDisplaysAPI.getHolograms(this);
        for (Hologram hologram : holograms) {
            hologram.delete();
        }
        return true;
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

    private boolean printInfo(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("§eConfig: §f" + getConfig() + "\n");
        sb.append("§eBoards: §f" + scoreboards + "\n");
        sender.sendMessage(sb.toString().split("\n"));
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
        return removeScoreboard(scoreboard);
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
        if (args.length < 4) {
            sender.sendMessage("§4Wrong number of arguments for create!");
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("§4Only a player can create scoreboards!");
            return false;
        }
        Player player = (Player)sender;
        String id = args[1];
        String refresh = args[2];
        int interval = TimeUtil.getTimeAsTicks(refresh);
        String cmd = args[3];
        for (int i = 4; i < args.length; i++) {
            cmd += " " + args[i];
        }
        if (interval > 0) {
            Scoreboard scoreboard = new Scoreboard(id, refresh, cmd, player.getLocation());
            scoreboards.add(scoreboard);
            YamlConfiguration config = new YamlConfiguration();
            ConfigWriter.save(config, scoreboard);
            sender.sendMessage(("Created scoreboard: " + id + "\n" + config.saveToString()).split("\n"));
            scheduleUpdater(scoreboard);
            return true;
        }
        return false;
    }
}
