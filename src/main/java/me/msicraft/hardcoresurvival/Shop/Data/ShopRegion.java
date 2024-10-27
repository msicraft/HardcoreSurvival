package me.msicraft.hardcoresurvival.Shop.Data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ShopRegion {

    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    private int minY;
    private int maxY;

    private Location centerLocation = null;
    private String centerWorldName = null;
    private int centerX;
    private int centerY;
    private int centerZ;
    private int radius;

    public ShopRegion(String centerWorldName, int centerX, int centerY, int centerZ, int radius) {
        this.centerWorldName = centerWorldName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
    }

    public boolean update() {
        if (centerWorldName != null) {
            World world = Bukkit.getWorld(centerWorldName);
            if (world != null) {
                this.centerLocation = new Location(world, centerX, centerY, centerZ);
            }
            if (centerLocation != null) {
                this.minX = Math.min((centerLocation.getBlockX() + radius), (centerLocation.getBlockX() - radius));
                this.maxX = Math.max((centerLocation.getBlockX() + radius), (centerLocation.getBlockX() - radius));
                this.minZ = Math.min((centerLocation.getBlockZ() + radius), (centerLocation.getBlockZ() - radius));
                this.maxZ = Math.max((centerLocation.getBlockZ() + radius), (centerLocation.getBlockZ() - radius));
                this.minY = centerLocation.getBlockY() - 5;
                this.maxY = centerLocation.getBlockY() + 5;
                return true;
            }
        }
        return false;
    }

    public boolean contains(Location location) {
        if (centerLocation != null && centerLocation.getWorld().getName().equals(location.getWorld().getName())) {
            return contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
        return false;
    }

    private boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    @Override
    public String toString() {
        return "ShopRegion[world:" + centerLocation.getWorld().getName() +
                ", minX:" + minX +
                ", minY:" + minY +
                ", minZ:" + minZ +
                "\n maxX:" + maxX +
                ", maxY:" + maxY +
                ", maxZ:" + maxZ + "]";
    }

    public int getMinX() {
        return minX;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(int centerZ) {
        this.centerZ = centerZ;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public String getCenterWorldName() {
        return centerWorldName;
    }

    public void setCenterWorldName(String centerWorldName) {
        this.centerWorldName = centerWorldName;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }


}
