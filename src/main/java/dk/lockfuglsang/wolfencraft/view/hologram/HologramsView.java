package dk.lockfuglsang.wolfencraft.view.hologram;

import com.sainttx.holograms.data.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * View for the Holograms plugin
 */
public class HologramsView extends AbstractView {
    Hologram hologram;

    @Override
    public void updateView(Plugin plugin, String output) {
        Bukkit.getScheduler().callSyncMethod(plugin, new SyncCallable(plugin, getLines(output)));
    }

    @Override
    public void removeView() {
        if (hologram != null) {
            hologram.delete();
        }
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
            if (hologram == null) {
                hologram = new Hologram(getId(), location, false, lines);
            } else {
                hologram.clearLines();
                for (String line : lines) {
                    hologram.addLine(line);
                }
            }
            hologram.refreshAll();
            return null;
        }
    }
}
