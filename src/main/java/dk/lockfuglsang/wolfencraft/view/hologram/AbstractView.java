package dk.lockfuglsang.wolfencraft.view.hologram;

import dk.lockfuglsang.wolfencraft.view.View;
import org.bukkit.Location;

/**
 * Common logic for views.
 */
public abstract class AbstractView implements View {
    protected Location location;
    private String id;

    protected String[] getLines(String output) {
        String[] lines = output.trim().replaceAll("\r", "").split("\n");
        //return StringUtil.alignLeft2(lines);
        return lines;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
