package dk.lockfuglsang.wolfencraft.view.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import dk.lockfuglsang.wolfencraft.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * View for HolographicDisplays
 */
public class HolographicDisplaysView extends AbstractView {
    private volatile Hologram hologram;

    @Override
    public void updateView(Plugin plugin, String output) {
        final String[] lines = getLines(output);
        Bukkit.getScheduler().callSyncMethod(plugin, new SyncCallable(plugin, lines));
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
            synchronized (HolographicDisplaysView.this) {
                if (hologram != null) {
                    hologram.clearLines();
                    hologram.delete();
                    hologram = null;
                }
                hologram = HologramsAPI.createHologram(plugin, location);
                for (String line : lines) {
                    if (line != null && !StringUtil.stripFormatting(line.trim()).isEmpty()) {
                        hologram.appendTextLine(line);
                    }
                }
            }
            return null;
        }
    }
}
