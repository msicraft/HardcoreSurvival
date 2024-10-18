package me.msicraft.hardcoresurvival.Guild;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {

    private final HardcoreSurvival plugin;
    private final Map<UUID, Guild> guildMap = new HashMap<>(); //leader-uuid, guild

    public GuildManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void registerGuild(UUID leaderUUID, Guild guild) {
        guildMap.put(leaderUUID, guild);
    }

    public Guild getGuild(UUID leaderUUID) {
        return guildMap.getOrDefault(leaderUUID, null);
    }

    public void removeGuild(UUID leaderUUID) {
        guildMap.remove(leaderUUID);
    }

}
