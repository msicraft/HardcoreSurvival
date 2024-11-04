package me.msicraft.hardcoresurvival.API.MythicMobs.CustomConditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HasMMOItems implements IEntityCondition {

    private final HardcoreSurvival plugin;
    private final String type;
    private final String id;

    public HasMMOItems(HardcoreSurvival plugin, MythicLineConfig mlc) {
        this.plugin = plugin;
        this.type = mlc.getString(new String[]{"type"}, null);
        this.id = mlc.getString(new String[]{"id"}, null);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        Entity entity = BukkitAdapter.adapt(abstractEntity);
        if (type != null && id != null) {
            if (entity instanceof Player player) {
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                NBTItem nbtItem = NBTItem.get(itemStack);
                if (nbtItem.hasType()) {
                    String typeString = nbtItem.getType().toUpperCase();
                    String idString = nbtItem.getString("MMOITEMS_ITEM_ID");
                    return type.equalsIgnoreCase(typeString) && id.equalsIgnoreCase(idString);
                }
            }
        }
        return false;
    }
}
