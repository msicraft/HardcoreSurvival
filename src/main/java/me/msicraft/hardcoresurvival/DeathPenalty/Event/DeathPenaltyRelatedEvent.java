package me.msicraft.hardcoresurvival.DeathPenalty.Event;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildSpawnLocation;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class DeathPenaltyRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final DeathPenaltyManager deathPenaltyManager;

    public DeathPenaltyRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.deathPenaltyManager = plugin.getDeathPenaltyManager();
    }

    @EventHandler
    public void spawnChestOwnerEvent(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            Block chestBlock = e.getClickedBlock();
            if (chestBlock != null && deathPenaltyManager.isContainerMaterial(chestBlock.getType())) {
                if (chestBlock.getState() instanceof TileState tileState) {
                    PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
                    String owner = dataContainer.get(DeathPenaltyManager.CHEST_OWNER_KEY, PersistentDataType.STRING);
                    if (owner == null) {
                        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                        playerData.getDeathPenaltyChestLog().addLocation(chestBlock.getLocation());
                        dataContainer.set(DeathPenaltyManager.CHEST_OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
                        tileState.update();

                        if (plugin.useDebug()) {
                            Location location = chestBlock.getLocation();
                            MessageUtil.sendDebugMessage("ChestInteraction-NoChestOwner", "Player: " + player.getName(),
                                    "Location: " + player.getWorld().getName() + " X: " + location.getBlockX() +
                                    " Y: " + location.getBlockY() + " Z: " + location.getBlockZ());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPlaceBlock(BlockPlaceEvent e) {
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            Block placedBlock = e.getBlockPlaced();
            if (deathPenaltyManager.isContainerMaterial(placedBlock.getType())) {
                PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
                if (playerData == null) {
                    e.setCancelled(true);
                    return;
                }
                Location location = placedBlock.getLocation();
                playerData.getDeathPenaltyChestLog().addLocation(location);

                if (placedBlock.getType() != Material.BARREL) {
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
                                    String owner = tileState.getPersistentDataContainer().get(DeathPenaltyManager.CHEST_OWNER_KEY, PersistentDataType.STRING);
                                    if (owner != null) {
                                        UUID uuid = UUID.fromString(owner);
                                        PlayerData ownerPlayerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                                        if (ownerPlayerData != null) {
                                            ownerPlayerData.getDeathPenaltyChestLog().addLocation(location);
                                            if (plugin.useDebug()) {
                                                MessageUtil.sendDebugMessage("DeathPenaltyChestLog-OtherChestPlace-OwnerChest",
                                                        "Place Player: " + player.getName(),
                                                        "Owner Chest Owner: " + ownerPlayerData.getLastName());
                                            }
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
                }

                if (placedBlock.getState() instanceof TileState tileState) {
                    tileState.getPersistentDataContainer().set(DeathPenaltyManager.CHEST_OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
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

    @EventHandler
    public void privateChestToPistonExtendedEvent(BlockPistonExtendEvent e) {
        List<Block> list = e.getBlocks();
        boolean check = false;
        for (Block block : list) {
            if (deathPenaltyManager.isContainerMaterial(block.getType())) {
                String chestOwner = deathPenaltyManager.getChestOwner(block);
                if (chestOwner != null) {
                    check = true;
                    break;
                }
            }
        }
        if (check) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void privateChestToPistonRetractEvent(BlockPistonRetractEvent e) {
        List<Block> list = e.getBlocks();
        boolean check = false;
        for (Block block : list) {
            if (deathPenaltyManager.isContainerMaterial(block.getType())) {
                String chestOwner = deathPenaltyManager.getChestOwner(block);
                if (chestOwner != null) {
                    check = true;
                    break;
                }
            }
        }
        if (check) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerBreakBlock(BlockBreakEvent e) {
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            Block block = e.getBlock();
            if (deathPenaltyManager.isContainerMaterial(block.getType())) {
                String chestOwner = deathPenaltyManager.getChestOwner(block);
                if (chestOwner != null) {
                    if (!chestOwner.equalsIgnoreCase(player.getUniqueId().toString())) {
                        player.sendMessage(ChatColor.RED + "해당 상자는 파괴가 불가능합니다");
                        e.setCancelled(true);
                        return;
                    }
                }

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
        if (deathPenaltyManager.isEnabled()) {
            Player player = e.getPlayer();
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

            boolean ignoreDeathPenalty = (boolean) playerData.getData("IgnoreDeathPenalty", false);
            if (!ignoreDeathPenalty) {
                playerData.setData("DeathWorldName", player.getWorld().getName());
                playerData.setData("DeathLastFoodLevel", player.getFoodLevel());
            }

            playerData.setTempData("DeathDamageType", e.getDamageSource().getDamageType().getKey().toString());

            e.setKeepInventory(true);
            e.setKeepLevel(true);
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerPostRespawn(PlayerPostRespawnEvent e) {
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
                        "Apply Status: " + ignoreDeathPenalty);
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                Location spawnLocation = null;
                UUID guildUUID = playerData.getGuildUUID();
                if (guildUUID != null) {
                    Guild guild = plugin.getGuildManager().getGuild(playerData.getGuildUUID());
                    if (guild != null) {
                        GuildSpawnLocation guildSpawnLocation = guild.getGuildRegion().getGuildSpawnLocation();
                        Location guildSpawnLoc = guildSpawnLocation.getSpawnLocation();
                        if (guildSpawnLoc != null) {
                            if (plugin.getGuildManager().isInOwnGuildRegion(guildSpawnLoc, player, true)) {
                                spawnLocation = guildSpawnLoc;
                            }
                        }
                    }
                }
                if (spawnLocation == null) {
                    spawnLocation = plugin.getShopManager().getShopRegion().getCenterLocation();
                }
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
