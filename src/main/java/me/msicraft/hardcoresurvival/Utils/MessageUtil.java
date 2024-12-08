package me.msicraft.hardcoresurvival.Utils;

import me.msicraft.hardcoresurvival.Data.MessageDataFile;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private MessageUtil() {}

    private static MessageDataFile messageDataFile = null;
    private static final Map<String, String> messageMap = new HashMap<>();

    public static void reloadVariables(HardcoreSurvival plugin) {
        if (messageDataFile == null) {
            messageDataFile = new MessageDataFile(plugin);
        }
        messageDataFile.reloadConfig();

        messageMap.clear();
        FileConfiguration config = messageDataFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("Messages");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                String path = "Messages." + key;
                messageMap.put(key, config.getString(path, "[Error None Message]"));
            }
        }
    }

    public static String getMessage(String key, boolean useColorCode) {
        String message = messageMap.getOrDefault(key, "[Error None Message]");
        if (useColorCode) {
            return translateColorCodes(message);
        }
        return message;
    }

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
