package me.msicraft.hardcoresurvival.DeathPenalty.Data;

import org.bukkit.Location;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DeathPenaltyChestLog {

    private final Set<Location> chestLocationSets = ConcurrentHashMap.newKeySet();

    public DeathPenaltyChestLog() {
    }

    public void addLocation(Location location) {
        chestLocationSets.add(location);
    }

    public void removeLocation(Location location) {
        chestLocationSets.remove(location);
    }

    public boolean hasLocation(Location location) {
        return chestLocationSets.contains(location);
    }

    public Set<Location> getChestLocationSets() {
        return chestLocationSets;
    }

    public void reset() {
        chestLocationSets.clear();
    }

}
