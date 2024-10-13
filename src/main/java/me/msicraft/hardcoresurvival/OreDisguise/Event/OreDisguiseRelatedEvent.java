package me.msicraft.hardcoresurvival.OreDisguise.Event;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.OreDisguise.Data.OreDisguise;
import me.msicraft.hardcoresurvival.OreDisguise.OreDisguiseManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class OreDisguiseRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final OreDisguiseManager oreDisguiseManager;

    public OreDisguiseRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.oreDisguiseManager = plugin.getOreDisguiseManager();
    }

    @EventHandler
    public void playerBlockBreak(BlockBreakEvent e) {
        if (oreDisguiseManager.isEnabled()) {
            Block block = e.getBlock();
            if (oreDisguiseManager.hasOre(block)) {
                OreDisguise oreDisguise = oreDisguiseManager.getOreDisguise(block);
                String internalName = oreDisguise.getInternalName();
                MythicMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(internalName).orElse(null);
                if (mythicMob == null) {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("PlayerBlockBreak-Summon Fail OreDisguise is Null",
                                "InternalName: " + internalName);
                    }
                    return;
                }
                double chance = oreDisguise.getChance();
                Player player = e.getPlayer();
                Location spawnLocation = block.getLocation();
                double randomChance = Math.random();
                if (randomChance <= chance) {
                    ActiveMob activeMob = mythicMob.spawn(BukkitAdapter.adapt(spawnLocation), 1);
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("PlayerBlockBreak-Summon Success",
                                "InternalName: " + internalName,
                                "Player: " + player.getName());
                    }
                } else {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("PlayerBlockBreak-Summon Fail RandomChance",
                                "InternalName: " + internalName, "Player: " + player.getName());
                    }
                }
            }
        }
    }

}
