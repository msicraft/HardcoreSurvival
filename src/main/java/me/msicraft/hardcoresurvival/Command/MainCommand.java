package me.msicraft.hardcoresurvival.Command;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
            try {
                switch (var) {
                    case "test" -> {
                        if (sender instanceof Player player) {
                            ItemStack handStack = player.getInventory().getItemInMainHand();
                            player.sendMessage("test: " + handStack.isSimilar(MythicBukkit.inst().getItemManager().getItemStack("TestIteM")));
                        }
                    }
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
                        String var2 = args[1];
                        try {
                            switch (var2) {
                                case "setspawn" -> { //hs deathpenalty setspawn
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
                                case "chestlog" -> { //hs deathpenalty chestlog [get, log-to-ItemBox] <player>
                                    String var3 = args[2];
                                    switch (var3) {
                                        case "get" -> {
                                            Player target = Bukkit.getPlayer(args[3]);
                                            if (target == null) {
                                                if (sender instanceof Player senderP) {
                                                    target = senderP;
                                                } else {
                                                    sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
                                                    return false;
                                                }
                                            }
                                            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(target);
                                            DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
                                            DeathPenaltyChestLog deathPenaltyChestLog = playerData.getDeathPenaltyChestLog();
                                            sender.sendMessage(ChatColor.GREEN + "Player: " + target.getName());
                                            deathPenaltyChestLog.getChestLocationList().forEach(location -> {
                                                String locationToFormat = deathPenaltyManager.locationToFormat(location);
                                                sender.sendMessage(ChatColor.GRAY + "Location: " + locationToFormat);
                                            });
                                            return true;
                                        }
                                        case "log-to-ItemBox" -> {
                                            DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
                                            String targetS = args[3];
                                            if (targetS.equalsIgnoreCase("all-players")) {
                                                plugin.getPlayerDataManager().getPlayerFileNames().forEach(uuidS -> {
                                                    UUID uuid = UUID.fromString(uuidS);
                                                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                                    OfflinePlayerData offlinePlayerData = new OfflinePlayerData(offlinePlayer);
                                                    offlinePlayerData.loadData();
                                                    deathPenaltyManager.sendChestLogToItemBox(offlinePlayerData);
                                                    offlinePlayerData.saveData();
                                                });
                                            } else {
                                                UUID uuid = UUID.fromString(targetS);
                                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                                OfflinePlayerData offlinePlayerData = new OfflinePlayerData(offlinePlayer);
                                                offlinePlayerData.loadData();
                                                deathPenaltyManager.sendChestLogToItemBox(offlinePlayerData);
                                                offlinePlayerData.saveData();
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            sender.sendMessage(ChatColor.RED + "/hardcoresurvival deathpenalty [setspawn, chestlog]");
                            return false;
                        }

                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage(ChatColor.RED + "/hardcoresurvival");
            }
        }
        return false;
    }

}
