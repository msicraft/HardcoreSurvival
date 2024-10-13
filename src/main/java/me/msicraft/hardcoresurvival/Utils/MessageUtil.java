package me.msicraft.hardcoresurvival.Utils;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private MessageUtil() {}

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static void sendDebugMessage(String prefix, String... messages) {
        String pluginPrefix = HardcoreSurvival.PREFIX;
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.YELLOW + "=====" + prefix + "=====");
        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.YELLOW + message);
        }
    }

    public static String translateColorCodes(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String c = message.substring(matcher.start(), matcher.end());
            message = message.replace(c, net.md_5.bungee.api.ChatColor.of(c) + "");
            matcher = HEX_PATTERN.matcher(message);
        }
        return message;
    }

}
