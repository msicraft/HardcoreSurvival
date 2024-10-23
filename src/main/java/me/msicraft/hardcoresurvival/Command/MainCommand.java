package me.msicraft.hardcoresurvival.Command;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.ItemBoxManager;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
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
                        int max = Integer.parseInt(args[1]);
                        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(((Player) sender).getUniqueId());
                        for (int i = 0; i < max; i++) {
                            Location location = new Location(Bukkit.getWorld("world"), i, 15, 0);
                            playerData.getDeathPenaltyChestLog().addLocation(location);
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
                    case "info" -> {
                        if (!sender.isOp()) {
                            return false;
                        }
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다");
                            return false;
                        }
                        sender.sendMessage("Player: " + player.getName());
                        sender.sendMessage("UUID: " + player.getUniqueId());
                        return true;
                    }
                    case "streamer" -> { //hs streamer [add, remove, list] <target> //스트리머 용
                        if (!sender.isOp()) {
                            return false;
                        }
                        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
                        switch (args[1]) {
                            case "add" -> {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UUID uuid = offlinePlayer.getUniqueId();
                                if (playerDataManager.isStreamer(uuid)) {
                                    sender.sendMessage(ChatColor.RED + "해당 플레이어는 이미 스트리머로 등록되어있습니다 -> " + offlinePlayer.getName());
                                    return false;
                                }
                                playerDataManager.addWhiteList(uuid);
                                playerDataManager.addStreamer(uuid);
                                sender.sendMessage(ChatColor.GREEN + "해당 플레이어가 스트리머로 등록되었습니다");
                                sender.sendMessage(ChatColor.GREEN + "Player: " + offlinePlayer.getName());
                                sender.sendMessage(ChatColor.GREEN + "UUID: " + uuid.toString());
                                OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                offlinePlayerData.loadData();
                                offlinePlayerData.setGuildUUID(offlinePlayer.getUniqueId());
                                offlinePlayerData.saveData();

                                Guild guild = new Guild(uuid, offlinePlayerData);
                                guild.addMember(uuid);
                                plugin.getGuildManager().registerGuild(uuid, guild);
                                return true;
                            }
                            case "remove" -> {
                                UUID uuid = UUID.fromString(args[2]);
                                if (!playerDataManager.isStreamer(uuid)) {
                                    sender.sendMessage(ChatColor.RED + "해당 플레이어는 스트리머에 등록되어있지 않습니다");
                                    return false;
                                }
                                playerDataManager.removeWhiteList(uuid);
                                playerDataManager.removeStreamer(uuid);
                                sender.sendMessage(ChatColor.GREEN + "해당 플레이어가 스트리머에서 제거되었습니다");
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                if (offlinePlayer.isOnline()) {
                                    offlinePlayer.getPlayer().kick(Component.text(ChatColor.RED + "접속권한이 제거되었습니다"));
                                }
                                OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                offlinePlayerData.loadData();
                                offlinePlayerData.setGuildUUID(null);
                                offlinePlayerData.saveData();
                                Guild guild = plugin.getGuildManager().getGuild(uuid);
                                guild.getMembers().forEach(memberUUID -> {
                                    OfflinePlayer memberO = Bukkit.getOfflinePlayer(memberUUID);
                                    if (memberO.isOnline()) {
                                        memberO.getPlayer().kick(Component.text(ChatColor.RED + "스트리머의 접속권한이 제거되어 관련 플레이어의 접속권한도 제거되었습니다"));
                                    }
                                    OfflinePlayerData memberData = new OfflinePlayerData(memberUUID);
                                    memberData.loadData();
                                    memberData.setGuildUUID(null);
                                    memberData.saveData();
                                });
                                plugin.getGuildManager().removeGuild(uuid);
                                return true;
                            }
                            case "list" -> {
                                sender.sendMessage("Streamer List: ");
                                playerDataManager.getStreamerList().forEach(uuid -> {
                                    sender.sendMessage("Player: " + Bukkit.getOfflinePlayer(uuid).getName());
                                    sender.sendMessage("UUID: " + uuid.toString());
                                });
                                return true;
                            }
                            default -> {}
                        }
                    }
                    case "customitem" -> { //hs customitem <internalName> <target> <amount>
                        if (!sender.isOp()) {
                            return false;
                        }
                        CustomItemManager customItemManager = plugin.getCustomItemManager();
                        String internalName = args[1];
                        CustomItem customItem = customItemManager.getCustomItem(internalName);
                        if (customItem == null) {
                            sender.sendMessage(ChatColor.RED + "잘못된 내부이름입니다");
                            return false;
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            if (sender instanceof Player p) {
                                target = p;
                            } else {
                                sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
                                return false;
                            }
                        }
                        int amount = Integer.parseInt(args[3]);
                        ItemStack itemStack = customItem.getItemStack();
                        for (int i = 0; i<amount; i++) {
                            target.getInventory().addItem(itemStack);
                        }
                    }
                    case "shop" -> { //hs shop [register, unregister, setcenter] <id> <itemType> <basePrice>
                        if (!sender.isOp()) {
                            return false;
                        }
                        String var2 = args[1];
                        try {
                            ShopManager shopManager = plugin.getShopManager();
                            switch (var2) {
                                case "setcenter" -> {
                                    if (sender instanceof Player player) {
                                        Location location = player.getLocation();
                                        shopManager.getShopRegion().update(location, shopManager.getShopRegionRadius());
                                        shopManager.getShopDataFile().getConfig().set("Setting.CenterLocation", location);
                                        shopManager.getShopDataFile().saveConfig();
                                    }
                                }
                                case "register" -> {
                                    if (sender instanceof Player player) {
                                        ItemStack itemStack = player.getInventory().getItemInMainHand();
                                        if (itemStack == null || itemStack.getType() == Material.AIR) {
                                            player.sendMessage(ChatColor.RED + "공기는 등록할 수 없습니다");
                                            return false;
                                        }
                                        String internalName = args[2];
                                        if (shopManager.hasInternalName(internalName)) {
                                            player.sendMessage(ChatColor.RED + "이미 존재하는 ID 입니다");
                                            return false;
                                        }
                                        ShopItem.ItemType itemType = ShopItem.ItemType.valueOf(args[3].toUpperCase());
                                        int basePrice = Integer.parseInt(args[4]);
                                        ShopItem shopItem = new ShopItem(itemType, false, false, itemStack, internalName,
                                                0, basePrice, basePrice);
                                        shopManager.registerShopItem(shopItem);
                                        String path = "Items." + internalName;
                                        FileConfiguration config = shopManager.getShopDataFile().getConfig();
                                        config.set(path + ".ItemType", shopItem.getItemType().name());
                                        config.set(path + ".UseStaticPrice", shopItem.isUseStaticPrice());
                                        config.set(path + ".UnlimitStock", shopItem.isUnlimitStock());
                                        config.set(path + ".BasePrice", shopItem.getBasePrice());
                                        config.set(path + ".Price", shopItem.getPrice(false));
                                        config.set(path + ".Stock", shopItem.getStock());
                                        switch (shopItem.getItemType()) {
                                            case VANILLA -> {
                                                config.set(path + ".ItemStack", itemStack);
                                            }
                                            case MYTHICMOBS -> {
                                                String sa = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemStack);
                                                config.set(path + ".InternalName", sa);
                                            }
                                            case CUSTOM_ITEM -> {
                                                String sa = plugin.getCustomItemManager().getCustomItemInternalName(itemStack);
                                                config.set(path + ".InternalName", sa);
                                            }
                                        }
                                        shopManager.getShopDataFile().saveConfig();

                                        player.sendMessage(ChatColor.GREEN + "아이템이 등록되었습니다");
                                    }
                                    return true;
                                }
                                case "unregister" -> {
                                    String internalName = args[2];
                                    if (!shopManager.hasInternalName(internalName)) {
                                        sender.sendMessage(ChatColor.RED + "존재하지 않는 ID 입니다");
                                        return false;
                                    }
                                    shopManager.unregisterShopItem(internalName);
                                    String path = "Items." + internalName;
                                    shopManager.getShopDataFile().getConfig().set(path, null);
                                    shopManager.getShopDataFile().saveConfig();
                                    sender.sendMessage(ChatColor.GREEN + "아이템이 제거되었습니다");
                                    return true;
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            sender.sendMessage(ChatColor.RED + "/hardcoresurvival shop [register, unregister] <id> <itemType> <basePrice>");
                            return false;
                        }
                    }
                    case "itembox" -> { //hs itembox give <target>
                       if (!sender.isOp()) {
                           return false;
                       }
                       if (sender instanceof Player player) {
                           ItemStack itemStack = player.getInventory().getItemInMainHand();
                           if (itemStack == null || itemStack.getType() == Material.AIR) {
                               return false;
                           }
                           ItemBoxManager itemBoxManager = plugin.getItemBoxManager();
                           switch (args[1]) {
                               case "give" -> {
                                   String target = args[2];
                                   if (target.equalsIgnoreCase("all-players")) {
                                       List<String> list = plugin.getPlayerDataManager().getPlayerFileNames();
                                       for (String uuidS : list) {
                                           UUID uuid = UUID.fromString(uuidS);
                                           OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                           if (offlinePlayer.isOnline()) {
                                               PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                               itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]");
                                           } else {
                                               OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                               offlinePlayerData.loadData();
                                               itemBoxManager.sendItemStackToItemBox(offlinePlayerData, itemStack, "[시스템]");
                                               offlinePlayerData.saveData();
                                           }
                                       }
                                       sender.sendMessage(ChatColor.GREEN + "총 " + list.size() + " 명의 플레이어에게 아이템을 보냈습니다");
                                   } else {
                                       UUID uuid = UUID.fromString(target);
                                       OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                       if (offlinePlayer.isOnline()) {
                                           PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                           itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]");
                                       } else {
                                           OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                           offlinePlayerData.loadData();
                                           itemBoxManager.sendItemStackToItemBox(offlinePlayerData, itemStack, "[시스템]");
                                           offlinePlayerData.saveData();
                                       }
                                       sender.sendMessage(ChatColor.GREEN + "해당 플레이어에게 아이템을 보냈습니다");
                                       sender.sendMessage(ChatColor.GREEN + "Player: " + offlinePlayer.getName());
                                   }
                               }
                               default -> {
                               }
                           }
                       }
                    }
                    case "deathpenalty" -> {
                        if (!sender.isOp()) {
                            return false;
                        }
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
                                                    if (offlinePlayer.isOnline()) {
                                                        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                                        deathPenaltyManager.sendChestLogToItemBox(playerData);
                                                    } else {
                                                        OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                                        offlinePlayerData.loadData();
                                                        deathPenaltyManager.sendChestLogToItemBox(offlinePlayerData);
                                                        offlinePlayerData.saveData();
                                                    }
                                                });
                                            } else {
                                                UUID uuid = UUID.fromString(targetS);
                                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                                if (offlinePlayer.isOnline()) {
                                                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                                    deathPenaltyManager.sendChestLogToItemBox(playerData);
                                                } else {
                                                    OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                                    offlinePlayerData.loadData();
                                                    deathPenaltyManager.sendChestLogToItemBox(offlinePlayerData);
                                                    offlinePlayerData.saveData();
                                                }
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
