package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.Command.MainCommand;
import me.msicraft.hardcoresurvival.Command.MainTabCompleter;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyRelatedEvent;
import me.msicraft.hardcoresurvival.Event.EntityRelatedEvent;
import me.msicraft.hardcoresurvival.Event.PlayerRelatedEvent;
import me.msicraft.hardcoresurvival.PlayerData.Event.PlayerDataRelatedEvent;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class HardcoreSurvival extends JavaPlugin {

    private static HardcoreSurvival plugin;

    public static HardcoreSurvival getPlugin() {
        return plugin;
    }

    public static final String PREFIX = ChatColor.GREEN + "[HardcoreSurvival] ";

    private boolean useDebug = false;

    private Economy economy;

    private PlayerDataManager playerDataManager;
    private DeathPenaltyManager deathPenaltyManager;

    @Override
    public void onEnable() {
        plugin = this;
        createConfigFile();

        playerDataManager = new PlayerDataManager(this);
        deathPenaltyManager = new DeathPenaltyManager(this);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registeredEvents();
        registeredCommands();

        reloadVariables();

        getServer().getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + "플러그인이 활성화 되었습니다");
    }

    @Override
    public void onDisable() {
    }

    public void registeredEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerDataRelatedEvent(this), this);
        pluginManager.registerEvents(EntityRelatedEvent.getInstance(), this);
        pluginManager.registerEvents(PlayerRelatedEvent.getInstance(), this);
        pluginManager.registerEvents(new DeathPenaltyRelatedEvent(this), this);
    }

    public void registeredCommands() {
        PluginCommand pluginCommand = Bukkit.getPluginCommand("hardcoresurvival");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(new MainCommand(this));
            pluginCommand.setTabCompleter(new MainTabCompleter(this));
        }
    }

    public void reloadVariables() {
        reloadConfig();
        this.useDebug = getConfig().contains("Debug") && getConfig().getBoolean("Debug");

        deathPenaltyManager.reloadVariables();

        EntityRelatedEvent.getInstance().reloadVariables();
        PlayerRelatedEvent.getInstance().reloadVariables();
    }

    private void createConfigFile() {
        File configf = new File(getDataFolder(), "config.yml");
        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configf);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean useDebug() {
        return useDebug;
    }

    public Economy getEconomy() {
        return economy;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public DeathPenaltyManager getDeathPenaltyManager() {
        return deathPenaltyManager;
    }

}
