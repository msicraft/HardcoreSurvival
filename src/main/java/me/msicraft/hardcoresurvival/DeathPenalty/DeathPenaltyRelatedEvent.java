package me.msicraft.hardcoresurvival.DeathPenalty;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathPenaltyRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;

    public DeathPenaltyRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerPlaceBlock(BlockPlaceEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            Block placedBlock = e.getBlockPlaced();
            String materialName = placedBlock.getType().name();
            if (materialName.contains("CHEST") || materialName.contains("ENDER_CHEST")
                    || materialName.contains("SHULKER_BOX")) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                Location location = placedBlock.getLocation();
                playerData.getDeathPenaltyChestLog().addLocation(location);

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DeathPenaltyChestLog-Place",
                            "Player: " + player.getName(),
                            "Location: " + location.getWorld().getName()
                                    + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerBreakBlock(BlockBreakEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            Block block = e.getBlock();
            String materialName = block.getType().name();
            if (materialName.contains("CHEST") || materialName.contains("ENDER_CHEST")
                    || materialName.contains("SHULKER_BOX")) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                Location location = block.getLocation();
                playerData.getDeathPenaltyChestLog().removeLocation(location);

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DeathPenaltyChestLog-Break",
                            "Player: " + player.getName(),
                            "Location: " + location.getWorld().getName()
                                    + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDeath(PlayerDeathEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPostRespawn(PlayerPostRespawnEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        }
    }

}
