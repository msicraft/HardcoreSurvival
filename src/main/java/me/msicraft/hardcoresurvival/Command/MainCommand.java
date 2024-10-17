package me.msicraft.hardcoresurvival.Command;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer("msicraftz");
                        System.out.println("Name: " + offlinePlayer.getName());
                        System.out.println("UUID: " + offlinePlayer.getUniqueId().toString());
                        OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer("da1d9vxllqqf");
                        System.out.println("Name: " + offlinePlayer1.getName());
                        System.out.println("UUId: "+ offlinePlayer1.getUniqueId());
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
                    case "whitelist" -> { //hs whitelist [add, remove] <target>
                        if (!sender.isOp()) {
                            return false;
                        }
                        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
                        switch (args[1]) {
                            case "add" -> {
                                String targetName = args[2];
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
                                UUID targetUniqueId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
                                sender.sendMessage(ChatColor.GREEN + targetName + "을 whitelist에 추가하였습니다.");
                            }
                            case "remove" -> {
                                String targetName = args[2];
                                UUID targetUniqueId = Bukkit.getOfflinePlayer(targetName).getUniqueId();
                                //plugin.getWhitelistManager().removePlayer(targetUniqueId);
                                sender.sendMessage(ChatColor.GREEN + targetName + "을 whitelist에서 제거하였습니다.");
                            }
                            default -> {
                                sender.sendMessage(ChatColor.RED + "/hardcoresurvival whitelist [add, remove] <target>");
                            }
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
                    case "gui" -> { //hs gui <guiType> <target>
                        GuiType guiType = GuiType.valueOf(args[1]);
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            if (sender instanceof Player p) {
                                target = p;
                            } else {
                                sender.sendMessage(ChatColor.RED + "플레이어를 찾을 수 없습니다.");
                                return false;
                            }
                        }
                        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(target);
                        switch (guiType) {
                            case MAIN -> {
                                CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                                if (customGui instanceof MenuGui menuGui) {
                                    target.openInventory(menuGui.getInventory());
                                }
                            }
                            case ITEM_BOX -> {
                                plugin.getItemBoxManager().openItemBox(playerData);
                            }
                            case SHOP -> {
                                plugin.getShopManager().openShopInventory(target, ShopGui.Type.BUY);
                            }
                        }
                        return true;
                    }
                    case "shop" -> { //hs shop [register, unregister, open] <id> <itemType> <basePrice>
                        if (!sender.isOp()) {
                            return false;
                        }
                        String var2 = args[1];
                        try {
                            ShopManager shopManager = plugin.getShopManager();
                            switch (var2) {
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
