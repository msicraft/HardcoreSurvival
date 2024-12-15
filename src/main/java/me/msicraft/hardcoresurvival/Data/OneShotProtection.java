package me.msicraft.hardcoresurvival.Data;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class OneShotProtection {

    private boolean isEnabled = false;
    private double healthPercent = -1;
    private int noDamageTicks = 0;
    private int cooldown = 0;

    public OneShotProtection() {
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public double getHealthPercent() {
        return healthPercent;
    }

    public void setHealthPercent(double healthPercent) {
        this.healthPercent = healthPercent;
    }

    public int getNoDamageTicks() {
        return noDamageTicks;
    }

    public void setNoDamageTicks(int noDamageTicks) {
        this.noDamageTicks = noDamageTicks;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isOSP(Player player) {
        if (healthPercent < 0) {
            return false;
        }
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double checkHealth = maxHealth * healthPercent;

        return player.getHealth() >= checkHealth;
    }

}
