package me.msicraft.hardcoresurvival.DeathPenalty.Event;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
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
            if (deathPenaltyManager.isContainerMaterial(placedBlock.getType())) {
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
            if (deathPenaltyManager.isContainerMaterial(block.getType())) {
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
            e.getItemsToKeep().clear();
            e.getDrops().clear();
            e.setDroppedExp(0);

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("DeathPenalty-Death", "Player: " + player.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPostRespawn(PlayerPostRespawnEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            deathPenaltyManager.applyDeathPenalty(playerData);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location spawnLocation = deathPenaltyManager.getSpawnLocation();
                if (spawnLocation != null) {
                    player.teleport(spawnLocation);
                }

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DeathPenalty-Respawn",
                            "Player: " + player.getName(), "Location: " + spawnLocation);
                }
            });
        }
    }

}
