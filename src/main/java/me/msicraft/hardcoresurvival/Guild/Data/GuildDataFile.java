package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class GuildDataFile {

    private final HardcoreSurvival plugin = HardcoreSurvival.getPlugin();

    public static final String FOLDER_NAME = "Guild";
    public static final String BACK_UP_FOLDER_NAME = "Backup";

    private final UUID guildUUID;
    private File file;
    private FileConfiguration config;

    public GuildDataFile(UUID guildUUID) {
        this.guildUUID = guildUUID;
        String fileS = guildUUID + ".yml";
        this.file = new File(plugin.getDataFolder() + File.separator + FOLDER_NAME, fileS);
        if (!file.exists()) {
            createFile(guildUUID);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    private void createFile(UUID uuid) {
        if(!this.file.exists()) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
                yamlConfiguration.set("GuildUUID", uuid.toString());
                yamlConfiguration.save(this.file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean backup(File backupFolder) {
        String fileName = guildUUID + ".yml";
        File backupFile = new File(backupFolder, fileName);
        if (!backupFile.exists()) {
            backupFile.mkdirs();
        }
        File originalFile = getFile();
        try {
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ignored) {}
        return false;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void reloadConfig() {
        if(this.config == null) {
            return;
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        Reader defaultConfigStream;
        try {
            defaultConfigStream = new InputStreamReader(plugin.getResource(this.file.getName()), StandardCharsets.UTF_8);
            if(defaultConfigStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
                config.setDefaults(defaultConfig);
            }
        }catch(NullPointerException ex) {
            //ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            getConfig().save(this.file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig(Player player) {
        String fileS = player.getUniqueId() + ".yml";
        if(this.file == null) {
            this.file = new File(plugin.getDataFolder() + File.separator + FOLDER_NAME, fileS);
        }
        if(!this.file.exists()) {
            plugin.saveResource(fileS, false);
        }
    }

    public File getFile() {
        return file;
    }

}
