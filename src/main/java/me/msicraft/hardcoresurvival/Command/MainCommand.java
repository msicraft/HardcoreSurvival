package me.msicraft.hardcoresurvival.Command;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.th0rgal.oraxen.api.OraxenItems;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.Data.GuildSpawnLocation;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.ItemBoxManager;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
                        if (!sender.isOp()) {
                            return false;
                        }
                        if (sender instanceof Player player) {
                            Location location = player.getLocation();
                            Chunk chunk = location.getChunk();
                            PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                            if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                dataContainer.remove(GuildRegion.GUILD_REGION_KEY);
                            }
                        }
                    }
                    case "debug" -> { //hs debug []
                        if (!sender.isOp()) {
                            return false;
                        }
                        switch (args[1]) {
                            case "change-nickname" -> {
                                Player player = Bukkit.getPlayer(args[2]);
                                if (player != null) {
                                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

                                    int max = args.length;
                                    StringBuilder nickName = new StringBuilder();
                                    for (int i = 3; i < max; i++) {
                                        nickName.append(args[i]).append(" ");
                                    }
                                    playerData.setData("NickName", nickName.toString());
                                    sender.sendMessage(ChatColor.GREEN + "변경된 닉네임: " + nickName.toString());
                                }
                            }
                            case "set-guild-spawnlocation" -> {
                                if (sender instanceof Player player) {
                                    UUID uuid = UUID.fromString(args[2]);
                                    GuildManager guildManager = plugin.getGuildManager();
                                    Guild guild = guildManager.getGuild(uuid);
                                    if (guild == null) {
                                        sender.sendMessage(ChatColor.RED + "존재하지 않는 길드입니다");
                                        return false;
                                    }
                                    GuildSpawnLocation guildSpawnLocation = guild.getGuildRegion().getGuildSpawnLocation();
                                    guildSpawnLocation.setGuildSpawnLocation(player.getLocation());
                                }
                            }
                            case "region" -> {
                                switch (args[2]) {
                                    case "info" -> {
                                        if (sender instanceof Player player) {
                                            Location location = player.getLocation();
                                            Chunk chunk = location.getChunk();
                                            PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                                            if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                                String guildUUID = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
                                                if (guildUUID == null) {
                                                    player.sendMessage(ChatColor.RED + "GuildUUID null");
                                                    return false;
                                                }
                                                Guild guild = plugin.getGuildManager().getGuild(UUID.fromString(guildUUID));
                                                player.sendMessage(ChatColor.GREEN + "Guild UUID - " + guildUUID);
                                                player.sendMessage(ChatColor.GREEN + "Guild Region Count: " + guild.getGuildRegion().getBuyRegionCount());
                                            } else {
                                                player.sendMessage(ChatColor.GREEN + "Unknown Guild UUID");
                                            }
                                        }
                                        return true;
                                    }
                                    case "remove" -> {
                                        if (sender instanceof Player player) {
                                            Location location = player.getLocation();
                                            Chunk chunk = location.getChunk();
                                            PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                                            if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                                String guildUUID = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
                                                if (guildUUID == null) {
                                                    player.sendMessage(ChatColor.RED + "GuildUUID null");
                                                    return false;
                                                }
                                                Guild guild = plugin.getGuildManager().getGuild(UUID.fromString(guildUUID));
                                                plugin.getGuildManager().removeRegion(guild, location);

                                                player.sendMessage(ChatColor.GREEN + "해당 지역을 길드로부터 제거하였습니다");
                                            }
                                        }
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    case "broadcast" -> {
                        if (!sender.isOp()) {
                            return false;
                        }
                        int max = args.length;
                        StringBuilder a = new StringBuilder(ChatColor.BOLD + "" + ChatColor.GOLD + "[공지] ");
                        for (int i = 1; i < max; i++) {
                            a.append(args[i]).append(" ");
                        }
                        String message = a.toString();
                        Bukkit.broadcast(Component.text(MessageUtil.translateColorCodes(message)));
                        return true;
                    }
                    case "set-maintenance" -> {
                        if (!sender.isOp()) {
                            return false;
                        }
                        if (plugin.isMaintenance()) {
                            plugin.setMaintenance(false);
                            sender.sendMessage(ChatColor.RED + "Disable Maintenance");
                            return true;
                        } else {
                            plugin.setMaintenance(true);
                            sender.sendMessage(ChatColor.RED + "Enable Maintenance");
                            return true;
                        }
                    }
                    case "maintenance" -> { //hs maintenance <message>
                        if (!sender.isOp()) {
                            return false;
                        }
                        int max = args.length;
                        StringBuilder a = new StringBuilder(ChatColor.BOLD + "" + ChatColor.RED + "[점검] ");
                        for (int i = 1; i < max; i++) {
                            a.append(args[i]).append(" ");
                        }
                        String message = a.toString();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.isOp()) {
                                continue;
                            }
                            player.kick(Component.text(message));
                        }
                        plugin.setMaintenance(true);
                        return true;
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
                    case "save-data" -> { //hs save-data [shop]
                        if (!sender.isOp()) {
                            return false;
                        }
                        switch (args[1]) {
                            case "shop" -> {
                                plugin.getShopManager().saveShopData();
                            }
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
                        UUID uuid = player.getUniqueId();
                        sender.sendMessage("Player: " + player.getName());
                        TextComponent textComponent = Component.text("UUID: " + uuid).
                                clickEvent(ClickEvent.suggestCommand(uuid.toString()));
                        sender.sendMessage(textComponent);
                        return true;
                    }
                    case "playerdata" -> { //hs playerdata <online_player> [get, edit] [variable]
                        if (!sender.isOp()) {
                            return false;
                        }
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

                                Guild guild = new Guild(uuid, offlinePlayerData.getPlayerDataFile());
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
                                    playerDataManager.removeWhiteList(memberUUID);
                                });
                                guild.getGuildRegion().getGuildChunks().forEach(guildChunk -> {
                                    Location location = plugin.getWorldManager().getCenterChunkLocation(guildChunk.getWorldName(),
                                            guildChunk.getChunkPair().getV1(), guildChunk.getChunkPair().getV2());
                                    Chunk chunk = location.getChunk();
                                    PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                                    if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                        dataContainer.remove(GuildRegion.GUILD_REGION_KEY);
                                    }
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
                                case "reset-price" -> { //hs shop reset-price
                                    List<String> internalNameList = shopManager.getInternalNameList();
                                    for (String internalName : internalNameList) {
                                        ShopItem shopItem = shopManager.getShopItem(internalName);
                                        if (shopItem != null) {
                                            shopItem.resetPrice();
                                        }
                                    }
                                }
                                case "price-update" -> { //hs shop price-update
                                    List<String> internalNameList = shopManager.getInternalNameList();
                                    for (String internalName : internalNameList) {
                                        ShopItem shopItem = shopManager.getShopItem(internalName);
                                        if (shopItem != null) {
                                            shopItem.updatePrice(shopManager);
                                        }
                                    }
                                }
                                case "edit" -> { //hs shop edit <internal> <edit_var> <value>
                                    String internalName = args[2];
                                    if (!shopManager.hasInternalName(internalName)) {
                                        sender.sendMessage(ChatColor.RED + "존재하지 않는 내부이름입니다.");
                                        return false;
                                    }
                                    String editVar = args[3];
                                    ShopItem shopItem = shopManager.getShopItem(internalName);
                                    if (shopItem == null) {
                                        sender.sendMessage(ChatColor.RED + "잘못된 Shopitem 입니다");
                                        return false;
                                    }
                                    String valueS = args[4];
                                    switch (editVar) {
                                        case "UseStaticPrice" -> {
                                            shopItem.setUseStaticPrice(Boolean.parseBoolean(valueS));
                                        }
                                        case "UnlimitStock" -> {
                                            shopItem.setUnlimitStock(Boolean.parseBoolean(valueS));
                                        }
                                        case "BasePrice" -> {
                                            shopItem.setBasePrice(Integer.parseInt(valueS));
                                        }
                                        case "Price" -> {
                                            shopItem.setPrice(Integer.parseInt(valueS));
                                        }
                                        case "Stock" -> {
                                            shopItem.setStock(Integer.parseInt(valueS));
                                        }
                                        case "DisableSell" -> {
                                            shopItem.setDisableSell(Boolean.parseBoolean(valueS));
                                        }
                                    }
                                    sender.sendMessage(ChatColor.GREEN + "변경되었습니다 값: " + valueS);
                                    return true;
                                }
                                case "setcenter" -> {
                                    if (sender instanceof Player player) {
                                        Location location = player.getLocation();
                                        shopManager.updateShopRegion(location);
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
                                                0, basePrice, basePrice, false);
                                        shopManager.registerShopItem(shopItem);
                                        String path = "Items." + internalName;
                                        FileConfiguration config = shopManager.getShopDataFile().getConfig();
                                        config.set(path + ".ItemType", shopItem.getItemType().name());
                                        config.set(path + ".UseStaticPrice", shopItem.isUseStaticPrice());
                                        config.set(path + ".UnlimitStock", shopItem.isUnlimitStock());
                                        config.set(path + ".BasePrice", shopItem.getBasePrice());
                                        config.set(path + ".Price", shopItem.getPrice(false));
                                        config.set(path + ".Stock", shopItem.getStock());
                                        config.set(path + ".DisableSell", shopItem.isDisableSell());
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
                                            case ORAXEN -> {
                                                String sa = OraxenItems.getIdByItem(itemStack);
                                                config.set(path + ".InternalName", sa);
                                            }
                                            case MMOITEMS -> {
                                                NBTItem nbtItem = NBTItem.get(itemStack);
                                                if (nbtItem.hasType()) {
                                                    String typeString = nbtItem.getType().toUpperCase();
                                                    String id = nbtItem.getString("MMOITEMS_ITEM_ID");
                                                    String sa = typeString + ":" + id;
                                                    config.set(path + ".InternalName", sa);
                                                }
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
                    case "itembox" -> { //hs itembox give <target> <expiredSeconds>
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
                                   long expiredTime = System.currentTimeMillis() + (Long.parseLong(args[3]) * 1000L);
                                   if (target.equalsIgnoreCase("all-players")) {
                                       List<String> list = plugin.getPlayerDataManager().getPlayerFileNames();
                                       for (String uuidS : list) {
                                           UUID uuid = UUID.fromString(uuidS);
                                           OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                           if (offlinePlayer.isOnline()) {
                                               PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                               itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", expiredTime);
                                           } else {
                                               OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                               offlinePlayerData.loadData();
                                               itemBoxManager.sendItemStackToItemBox(offlinePlayerData, itemStack, "[시스템]", expiredTime);
                                               offlinePlayerData.saveData();
                                           }
                                       }
                                       sender.sendMessage(ChatColor.GREEN + "총 " + list.size() + " 명의 플레이어에게 아이템을 보냈습니다");
                                   } else {
                                       UUID uuid = UUID.fromString(target);
                                       OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                                       if (offlinePlayer.isOnline()) {
                                           PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getPlayer());
                                           itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", expiredTime);
                                       } else {
                                           OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                           offlinePlayerData.loadData();
                                           itemBoxManager.sendItemStackToItemBox(offlinePlayerData, itemStack, "[시스템]", expiredTime);
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
                                            deathPenaltyChestLog.getChestLocationSets().forEach(location -> {
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
