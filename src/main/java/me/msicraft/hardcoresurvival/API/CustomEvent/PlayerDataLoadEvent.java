package me.msicraft.hardcoresurvival.API.CustomEvent;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.entity.Player;

public class PlayerDataLoadEvent extends HardcoreSurvivalEvent {

    private final Player player;
    private final PlayerData playerData;

    public PlayerDataLoadEvent(Player player, PlayerData playerData) {
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
