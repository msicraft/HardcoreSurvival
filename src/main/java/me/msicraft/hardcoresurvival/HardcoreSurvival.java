package me.msicraft.hardcoresurvival;

import me.msicraft.hardcoresurvival.API.MythicMobs.MythicMobsRegisterEvent;
import me.msicraft.hardcoresurvival.Command.MainCommand;
import me.msicraft.hardcoresurvival.Command.MainTabCompleter;
import me.msicraft.hardcoresurvival.CustomItem.CustomItemManager;
import me.msicraft.hardcoresurvival.CustomItem.Event.CustomItemRelatedEvent;
import me.msicraft.hardcoresurvival.DeathPenalty.DeathPenaltyManager;
import me.msicraft.hardcoresurvival.DeathPenalty.Event.DeathPenaltyRelatedEvent;
import me.msicraft.hardcoresurvival.Event.EntityRelatedEvent;
import me.msicraft.hardcoresurvival.Event.PlayerRelatedEvent;
import me.msicraft.hardcoresurvival.Guild.GuildManager;
import me.msicraft.hardcoresurvival.Guild.Menu.Event.GuildGuiEvent;
import me.msicraft.hardcoresurvival.ItemBox.Event.ItemBoxGuiEvent;
import me.msicraft.hardcoresurvival.ItemBox.ItemBoxManager;
import me.msicraft.hardcoresurvival.Menu.Event.MenuGuiEvent;
import me.msicraft.hardcoresurvival.OreDisguise.Event.OreDisguiseRelatedEvent;
import me.msicraft.hardcoresurvival.OreDisguise.OreDisguiseManager;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Event.PlayerDataRelatedEvent;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Shop.Menu.Event.ShopGuiEvent;
import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.TeamManager.TeamManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class HardcoreSurvival extends JavaPlugin {

    private static HardcoreSurvival plugin;

    public static HardcoreSurvival getPlugin() {
        return plugin;
    }

    public static final String PREFIX = ChatColor.GREEN + "[HardcoreSurvival] ";

    private final List<String> checkPluginNameList = List.of("MythicMobs", "Vault");

    private boolean useDebug = false;
    private int playerTaskTick = 20;
    private int combatSeconds = 10;

    private Economy economy;

    private PlayerDataManager playerDataManager;
    private DeathPenaltyManager deathPenaltyManager;
    private WorldManager worldManager;
    private OreDisguiseManager oreDisguiseManager;
    private ItemBoxManager itemBoxManager;
    private ShopManager shopManager;
    private CustomItemManager customItemManager;
    private GuildManager guildManager;
    private TeamManager teamManager;

    @Override
    public void onEnable() {
        plugin = this;
        createConfigFile();

        playerDataManager = new PlayerDataManager(this);
        deathPenaltyManager = new DeathPenaltyManager(this);
        worldManager = new WorldManager(this);
        oreDisguiseManager = new OreDisguiseManager(this);
        itemBoxManager = new ItemBoxManager(this);
        customItemManager = new CustomItemManager(this);
        shopManager = new ShopManager(this);
        guildManager = new GuildManager(this);
        teamManager = new TeamManager(this);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        for (String pluginName : checkPluginNameList) {
            if (pluginManager.getPlugin(pluginName) == null) {
                getServer().getConsoleSender().sendMessage(PREFIX + ChatColor.RED + pluginName + " 플러그인을 찾을 수 없습니다");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        registeredEvents();
        registeredCommands();

        reloadVariables();

        getServer().getConsoleSender().sendMessage(PREFIX + ChatColor.GREEN + "플러그인이 활성화 되었습니다");

        guildManager.loadGuild();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            if (playerData == null) {
                continue;
            }
            playerData.saveData();
            teamManager.unRegisterTeam(player);
        }

        shopManager.saveShopData();
        playerDataManager.saveData();
        guildManager.saveGuild();

        //teamManager.unRegisterAll();
    }

    public void registeredEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerDataRelatedEvent(this), this);
        pluginManager.registerEvents(EntityRelatedEvent.getInstance(), this);
        pluginManager.registerEvents(PlayerRelatedEvent.getInstance(), this);
        pluginManager.registerEvents(new DeathPenaltyRelatedEvent(this), this);
        pluginManager.registerEvents(new OreDisguiseRelatedEvent(this), this);
        pluginManager.registerEvents(new MenuGuiEvent(this), this);
        pluginManager.registerEvents(new ItemBoxGuiEvent(this), this);
        pluginManager.registerEvents(new ShopGuiEvent(this), this);
        pluginManager.registerEvents(new CustomItemRelatedEvent(this), this);
        pluginManager.registerEvents(new GuildGuiEvent(this),this);
        pluginManager.registerEvents(new MythicMobsRegisterEvent(this), this);
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

        playerDataManager.reloadVariables();
        deathPenaltyManager.reloadVariables();
        worldManager.reloadVariables();
        oreDisguiseManager.reloadVariables();
        customItemManager.reloadVariables();
        guildManager.reloadVariables();

        EntityRelatedEvent.getInstance().reloadVariables();
        PlayerRelatedEvent.getInstance().reloadVariables();

        this.useDebug = getConfig().contains("Debug") && getConfig().getBoolean("Debug");
        this.combatSeconds = getConfig().contains("Setting.CombatSeconds") ? getConfig().getInt("Setting.CombatSeconds") : 10;
        this.playerTaskTick = getConfig().contains("Setting.PlayerTaskTick") ? getConfig().getInt("Setting.PlayerTaskTick") : 20;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerDataManager.getPlayerData(player);
            if (playerData == null) {
                continue;
            }
            playerData.updateTask(playerTaskTick);
        }

        shopManager.reloadVariables();
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

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public int getPlayerTaskTick() {
        return playerTaskTick;
    }

    public OreDisguiseManager getOreDisguiseManager() {
        return oreDisguiseManager;
    }

    public ItemBoxManager getItemBoxManager() {
        return itemBoxManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    public int getCombatSeconds() {
        return combatSeconds;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

}
