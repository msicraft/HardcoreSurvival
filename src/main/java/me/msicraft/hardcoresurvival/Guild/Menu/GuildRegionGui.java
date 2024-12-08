package me.msicraft.hardcoresurvival.Guild.Menu;

import me.msicraft.hardcoresurvival.API.Data.Pair;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildChunk;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import me.msicraft.hardcoresurvival.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuildRegionGui extends CustomGui {

    public static final NamespacedKey MAIN_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "GuildRegion-Main");

    private final Inventory gui;
    private final HardcoreSurvival plugin;
    private final Guild guild;
    private final PlayerData playerData;

    public GuildRegionGui(HardcoreSurvival plugin, PlayerData playerData) {
        this.plugin = plugin;
        this.guild = plugin.getGuildManager().getGuild(playerData.getGuildUUID());
        this.playerData = playerData;
        this.gui = Bukkit.createInventory(this, 54, Component.text("땅 관리"));
    }

    public void setMain() {
        WorldManager worldManager = plugin.getWorldManager();
        GuildManager guildManager = plugin.getGuildManager();
        GuildRegion guildRegion = guild.getGuildRegion();

        gui.clear();
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Next");
        gui.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Previous");
        gui.setItem(48, itemStack);
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Back");
        gui.setItem(45, itemStack);

        itemStack = GuiUtil.createItemStack(Material.PAPER, "땅 옵션 설정", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "RegionOptions");
        gui.setItem(46, itemStack);

        List<String> guildSpawnLore = new ArrayList<>();
        guildSpawnLore.add(ChatColor.WHITE + "현재 위치를 길드의 스폰지점으로 설정합니다");
        guildSpawnLore.add(ChatColor.WHITE + "구매한 땅(청크)에서만 설정 가능합니다");
        guildSpawnLore.add(ChatColor.WHITE + "");
        Location guildSpawnLoc = guildRegion.getGuildSpawnLocation().getSpawnLocation();
        String guildSpawnLocString = "X";
        if (guildSpawnLoc != null) {
            guildSpawnLocString = worldManager.getCurrentWorldName(guildSpawnLoc.getWorld().getName()) + " X:" + guildSpawnLoc.getBlockX() + " Y:" + guildSpawnLoc.getBlockY() + " Z:" + guildSpawnLoc.getBlockZ();
        }
        guildSpawnLore.add(ChatColor.WHITE + "현재 설정된 스폰 위치 -> ");
        guildSpawnLore.add(ChatColor.WHITE + guildSpawnLocString);
        guildSpawnLore.add(ChatColor.RED + "리더 전용 설정");
        itemStack = GuiUtil.createItemStack(Material.OAK_SIGN, "스폰위치 설정", guildSpawnLore, -1, MAIN_KEY, "GuildSpawn");
        gui.setItem(52, itemStack);

        List<String> regionBuyLore = new ArrayList<>();
        regionBuyLore.add(ChatColor.GRAY + "1번째 땅은 무료입니다");
        int ownRegionCount = guildRegion.getBuyRegionCount();
        int maxRegionCount = guildManager.getMaxRegionPrice();
        if (ownRegionCount >= maxRegionCount) {
            regionBuyLore.add(ChatColor.GRAY + "다음 땅 계약시 필요 돈: X");
        } else {
            regionBuyLore.add(ChatColor.GRAY + "다음 땅 계약시 필요 돈: " + (guildManager.getRegionPrice(ownRegionCount + 1) - guildManager.getRegionPrice(ownRegionCount)));
        }
        regionBuyLore.add("");
        Set<Integer> regionPriceKeySet = guildManager.getRegionPriceKeySet();
        for (int i : regionPriceKeySet) {
            int price = guildManager.getRegionPrice(i);
            regionBuyLore.add(ChatColor.GRAY + "땅 소지 개수: " + i + " -> 가격: " + price);
        }
        itemStack = GuiUtil.createItemStack(Material.ITEM_FRAME, "현재 땅(청크) 구입", regionBuyLore, -1, MAIN_KEY, "GuildRegion-Buy");
        gui.setItem(53, itemStack);

        List<GuildChunk> guildChunkList = guildRegion.getGuildChunks();
        int maxSize = guildChunkList.size();
        int page = (int) playerData.getTempData("GuildRegion-Main-Page", 0);
        int guiCount = 0;
        int lastCount = page * 45;
        String pageS = "페이지: " + (page + 1) + "/" + ((maxSize / 45) + 1);
        List<String> pageLore = new ArrayList<>();
        pageLore.add(ChatColor.GRAY + "구매한 땅(청크)의 수: " + maxSize);
        pageLore.add("");
        int totalPricePerDay = guildManager.getRegionPrice(maxSize);
        if (totalPricePerDay == -1) {
            totalPricePerDay = 0;
        }
        pageLore.add(ChatColor.GOLD + "보유한 땅(청크) 총 비용: " + totalPricePerDay);
        int overDueDay = guildRegion.getOverdueDay(true);
        pageLore.add(ChatColor.GOLD + "연체 횟수: " + overDueDay);
        String expiredTime;
        long lastPayTime = guildRegion.getLastRegionPayTime();
        if (lastPayTime == -1) {
            expiredTime = "x";
        } else {
            lastPayTime = lastPayTime + (guildManager.getRegionPaySeconds() * 1000L);
            expiredTime = TimeUtil.getTimeToFormat(lastPayTime);
        }
        pageLore.add(ChatColor.GOLD + "만료 기간: " + expiredTime);
        pageLore.add("");
        pageLore.add(ChatColor.GRAY + "=====적용된 상점 페널티=====");

        double shopPenalty = guildManager.getShopPenalty(overDueDay) * 100.0;
        String shopPenaltyFormat = String.format("%.2f", shopPenalty);
        pageLore.add(ChatColor.GRAY + "구입 비용: " + ChatColor.GOLD + "+" + shopPenaltyFormat + "%");
        pageLore.add(ChatColor.GRAY + "판매 비용: " + ChatColor.GOLD + "-" + shopPenaltyFormat + "%");
        pageLore.add("");
        pageLore.add(ChatColor.YELLOW + "좌 클릭: 땅 비용 지불");
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, pageLore, -1, MAIN_KEY, "Page");
        gui.setItem(49, itemStack);

        List<Component> lore = new ArrayList<>();
        for (int a = lastCount; a < maxSize; a++) {
            lore.clear();
            GuildChunk guildChunk = guildChunkList.get(a);
            Pair<Integer, Integer> pair = guildChunk.getChunkPair();
            ItemStack chunkStack = new ItemStack(Material.GLOW_ITEM_FRAME);
            ItemMeta itemMeta = chunkStack.getItemMeta();
            itemMeta.displayName(Component.text(worldManager.getCurrentWorldName(guildChunk.getWorldName())));
            Location centerLocation = worldManager.getCenterChunkLocation(guildChunk.getWorldName(), pair.getV1(), pair.getV2());
            if (centerLocation == null) {
                continue;
            }
            lore.add(Component.text(ChatColor.GRAY + "중심 좌표"));
            lore.add(Component.text(ChatColor.GRAY + "X: " + ChatColor.GREEN + centerLocation.getBlockX()));
            lore.add(Component.text(ChatColor.GRAY + "Z: " + ChatColor.GREEN + centerLocation.getBlockZ()));
            lore.add(Component.text(""));
            lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: 땅 계약 취소"));
            itemMeta.lore(lore);
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            String data = guildChunk.getWorldName() + ":" + pair.getV1() + ":" + pair.getV2();
            dataContainer.set(MAIN_KEY, PersistentDataType.STRING, data);

            chunkStack.setItemMeta(itemMeta);

            gui.setItem(guiCount, chunkStack);
            guiCount++;
            if (guiCount >= 45) {
                break;
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }
}
