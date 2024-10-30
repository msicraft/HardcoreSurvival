package me.msicraft.hardcoresurvival.API;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                    case "nickname" -> {
                        String s = (String) plugin.getPlayerDataManager().getPlayerData(onlineP.getUniqueId()).getData("NickName", null);
                        if (s == null) {
                            return "Unknown";
                        }
                        return s;
                    }
                    case "worldname" -> {
                        return plugin.getWorldManager().getCurrentWorldName(onlineP.getWorld().getName());
                    }
                }
            }
        }
        return null;
    }
}
