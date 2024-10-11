package me.msicraft.hardcoresurvival.Command;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final HardcoreSurvival plugin;

    public MainCommand(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    private void sendPermissionMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "관리자만 사용가능한 명령어입니다.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equals("hardcoresurvival")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "/hardcoresurvival help");
                return true;
            }
            String var = args[0];
            if (var != null) {
                switch (var) {
                    case "reload" -> {
                        if (sender.isOp()) {
                            plugin.reloadVariables();
                            sender.sendMessage(ChatColor.GREEN + "플러그인 구성이 리로드되었습니다.");
                            return true;
                        } else {
                            sendPermissionMessage(sender);
                            return false;
                        }
                    }
                    case "deathpenalty" -> {
                        if (sender.isOp()) {
                            if (sender instanceof Player player) {
                                Location location = player.getLocation();
                                String format = plugin.getDeathPenaltyManager().locationToFormat(location);
                                plugin.getConfig().set("Setting.DeathPenalty.SpawnLocation", location);
                                plugin.saveConfig();
                                player.sendMessage(ChatColor.GREEN + "DeathPenalty 스폰위치가 저장되었습니다");
                                player.sendMessage(ChatColor.GREEN + "위치: " + format);
                                plugin.getDeathPenaltyManager().setSpawnLocation(location);
                                return true;
                            }
                        } else {
                            sendPermissionMessage(sender);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

}
