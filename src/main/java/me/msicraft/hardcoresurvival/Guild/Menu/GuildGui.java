package me.msicraft.hardcoresurvival.Guild.Menu;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuildGui extends CustomGui {

    public static final NamespacedKey MAIN_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "Guild");

    private final Inventory gui;
    private final HardcoreSurvival plugin;
    private final Guild guild;
    private final PlayerData playerData;

    public GuildGui(HardcoreSurvival plugin, PlayerData playerData) {
        this.plugin = plugin;
        this.guild = plugin.getGuildManager().getGuild(playerData.getGuildUUID());
        this.playerData = playerData;
        this.gui = Bukkit.createInventory(this, 54, Component.text("길드"));
    }

    public void setMain() {
        gui.clear();
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.ARROW, "다음 페이지", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Next");
        gui.setItem(50, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ARROW, "이전 페이지", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Previous");
        gui.setItem(48, itemStack);
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Back");
        gui.setItem(45, itemStack);

        itemStack = GuiUtil.createItemStack(Material.END_CRYSTAL, "땅 관리", GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Region");
        gui.setItem(51, itemStack);

        int guildLeftInviteCount = guild.getLeftInviteCount();
        itemStack = GuiUtil.createItemStack(Material.PAPER, "초대장",
                List.of(ChatColor.WHITE + "현재 가격: " + guild.getInvitePrice(),
                        ChatColor.WHITE + "초대가능한 남은인원수: " + (guildLeftInviteCount == -1 ? "이용불가" : guildLeftInviteCount)),
                2, MAIN_KEY, "Invite");
        gui.setItem(52, itemStack);

        int prefixPrice = plugin.getGuildManager().getPrefixChangePrice();
        itemStack = GuiUtil.createItemStack(Material.NAME_TAG, "길드 칭호 변경",
                List.of(ChatColor.GRAY + "현재 칭호: " + guild.getPrefix(),
                        ChatColor.GRAY + "변경 비용: " + (prefixPrice == -1 ? "변경 불가" : prefixPrice),
                        "", ChatColor.GRAY + "최초 1회 무료변경 가능"),
                -1, MAIN_KEY, "Prefix");
        gui.setItem(53, itemStack);

        List<UUID> members = guild.getMembers();
        int maxSize = members.size();
        int page = (int) playerData.getTempData("Guild_Main_Page", 0);
        int guiCount = 0;
        int lastCount = page * 45;

        String pageS = "페이지: " + (page + 1) + "/" + ((maxSize / 45) + 1);
        itemStack = GuiUtil.createItemStack(Material.BOOK, pageS, GuiUtil.EMPTY_LORE, -1, MAIN_KEY, "Page");
        gui.setItem(49, itemStack);

        List<Component> lore = new ArrayList<>();
        for (int a = lastCount; a < maxSize; a++) {
            UUID memberUUID = members.get(a);
            lore.clear();
            PlayerData memberData = plugin.getPlayerDataManager().getPlayerData(memberUUID);
            if (memberData != null) {
                OfflinePlayer offlinePlayer = memberData.getOfflinePlayer();

                ItemStack memberStack = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) memberStack.getItemMeta();
                PersistentDataContainer dataContainer = skullMeta.getPersistentDataContainer();
                dataContainer.set(MAIN_KEY, PersistentDataType.STRING, memberUUID.toString());

                boolean hasPermission = guild.isLeader(offlinePlayer.getUniqueId()) || guild.isSubLeader(offlinePlayer.getUniqueId());

                skullMeta.setPlayerProfile(offlinePlayer.getPlayerProfile());
                skullMeta.displayName(Component.text(ChatColor.GREEN + offlinePlayer.getName()));

                if (hasPermission) {
                    lore.add(Component.text(ChatColor.YELLOW + "좌 클릭: 추방"));
                    lore.add(Component.text(ChatColor.YELLOW + "우 클릭: 임시 추방 (임시 추방 취소)")); //-1 사용시 취소
                    lore.add(Component.text(""));
                }
                lore.add(Component.text(ChatColor.GRAY + "닉네임: " + memberData.getLastName()));
                if (hasPermission) {
                    lore.add(Component.text(ChatColor.GRAY + "소지금: " + plugin.getEconomy().getBalance(offlinePlayer)));
                }
                lore.add(Component.text(""));
                lore.add(Component.text(ChatColor.GRAY + "등급: " +
                        (guild.isLeader(offlinePlayer.getUniqueId()) ? "마스터" :
                                guild.isSubLeader(offlinePlayer.getUniqueId()) ? "부 마스터" : "멤버")));
                lore.add(Component.text(""));
                if (offlinePlayer.isOnline()) {
                    lore.add(Component.text(ChatColor.GRAY + "현재 상태: " + ChatColor.GREEN + "온라인"));
                } else {
                    lore.add(Component.text(ChatColor.GRAY + "현재 상태: " + ChatColor.RED + "오프라인"));
                    lore.add(Component.text(ChatColor.GRAY + "마지막 접속시간: " + TimeUtil.getTimeToFormat(memberData.getLastLogin())));
                    if (guild.isTempKickMember(memberUUID)) {
                        lore.add(Component.text(ChatColor.RED + ""));
                        lore.add(Component.text(ChatColor.GRAY + "임시 추방 만료 기간: " + TimeUtil.getTimeToFormat(guild.getTempKickTime(memberUUID))));
                    }
                }
                skullMeta.lore(lore);

                memberStack.setItemMeta(skullMeta);
                gui.setItem(guiCount, memberStack);
                guiCount++;
                if (guiCount >= 45) {
                    break;
                }
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }
}
