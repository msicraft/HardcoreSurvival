package me.msicraft.hardcoresurvival.API;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildRegion;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PAPIExpansion extends PlaceholderExpansion {

    private final HardcoreSurvival plugin;

    public PAPIExpansion(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "hs";
    }

    @Override
    public @NotNull String getAuthor() {
        return "msicraft";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player.isOnline()) {
            Player onlineP = player.getPlayer();
            if (onlineP != null) {
                switch (params) {
                    case "guild_prefix" -> {
                        Guild guild = plugin.getGuildManager().getGuild(onlineP);
                        if (guild == null) {
                            return "X";
                        }
                        String prefix = guild.getPrefix();
                        if (prefix == null) {
                            return "X";
                        }
                        return prefix;
                    }
                    case "worldname" -> {
                        return plugin.getWorldManager().getCurrentWorldName(onlineP.getWorld().getName());
                    }
                    case "chunk_info" -> {
                        Location location = onlineP.getLocation();
                        Chunk chunk = location.getChunk();
                        return chunk.getX() + " " + chunk.getZ();
                    }
                    case "chunk_owner" -> {
                        Location location = onlineP.getLocation();
                        Chunk chunk = location.getChunk();
                        PersistentDataContainer dataContainer = chunk.getPersistentDataContainer();
                        if (dataContainer.has(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING)) {
                            String guildRegionKey = dataContainer.get(GuildRegion.GUILD_REGION_KEY, PersistentDataType.STRING);
                            if (guildRegionKey != null) {
                                Guild guild = plugin.getGuildManager().getGuild(UUID.fromString(guildRegionKey));
                                if (guild != null) {
                                    String prefix = guild.getPrefix();
                                    if (prefix == null) {
                                        return "[X]";
                                    }
                                    return "[" + prefix + "] 길드의 땅";
                                }
                            }
                        }
                        return "[X]";
                    }
                }
            }
        }
        return null;
    }
}
