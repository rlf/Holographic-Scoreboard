package dk.lockfuglsang.wolfencraft.view;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 * The Scoreboard view
 */
public interface View {
    /**
     * Updates the view to display the output supplied.
     * @param plugin The current plugin
     * @param output The output to visualize
     */
    void updateView(Plugin plugin, String output);

    /**
     * Updates the location of the view
     * @param location Where the view should be displayed
     */
    void setLocation(Location location);

    /**
     * Removes the view
     */
    void removeView();
}
