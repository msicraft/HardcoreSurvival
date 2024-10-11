package me.msicraft.hardcoresurvival.PlayerData.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerTask extends BukkitRunnable {

    private final Player player;

    public PlayerTask(Player player) {
        this.player = player;

        if (HardcoreSurvival.getPlugin().useDebug()) {
            MessageUtil.sendDebugMessage("PlayerTask-Create", "Player: " + player.getName());
        }
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();

            if (HardcoreSurvival.getPlugin().useDebug()) {
                MessageUtil.sendDebugMessage("PlayerTask-Cancel", "Player: " + player.getName());
            }
            return;
        }
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        String currentWorldName = getCurrentWorldName(worldName);
        String tabListString = player.getName() + currentWorldName;

        player.setPlayerListName(tabListString);

        String sb = ChatColor.BOLD + "" + ChatColor.AQUA + currentWorldName
                + ChatColor.WHITE + " | " + ChatColor.GREEN +  "X: " + location.getBlockX() +
                " Y: " + location.getBlockY() + " Z: " + location.getBlockZ()
                + ChatColor.WHITE + " | " + ChatColor.GOLD  + TimeUtil.timeTo24Format(location.getWorld().getTime());
        player.sendActionBar(Component.text(sb));
    }

    private String getCurrentWorldName(String worldName) {
        String currentWorldName;
        if (worldName.equalsIgnoreCase("world")) {
            currentWorldName = ChatColor.BOLD + "" + ChatColor.AQUA + " [오버월드]";
        } else if (worldName.equalsIgnoreCase("world_nether")) {
            currentWorldName = ChatColor.BOLD + "" + ChatColor.AQUA + " [지옥]";
        } else if (worldName.equalsIgnoreCase("world_the_end")) {
            currentWorldName = ChatColor.BOLD + "" + ChatColor.AQUA + " [엔더]";
        } else {
            currentWorldName = ChatColor.BOLD + "" + ChatColor.AQUA + " [Unknown]";
        }
        return currentWorldName;
    }

}
