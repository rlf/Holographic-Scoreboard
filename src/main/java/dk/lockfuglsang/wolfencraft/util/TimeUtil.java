package dk.lockfuglsang.wolfencraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple conversions between ticks and string denoting intervals (i.e. 3m, 15s, 5d3h45m14s).
 */
public enum TimeUtil {
    ;
    public static final Pattern TIME_PATTERN = Pattern.compile("(\\d+d)?\\s*(\\d+h)?\\s*(\\d+m)?\\s*(\\d+s)?");

    public static String getTicksAsTime(int ticks) {
        int secs = ticks / 20;
        int mins = secs / 60;
        int hours = mins / 60;
        int days = hours / 24;
        hours = hours % 24;
        mins = mins % 60;
        secs = secs % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
        }
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (mins > 0) {
            sb.append(mins).append("m");
        }
        if (secs > 0) {
            sb.append(secs).append("s");
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    public static int getTimeAsTicks(String timeString) {
        if (timeString != null) {
            Matcher matcher = TIME_PATTERN.matcher(timeString);
            if (matcher.matches()) {
                int days = getInt(matcher.group(1));
                int hours = getInt(matcher.group(2));
                int mins = getInt(matcher.group(3));
                int secs = getInt(matcher.group(4));
                return ((((days*24 + hours)*60) + mins)*60 + secs) * 20;
            }
        }
        return 0;
    }

    private static int getInt(String group) {
        return group == null || group.isEmpty() ? 0 : Integer.parseInt(group.substring(0, group.length()-1));
    }
}
