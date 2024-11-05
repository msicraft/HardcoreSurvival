package me.msicraft.hardcoresurvival.Guild.Task;

import me.msicraft.hardcoresurvival.Guild.Data.GuildSpawnLocation;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.scheduler.BukkitRunnable;

public class GuildSpawnTask extends BukkitRunnable {

    private final GuildSpawnLocation guildSpawnLocation;
    private int count = 0;

    public GuildSpawnTask(GuildSpawnLocation guildSpawnLocation) {
        this.guildSpawnLocation = guildSpawnLocation;

        this.runTaskTimer(HardcoreSurvival.getPlugin(), 20L, 20L);
    }

    @Override
    public void run() {
        if (guildSpawnLocation.getLocation() != null) {
            cancel();
            return;
        }
        if (guildSpawnLocation.update()) {
            cancel();
            return;
        }
        count++;
        if (count > 300) {
            cancel();
        }
    }
}
