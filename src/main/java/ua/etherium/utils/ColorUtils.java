package ua.etherium.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Translates a string using an alternate color code character '&' and hex codes '&#RRGGBB'.
     *
     * @param textToTranslate The string to translate.
     * @return The translated string.
     */
    public static String color(String textToTranslate) {
        if (textToTranslate == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer(textToTranslate.length() + 4 * 8);

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + group).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}