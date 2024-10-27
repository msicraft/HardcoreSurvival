package me.msicraft.hardcoresurvival.Menu.Event;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MenuGuiEvent implements Listener {

    private final HardcoreSurvival plugin;

    public MenuGuiEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    private void openMainMenu(Player player) {
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player);
        CustomGui customGui = playerData.getCustomGui(GuiType.MAIN);
        if (customGui instanceof MenuGui menuGui) {
            player.openInventory(menuGui.getInventory());
            menuGui.setMain();
        }
    }

    @EventHandler
    public void menuOpenEvent(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        ItemStack handStack = player.getInventory().getItemInMainHand();
        if (handStack != null && handStack.getType() == Material.AIR) {
            if (player.isSneaking()) {
                e.setCancelled(true);
                openMainMenu(player);
            }
        }
    }

    @EventHandler
    public void menuChatEditEvent(AsyncChatEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        String auctionSell = (String) playerData.getTempData("Menu_auction_sell", null);
        if (auctionSell != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("Menu_auction_sell");
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    openMainMenu(player);
                });
                return;
            }
            String ms = message.replaceAll("[^0-9]", "");
            if (ms.isEmpty()) {
                ms = "0";
            }
            int price = Integer.parseInt(ms);
            playerData.removeTempData("Menu_auction_sell");
            Bukkit.getScheduler().runTask(plugin, ()-> {
                Bukkit.getServer().dispatchCommand(player, "ah sell " + price);
            });
            return;
        }
        String nickName_first = (String) playerData.getTempData("NickName_First", null);
        if (nickName_first != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("NickName_First");
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    openMainMenu(player);
                });
                return;
            }
            String nickName = message.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]", "");
            if (nickName.length() > 8) {
                player.sendMessage(ChatColor.RED + "8 자리를 초과하였습니다");
            } else {
                playerData.setData("NickName", nickName);
                player.sendMessage(ChatColor.GREEN + "닉네임: " + nickName);
            }
            playerData.removeTempData("NickName_First");
            Bukkit.getScheduler().runTask(plugin, ()-> {
                plugin.getTeamManager().updateTeam(player);
                openMainMenu(player);
            });
            return;
        }
        String nickName_Change = (String) playerData.getTempData("NickName_Change", null);
        if (nickName_Change != null) {
            e.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(e.message());
            if (message.equalsIgnoreCase("cancel")) {
                playerData.removeTempData("NickName_Change");
                Bukkit.getScheduler().runTask(plugin, ()-> {
                    openMainMenu(player);
                });
                return;
            }
            String nickName = message.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]", "");
            if (nickName.length() > 8) {
                player.sendMessage(ChatColor.RED + "8 자리를 초과하였습니다");
            } else {
                playerData.setData("NickName", nickName);
                player.sendMessage(ChatColor.GREEN + "닉네임: " + nickName);
            }
            playerData.removeTempData("NickName_Change");
            Bukkit.getScheduler().runTask(plugin, ()-> {
                plugin.getTeamManager().updateTeam(player);
                openMainMenu(player);
            });
            return;
        }
    }

    @EventHandler
    public void menuGuiClickEvent(InventoryClickEvent e) {
        Inventory topInventory = e.getView().getTopInventory();
        if (topInventory.getHolder(false) instanceof MenuGui menuGui) {
            ClickType type = e.getClick();
            if (type == ClickType.NUMBER_KEY || type == ClickType.SWAP_OFFHAND
                    || type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            Player player = (Player) e.getWhoClicked();
            PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
            PlayerData playerData = playerDataManager.getPlayerData(player);

            ItemStack itemStack = e.getCurrentItem();
            if (itemStack != null) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                    if (dataContainer.has(MenuGui.MENU_KEY)) {
                        String data = dataContainer.get(MenuGui.MENU_KEY, PersistentDataType.STRING);
                        if (data != null) {
                            switch (data) {
                                case "personal-settings" -> {
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setPersonalSettings();
                                }
                                case "item-box" -> {
                                    plugin.getItemBoxManager().openItemBox(playerData);
                                }
                                case "shop" -> {
                                    plugin.getShopManager().openShopInventory(player, ShopGui.Type.BUY);
                                }
                                case "fish-shop" -> {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                                            "cfishing open market " + player.getName() + " --silent");
                                }
                                case "Guild" -> {
                                    GuildGui guildGui = (GuildGui) playerData.getCustomGui(GuiType.GUILD);
                                    player.openInventory(guildGui.getInventory());
                                    guildGui.setMain();
                                }
                                case "auction" -> {
                                    if (e.isLeftClick()) {
                                        Bukkit.getServer().dispatchCommand(player, "ah");
                                    } else if (e.isRightClick()) {
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.sendMessage(ChatColor.GRAY + "손에 있는 아이템을 경매장에 등록합니다");
                                        player.sendMessage(ChatColor.GRAY + "가격을 입력해주세요");
                                        player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                                        player.sendMessage(ChatColor.GRAY + "========================================");
                                        player.closeInventory();
                                        playerData.setTempData("Menu_auction_sell", "none");
                                    }
                                }
                                case "NickName-First" -> {
                                    player.sendMessage(ChatColor.GRAY + "========================================");
                                    player.sendMessage(ChatColor.GRAY + "닉네임을 입력해주세요.");
                                    player.sendMessage(ChatColor.GRAY + "숫자,영어,한글 만 사용가능합니다");
                                    player.sendMessage(ChatColor.GRAY + "최대 8자리(공백 포함)");
                                    player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                                    player.sendMessage(ChatColor.GRAY + "========================================");
                                    player.closeInventory();
                                    playerData.setTempData("NickName_First", "none");
                                }
                                case "NickName-Change" -> {
                                    player.sendMessage(ChatColor.RED + "현재 이용 불가능합니다");
                                    /*
                                    player.sendMessage(ChatColor.GRAY + "========================================");
                                    player.sendMessage(ChatColor.GRAY + "변경할 닉네임을 입력해주세요.");
                                    player.sendMessage(ChatColor.GRAY + "숫자 영어 한글 만 사용가능합니다");
                                    player.sendMessage(ChatColor.GRAY + "최대 6자리(공백 포함)");
                                    player.sendMessage(ChatColor.GRAY + "'cancel' 입력시 취소");
                                    player.sendMessage(ChatColor.GRAY + "========================================");
                                    player.closeInventory();
                                    playerData.setTempData("NickName_Change", "none");

                                     */
                                }
                            }
                        }
                    } else if (dataContainer.has(MenuGui.PERSONAL_SETTINGS_KEY)) {
                        String data = dataContainer.get(MenuGui.PERSONAL_SETTINGS_KEY, PersistentDataType.STRING);
                        if (data != null) {
                            switch (data) {
                                case "Back" -> {
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setMain();
                                }
                                default -> {
                                    PersonalOption personalOption = PersonalOption.valueOf(data);
                                    switch (personalOption) {
                                        case DISPLAY_ACTIONBAR, PRIVATE_CHEST -> {
                                            if (e.isLeftClick()) {
                                                boolean b = (boolean) playerData.getPersonalOption(personalOption);
                                                if (b) {
                                                    playerData.setPersonalOption(personalOption, false);
                                                } else {
                                                    playerData.setPersonalOption(personalOption, true);
                                                }
                                            } else if (e.isRightClick()) {
                                                playerData.setPersonalOption(personalOption, personalOption.getBaseValue());
                                            }
                                        }
                                    }
                                    player.openInventory(menuGui.getInventory());
                                    menuGui.setPersonalSettings();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
