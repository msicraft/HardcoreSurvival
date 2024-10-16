package me.msicraft.hardcoresurvival.PlayerData.Task;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import org.bukkit.scheduler.BukkitRunnable;

public class CombatTask extends BukkitRunnable {

    private final PlayerData playerData;

    private long lastHit = System.currentTimeMillis();

    public CombatTask(PlayerData playerData) {
        this.playerData = playerData;
        if (playerData.getPlayer().isOnline()) {
            this.runTaskTimer(HardcoreSurvival.getPlugin(), 20L, 20L);
        }
    }

    public void update() {
        this.lastHit = System.currentTimeMillis();
    }

    @Override
    public void run() {
        if (!playerData.getPlayer().isOnline()) {
            this.close();
        } else {
            long c = (long) (HardcoreSurvival.getPlugin().getCombatSeconds() * 1000L);
            if (this.lastHit + c < System.currentTimeMillis()) {
                this.close();
            }
        }
    }

    private void close() {
        playerData.setCombatTask(null);
        this.cancel();
    }

}
