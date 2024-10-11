package me.msicraft.hardcoresurvival.Utils;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MessageUtil {

    private MessageUtil() {}

    public static void sendDebugMessage(String prefix, String... messages) {
        String pluginPrefix = HardcoreSurvival.PREFIX;
        Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.YELLOW + "=====" + prefix + "=====");
        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(pluginPrefix + ChatColor.YELLOW + message);
        }
    }

}
