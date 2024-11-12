package me.msicraft.hardcoresurvival.Guild.Event;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.Guild.Data.RegionOptions;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import net.momirealms.customcrops.api.event.*;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GuildRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final GuildManager guildManager;

    public GuildRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void guildRegionPrivateChest(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            if (player.isOp()) {
                return;
            }
            Block chestBlock = e.getClickedBlock();
            if (chestBlock != null && plugin.getDeathPenaltyManager().isContainerMaterial(chestBlock.getType())) {
                Location location = chestBlock.getLocation();
                Chunk chunk = location.getChunk();
                PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                    String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
                    if (guildRegionKey != null) {
                        UUID guildUUID = UUID.fromString(guildRegionKey);
                        Guild guild = plugin.getGuildManager().getGuild(guildUUID);
                        if (guild != null) {
                            GuildRegion guildRegion = guild.getGuildRegion();
                            if ((boolean) guildRegion.getRegionOption(RegionOptions.PRIVATE_CHEST)) {
                                if (!guild.isMember(player)) {
                                    e.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "해당 상자는 잠겨있습니다");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void guildRegionBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if (player.isOp()) {
            return;
        }
        Location location = block.getLocation();
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                UUID guildUUID = UUID.fromString(guildRegionKey);
                Guild guild = plugin.getGuildManager().getGuild(guildUUID);
                if (guild != null) {
                    GuildRegion guildRegion = guild.getGuildRegion();
                    if ((boolean) guildRegion.getRegionOption(RegionOptions.BLOCK_BREAK)) {
                        if (!guild.isMember(player)) {
                            e.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "다른 길드의 땅의 블록은 파괴 불가능합니다");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void guildRegionBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        if (player.isOp()) {
            return;
        }
        Location location = block.getLocation();
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                UUID guildUUID = UUID.fromString(guildRegionKey);
                Guild guild = plugin.getGuildManager().getGuild(guildUUID);
                if (guild != null) {
                    GuildRegion guildRegion = guild.getGuildRegion();
                    if ((boolean) guildRegion.getRegionOption(RegionOptions.BLOCK_PLACE)) {
                        if (!guild.isMember(player)) {
                            e.setCancelled(true);
                            player.sendMessage(ChatColor.RED + "다른 길드의 땅에는 블록을 설치할 수 없습니다");
                        }
                    }
                }
            }
        }
    }

    private boolean canUseGuildRegion(Player player, Location location) {
        Chunk chunk = location.getChunk();
        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
            if (guildRegionKey != null) {
                UUID guildUUID = UUID.fromString(guildRegionKey);
                Guild guild = plugin.getGuildManager().getGuild(guildUUID);
                if (guild != null) {
                    if (guild.isMember(player)) {
                        GuildRegion guildRegion = guild.getGuildRegion();
                        int overDueDay = guildRegion.getOverdueDay(false);
                        return overDueDay == 0;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void customCropPlant(CropPlantEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 작물을 심을 수 있습니다");
        }
    }

    @EventHandler
    public void customCropBreak(CropBreakEvent e) {
        Entity entity = e.entityBreaker();
        if (entity instanceof Player player) {
            if (player.isOp()) {
                return;
            }
            if (!canUseGuildRegion(player, e.location())) {
                e.setCancelled(true);
                player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 작물을 파괴할 수 있습니다");
            }
        }
    }

    @EventHandler
    public void customCropInteract(CropInteractEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 작물을 수확할 수 있습니다");
        }
    }

    @EventHandler
    public void customCropPotPlace(PotPlaceEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 경작지을 설치할 수 있습니다");
        }
    }

    @EventHandler
    public void customCropPotFill(PotFillEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 경작지에 물을 줄 수 있습니다");
        }
    }

    @EventHandler
    public void customCropSprinklerPlace(SprinklerPlaceEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 스프링클러를 설치할 수 있습니다");
        }
    }

    @EventHandler
    public void customCropSprinklerFill(SprinklerFillEvent e) {
        Player player = e.getPlayer();
        if (player.isOp()) {
            return;
        }
        if (!canUseGuildRegion(player, e.location())) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "만료되지 않은 자신의 길드 땅에서만 스프링클러에 물을 채울수 있습니다");
        }
    }

}
