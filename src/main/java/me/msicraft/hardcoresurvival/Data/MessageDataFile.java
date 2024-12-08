package me.msicraft.hardcoresurvival.Data;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class MessageDataFile {

    private final HardcoreSurvival plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public MessageDataFile(HardcoreSurvival plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), "messages.yml");

        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        /*
        InputStream defaultStream = plugin.getResource("shopData.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }

         */
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }

    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            this.configFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!this.configFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

}
