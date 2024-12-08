package me.msicraft.hardcoresurvival.PlayerData.Data;

public class CustomHealthRegen {

    private boolean disableVanillaRegen = false;
    private double base = 0;
    private int taskSeconds = -1;
    private int minFoodLevel = 6;

    public CustomHealthRegen() {
    }

    public double getBase() {
        return base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public int getTaskSeconds() {
        return taskSeconds;
    }

    public void setTaskSeconds(int taskSeconds) {
        this.taskSeconds = taskSeconds;
    }

    public boolean isDisableVanillaRegen() {
        return disableVanillaRegen;
    }

    public void setDisableVanillaRegen(boolean disableVanillaRegen) {
        this.disableVanillaRegen = disableVanillaRegen;
    }

    public int getMinFoodLevel() {
        return minFoodLevel;
    }

    public void setMinFoodLevel(int minFoodLevel) {
        this.minFoodLevel = minFoodLevel;
    }

}
