package dk.lockfuglsang.wolfencraft.config;

import dk.lockfuglsang.wolfencraft.util.*;
import dk.lockfuglsang.wolfencraft.view.View;
import dk.lockfuglsang.wolfencraft.view.hologram.HolographicDisplaysView;
import dk.lockfuglsang.wolfencraft.view.hologram.ViewFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private List<String> filter;
    private List<Pattern> pattern;
    private List<String> replacement;

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
    public String getDelay() {
        return getTicksAsTime(delayTicks);
    }

    public String getCommand() {
        return command;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
        pattern = new ArrayList<>();
        replacement = new ArrayList<>();
        for (String f : filter) {
            String[] parts = f.split("=");
            pattern.add(Pattern.compile(parts[0]));
            if (parts.length == 2) {
                replacement.add(parts[1]);
            } else {
                replacement.add("");
            }
        }
    }
    public void setFilter(String filter) {
        List<String> list = new ArrayList<>();
        if (filter != null) {
            list.add(filter);
        }
        setFilter(list);
    }

    public void setDelay(String time) {
        delayTicks = TimeUtil.getTimeAsTicks(time);
    }

    public void setInterval(String time) {
        refreshTicks = TimeUtil.getTimeAsTicks(time);
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
            String output = ResourceManager.getRM().format("error.cmd.unabletoexecute", command);
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
        String filtered = output;
        if (pattern != null && output != null) {
            for (int i = 0; i < pattern.size(); i++) {
                Matcher m = pattern.get(i).matcher(filtered);
                filtered = m.replaceAll(replacement.get(i));
            }
        }
        getView().updateView(plugin, filtered);
    }

    private View getView() {
        if (view == null) {
            view = ViewFactory.createView(id);
            view.setLocation(location);
        }
        return view;
    }

    // TODO: Cleanup the propertychangelisteners? perhaps a leak?
    private BufferedSender createBufferedSender(final Plugin plugin) {
        if (sender == Sender.CONSOLE) {
            return new BufferedConsoleSender(Bukkit.getConsoleSender());
        } else {
            Player nearestPlayer = LocationUtil.getNearestPlayer(location);
            if (nearestPlayer == null) {
                nearestPlayer = !Bukkit.getOnlinePlayers().isEmpty() ? Bukkit.getOnlinePlayers().iterator().next() : null;
            }
            if (nearestPlayer != null) {
                return new BufferedPlayerSender(nearestPlayer);
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
