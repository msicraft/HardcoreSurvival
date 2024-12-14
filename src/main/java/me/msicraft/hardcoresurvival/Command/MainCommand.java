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
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import java.util.Set;
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
                    }
                    case "debug" -> { //hs debug []
                        if (!sender.isOp()) {
                            return false;
                        }
                        switch (args[1]) {
                            case "check-chunk" -> {
                                if (sender instanceof Player player) {
                                    long start = System.currentTimeMillis();
                                    int coal = 0;
                                    int copper = 0;
                                    int iron = 0;
                                    int gold = 0;
                                    int lapis = 0;
                                    int diamond = 0;
                                    int emerald = 0;
                                    Chunk chunk = player.getChunk();
                                    for(int y = -64; y <= 1024; y++) {
                                        for(int x = 0; x <= 15; x++) {
                                            for(int z = 0; z <= 15; z++) {
                                                Block block = chunk.getBlock(x, y, z);
                                                Material material = block.getType();
                                                switch (material) {
                                                    case COAL_ORE, DEEPSLATE_COAL_ORE -> {
                                                        coal++;
                                                    }
                                                    case COPPER_ORE, DEEPSLATE_COPPER_ORE -> {
                                                        copper++;
                                                    }
                                                    case IRON_ORE, DEEPSLATE_IRON_ORE -> {
                                                        iron++;
                                                    }
                                                    case GOLD_ORE, DEEPSLATE_GOLD_ORE -> {
                                                        gold++;
                                                    }
                                                    case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> {
                                                        lapis++;
                                                    }
                                                    case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> {
                                                        diamond++;
                                                    }
                                                    case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> {
                                                        emerald++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    long end = System.currentTimeMillis();
                                    player.sendMessage("Time: " + (end - start) + "ms");
                                    player.sendMessage("Chunk: " + chunk.getX() + " | " + chunk.getZ());
                                    player.sendMessage("Coal: " + coal);
                                    player.sendMessage("Copper: " + copper);
                                    player.sendMessage("Iron: " + iron);
                                    player.sendMessage("Gold: " + gold);
                                    player.sendMessage("Lapis: " + lapis);
                                    player.sendMessage("Diamond: " + diamond);
                                    player.sendMessage("Emerald: " + emerald);
                                    return true;
                                }
                            }
                            case "guild" -> { // <guild_uuid> [spawnlocation, prefix]
                                UUID uuid = UUID.fromString(args[3]);
                                GuildManager guildManager = plugin.getGuildManager();
                                Guild guild = guildManager.getGuild(uuid);
                                if (guild == null) {
                                    sender.sendMessage(ChatColor.RED + "존재하지 않는 길드입니다");
                                    return false;
                                }
                                switch (args[2]) {
                                    case "spawnlocation" -> {
                                        if (sender instanceof Player player) {
                                            GuildSpawnLocation guildSpawnLocation = guild.getGuildRegion().getGuildSpawnLocation();
                                            guildSpawnLocation.setGuildSpawnLocation(player.getLocation());
                                        }
                                    }
                                    case "prefix" -> { //[set, get] <value>
                                        switch (args[4]) {
                                            case "set" -> {
                                                String prefix = args[5];
                                                if (prefix.equalsIgnoreCase("null")) {
                                                    prefix = null;
                                                }
                                                guild.setPrefix(prefix);
                                                sender.sendMessage(ChatColor.GREEN + "GuildUUID: " + uuid);
                                                sender.sendMessage(ChatColor.GREEN + "Prefix: " + prefix);
                                                return true;
                                            }
                                            case "get" -> {
                                                sender.sendMessage(ChatColor.GREEN + "GuildUUID: " + uuid);
                                                sender.sendMessage(ChatColor.GREEN + "Prefix: " + ChatColor.WHITE + guild.getPrefix());
                                                return true;
                                            }
                                        }
                                    }
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
                            case "playerdata" -> {
                                switch (args[2]) {
                                    case "tags" -> { //[list, add, remove] <player>
                                        Player target = Bukkit.getPlayer(args[4]);
                                        if (target == null) {
                                            sender.sendMessage("존재하지 않는 플레이어입니다");
                                            return false;
                                        }
                                        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(target);
                                        if (playerData == null) {
                                            sender.sendMessage("플레이어 데이터가 존재하지 않습니다");
                                            return false;
                                        }
                                        switch (args[3]) {
                                            case "list" -> {
                                                sender.sendMessage(ChatColor.GREEN + "Tags: ");
                                                for (String tag : playerData.getTags()) {
                                                    sender.sendMessage(ChatColor.GRAY + "- " + tag);
                                                }
                                                return true;
                                            }
                                            case "add" -> {
                                                String tag = args[5];
                                                if (playerData.getTags().contains(tag)){
                                                    sender.sendMessage(ChatColor.RED + "이미 존재하는 태그입니다");
                                                    return false;
                                                }
                                                playerData.addTag(tag);
                                                sender.sendMessage("태그 '" + tag + "'을 추가하였습니다");
                                                return true;
                                            }
                                            case "remove" -> {
                                                String tag = args[5];
                                                if (!playerData.getTags().contains(tag)){
                                                    sender.sendMessage(ChatColor.RED + "존재하지 않는 태그입니다");
                                                    return false;
                                                }
                                                playerData.removeTag(tag);
                                                sender.sendMessage("태그 '" + tag + "'을 제거하였습니다");
                                                return true;
                                            }
                                        }
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
                        } else {
                            plugin.setMaintenance(true);
                            sender.sendMessage(ChatColor.RED + "Enable Maintenance");
                        }
                        return true;
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
                        TextComponent uuidTextComponent = Component.text("UUID: " + uuid).
                                clickEvent(ClickEvent.suggestCommand(uuid.toString()));
                        sender.sendMessage(uuidTextComponent);

                        UUID guildUUID = plugin.getPlayerDataManager().getPlayerData(player).getGuildUUID();
                        String guildUUIDs = "";
                        if (guildUUID == null) {
                            guildUUIDs = "";
                        }
                        TextComponent guildUUIDTextComponent = Component.text("GuildUUID: " + guildUUIDs).
                                clickEvent(ClickEvent.suggestCommand(guildUUIDs));
                        sender.sendMessage(guildUUIDTextComponent);
                        return true;
                    }
                    case "streamer" -> { //hs streamer [add, remove, list] <target>
                        if (!sender.isOp()) {
                            return false;
                        }
                        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
                        switch (args[1]) {
                            case "create-for-player" -> { //hs streamer create-for-player <leader> [member_1:member_2] 일반 플레이어용 길드 생성
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UUID uuid = offlinePlayer.getUniqueId();
                                if (playerDataManager.isStreamer(uuid)) {
                                    sender.sendMessage(ChatColor.RED + "해당 플레이어는 이미 스트리머로 등록되어있습니다 -> " + offlinePlayer.getName());
                                    return false;
                                }
                                playerDataManager.addStreamer(uuid);

                                try {
                                    String[] memberNames = args[3].split(":");
                                    Guild guild = plugin.getGuildManager().createGuild(uuid);

                                    PlayerData playerData = playerDataManager.getPlayerData(uuid);
                                    if (playerData == null) {
                                        playerData = playerDataManager.createPlayerData(uuid);
                                    }
                                    UUID guildUUID = guild.getGuildUUID();
                                    playerData.setGuildUUID(guildUUID);

                                    for (String memberName : memberNames) {
                                        OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberName);
                                        UUID memberUUID = memberPlayer.getUniqueId();
                                        PlayerData memberData = playerDataManager.getPlayerData(memberUUID);
                                        if (memberData == null) {
                                            memberData = playerDataManager.createPlayerData(memberUUID);
                                        }
                                        memberData.setGuildUUID(guildUUID);
                                        guild.addMember(memberUUID);
                                    }

                                    sender.sendMessage(ChatColor.GREEN + "해당 플레이어에 대한 길드가 생성되었습니다");
                                    sender.sendMessage(ChatColor.GREEN + "Player: " + offlinePlayer.getName());
                                    sender.sendMessage(ChatColor.GREEN + "UUID: " + uuid);
                                    sender.sendMessage(ChatColor.GREEN + "Guild-UUID: " + guild.getGuildUUID());
                                    sender.sendMessage(ChatColor.GREEN + "멤버: " + memberNames[0] + ", " + memberNames[1]);
                                    return true;
                                } catch (ArrayIndexOutOfBoundsException ex) {
                                    sender.sendMessage("hs streamer create-for-player <leader> [member_1:member_2] (일반 플레이어용 길드 생성)");
                                }
                            }
                            case "add" -> {
                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                                UUID uuid = offlinePlayer.getUniqueId();
                                if (playerDataManager.isStreamer(uuid)) {
                                    sender.sendMessage(ChatColor.RED + "해당 플레이어는 이미 스트리머로 등록되어있습니다 -> " + offlinePlayer.getName());
                                    return false;
                                }
                                playerDataManager.addStreamer(uuid);

                                Guild guild = plugin.getGuildManager().createGuild(uuid);
                                PlayerData playerData = playerDataManager.getPlayerData(uuid);
                                if (playerData == null) {
                                    playerData = playerDataManager.createPlayerData(uuid);
                                }
                                playerData.setGuildUUID(guild.getGuildUUID());

                                sender.sendMessage(ChatColor.GREEN + "해당 플레이어에 대한 길드가 생성되었습니다");
                                sender.sendMessage(ChatColor.GREEN + "Player: " + offlinePlayer.getName());
                                sender.sendMessage(ChatColor.GREEN + "UUID: " + uuid);
                                sender.sendMessage(ChatColor.GREEN + "Guild-UUID: " + guild.getGuildUUID());
                                return true;
                            }
                            case "remove" -> {
                                UUID uuid = UUID.fromString(args[2]);
                                if (!playerDataManager.isStreamer(uuid)) {
                                    sender.sendMessage(ChatColor.RED + "해당 플레이어는 스트리머에 등록되어있지 않습니다");
                                    return false;
                                }
                                playerDataManager.removeStreamer(uuid);

                                PlayerData playerData = playerDataManager.getPlayerData(uuid);
                                OfflinePlayer offlinePlayer = playerData.getOfflinePlayer();
                                if (offlinePlayer.isOnline()) {
                                    offlinePlayer.getPlayer().kick(Component.text(ChatColor.RED + "관리자에의해 길드가 제거되었습니다"));
                                }
                                UUID guildUUID = playerData.getGuildUUID();
                                playerData.setGuildUUID(null);

                                plugin.getGuildManager().removeGuild(guildUUID);

                                sender.sendMessage(ChatColor.GREEN + "해당 플레이어가 스트리머에서 제거되었습니다");
                                return true;
                            }
                            case "list" -> {
                                sender.sendMessage("Streamer List: ");
                                playerDataManager.getStreamerList().forEach(uuid -> {
                                    sender.sendMessage("Player: " + playerDataManager.getPlayerData(uuid).getLastName());
                                    sender.sendMessage("UUID: " + uuid);
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
                                        case "Category" -> {
                                            shopItem.setCategory(ShopItem.Category.valueOf(valueS.toUpperCase()));
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
                                                0, basePrice, basePrice, false, ShopItem.Category.NONE);
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
                                        config.set(path + ".Category", ShopItem.Category.NONE.name());
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
                                       Set<UUID> sets = plugin.getPlayerDataManager().getPlayerUUIDs();
                                       for (UUID uuid : sets) {
                                           PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                                           itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", expiredTime);
                                       }
                                       sender.sendMessage(ChatColor.GREEN + "총 " + sets.size() + " 명의 플레이어에게 아이템을 보냈습니다");
                                   } else {
                                       UUID uuid = UUID.fromString(target);
                                       PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                                       if (playerData == null) {
                                           player.sendMessage(ChatColor.RED + "존재하지 않는 플레이어데이터 입니다");
                                           return false;
                                       }
                                       itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", expiredTime);
                                       sender.sendMessage(ChatColor.GREEN + "해당 플레이어에게 아이템을 보냈습니다");
                                       sender.sendMessage(ChatColor.GREEN + "Player: " + playerData.getLastName());
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
                                            if (playerData == null) {
                                                sender.sendMessage(ChatColor.RED + "존재하지 않는 플레이어데이터 입니다");
                                                return false;
                                            }
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
                                                plugin.getPlayerDataManager().getPlayerUUIDs().forEach(uuid -> {
                                                    PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                                                    deathPenaltyManager.sendChestLogToItemBox(playerData);
                                                });
                                            } else {
                                                UUID uuid = UUID.fromString(targetS);
                                                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                                                deathPenaltyManager.sendChestLogToItemBox(playerData);
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
