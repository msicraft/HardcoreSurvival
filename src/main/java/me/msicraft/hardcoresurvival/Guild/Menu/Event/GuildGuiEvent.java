package me.msicraft.hardcoresurvival.Guild.Menu.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

public class GuildGuiEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final GuildManager guildManager;

    public GuildGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
    }

    private void openGuildGui(PlayerData playerData, boolean nextTick) {
        if (nextTick) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
                playerData.getPlayer().openInventory(guildGui.getInventory());
                guildGui.setMain();
            });
        } else {
            GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
            playerData.getPlayer().openInventory(guildGui.getInventory());
            guildGui.setMain();
        }
    }

    @EventHandler
    public void guildGuiChatEdit(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        String message = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (playerData.hasTempData("Guild_TempKick_Second_Edit")) {
            e.setCancelled(true);
            String targetUUIDS = (String) playerData.getTempData("Guild_TempKick_Second_Edit", null);
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("Guild_TempKick_Second_Edit");
                openGuildGui(playerData, true);
                return;
            }
            int seconds = -1;
            try {
                seconds = Integer.parseInt(message);
            } catch (NumberFormatException ignored) {}
            if (seconds < -1) {
                seconds = -1;
            }
            int a = seconds;
            playerData.removeTempData("Guild_TempKick_Second_Edit");
            Bukkit.getScheduler().runTask(plugin, () -> {
                guildManager.tempKickGuild(player, Bukkit.getOfflinePlayer(UUID.fromString(targetUUIDS)), a);
                openGuildGui(playerData, false);
            });
        } else if (playerData.hasTempData("Guild_Invite")) {
            e.setCancelled(true);
            String priceS = (String) playerData.getTempData("Guild_Invite");
            if (priceS == null || message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("Guild_Invite");
                openGuildGui(playerData, true);
                return;
            }
            int price = Integer.parseInt(priceS);
            double balance = plugin.getEconomy().getBalance(player);
            if (balance < price) {
                player.sendMessage(MessageUtil.getMessage("NotEnoughMoney",true));
                playerData.removeTempData("Guild_Invite");
                return;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(message);
            PlayerData targetPlayerData = plugin.getPlayerDataManager().getPlayerData(offlinePlayer.getUniqueId());
            if (targetPlayerData == null) {
                player.sendMessage(ChatColor.RED + "접속 기록이 존재하지 않는 플레이어입니다");
                playerData.removeTempData("Guild_Invite");
                return;
            }
            if (targetPlayerData.getGuildUUID() != null) {
                player.sendMessage(ChatColor.RED + "해당 플레이어는 이미 길드에 속해있습니다");
                return;
            }
            if (!guildManager.inviteGuild(player, offlinePlayer)) {
                player.sendMessage(ChatColor.RED + "{Fail Invite Guild}");
                playerData.removeTempData("Guild_Invite");
                return;
            }
            Player targetPlayer = targetPlayerData.getPlayer();
            if (targetPlayer != null) {
                String prefix = guildManager.getGuild(playerData.getGuildUUID()).getPrefix();
                targetPlayer.sendMessage(ChatColor.GREEN + (prefix != null ? prefix : playerData.getLastName()) + " 길드에 가입되었습니다");
            }
            plugin.getEconomy().withdrawPlayer(player, price);

            player.sendMessage(ChatColor.GREEN + "해당 플레이어가 길드에 가입되었습니다");
            playerData.removeTempData("Guild_Invite");
            openGuildGui(playerData, true);
        } else if (playerData.hasTempData("Guild_ChangePrefix")) {
            e.setCancelled(true);
            String priceS = (String) playerData.getTempData("Guild_ChangePrefix");
            if (priceS == null || message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("Guild_ChangePrefix");
                openGuildGui(playerData, true);
                return;
            }
            int price = Integer.parseInt(priceS);
            double balance = plugin.getEconomy().getBalance(player);
            if (balance < price) {
                player.sendMessage(MessageUtil.getMessage("NotEnoughMoney",true));
                playerData.removeTempData("Guild_ChangePrefix");
                return;
            }
            message = message.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]", "");
            message = message.replaceAll(" ", "");
            if (message.length() > 6) {
                player.sendMessage(ChatColor.RED + "6자리를 초과할 수 없습니다");
                playerData.removeTempData("Guild_ChangePrefix");
                openGuildGui(playerData, true);
                return;
            }
            if (message.equalsIgnoreCase("x")) {
                player.sendMessage(ChatColor.RED + "해당 칭호는 사용 불가능 합니다");
                playerData.removeTempData("Guild_ChangePrefix");
                openGuildGui(playerData, true);
                return;
            }
            Guild guild = plugin.getGuildManager().getGuild(playerData.getGuildUUID());
            if (guild != null) {
                plugin.getEconomy().withdrawPlayer(player, price);
                guild.setPrefix(message);
                player.sendMessage(ChatColor.GREEN + "길드 칭호가 변경되었습니다 (" + message + ")");
            } else {
                player.sendMessage(ChatColor.RED + "잘못된 길드 입니다");
            }
            playerData.removeTempData("Guild_ChangePrefix");
            openGuildGui(playerData, true);
            return;
        }
    }

    @EventHandler
    public void clickGuildGui(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof GuildGui guildGui) {
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
                player.sendMessage(ChatColor.RED + "길드가 없습니다");
                return;
            }
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(GuildGui.MAIN_KEY)) {
                String data = dataContainer.get(GuildGui.MAIN_KEY, PersistentDataType.STRING);
                if (data != null) {
                    int maxPage = guild.getMembers().size() / 45;
                    int current = (int) playerData.getTempData("Guild_Main_Page", 0);
                    switch (data) {
                        case "Next" -> {
                            int next = current + 1;
                            if (next > maxPage) {
                                next = 0;
                            }
                            playerData.setTempData("Guild_Main_Page", next);
                            player.openInventory(guildGui.getInventory());
                            guildGui.setMain();
                        }
                        case "Previous" -> {
                            int previous = current - 1;
                            if (previous < 0) {
                                previous = maxPage;
                            }
                            playerData.setTempData("Guild_Main_Page", previous);
                            player.openInventory(guildGui.getInventory());
                            guildGui.setMain();
                        }
                        case "Page" -> {
                        }
                        case "Back" -> {
                            CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                            if (customGui instanceof MenuGui menuGui) {
                                player.openInventory(menuGui.getInventory());
                                menuGui.setMain();
                            }
                        }
                        case "Region" -> {
                            GuildRegionGui guildRegionGui = (GuildRegionGui) playerData.getCustomGui(GuiType.GUILD_REGION);
                            player.openInventory(guildRegionGui.getInventory());
                            guildRegionGui.setMain();
                        }
                        case "Invite" -> {
                            if (!guild.isLeader(player.getUniqueId())) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            if (guildManager.getMaxInviteCount() == -1) {
                                player.sendMessage(ChatColor.RED + "현재는 이용 불가능 합니다");
                                return;
                            }
                            int leftInviteCount = guild.getLeftInviteCount();
                            if (leftInviteCount == 0) {
                                player.sendMessage(ChatColor.RED + "더이상 초대할 수 없습니다");
                                return;
                            }
                            int price = guild.getInvitePrice();
                            player.sendMessage(ChatColor.GRAY + "========================================");
                            player.sendMessage(ChatColor.GRAY + "초대할 플레이어 이름을 입력해주세요");
                            player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                            player.sendMessage(ChatColor.GRAY + "========================================");
                            playerData.setTempData("Guild_Invite", String.valueOf(price));
                            player.closeInventory();
                        }
                        case "Prefix" -> {
                            if (!guild.isLeader(player.getUniqueId())) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            int price = guildManager.getPrefixChangePrice();
                            if (price == -1) {
                                player.sendMessage(ChatColor.RED + "현재는 변경불가능합니다");
                                return;
                            }
                            if (guild.getPrefix() == null) {
                                price = 0;
                            }
                            player.sendMessage(ChatColor.GRAY + "========================================");
                            player.sendMessage(ChatColor.GRAY + "변경할 칭호를 입력해주세요");
                            player.sendMessage(ChatColor.GRAY + "한글,영어,숫자만 가능. 최대 6자리");
                            player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                            player.sendMessage(ChatColor.GRAY + "========================================");
                            playerData.setTempData("Guild_ChangePrefix", String.valueOf(price));
                            player.closeInventory();
                        }
                        default -> {
                            if (!guild.isLeader(player.getUniqueId())) {
                                player.sendMessage(ChatColor.RED + "권한이 없습니다");
                                return;
                            }
                            UUID targetUUID = UUID.fromString(data);
                            if (targetUUID.equals(player.getUniqueId())) {
                                player.sendMessage(ChatColor.RED + "자기 자신을 추방시킬 수 없습니다");
                                return;
                            }
                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
                            if (e.isLeftClick()) {
                                guildManager.kickGuild(player, target);
                                player.openInventory(guildGui.getInventory());
                                guildGui.setMain();
                            } else if (e.isRightClick()) {
                                player.sendMessage(ChatColor.GRAY + "========================================");
                                player.sendMessage(ChatColor.GRAY + "임시 추방 시간을 입력해주세요 (초). 숫자만 입력");
                                player.sendMessage(ChatColor.GRAY + "이미 임시 추방 상태인경우 시간을 연장합니다");
                                player.sendMessage(ChatColor.GRAY + "-1 입력시 추방 취소");
                                player.sendMessage(ChatColor.GRAY + "예시: 60, 180, 10");
                                player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                                player.sendMessage(ChatColor.GRAY + "========================================");
                                playerData.setTempData("Guild_TempKick_Second_Edit", data);
                                player.closeInventory();
                            }
                        }
                    }
                }
            }
        }
    }

}
