package me.msicraft.hardcoresurvival.TeamManager;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeamManager {

    private final HardcoreSurvival plugin;
    private final PlayerDataManager playerDataManager;
    private final Scoreboard scoreboard;

    private final Map<UUID, Team> teamMap = new HashMap<>();

    public TeamManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        this.scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
    }

    public void unRegisterAll() {
        Set<UUID> sets = teamMap.keySet();
        for (UUID uuid : sets) {
            Team team = teamMap.get(uuid);
            team.unregister();
        }
        teamMap.clear();
    }

    public void registerTeam(Player player) {
        UUID uuid = player.getUniqueId();
        Team team = scoreboard.getTeam(uuid.toString());
        if (team == null) {
            team = scoreboard.registerNewTeam(uuid.toString());
        }
        if (!team.hasEntity(player)) {
            team.addEntity(player);
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        teamMap.put(uuid, team);
    }

    public void unRegisterTeam(Player player) {
        UUID uuid = player.getUniqueId();
        if (teamMap.containsKey(uuid)) {
            Team team = teamMap.get(uuid);
            team.removeEntity(player);
            team.unregister();
        }
    }

    public void updateTeam(Player player) {
        Team team = scoreboard.getTeam(player.getUniqueId().toString());
        if (team != null) {
            String nickName = (String) playerDataManager.getPlayerData(player).getData("NickName", null);
            if (nickName != null) {
                team.prefix(Component.text("[" + nickName + "] "));
            }
        }
    }

    public Team getTeam(Player player) {
        return teamMap.getOrDefault(player.getUniqueId(), null);
    }

}
