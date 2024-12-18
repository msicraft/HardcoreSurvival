package me.msicraft.hardcoresurvival.Menu;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
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
        this.gui = Bukkit.createInventory(this, 54, Component.text("메뉴"));
    }

    public void setMain() {
        gui.clear();
        ItemStack itemStack;
        itemStack = GuiUtil.createItemStack(Material.JUKEBOX, "개인 설정", GuiUtil.EMPTY_LORE, -1,
                MENU_KEY, "personal-settings");
        gui.setItem(0, itemStack);

        itemStack = GuiUtil.createItemStack(Material.HOPPER, "아이템 우편함", GuiUtil.EMPTY_LORE, -1,
                MENU_KEY, "item-box");
        gui.setItem(10, itemStack);

        if (playerData.getGuildUUID() != null) {
            itemStack = GuiUtil.createItemStack(Material.PLAYER_HEAD, "길드", GuiUtil.EMPTY_LORE, -1,
                    MENU_KEY, "Guild");
            gui.setItem(12, itemStack);
        }

        itemStack = GuiUtil.createItemStack(Material.PAPER, "장신구 인벤토리", GuiUtil.EMPTY_LORE, -1, MENU_KEY, "mmoinv");
        gui.setItem(14, itemStack);

        itemStack = GuiUtil.createItemStack(Material.ENDER_CHEST, "경매장",
                List.of(ChatColor.YELLOW + "좌 클릭: 열기", ChatColor.YELLOW + "우 클릭: 아이템 등록"), -1,
                MENU_KEY, "auction");
        gui.setItem(19, itemStack);

        Location location = playerData.getPlayer().getLocation();
        if (HardcoreSurvival.getPlugin().getShopManager().getShopRegion().contains(location)
                || HardcoreSurvival.getPlugin().getGuildManager().isInOwnGuildRegion(location, playerData.getPlayer(), false)) {
            double shopPenalty;
            Guild guild = HardcoreSurvival.getPlugin().getGuildManager().getGuild(playerData.getGuildUUID());
            if (guild== null) {
                shopPenalty = HardcoreSurvival.getPlugin().getShopManager().getNoGuildShopPenalty();
            } else {
                int overdue = guild.getGuildRegion().getOverdueDay(true);
                shopPenalty = HardcoreSurvival.getPlugin().getGuildManager().getShopPenalty(overdue) * 100.0;
            }
            String shopPenaltyFormat = String.format("%.2f", shopPenalty);
            itemStack = GuiUtil.createItemStack(Material.CHEST, "상점",
                    List.of(ChatColor.YELLOW + "길드에 속해있지 않을시 상점페널티가 발생합니다",
                            "", ChatColor.GRAY + "=====적용된 상점 페널티=====",
                            ChatColor.GRAY + "구매: " + ChatColor.RED + "+" + shopPenaltyFormat + "%",
                            ChatColor.GRAY + "판매: " + ChatColor.BLUE + "-" + shopPenaltyFormat + "%"),
                    -1, MENU_KEY, "shop");
            gui.setItem(20, itemStack);

            itemStack = GuiUtil.createItemStack(Material.BARREL, "물고기 판매 상점", GuiUtil.EMPTY_LORE, -1,
                    MENU_KEY, "fish-shop");
            gui.setItem(21, itemStack);
        }

        itemStack = GuiUtil.createItemStack("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFkYzA0OGE3Y2U3OGY3ZGFkNzJhMDdkYTI3ZDg1YzA5MTY4ODFlNTUyMmVlZWQxZTNkYWYyMTdhMzhjMWEifX19",
                "펫 관리", GuiUtil.EMPTY_LORE, -1, MENU_KEY, "Pets");
        gui.setItem(23, itemStack);
    }

    private static final int[] OPTION_SLOTS = new int[]{
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
                case DISPLAY_ACTIONBAR -> {
                    lore.add(ChatColor.GRAY + "현재 값: " + object);
                    itemStack = GuiUtil.createItemStack(Material.PAPER, option.getDisplayName(), lore, -1,
                            PERSONAL_SETTINGS_KEY, option.name());
                }
            }
            int slot = OPTION_SLOTS[count];
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
