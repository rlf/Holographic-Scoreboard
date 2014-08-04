package dk.lockfuglsang.wolfencraft.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Shared resource-bundle across classes.
 */
public enum ResourceManager {INSTANCE;
    private ResourceBundle rm = ResourceBundle.getBundle("messages");

    public static ResourceManager getRM() {
        return INSTANCE;
    }

    public String format(String key, Object... args) {
        String format = rm.getString(key);
        return MessageFormat.format(format, args);
    }
}
