package me.msicraft.hardcoresurvival.Guild.Menu.Event;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.Data.GuildSpawnLocation;
import me.msicraft.hardcoresurvival.Guild.Data.RegionOptions;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionOptionGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GuildRegionGuiEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final GuildManager guildManager;

    public GuildRegionGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
    }

    @EventHandler
    public void clickGuildRegionGui(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof GuildRegionGui guildRegionGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            UUID guildUUID = playerData.getGuildUUID();
            if (guildUUID == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "길드가 존재하지 않습니다");
                return;
            }
            Guild guild = guildManager.getGuild(guildUUID);
            if (guild == null) {
                player.closeInventory();

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("Unknown guild (guild region)", "Player: " + player.getName(), "GuildUUID: " + guildUUID);
                }
                return;
            }
            GuildRegion guildRegion = guild.getGuildRegion();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(GuildRegionGui.MAIN_KEY)) {
                String data = dataContainer.get(GuildRegionGui.MAIN_KEY, PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = guildRegion.getGuildChunks().size() / 45;
                    int current = (int) playerData.getTempData("GuildRegion-Main-Page", 0);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTempData("GuildRegion-Main-Page", next);
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTempData("GuildRegion-Main-Page", previous);
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        case "RegionOptions" -> {
                            if (!guild.isLeader(player)) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            GuildRegionOptionGui guildRegionOptionGui = (GuildRegionOptionGui) playerData.getCustomGui(GuiType.GUILD_REGION_OPTIONS);
                            player.openInventory(guildRegionOptionGui.getInventory());
                            guildRegionOptionGui.setMain();
                        }
                        case "GuildRegion-Buy" -> {
                            if (!guild.isLeader(player)) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            Location location = player.getLocation();
                            if (!guildManager.canBuyRegionInWorld(location)) {
                                player.sendMessage(ChatColor.RED + "현재 월드에서는 땅 구입이 불가능합니다");
                                return;
                            }
                            Location shopLocation = plugin.getShopManager().getShopRegion().getCenterLocation();
                            if (shopLocation != null) {
                                double distance = shopLocation.distance(location);
                                if (distance < guildManager.getRegionProtectRadius()) {
                                    player.sendMessage(ChatColor.RED + "스폰지역 근처의 땅은 구매 불가능합니다");
                                    return;
                                }
                            }

                            Chunk chunk = location.getChunk();
                            PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
                            if (chunkData.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                player.sendMessage(ChatColor.RED + "이미 소유자가 있는 땅 입니다");
                                return;
                            }

                            int ownRegionCount = guildRegion.getBuyRegionCount();
                            int maxRegionCount = guildManager.getMaxRegionPrice();
                            if (ownRegionCount >= maxRegionCount) {
                                player.sendMessage(ChatColor.RED + "더 이상 땅을 구입할 수 없습니다 (최대: " + maxRegionCount + ")");
                                return;
                            }
                            int requiredBalance;
                            if (ownRegionCount == 0) {
                                requiredBalance = 0;
                            } else {
                                requiredBalance = guildManager.getRegionPrice(ownRegionCount + 1) - guildManager.getRegionPrice(ownRegionCount);
                                if (requiredBalance < 0) {
                                    player.sendMessage(ChatColor.RED + "{" + (ownRegionCount + 1) + "} 땅 가격이 존재하지 않습니다");
                                    return;
                                }
                            }
                            double balance = plugin.getEconomy().getBalance(player);
                            if (balance < requiredBalance) {
                                player.sendMessage(ChatColor.RED + "충분한 돈을 보유하고 있지 않습니다 (가격: " + requiredBalance + ")");
                                return;
                            }
                            guildManager.buyRegion(player, guild, location, requiredBalance);

                            player.sendMessage(ChatColor.GREEN + "땅을 구매하였습니다 (보유한 땅의 수: " + (ownRegionCount + 1) + ")");
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        case "GuildSpawn" -> {
                            if (!guild.isLeader(player)) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            Location location = player.getLocation();
                            if (!guildManager.canBuyRegionInWorld(location)) {
                                player.sendMessage(ChatColor.RED + "현재 월드에서는 사용 불가능한 기능입니다");
                                return;
                            }
                            Chunk chunk = player.getLocation().getChunk();
                            PersistentDataContainer chunkData = chunk.getPersistentDataContainer();
                            if (!chunkData.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                                player.sendMessage(ChatColor.RED + "현재 땅은 소유자가 없습니다");
                                return;
                            }
                            String guildRegionUUID = chunkData.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
                            if (guildRegionUUID != null) {
                                if (guildRegionUUID.equals(guild.getGuildUUID().toString())) {
                                    GuildSpawnLocation guildSpawnLocation = guildRegion.getGuildSpawnLocation();
                                    guildSpawnLocation.setGuildSpawnLocation(location);

                                    player.sendMessage(ChatColor.GREEN + "해당 위치가 길드의 스폰지점으로 설정되었습니다");
                                    player.openInventory(guildRegionGui.getInventory());
                                    guildRegionGui.setMain();
                                } else {
                                    player.sendMessage(ChatColor.RED + "계약한 땅이 아닙니다");
                                }
                            }
                        }
                        case "Page" -> {
                            int totalPricePerDay = guildManager.getRegionPrice(guildRegion.getGuildChunks().size());
                            int overDueDay = guildRegion.getOverdueDay(false);
                            if (overDueDay == 0) {
                                player.sendMessage(ChatColor.RED + "만료기간이 지나지 않았습니다");
                                return;
                            }
                            double balance = plugin.getEconomy().getBalance(player);
                            if (balance < totalPricePerDay) {
                                player.sendMessage(ChatColor.RED + "충분한 돈이 없습니다");
                                return;
                            }

                            plugin.getEconomy().withdrawPlayer(player, totalPricePerDay);
                            long currentTime = System.currentTimeMillis();
                            guildRegion.setLastRegionPayTime(currentTime);

                            player.sendMessage(ChatColor.GREEN + "땅(청크) 기간이 연장되었습니다");
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        case "Back" -> {
                            CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                            if (customGui instanceof MenuGui menuGui) {
                                player.openInventory(menuGui.getInventory());
                                menuGui.setMain();
                            }
                        }
                        default -> {
                            if (!guild.isLeader(player)) {
                                player.sendMessage(ChatColor.RED + "계약 취소 권한이 없습니다");
                                return;
                            }
                            String[] format = data.split(":");
                            String worldName = format[0];
                            int x = Integer.parseInt(format[1]);
                            int z = Integer.parseInt(format[2]);

                            Location location = player.getLocation();
                            Chunk chunk = location.getChunk();
                            if (worldName.equalsIgnoreCase(player.getWorld().getName()) && chunk.getX() == x && chunk.getZ() == z) {
                                guildManager.removeRegion(guild, worldName, x, z);
                                player.sendMessage(ChatColor.GREEN + "해당 땅(청크) 계약을 취소하였습니다");
                            } else {
                                player.sendMessage(ChatColor.RED + "해당 땅(청크)내에서만 계약을 취소할 수 있습니다");
                                return;
                            }
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                    }
                }
            }
        } else if (topInventory.getHolder(false) instanceof GuildRegionOptionGui guildRegionOptionGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            ItemStack itemStack = e.getCurrentItem();
            if (itemStack == null) {
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return;
            }
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            Guild guild = guildManager.getGuild(playerData.getGuildUUID());
            if (guild == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "길드가 존재하지 않습니다");
                return;
            }
            GuildRegion guildRegion = guild.getGuildRegion();
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(GuildRegionOptionGui.MAIN_KEY)) {
                String data = dataContainer.get(GuildRegionOptionGui.MAIN_KEY, PersistentDataType.STRING);
                if (data != null) {
                    switch (data) {
                        case "Back" -> {
                            GuildRegionGui guildRegionGui = (GuildRegionGui) playerData.getCustomGui(GuiType.GUILD_REGION);
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        default -> {
                            RegionOptions regionOptions = RegionOptions.valueOf(data);
                            switch (regionOptions) {
                                case PRIVATE_CHEST, BLOCK_BREAK, BLOCK_PLACE -> {
                                    if (e.isLeftClick()) {
                                        boolean b = (boolean) guildRegion.getRegionOption(regionOptions);
                                        if (b) {
                                            guildRegion.setRegionOption(regionOptions, false);
                                        } else {
                                            guildRegion.setRegionOption(regionOptions, true);
                                        }
                                    } else if (e.isRightClick()) {
                                        guildRegion.setRegionOption(regionOptions, regionOptions.getBaseValue());
                                    }
                                }
                            }
                            player.openInventory(guildRegionOptionGui.getInventory());
                            guildRegionOptionGui.setMain();
                        }
                    }
                }
            }
        }
    }

}
