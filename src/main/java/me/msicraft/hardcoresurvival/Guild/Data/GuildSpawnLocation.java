package me.msicraft.hardcoresurvival.Guild.Data;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GuildSpawnLocation {

    private Location spawnLocation = null;
    private String spawnWorldName = null;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    public GuildSpawnLocation(FileConfiguration config) {
        String worldName = config.getString("Guild.Region.SpawnLocation.WorldName", null);
        int spawnX = config.getInt("Guild.Region.SpawnLocation.X", 0);
        int spawnY = config.getInt("Guild.Region.SpawnLocation.Y", 84);
        int spawnZ = config.getInt("Guild.Region.SpawnLocation.Z", 0);

        this.spawnWorldName = worldName;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        update();
    }

    public boolean isInSpawnLocation(Player player) {
        if (spawnLocation != null) {
            if (spawnLocation.getWorld().getName().equals(player.getWorld().getName())) {
                Chunk chunk = spawnLocation.getChunk();
                Chunk playerChunk = player.getChunk();
                return chunk.getX() == playerChunk.getX() && chunk.getZ() == playerChunk.getZ();
            }
        }
        return false;
    }

    public boolean update() {
        if (spawnWorldName != null) {
            World world = Bukkit.getWorld(spawnWorldName);
            if (world != null) {
                this.spawnLocation = new Location(world, spawnX, spawnY, spawnZ);
                return true;
            }
        }
        return false;
    }

    public void setGuildSpawnLocation(Location location) {
        setSpawnWorldName(location.getWorld().getName());
        setSpawnX(location.getBlockX());
        setSpawnY(location.getBlockY());
        setSpawnZ(location.getBlockZ());
        update();
    }

    public Location getSpawnLocation() {
        return spawnLocation;
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
