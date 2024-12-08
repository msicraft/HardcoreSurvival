package me.msicraft.hardcoresurvival.PlayerData.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.CustomHealthRegen;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.WorldManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerTask extends BukkitRunnable {

    private final HardcoreSurvival plugin;
    private final PlayerData playerData;
    private final WorldManager worldManager;

    private int secondsCounter = 0;

    public PlayerTask(PlayerData playerData) {
        this.plugin = HardcoreSurvival.getPlugin();
        this.playerData = playerData;
        this.worldManager = plugin.getWorldManager();

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("PlayerTask-Init", "Player: " + playerData.getLastName());
        }
    }

    @Override
    public void run() {
        Player player = playerData.getPlayer();
        if (player == null) {
            cancel();

            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("PlayerTask-Cancel", "Player: " + playerData.getLastName());
            }
            return;
        }
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        String currentWorldName = worldManager.getCurrentWorldName(worldName);

        boolean displayActionBar = (boolean) plugin.getPlayerDataManager().getPlayerData(player).getPersonalOption(PersonalOption.DISPLAY_ACTIONBAR, true);
        if (displayActionBar) {
            String sb = currentWorldName + ChatColor.WHITE + " | " + ChatColor.GREEN + "XYZ: " + location.getBlockX()
                    + " " + location.getBlockY() + " " + location.getBlockZ()
                    + ChatColor.WHITE + " | " + ChatColor.GOLD + worldManager.timeTo24Format(location.getWorld().getTime());
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(sb));
        }

        CustomHealthRegen customHealthRegen = plugin.getPlayerDataManager().getCustomHealthRegen();
        int healthRegenSeconds = customHealthRegen.getTaskSeconds();
        if (healthRegenSeconds != -1 && secondsCounter % healthRegenSeconds == 0) {
            int minFoodLevel = customHealthRegen.getMinFoodLevel();
            if (player.getFoodLevel() >= minFoodLevel) {
                double regen = customHealthRegen.getBase();
                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = player.getHealth();
                double cal = currentHealth + regen;
                if (cal >= maxHealth) {
                    cal = maxHealth;
                }
                player.setHealth(cal);
            }
        }

        secondsCounter++;

        if (secondsCounter >= 60) {
            secondsCounter = 1;
        }
    }

}
