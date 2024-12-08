package me.msicraft.hardcoresurvival.API.CustomEvent;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.OfflinePlayer;

public class PlayerDataLoadEndEvent extends HardcoreSurvivalEvent {

    private final OfflinePlayer offlinePlayer;
    private final PlayerData playerData;

    public PlayerDataLoadEndEvent(OfflinePlayer offlinePlayer, PlayerData playerData) {
        this.offlinePlayer = offlinePlayer;
        this.playerData = playerData;
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}
