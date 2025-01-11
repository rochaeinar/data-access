package com.erc.dal.util;

import java.util.regex.Pattern;

public class StringUtil {
    public static boolean isNullOrEmpty(String text) {
        return (text == null || text.trim().equals("null") || text.trim().length() <= 0);
    }

    public static String format(String string, Object... params) {
        return String.format(string.replaceAll("\\{[0-9]\\}", "%s"), params);
    }

    public static String removeExtension(String text) {
        if (text.indexOf(".") > 0)
            text = text.substring(0, text.lastIndexOf("."));
        return text;
    }

    public static String getExtension(String text) {
        if (text.indexOf(".") > 0) {
            return text.substring(text.lastIndexOf(".") + 1);
        }
        return "";
    }

    /**
     * Replaces a pattern in the text without altering escape sequences or special characters.
     *
     * @param text        The original text.
     * @param pattern     The pattern to search for.
     * @param replacement The replacement text.
     * @return The resulting text after the replacement.
     */
    public static String replaceLiteral(String text, String pattern, String replacement) {
        // Escape the pattern to ensure it is treated literally.
        String quotedPattern = Pattern.quote(pattern);
        // Escape the replacement text to prevent it from being treated as a regex group reference.
        String quotedReplacement = java.util.regex.Matcher.quoteReplacement(replacement);
        // Perform the replacement.
        return text.replaceAll(quotedPattern, quotedReplacement);
    }
}