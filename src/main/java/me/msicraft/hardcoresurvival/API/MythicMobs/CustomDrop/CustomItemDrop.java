package me.msicraft.hardcoresurvival.API.MythicMobs.CustomDrop;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.BukkitAdapter;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;

public class CustomItemDrop implements IItemDrop {

    private final HardcoreSurvival plugin;
    private final String customItemId;

    public CustomItemDrop(HardcoreSurvival plugin, MythicLineConfig mlc) {
        this.plugin = plugin;
        this.customItemId = mlc.getString(new String[]{"id"}, null);
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double v) {
        if (customItemId != null) {
            CustomItemManager customItemManager = plugin.getCustomItemManager();
            CustomItem customItem = customItemManager.getCustomItem(customItemId);
            if (customItem != null) {
                return BukkitAdapter.adapt(customItem.getItemStack());
            }
        }
        return BukkitAdapter.adapt(GuiUtil.AIR_STACK);
    }

}
