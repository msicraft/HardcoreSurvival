package me.msicraft.hardcoresurvival.DeathPenalty.Event;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DeathPenaltyRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    public static final NamespacedKey BLOCK_OWNER_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "Block_Owner");

    public DeathPenaltyRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPlaceBlock(BlockPlaceEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            Block placedBlock = e.getBlockPlaced();
            if (deathPenaltyManager.isContainerMaterial(placedBlock.getType())) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                if (playerData == null) {
                    e.setCancelled(true);
                    MessageUtil.sendPlayerDataLoadingMessage(player);
                    return;
                }
                Location location = placedBlock.getLocation();
                playerData.getDeathPenaltyChestLog().addLocation(location);

                Chest chest = (Chest) placedBlock.getState();
                if (chest.getBlockData() instanceof org.bukkit.block.data.type.Chest chestType) {
                    if (chestType.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                        Location otherChestLocation = deathPenaltyManager.getOtherChestLocation(placedBlock, chestType);
                        if (otherChestLocation != null) {
                            playerData.getDeathPenaltyChestLog().addLocation(otherChestLocation);
                            if (plugin.useDebug()) {
                                MessageUtil.sendDebugMessage("DeathPenaltyChestLog-OtherChestPlace",
                                        "Place Player: " + player.getName());
                            }
                            if (otherChestLocation.getBlock().getState() instanceof TileState tileState) {
                                String owner = tileState.getPersistentDataContainer().get(BLOCK_OWNER_KEY, PersistentDataType.STRING);
                                if (owner != null) {
                                    UUID uuid = UUID.fromString(owner);
                                    OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(uuid);
                                    if (ownerPlayer.isOnline()) {
                                        PlayerData ownerPlayerData = plugin.getPlayerDataManager().getPlayerData((Player) ownerPlayer);
                                        ownerPlayerData.getDeathPenaltyChestLog().addLocation(location);
                                        if (plugin.useDebug()) {
                                            MessageUtil.sendDebugMessage("DeathPenaltyChestLog-OtherChestPlace-OtherChest",
                                                    "Place Player: " + player.getName(),
                                                    "Original Chest Owner: " + ownerPlayer.getName() + " | Status: online");
                                        }
                                    } else {
                                        CompletableFuture<OfflinePlayerData> future = CompletableFuture.supplyAsync(() -> {
                                            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
                                            offlinePlayerData.loadData();
                                            return offlinePlayerData;
                                        });
                                        future.thenAccept(offlinePlayerData -> {
                                            offlinePlayerData.getDeathPenaltyChestLog().addLocation(location);
                                            offlinePlayerData.saveData();

                                            if (plugin.useDebug()) {
                                                MessageUtil.sendDebugMessage("DeathPenaltyChestLog-OtherChestPlace-OtherChest",
                                                        "Place Player: " + player.getName(),
                                                        "Original Chest Owner: " + ownerPlayer.getName() + " | Status: offline");
                                            }
                                        });
                                    }
                                } else {
                                    if (plugin.useDebug()) {
                                        MessageUtil.sendDebugMessage("DeathPenaltyChestLog-OtherChestPlace",
                                                "Unknown Owner: " + location.getWorld().getName() +
                                                        " | X: " + location.getBlockX() +
                                                        " | Y: " + location.getBlockY() + " | Z: " + location.getBlockZ());
                                    }
                                }
                            }
                        }
                    }
                }

                if (placedBlock.getState() instanceof TileState tileState) {
                    tileState.getPersistentDataContainer().set(BLOCK_OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
                    tileState.update();
                }

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DeathPenaltyChestLog-Place",
                            "Player: " + player.getName(),
                            "Location: " + location.getWorld().getName()
                                    + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
            if (playerData != null) {
                if ((boolean) playerData.getData("IgnoreDeathPenalty", false)) {
                    e.setKeepInventory(true);
                    e.setKeepLevel(true);

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("DeathPenalty-Death-IgnorePenalty", "Player: " + player.getName());
                    }
                } else {
                    e.getItemsToKeep().clear();
                }
                e.getDrops().clear();
                e.setDroppedExp(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPostRespawn(PlayerPostRespawnEvent e) {
        DeathPenaltyManager deathPenaltyManager = plugin.getDeathPenaltyManager();
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

            boolean ignoreDeathPenalty = (boolean) playerData.getData("IgnoreDeathPenalty", false);
            if (ignoreDeathPenalty) {
                playerData.setData("IgnoreDeathPenalty", false);
                playerData.setData("LastIgnoreDeathPenaltyTime", System.currentTimeMillis());

                player.sendMessage(ChatColor.GREEN + "죽음 패널티 면역으로 인해 패널티가 적용되지않았습니다");
            } else {
                deathPenaltyManager.applyDeathPenalty(playerData);
            }

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("DeathPenalty-Ignore Status", "Player: " + player.getName(),
                        "Status: " + ignoreDeathPenalty);
            }

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
