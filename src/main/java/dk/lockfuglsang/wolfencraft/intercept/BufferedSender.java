package dk.lockfuglsang.wolfencraft.intercept;

import org.bukkit.command.CommandSender;

import java.beans.PropertyChangeListener;

/**
 * Common interface for the interceptors
 */
public interface BufferedSender {
    String getStdout();
    CommandSender getSender();
}
