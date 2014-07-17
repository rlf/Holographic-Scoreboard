package dk.lockfuglsang.wolfencraft.config;

import dk.lockfuglsang.wolfencraft.util.*;
import dk.lockfuglsang.wolfencraft.view.View;
import dk.lockfuglsang.wolfencraft.view.hologram.HologramView;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import static dk.lockfuglsang.wolfencraft.util.TimeUtil.getTicksAsTime;

/**
 * The actual Scoreboard data-object.
 * TODO: A bit too much business logic in here... perhaps...
 */
public class Scoreboard {
    public enum Sender { PLAYER, CONSOLE;}
    private String id;

    private String command;
    private Location location;
    private int refreshTicks;
    private int delayTicks;
    private Sender sender;

    private volatile View view;
    private volatile BukkitTask pendingTask;

    public Scoreboard(String id, String refresh, Sender sender, String command, Location location) {
        this.id = id;
        this.refreshTicks = TimeUtil.getTimeAsTicks(refresh);
        this.sender = sender;
        this.command = command;
        this.location = location;
        delayTicks = 20*5; // 5 seconds later TODO: Make this configurable as well
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
        if (view != null) {
            view.setLocation(location);
        }
    }

    public Location getLocation() {
        return location;
    }

    public Sender getSender() {
        return sender;
    }

    public void refreshView(final Plugin plugin) {
        cancelTask();
        final BufferedSender sender = createBufferedSender(plugin);
        if (sender == null || !Bukkit.dispatchCommand(sender.getSender(), command)) {
            String output = "\u00a74Unable to execute \u00a73" + command;
            updateView(plugin, output);
        } else {
            // TODO: Make this configurable
            pendingTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    updateView(plugin, sender.getStdout());
                    pendingTask = null;
                }
            }, delayTicks);
        }
    }

    private void updateView(final Plugin plugin, String output) {
        getView().updateView(plugin, output);
    }

    private View getView() {
        if (view == null) {
            view = new HologramView();
            view.setLocation(location);
        }
        return view;
    }

    // TODO: Cleanup the propertychangelisteners? perhaps a leak?
    private BufferedSender createBufferedSender(final Plugin plugin) {
        if (sender == Sender.CONSOLE) {
            return new BufferedConsoleSender(Bukkit.getConsoleSender());
        } else {
            Player nearestPlayer = DistanceUtil.getNearestPlayer(location);
            if (nearestPlayer == null) {
                nearestPlayer = Bukkit.getOnlinePlayers().length > 0 ? Bukkit.getOnlinePlayers()[0] : null;
            }
            if (nearestPlayer != null) {
                // TODO: This would be "the best" solution currently, but not possible atm (1.7.9).
                //bufferedSender = new BufferedPlayerSender(nearestPlayer);
                return new ProxyPlayer(nearestPlayer);
            }
        }
        return null;
    }

    public void removeView() {
        cancelTask();
        getView().removeView();
    }

    private void cancelTask() {
        if (pendingTask != null) {
            pendingTask.cancel();
            pendingTask = null;
        }
    }

    @Override
    public String toString() {
        return "Scoreboard{" +
                "id='" + id + '\'' +
                ", command='" + command + '\'' +
                ", location=" + location +
                ", refreshTicks=" + refreshTicks +
                ", view=" + view +
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
