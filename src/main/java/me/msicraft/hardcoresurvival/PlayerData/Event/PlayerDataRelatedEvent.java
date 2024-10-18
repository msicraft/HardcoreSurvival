package me.msicraft.hardcoresurvival.PlayerData.Event;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataUnLoadEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final PlayerDataManager playerDataManager;

    public PlayerDataRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        if (!playerDataManager.hasWhiteList(player)) {
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            e.kickMessage(Component.text(plugin.getPlayerDataManager().getWhitelistMessage()));

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("WhiteList-Disable Join", "Player: " + player.getName());
            }
            return;
        }

        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerDataManager().registerPlayerData(player);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = playerDataManager.getPlayerData(player);

        playerData.loadData();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(playerData));
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerData playerData = playerDataManager.getPlayerData(player);
        playerData.setLastLogin(System.currentTimeMillis());
        playerData.saveData();

        playerDataManager.unregisterPlayerData(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataUnLoadEvent(playerData));
        });
    }

    @EventHandler
    public void playerDataLoad(PlayerDataLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
        playerData.updateTask(plugin.getPlayerTaskTick());
        playerData.setLastLogin(System.currentTimeMillis());

        if (playerData.getGuildUUID() == null) {
            playerData.getPlayer().kick(Component.text("접속 권한이 없거나 길드-UUID 가 없습니다"));
            return;
        }
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
