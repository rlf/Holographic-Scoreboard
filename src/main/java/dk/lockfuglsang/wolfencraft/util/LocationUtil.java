package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Utility for manipulating locations.
 */
public enum LocationUtil {
    ;

    /**
     * Converts a string on the format: <code>world:x,y,z</code> to a location.
     *
     * @param locAsString
     * @return
     */
    public static Location getLocation(String locAsString) {
        String[] p1 = locAsString != null ? locAsString.split(":") : null;
        if (p1 != null && p1.length == 2) {
            try {
                World world = Bukkit.getWorld(p1[0]);
                String[] p2 = p1[1].split(",");
                if (p2.length == 3 && world != null) {
                    double x = Double.parseDouble(p2[0]);
                    double y = Double.parseDouble(p2[1]);
                    double z = Double.parseDouble(p2[2]);
                    return new Location(world, x, y, z);
                }
            } catch (NumberFormatException | NullPointerException e) {
                // ignore
            }
        }
        return null;
    }

    public static Player getNearestPlayer(Location location) {
        double minDist = Double.MAX_VALUE;
        Player closest = null;
        String locationWorld = location.getWorld().getName();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(locationWorld)) {
                try {
                    double distanceSq = player.getLocation().distance(location);
                    if (distanceSq < minDist) {
                        minDist = distanceSq;
                        closest = player;
                    }
                } catch (IllegalArgumentException ignored) {
                    // Do nothing - they are not closest!
                }
            }
        }
        return closest;
    }
}
