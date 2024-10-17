package me.msicraft.hardcoresurvival.Shop.Data;

import org.bukkit.Location;

public class ShopRegion {

    private int minX;
    private int maxX;
    private int minZ;
    private int maxZ;
    private int minY;
    private int maxY;

    private Location centerLocation = null;

    public ShopRegion(Location center, int radius) {
        if (center != null) {
            update(center, radius);
        }
    }

    public void update(Location center, int radius) {
        this.centerLocation = center;
        this.minX = Math.min((center.getBlockX()+radius), (center.getBlockX()-radius));
        this.maxX = Math.max((center.getBlockX()+radius), (center.getBlockX()-radius));
        this.minZ = Math.min((center.getBlockZ()+radius), (center.getBlockZ()-radius));
        this.maxZ = Math.max((center.getBlockZ()+radius), (center.getBlockZ()-radius));
        this.minY = center.getBlockY() - 5;
        this.maxY = center.getBlockY() + 5;
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
}
