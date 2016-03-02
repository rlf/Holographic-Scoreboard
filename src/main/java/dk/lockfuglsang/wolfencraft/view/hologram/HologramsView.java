package dk.lockfuglsang.wolfencraft.view.hologram;

import com.sainttx.holograms.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * View for the Holograms plugin
 */
public class HologramsView extends AbstractView {
    Hologram hologram;

    @Override
    public void updateView(Plugin plugin, String output) {
        Bukkit.getScheduler().callSyncMethod(plugin, new SyncCallable(getLines(output)));
    }

    @Override
    public void removeView() {
        if (hologram != null) {
            hologram.despawn();
        }
    }

    @Override
    public void setLocation(Location location) {
        super.setLocation(location);
        if (hologram != null) {
            hologram.teleport(location);
        }
    }

    private class SyncCallable implements Callable<Void> {
        private final String[] lines;

        public SyncCallable(String[] lines) {
            this.lines = lines;
        }

        @Override
        public Void call() throws Exception {
            if (hologram != null) {
                hologram.despawn();
            }
            hologram = new Hologram(getId(), location, false, lines);
            hologram.refresh();
            return null;
        }
    }
}
