package me.msicraft.hardcoresurvival.Task;

import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.Guild.Data.GuildDataFile;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerDataFile;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public class BackupTask extends BukkitRunnable {

    private final HardcoreSurvival plugin;

    public BackupTask(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage("플레이어/길드 데이터 백업중...");
        String folderName = TimeUtil.getTimeToFormat("yyyy-MM-dd-HH_mm_ss", start);
        File playerBackupFolder = new File(plugin.getDataFolder()
                + File.separator + PlayerDataFile.FOLDER_NAME
                + File.separator + PlayerDataFile.BACK_UP_FOLDER_NAME
                + File.separator + folderName);
        if (!playerBackupFolder.exists()) {
            playerBackupFolder.mkdirs();
        }
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        Set<UUID> uuids = playerDataManager.getPlayerUUIDs();
        int success = 0;
        int fail = 0;
        for (UUID uuid : uuids) {
            PlayerData playerData = playerDataManager.getPlayerData(uuid);
            playerData.saveData();

            if (playerData.getPlayerDataFile().backup(playerBackupFolder)) {
                success++;
            } else {
                fail++;
            }
        }
        long end = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage("플에이어 데이터 백업완료 | 성공: " + success
                + " | 실패: " + fail
                + " | " + (end - start) + "ms");
        success = 0;
        fail = 0;

        File guildBackupFolder = new File(plugin.getDataFolder()
                + File.separator + GuildDataFile.FOLDER_NAME
                + File.separator + GuildDataFile.BACK_UP_FOLDER_NAME
                + File.separator + folderName);
        if (!guildBackupFolder.exists()) {
            guildBackupFolder.mkdirs();
        }
        GuildManager guildManager = plugin.getGuildManager();
        Set<UUID> guildUUIDs = guildManager.getGuildUUIDs();
        for (UUID uuid : guildUUIDs) {
            Guild guild = guildManager.getGuild(uuid);
            guild.save();

            if (guild.getGuildDataFile().backup(guildBackupFolder)) {
                success++;
            } else {
                fail++;
            }
        }
        end = System.currentTimeMillis();
        Bukkit.getConsoleSender().sendMessage("길드 데이터 백업완료 | 성공: " + success
                + " | 실패: " + fail
                + " | " + (end - start) + "ms");
    }

}
