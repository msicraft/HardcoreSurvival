package me.msicraft.hardcoresurvival.DeathPenalty;

import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;

public class DeathPenaltyManager {

    private final HardcoreSurvival plugin;
    private boolean isEnabled = false;
    private Location spawnLocation = null;

    public DeathPenaltyManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        this.isEnabled = plugin.getConfig().contains("Setting.DeathPenalty.Enabled") && plugin.getConfig().getBoolean("Setting.DeathPenalty.Enabled");
        this.spawnLocation = plugin.getConfig().contains("Setting.DeathPenalty.SpawnLocation") ? plugin.getConfig().getLocation("Setting.DeathPenalty.SpawnLocation") : null;
    }

    public boolean isContainerMaterial(Material material) {
        if (material == Material.ENDER_CHEST) {
            return false;
        }
        String name = material.name();
        return name.contains("CHEST") || name.contains("SHULKER_BOX");
    }

    public void applyDeathPenalty(PlayerData playerData) {
        Player player = playerData.getPlayer();
        player.setLevel(0);
        player.setExp(0);
        player.getInventory().clear();
        player.getEnderChest().clear();

        DeathPenaltyChestLog deathPenaltyChestLog = playerData.getDeathPenaltyChestLog();
        deathPenaltyChestLog.getChestLocationList().forEach(location -> {
            Block block = location.getBlock();
            String materialName = block.getType().name();
            if (materialName.contains("CHEST")) {
                Chest chest = (Chest) block.getState();
                chest.getBlockInventory().clear();
                block.breakNaturally();
            } else if (materialName.contains("SHULKER_BOX")) {
                ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                shulkerBox.getInventory().clear();
                block.breakNaturally();
            }
        });
        deathPenaltyChestLog.reset();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public String locationToFormat(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
    }

    public Location formatToLocation(String format) {
        String[] split = format.split(":");
        String worldName = split[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        return new Location(world, x, y, z);
    }

}
