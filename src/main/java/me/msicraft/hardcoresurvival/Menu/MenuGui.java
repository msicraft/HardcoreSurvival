package me.msicraft.hardcoresurvival.Menu;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuGui extends CustomGui {

    public static final NamespacedKey MENU_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "MenuGui_Main");
    public static final NamespacedKey PERSONAL_SETTINGS_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "MenuGui_PersonalSettings");

    private final Inventory gui;
    private final PlayerData playerData;

    public MenuGui(PlayerData playerData) {
        this.playerData = playerData;
        this.gui = Bukkit.createInventory(this, 54, Component.text("Menu"));
    }

    public void setMain() {
        gui.clear();
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.JUKEBOX, "개인 설정", GuiUtil.EMPTY_LORE, -1,
                MENU_KEY, "personal-settings");
        gui.setItem(10, itemStack);
        itemStack = GuiUtil.createItemStack(Material.HOPPER, "아이템 우편함", GuiUtil.EMPTY_LORE, -1,
                MENU_KEY, "item-box");
        gui.setItem(11, itemStack);
        itemStack = GuiUtil.createItemStack(Material.ENDER_CHEST, "경매장",
                List.of(ChatColor.YELLOW + "좌 클릭: 열기", ChatColor.YELLOW + "우 클릭: 아이템 등록"), -1,
                MENU_KEY, "auction");
        gui.setItem(12, itemStack);
        Location location = playerData.getPlayer().getLocation();
        if (HardcoreSurvival.getPlugin().getShopManager().getShopRegion().contains(location)) {
            itemStack = GuiUtil.createItemStack(Material.CHEST, "상점", GuiUtil.EMPTY_LORE, -1,
                    MENU_KEY, "shop");
            gui.setItem(19, itemStack);

            itemStack = GuiUtil.createItemStack(Material.BARREL, "물고기 판매 상점", GuiUtil.EMPTY_LORE, -1,
                    MENU_KEY, "fish-shop");
            gui.setItem(20, itemStack);
        }
        if (HardcoreSurvival.getPlugin().getPlayerDataManager().isStreamer(playerData.getPlayer().getUniqueId())) {
            itemStack = GuiUtil.createItemStack(Material.PAPER, "시청자 관리", GuiUtil.EMPTY_LORE, -1,
                    MENU_KEY, "Guild");
            gui.setItem(1, itemStack);
        }
        String nickName = (String) playerData.getData("NickName", null);
        if (nickName == null) {
            itemStack = GuiUtil.createItemStack(Material.NAME_TAG, "닉네임 변경",
                    List.of(ChatColor.WHITE + "최초 1회 무료 변경 가능합니다"), -1,
                    MENU_KEY, "NickName-First");
            gui.setItem(2, itemStack);
        } else {
            itemStack = GuiUtil.createItemStack(Material.NAME_TAG, "닉네임 변경",
                    List.of(ChatColor.WHITE + "현재 이용 불가능합니다"), -1,
                    MENU_KEY, "NickName-Change");
            gui.setItem(2, itemStack);
        }
    }

    private static final int[] optionSlots = new int[]{
            0,1,2,3,4,5,6,7,8,
            9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,
            27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44};

    public void setPersonalSettings() {
        gui.clear();
        ItemStack itemStack = GuiUtil.AIR_STACK;
        PersonalOption[] personalOptions = PersonalOption.values();
        int count = 0;
        for (PersonalOption option : personalOptions) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "좌 클릭: 변경");
            lore.add(ChatColor.YELLOW + "우 클릭: 리셋");
            lore.add("");
            Object object = playerData.getPersonalOption(option);
            switch (option) {
                case DISPLAY_ACTIONBAR, PRIVATE_CHEST -> {
                    lore.add(ChatColor.GRAY + "현재 값: " + object);
                    itemStack = GuiUtil.createItemStack(Material.PAPER, option.getDisplayName(), lore, -1,
                            PERSONAL_SETTINGS_KEY, option.name());
                }
            }
            int slot = optionSlots[count];
            gui.setItem(slot, itemStack);
            count++;
        }
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1,
                PERSONAL_SETTINGS_KEY, "Back");
        gui.setItem(45, itemStack);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }

}
