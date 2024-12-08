package me.msicraft.hardcoresurvival.Guild.Menu;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.Data.RegionOptions;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuildRegionOptionGui extends CustomGui {

    public static final NamespacedKey MAIN_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "GuildRegionOptions-Main");

    private final Inventory gui;
    private final HardcoreSurvival plugin;
    private final Guild guild;
    private final PlayerData playerData;

    public GuildRegionOptionGui(HardcoreSurvival plugin, PlayerData playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
        this.guild = plugin.getGuildManager().getGuild(playerData.getGuildUUID());
        this.gui = Bukkit.createInventory(this, 54, Component.text("땅 옵션"));
    }

    private static final int[] OPTION_SLOTS = new int[]{
            0,1,2,3,4,5,6,7,8,
            9,10,11,12,13,14,15,16,17,
            18,19,20,21,22,23,24,25,26,
            27,28,29,30,31,32,33,34,35,
            36,37,38,39,40,41,42,43,44};

    public void setMain() {
        gui.clear();
        GuildRegion guildRegion = guild.getGuildRegion();
        RegionOptions[] regionOptions = RegionOptions.values();
        ItemStack itemStack = GuiUtil.AIR_STACK;
        int count = 0;
        for (RegionOptions option : regionOptions) {
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "좌 클릭: 변경");
            lore.add(ChatColor.YELLOW + "우 클릭: 리셋");
            lore.add("");
            Object object = guildRegion.getRegionOption(option);
            switch (option) {
                case PRIVATE_CHEST, BLOCK_BREAK, BLOCK_PLACE -> {
                    lore.add(ChatColor.GRAY + "현재 값: " + object);
                    itemStack = GuiUtil.createItemStack(Material.PAPER, option.getDisplayName(), lore, -1,
                            MAIN_KEY, option.name());
                }
            }
            int slot = OPTION_SLOTS[count];
            gui.setItem(slot, itemStack);
            count++;
        }
        itemStack = GuiUtil.createItemStack(Material.BARRIER, "뒤로", GuiUtil.EMPTY_LORE, -1,
                MAIN_KEY, "Back");
        gui.setItem(45, itemStack);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui;
    }
}
