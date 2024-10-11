package me.msicraft.hardcoresurvival.API.CustomEvent;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;

public class PlayerDataLoadEvent extends HardcoreSurvivalEvent {

    private final PlayerData playerData;

    public PlayerDataLoadEvent(PlayerData playerData) {
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}
