package dk.lockfuglsang.wolfencraft.view.hologram;

import dk.lockfuglsang.wolfencraft.view.View;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Factory for creating views for multiple plugins.
 */
public class ViewFactory {
    private static final View NULL_VIEW = new AbstractView() {
        @Override
        public void updateView(Plugin plugin, String output) {

        }

        @Override
        public void setLocation(Location location) {

        }

        @Override
        public void removeView() {

        }
    };
    public static View createView(String id) {
        if (Bukkit.getPluginManager().isPluginEnabled("Holograms")) {
            return createViewImpl("dk.lockfuglsang.wolfencraft.view.hologram.HologramsView", id);
        } else if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            return createViewImpl("dk.lockfuglsang.wolfencraft.view.hologram.HolographicDisplaysView", id);
        }
        return NULL_VIEW;
    }

    public static View createViewImpl(String className, String id) {
        try {
            Class<? extends View> viewClass = null;
            viewClass = (Class<? extends View>) Class.forName(className);
            View view = viewClass.newInstance();
            view.setId(id);
            return view;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.WARNING, "Unable to instantiate view: " + className, e);
            return NULL_VIEW;
        }
    }
}
