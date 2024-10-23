package me.msicraft.hardcoresurvival.Utils;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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

    public static void sendPlayerDataLoadingMessage(CommandSender sender) {
        if (sender instanceof Player player) {
            player.sendMessage(ChatColor.RED + "데이터 로딩 중입니다. 행동에 제약이 걸릴 수 있습니다");
        } else {
            sender.sendMessage(ChatColor.RED + "해당 플레이어의 데이터를 로딩중입니다");
        }
    }

}
