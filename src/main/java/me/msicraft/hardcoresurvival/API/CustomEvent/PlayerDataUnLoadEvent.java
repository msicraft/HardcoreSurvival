package me.msicraft.hardcoresurvival.API.CustomEvent;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;

public class PlayerDataUnLoadEvent extends HardcoreSurvivalEvent {

    private final PlayerData playerData;

    public PlayerDataUnLoadEvent(PlayerData playerData) {
        this.playerData = playerData;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}
