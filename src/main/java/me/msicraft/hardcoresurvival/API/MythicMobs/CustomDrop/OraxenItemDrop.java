package me.msicraft.hardcoresurvival.API.MythicMobs.CustomDrop;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;

public class OraxenItemDrop implements IItemDrop {

    private final HardcoreSurvival plugin;
    private final String oraxenItemId;

    public OraxenItemDrop(HardcoreSurvival plugin, MythicLineConfig mlc) {
        this.plugin = plugin;
        this.oraxenItemId = mlc.getString(new String[]{"id"}, null);
    }

    @Override
    public AbstractItemStack getDrop(DropMetadata dropMetadata, double v) {
        if (oraxenItemId != null) {
            ItemBuilder itemBuilder = OraxenItems.getItemById(oraxenItemId);
            if (itemBuilder != null) {
                return BukkitAdapter.adapt(itemBuilder.build());
            }
        }
        return BukkitAdapter.adapt(GuiUtil.AIR_STACK);
    }

}
