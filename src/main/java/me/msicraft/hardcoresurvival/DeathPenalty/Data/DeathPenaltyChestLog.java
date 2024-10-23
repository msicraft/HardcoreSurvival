package me.msicraft.hardcoresurvival.DeathPenalty.Data;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class DeathPenaltyChestLog {

    private final List<Location> chestLocationList = new ArrayList<>();

    public DeathPenaltyChestLog() {
    }

    public synchronized void addLocation(Location location) {
        chestLocationList.add(location);
    }

    public synchronized void removeLocation(Location location) {
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
