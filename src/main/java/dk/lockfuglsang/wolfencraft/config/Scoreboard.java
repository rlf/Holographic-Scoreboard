package dk.lockfuglsang.wolfencraft.config;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import dk.lockfuglsang.wolfencraft.util.BufferedConsoleSender;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import static dk.lockfuglsang.wolfencraft.util.TimeUtil.getTicksAsTime;

/**
 * TODO: Rasmus javadoc
 */
public class Scoreboard {
    private String id;
    private String command;
    private Location location;
    private int refreshTicks;
    private Hologram hologram;

    public Scoreboard(String id, String refresh, String command, Location location) {
        this.id = id;
        this.refreshTicks = TimeUtil.getTimeAsTicks(refresh);
        this.command = command;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRefreshTicks() {
        return refreshTicks;
    }

    public String getRefresh() {
        return getTicksAsTime(refreshTicks);
    }

    public void setRefresh(String refresh) {
        this.refreshTicks = TimeUtil.getTimeAsTicks(refresh);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void refreshHologram(Plugin plugin) {
        BufferedConsoleSender sender = new BufferedConsoleSender(Bukkit.getConsoleSender());
        String output;
        if (Bukkit.dispatchCommand(sender, command)) {
            output = sender.getStdout();
        } else {
            output = "§4Unable to execute " + command;
        }
        output = output.trim().replaceAll("\r", "");
        String[] lines = getLines(output);
        if (hologram != null && !hologram.isDeleted()) {
            hologram.clearLines();
            for (String line : lines) {
                hologram.addLine(line.trim());
            }
            hologram.update();
        } else {
            hologram = HolographicDisplaysAPI.createHologram(plugin, location, lines);
        }
    }

    private String[] getLines(String output) {
        String[] lines = output.split("\n");
        // TODO: Add formatting + padding (perhaps we need separate holograms per line?)
        return lines;
    }

    public void removeHologram() {
        if (hologram != null && !hologram.isDeleted()) {
            hologram.delete();
            hologram = null;
        }
    }

    @Override
    public String toString() {
        return "Scoreboard{" +
                "id='" + id + '\'' +
                ", command='" + command + '\'' +
                ", location=" + location +
                ", refreshTicks=" + refreshTicks +
                ", hologram=" + hologram +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Scoreboard that = (Scoreboard) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
