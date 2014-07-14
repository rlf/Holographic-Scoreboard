package dk.lockfuglsang.wolfencraft.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simple persistence wrapper for the Bukkit FileConfiguration.
 */
public enum ConfigWriter {;
    public static void save(Configuration config, Set<Scoreboard> scoreboards) {
        ConfigurationSection section = config.createSection("boards");
        for (Scoreboard scoreboard : scoreboards) {
            save(section.createSection(scoreboard.getId()), scoreboard);
        }
    }

    public static void save(ConfigurationSection config, Scoreboard scoreboard) {
        config.set("command", scoreboard.getCommand());
        config.set("interval", scoreboard.getRefresh());
        config.set("sender", scoreboard.getSender().name().toLowerCase());
        save(config.createSection("location"), scoreboard.getLocation());
    }

    private static void save(ConfigurationSection section, Location location) {
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
    }

    public static List<Scoreboard> load(FileConfiguration config) {
        List<Scoreboard> scoreboards = new ArrayList<>();
        ConfigurationSection boards = config.getConfigurationSection("boards");
        if (boards != null) {
            for (String id : boards.getKeys(false)) {
                ConfigurationSection section = boards.getConfigurationSection(id);
                if (section != null) {
                    scoreboards.add(loadScoreboard(section, id));
                }
            }
        }
        return scoreboards;
    }

    private static Scoreboard loadScoreboard(ConfigurationSection section, String id) {
        String interval = section.getString("interval", "30m");
        String command = section.getString("command");
        Scoreboard.Sender senderType = Scoreboard.Sender.valueOf(section.getString("sender", "console").toUpperCase());
        return new Scoreboard(id, interval, senderType, command, loadLocation(section.getConfigurationSection("location")));
    }

    private static Location loadLocation(ConfigurationSection section) {
        String world = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
