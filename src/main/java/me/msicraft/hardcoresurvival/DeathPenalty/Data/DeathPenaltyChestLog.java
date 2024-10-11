package me.msicraft.hardcoresurvival.DeathPenalty.Data;

import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DeathPenaltyChestLog {

    private final List<Location> chestLocationList = new ArrayList<>();

    public DeathPenaltyChestLog(PlayerData playerData) {
    }

    public void addLocation(Location location) {
        chestLocationList.add(location);
    }

    public void removeLocation(Location location) {
        chestLocationList.remove(location);
    }

    public boolean hasLocation(Location location) {
        return chestLocationList.contains(location);
    }

    public List<Location> getChestLocationList() {
        return chestLocationList;
    }

    public void reset() {
        chestLocationList.clear();
    }

}
