package me.msicraft.hardcoresurvival.PlayerData.Event;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataUnLoadEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;

    public PlayerDataRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        if (e.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            plugin.getPlayerDataManager().registerPlayerData(player);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);

        playerData.loadData();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataLoadEvent(playerData));
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        playerData.saveData();

        plugin.getPlayerDataManager().unregisterPlayerData(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getPluginManager().callEvent(new PlayerDataUnLoadEvent(playerData));
        });
    }

    @EventHandler
    public void playerDataLoad(PlayerDataLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
        playerData.updateTask(plugin.getPlayerTaskTick());
    }

    @EventHandler
    public void playerDataUnload(PlayerDataUnLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
    }

}
