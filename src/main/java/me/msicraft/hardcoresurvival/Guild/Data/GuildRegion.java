package me.msicraft.hardcoresurvival.Guild.Data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class GuildRegion {

    private String worldName;
    private int centerX;
    private int centerY;
    private int centerZ;

    private Location guildSpawnLocation;

    public GuildRegion() {
    }

    public Location getGuildSpawnLocation() {
        if (guildSpawnLocation == null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                guildSpawnLocation = new Location(world, centerX, centerY, centerZ);
            }
        }
        return guildSpawnLocation;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public boolean hasGuildSpawnLocation() {
        return worldName == null;
    }

}
