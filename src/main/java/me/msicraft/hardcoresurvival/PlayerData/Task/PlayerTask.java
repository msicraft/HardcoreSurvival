package me.msicraft.hardcoresurvival.PlayerData.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.WorldManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
            MessageUtil.sendDebugMessage("PlayerTask-Start", "Player: " + player.getName());
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
        String currentWorldName = worldManager.getCurrentWorldName(worldName);

        String tabListString = player.getName() + " " + currentWorldName;
        player.setPlayerListName(tabListString);

        boolean displayActionBar = (boolean) plugin.getPlayerDataManager().getPlayerData(player).getPersonalOption(PersonalOption.DISPLAY_ACTIONBAR, true);
        if (displayActionBar) {
            String sb = currentWorldName + ChatColor.WHITE + " | " + ChatColor.GREEN +"XYZ: " + location.getBlockX()
                    + " " + location.getBlockY() + " " + location.getBlockZ()
                    + ChatColor.WHITE + " | " + ChatColor.GOLD + worldManager.timeTo24Format(location.getWorld().getTime());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(sb));
        }
    }

}
