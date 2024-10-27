package me.msicraft.hardcoresurvival.Guild.Menu.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
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

    @EventHandler
    public void guildGuiChatEdit(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        String targetUUIDS = (String) playerData.getTempData("Guild_TempKick_Second_Edit", null);
        if (targetUUIDS != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("Guild_TempKick_Second_Edit");
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
                    player.openInventory(guildGui.getInventory());
                    guildGui.setMain();
                });
                return;
            }
            int seconds = Integer.parseInt(message.replaceAll("[^0-9]", ""));

            playerData.removeTempData("Guild_TempKick_Second_Edit");
            Bukkit.getScheduler().runTask(plugin, ()-> {
                guildManager.tempKickGuild(player, Bukkit.getOfflinePlayer(UUID.fromString(targetUUIDS)), seconds);
                GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
                player.openInventory(guildGui.getInventory());
                guildGui.setMain();
            });
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
            Guild guild = guildManager.getGuild(player.getUniqueId());
            if (guild == null) {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "권한이 없습니다");

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("Can't access guild gui", "Player: " + player.getName());
                }
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
                            return;
                        }
                        case "Back" -> {
                            CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
                            if (customGui instanceof MenuGui menuGui) {
                                player.openInventory(menuGui.getInventory());
                                menuGui.setMain();
                            }
                        }
                        default -> {
                            UUID targetUUID = UUID.fromString(data);
                            OfflinePlayer target = Bukkit.getOfflinePlayer(targetUUID);
                            if (e.isLeftClick()) {
                                guildManager.kickGuild(player, target);
                                player.openInventory(guildGui.getInventory());
                                guildGui.setMain();
                            } else if (e.isRightClick()) {
                                if (guild.isTempKickMember(targetUUID)) {
                                    guildManager.tempKickGuild(player, target, -1);
                                    player.openInventory(guildGui.getInventory());
                                    guildGui.setMain();
                                } else {
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
                    return;
                }
            }
        }
    }

}
