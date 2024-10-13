package me.msicraft.hardcoresurvival.OreDisguise.Data;

public class OreDisguise {

    private final String internalName;
    private final double chance;

    public OreDisguise(String internalName, double chance) {
        this.internalName = internalName;
        this.chance = chance;
    }

    public String getInternalName() {
        return internalName;
    }

    public double getChance() {
        return chance;
    }

}
