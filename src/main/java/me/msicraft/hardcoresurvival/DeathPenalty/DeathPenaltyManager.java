package me.msicraft.hardcoresurvival.DeathPenalty;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.entity.Player;

public class DeathPenaltyManager {

    private final HardcoreSurvival plugin;
    private boolean isEnabled = false;

    public DeathPenaltyManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        this.isEnabled = plugin.getConfig().contains("Setting.DeathPenalty.Enabled") && plugin.getConfig().getBoolean("Setting.DeathPenalty.Enabled");
    }

    public void applyDeathPenalty(Player player) {
    }

    public boolean isEnabled() {
        return isEnabled;
    }

}
