package me.msicraft.hardcoresurvival.DeathPenalty.Data;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class DeathPenaltyChestLog {

    private final Set<Location> chestLocationSets = new HashSet<>();

    public DeathPenaltyChestLog() {
    }

    public synchronized void addLocation(Location location) {
        chestLocationSets.add(location);
    }

    public synchronized void removeLocation(Location location) {
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
