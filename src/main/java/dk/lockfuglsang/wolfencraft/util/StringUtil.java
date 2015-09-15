package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.map.MinecraftFont;

/**
 * String utilities concerning formatting in MapFont.
 */
public enum StringUtil {
    ;

    public static int getWidth(String s) {
        if (s == null) {
            return 0;
        }
        String stripped = stripFormatting(s);
        int fontWidth = MinecraftFont.Font.getWidth(stripped);
        return fontWidth + stripped.length() - 1; // Padding between letters
    }

    public static String stripFormatting(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("(\u00a7|&)[0-9a-fk-or]", "");
    }
}
