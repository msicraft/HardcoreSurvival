package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.Guild.Task.GuildSpawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GuildSpawnLocation {

    private Location location = null;
    private String spawnWorldName;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    private GuildSpawnTask guildSpawnTask;

    public GuildSpawnLocation(String spawnWorldName, int spawnX, int spawnY, int spawnZ) {
        this.spawnWorldName = spawnWorldName;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
    }

    public boolean isInChunk(Player player) {
        if (location != null) {
            Chunk chunk = location.getChunk();
            Chunk playerChunk = player.getChunk();
            return chunk.getX() == playerChunk.getX() && chunk.getZ() == playerChunk.getZ();
        }
        return false;
    }

    public boolean update() {
        if (spawnWorldName != null) {
            World world = Bukkit.getWorld(spawnWorldName);
            if (world != null) {
                this.location = new Location(world, spawnX, spawnY, spawnZ);
                return true;
            }
        }
        return false;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getSpawnWorldName() {
        return spawnWorldName;
    }

    public void setSpawnWorldName(String spawnWorldName) {
        this.spawnWorldName = spawnWorldName;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public void setSpawnY(int spawnY) {
        this.spawnY = spawnY;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public void setSpawnZ(int spawnZ) {
        this.spawnZ = spawnZ;
    }
}
