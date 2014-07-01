package dk.lockfuglsang.wolfencraft.config;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import dk.lockfuglsang.wolfencraft.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;
import org.bukkit.plugin.Plugin;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import static dk.lockfuglsang.wolfencraft.util.TimeUtil.getTicksAsTime;

/**
 * The actual Scoreboard data-object.
 * TODO: A bit too much business logic in here... perhaps...
 */
public class Scoreboard {
    public enum Sender { PLAYER, CONSOLE }

    private String id;
    private String command;
    private Location location;
    private int refreshTicks;
    private Sender sender;

    private volatile Hologram hologram;
    private volatile BufferedSender bufferedSender;

    public Scoreboard(String id, String refresh, Sender sender, String command, Location location) {
        this.id = id;
        this.refreshTicks = TimeUtil.getTimeAsTicks(refresh);
        this.sender = sender;
        this.command = command;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public int getRefreshTicks() {
        return refreshTicks;
    }

    public String getRefresh() {
        return getTicksAsTime(refreshTicks);
    }

    public String getCommand() {
        return command;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Sender getSender() {
        return sender;
    }

    public void refreshHologram(Plugin plugin) {
        BufferedSender sender = createBufferedSender(plugin);
        if (sender == null || !Bukkit.dispatchCommand(sender.getSender(), command)) {
            String output = "\u00a74Unable to execute \u00a73" + command;
            updateHologram(plugin, output);
        }
    }

    private void updateHologram(final Plugin plugin, String output) {
        output = output.trim().replaceAll("\r", "");
        final String[] lines = getLines(output);
        Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (hologram != null) {
                    hologram.delete();
                    hologram = null;
                }
                hologram = HolographicDisplaysAPI.createHologram(plugin, location, lines);
                return null;
            }
        });
    }

    // TODO: Cleanup the propertychangelisteners? perhaps a leak?
    private BufferedSender createBufferedSender(final Plugin plugin) {
        PropertyChangeListener changeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("stdout")) {
                    updateHologram(plugin, (String) evt.getNewValue());
                }
            }
        };
        if (sender == Sender.CONSOLE) {
            if (bufferedSender != null) {
                bufferedSender.clear();
                return bufferedSender;
            }
            bufferedSender = new BufferedConsoleSender(Bukkit.getConsoleSender());
            bufferedSender.addPropertyChangeListener(changeListener);
            return bufferedSender;
        } else {
            Player nearestPlayer = DistanceUtil.getNearestPlayer(location);
            if (nearestPlayer == null) {
                nearestPlayer = Bukkit.getOnlinePlayers().length > 0 ? Bukkit.getOnlinePlayers()[0] : null;
            }
            if (nearestPlayer != null) {
                //bufferedSender = new BufferedPlayerSender(nearestPlayer);
                bufferedSender = new ProxyPlayer(nearestPlayer);
                bufferedSender.addPropertyChangeListener(changeListener);
                return bufferedSender;
            }
        }
        return null;
    }

    private String[] getLines(String output) {
        String[] lines = output.split("\n");
        //return StringUtil.alignLeft2(lines);
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
