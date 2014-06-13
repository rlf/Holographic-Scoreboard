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
        return s.replaceAll("\u00a7[0-9a-fk-or]", "");
    }

    public static String[] alignLeft2(String[] original) {
        String[] lines = new String[original.length];
        System.arraycopy(original, 0, lines, 0, original.length);
        double spaceWidth = getWidth("_");
        double dotWidth = getWidth(".");
        // First pass - get the pixel-width
        int maxWidth = 0;
        for (String line : lines) {
            int fontWidth = getWidth(line);
            if (fontWidth > maxWidth) {
                maxWidth = fontWidth;
            }
        }
        // Second pass, try to right-pad with spaces to make all lines app. equal width.
        for (int i = 0; i < lines.length; i++) {
            int fontWidth = getWidth(lines[i]);
            int numSpaces = (int) Math.floor((maxWidth - fontWidth) / (spaceWidth+1));
            int diff = numSpaces > 0 ? (int) (maxWidth - (fontWidth + numSpaces*spaceWidth + numSpaces-1)) : maxWidth - fontWidth;
            int numDots = (int) Math.floor(diff / (dotWidth + 1)); // floor?
            String padding = "";
            if (numSpaces > 0) {
                padding += String.format("%" + numSpaces + "s", "").replaceAll(" ", "_");
            }
            if (numDots > 0) {
                padding += String.format("%" + numDots + "s", "").replaceAll(" ", ".");
            }
            if (!padding.isEmpty()) {
                lines[i] = lines[i] + "\u00a78" + padding; // Dark-gray
            }
        }
        return lines;
    }

    public static String[] alignLeft(String[] original) {
        String[] lines = new String[original.length];
        System.arraycopy(original, 0, lines, 0, original.length);
        // First pass - get the pixel-width
        int maxWidth = 0;
        for (String line : lines) {
            int fontWidth = getWidth(line);
            if (fontWidth > maxWidth) {
                maxWidth = fontWidth;
            }
        }
        double spaceWidth = getWidth(" ");
        // Second pass, try to right-pad with spaces to make all lines app. equal width.
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int fontWidth = getWidth(line);
            int numSpaces = (int) Math.floor((maxWidth - fontWidth) / spaceWidth);
            if (numSpaces > 0) {
                lines[i] = line + String.format("%" + numSpaces + "s", "");
            }
        }
        return lines;
    }
}
