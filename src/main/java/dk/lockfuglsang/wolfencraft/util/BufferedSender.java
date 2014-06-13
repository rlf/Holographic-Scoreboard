package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.command.CommandSender;

import java.beans.PropertyChangeListener;

/**
 * Common interface for the interceptors
 */
public interface BufferedSender {
    void addPropertyChangeListener(PropertyChangeListener listener);
    String getStdout();
    CommandSender getSender();
    void clear();
}
