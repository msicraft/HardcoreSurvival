package me.msicraft.hardcoresurvival.API.CustomEvent;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;

public class PlayerDataUnLoadEvent extends HardcoreSurvivalEvent {

    private final Player player;
    private final PlayerData playerData;

    public PlayerDataUnLoadEvent(Player player, PlayerData playerData) {
        this.player = player;
        this.playerData = playerData;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }
}
