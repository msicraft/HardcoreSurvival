package me.msicraft.hardcoresurvival.PlayerData.Event;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataUnLoadEvent;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final PlayerDataManager playerDataManager;

    public PlayerDataRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.isMaintenance()) {
            if (!player.isOp()) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.kickMessage(Component.text("점검 중 입니다"));
                return;
            }
        }

        if (!player.isOp()) {
            if (!playerDataManager.hasWhiteList(uuid)) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.kickMessage(Component.text(plugin.getPlayerDataManager().getWhitelistMessage()));

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("WhiteList-Disable Join", "Player: " + player.getName());
                }
                return;
            }
            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(uuid);
            offlinePlayerData.loadData();
            if (offlinePlayerData.getGuildUUID() == null) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.kickMessage(Component.text("접속 권한이 없거나 {G-UUID} 가 존재하지 않습니다")); //G-uuid = guild-uuid
                return;
            }
            Guild guild = plugin.getGuildManager().getGuild(offlinePlayerData.getGuildUUID());
            if (guild.isTempKickMember(player.getUniqueId())) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.kickMessage(Component.text("임시 추방 상태입니다\n만료 기간: "
                        + TimeUtil.getTimeToFormat(guild.getTempKickTime(player.getUniqueId()))));

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("TempKick-Disable Join", "Player: " + player.getName());
                }
                return;
            }
        }

        PlayerData playerData = new PlayerData(uuid, player);
        plugin.getPlayerDataManager().registerPlayerData(player, playerData);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        player.sendMessage(ChatColor.GREEN + "데이터 로딩중...");
        CompletableFuture<PlayerData> future = CompletableFuture.supplyAsync(() -> {
            return playerDataManager.getPlayerData(player);
        });
        future.thenAcceptAsync(playerData -> {
            playerData.loadData();
            playerData.updateTask(plugin.getPlayerTaskTick());
            playerData.setLastLogin(System.currentTimeMillis());
            player.sendMessage(ChatColor.GREEN + "데이터 로딩 완료");

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getTeamManager().updateTeam(player);
            });
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("PlayerData Loaded", "Player: " + player.getName());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        CompletableFuture<PlayerData> future = CompletableFuture.supplyAsync(() -> {
            return playerDataManager.getPlayerData(player);
        });
        future.thenAcceptAsync(playerData -> {
            playerData.setLastLogin(System.currentTimeMillis());
            playerData.saveData();

            playerDataManager.unregisterPlayerData(player);
        });

        plugin.getTeamManager().unRegisterTeam(player);
    }

    @EventHandler
    public void playerDataLoad(PlayerDataLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
        Player player = playerData.getPlayer();

        plugin.getTeamManager().registerTeam(player);
    }

    @EventHandler
    public void playerDataUnload(PlayerDataUnLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCombatUpdate(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            PlayerData playerData = playerDataManager.getPlayerData(player);
            playerData.updateCombat();
        }
    }

}
