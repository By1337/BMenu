package org.by1337.bmenu.util;

public class StringUtil {
    public static boolean hasNoPlaceholders(String input) {
        return !input.contains("{") && !input.contains("%");
    }
}
