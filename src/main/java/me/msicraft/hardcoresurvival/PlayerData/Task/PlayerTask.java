package me.msicraft.hardcoresurvival.PlayerData.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerTask extends BukkitRunnable {

    private final HardcoreSurvival plugin;
    private final Player player;
    private final WorldManager worldManager;

    public PlayerTask(Player player) {
        this.plugin = HardcoreSurvival.getPlugin();
        this.player = player;
        this.worldManager = plugin.getWorldManager();

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("PlayerTask-Create", "Player: " + player.getName());
        }
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("PlayerTask-Cancel", "Player: " + player.getName());
            }
            return;
        }
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        String currentWorldName = worldManager.getCurrentWorldName(worldName, true);
        String tabListString = player.getName() + currentWorldName;

        player.setPlayerListName(tabListString);

        String sb = ChatColor.BOLD + "" + ChatColor.AQUA + currentWorldName
                + ChatColor.WHITE + " | " + ChatColor.GREEN +  "X: " + location.getBlockX() +
                " Y: " + location.getBlockY() + " Z: " + location.getBlockZ()
                + ChatColor.WHITE + " | " + ChatColor.GOLD  + worldManager.timeTo24Format(location.getWorld().getTime());
        player.sendActionBar(Component.text(sb));
    }

}
