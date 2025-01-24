package me.msicraft.hardcoresurvival;

public class Test {

    public static void main(String[] args) {
        int level = 1;
        double maxHealth = 20;
        double currentHealth = 20;
        for (int i = 0; i < maxHealth; i++) {
            double p = currentHealth / maxHealth;
            double multiplier = 1 + ((1 - p) * 0.065 * level);
            System.out.println("Percent: " + p + " | Multiplier: " + multiplier);
            currentHealth--;
        }
    }

}
