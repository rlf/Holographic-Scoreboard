package dk.lockfuglsang.wolfencraft.view.hologram;

import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import dk.lockfuglsang.wolfencraft.view.View;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * TODO: Rasmus javadoc
 */
public class HologramView implements View {
    private Location location;
    private volatile Hologram hologram;

    @Override
    public void updateView(Plugin plugin, String output) {
        output = output.trim().replaceAll("\r", "");
        final String[] lines = getLines(output);
        Bukkit.getScheduler().callSyncMethod(plugin, new SyncCallable(plugin, lines));
    }

    private String[] getLines(String output) {
        String[] lines = output.split("\n");
        //return StringUtil.alignLeft2(lines);
        return lines;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public synchronized void removeView() {
        if (hologram != null && !hologram.isDeleted()) {
            hologram.delete();
            hologram = null;
        }
    }

    @Override
    public String toString() {
        return "HologramView{" +
                "location=" + location +
                ", hologram=" + hologram +
                '}';
    }

    private class SyncCallable implements Callable<Void> {
        private final Plugin plugin;
        private final String[] lines;

        public SyncCallable(Plugin plugin, String[] lines) {
            this.plugin = plugin;
            this.lines = lines;
        }

        @Override
        public Void call() throws Exception {
            synchronized (HologramView.this) {
                if (hologram != null) {
                    hologram.clearLines();
                    hologram.delete();
                    hologram = null;
                }
                hologram = HolographicDisplaysAPI.createHologram(plugin, location, lines);
            }
            return null;
        }
    }
}
