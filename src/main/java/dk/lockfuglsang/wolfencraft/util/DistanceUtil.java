package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Convenience utility for measuring distances
 */
public enum DistanceUtil {
    ;
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
