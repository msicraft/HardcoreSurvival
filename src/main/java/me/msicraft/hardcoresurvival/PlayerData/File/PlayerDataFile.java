package me.msicraft.hardcoresurvival.PlayerData.File;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PlayerDataFile {

    private final HardcoreSurvival plugin = HardcoreSurvival.getPlugin();

    private final String folderName = "PlayerData";

    private File file;
    private FileConfiguration config;

    public PlayerDataFile(UUID uuid) {
        String fileS = uuid + ".yml";
        this.file = new File(plugin.getDataFolder() + File.separator + folderName, fileS);
        if (!file.exists()) {
            createFile(uuid);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    private void createFile(UUID uuid) {
        if(!this.file.exists()) {
            try {
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.file);
                yamlConfiguration.set("UUID", uuid.toString());
                yamlConfiguration.save(this.file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            this.file = new File(plugin.getDataFolder() + File.separator + folderName, fileS);
        }
        if(!this.file.exists()) {
            plugin.saveResource(fileS, false);
        }
    }

    public File getFile() {
        return file;
    }

}
