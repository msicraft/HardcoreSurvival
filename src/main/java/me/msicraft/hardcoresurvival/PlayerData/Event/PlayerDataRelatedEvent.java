package me.msicraft.hardcoresurvival.PlayerData.Event;

import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadEndEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataLoadStartEvent;
import me.msicraft.hardcoresurvival.API.CustomEvent.PlayerDataUnLoadEvent;
import me.msicraft.hardcoresurvival.Guild.Data.Guild;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.PlayerData.Data.CustomHealthRegen;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.PlayerData.PlayerDataManager;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import me.msicraft.hardcoresurvival.Utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerDataRelatedEvent implements Listener {

    private final HardcoreSurvival plugin;
    private final PlayerDataManager playerDataManager;

    public PlayerDataRelatedEvent(HardcoreSurvival plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.isMaintenance()) {
            if (!player.isOp()) {
                e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                e.kickMessage(Component.text("점검 중 입니다"));
                return;
            }
        }

        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        if (playerData == null) {
            playerData = playerDataManager.createPlayerData(player.getUniqueId());
        }

        UUID guildUUID = playerData.getGuildUUID();
        if (guildUUID == null) {
            player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "길드에 속해있지 않을 경우 일부 행동에 불이익이 발생할 수 있습니다");
        } else {
            if (!player.isOp()) {
                Guild guild = plugin.getGuildManager().getGuild(guildUUID);
                if (guild.isTempKickMember(player.getUniqueId())) {
                    String leftTime = TimeUtil.getTimeToFormat(guild.getTempKickTime(player.getUniqueId()));
                    e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                    e.kickMessage(Component.text("임시 추방 상태입니다\n만료 기간: "
                            + leftTime));

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Guild-TempKick", "Player: " + player.getName(), "Expired Time: " + leftTime);
                    }
                } else {
                    guild.removeTempKickMember(player.getUniqueId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.kick(Component.text("플레이어 데이터가 존재하지 않습니다"));
            return;
        }
        if (!player.isOp()) {
            World world = player.getWorld();
            if (world.getName().equalsIgnoreCase("world")) {
                player.teleport(world.getSpawnLocation());
            }
        }
        playerData.resetTempData();
        playerData.setLastName(player.getName());
        playerData.setLastLogin(System.currentTimeMillis());
        playerData.updateTask(plugin.getPlayerTaskTick());
        playerData.setOfflinePlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

        if (playerData.hasData("DeathWorldName")) {
            plugin.getDeathPenaltyManager().applyDeathPenalty(playerData);
        }

        if (!playerData.hasTag("EcoPet_skeleton")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecopets give " + player.getName() + " skeleton");
            playerData.addTag("EcoPet_skeleton");
        }
        if (!playerData.hasTag("EcoPet_elephant")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecopets give " + player.getName() + " elephant");
            playerData.addTag("EcoPet_elephant");
        }
        if (!playerData.hasTag("EcoPet_tiger")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ecopets give " + player.getName() + " tiger");
            playerData.addTag("EcoPet_tiger");
        }
        if (!playerData.hasTag("hs_basic_kit-1")) {
            playerData.addTag("hs_basic_kit-1");
            ItemBox itemBox = playerData.getItemBox();
            long time = System.currentTimeMillis();
            itemBox.addItemBoxStack(new ItemBoxStack(new ItemStack(Material.BREAD, 32), time, "[시스템]", -1));
            itemBox.addItemBoxStack(new ItemBoxStack(new ItemStack(Material.LEATHER_CHESTPLATE, 1), time, "[시스템]", -1));
            itemBox.addItemBoxStack(new ItemBoxStack(new ItemStack(Material.LEATHER_BOOTS, 1), time, "[시스템]", -1));
            itemBox.addItemBoxStack(new ItemBoxStack(new ItemStack(Material.STONE_AXE, 1), time, "[시스템]", -1));
            itemBox.addItemBoxStack(new ItemBoxStack(new ItemStack(Material.STONE_PICKAXE, 1), time, "[시스템]", -1));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return;
        }

        playerData.resetTempData();
        playerData.setLastLogin(System.currentTimeMillis());
        playerData.setLastName(player.getName());
    }

    @EventHandler
    public void playerDataLoadStart(PlayerDataLoadStartEvent e) {
        PlayerData playerData = e.getPlayerData();

        Player player = playerData.getPlayer();
        if (player != null) {
            player.sendMessage(Component.text(ChatColor.GREEN + "데이터 로딩중..."));
        }
    }

    @EventHandler
    public void playerDataLoadEnd(PlayerDataLoadEndEvent e) {
        PlayerData playerData = e.getPlayerData();

        Player player = playerData.getPlayer();
        if (player != null) {
            player.sendMessage(Component.text(ChatColor.GREEN + "데이터 로딩완료"));
        }
    }

    @EventHandler
    public void playerDataUnload(PlayerDataUnLoadEvent e) {
        PlayerData playerData = e.getPlayerData();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerCombatUpdate(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            PlayerData playerData = playerDataManager.getPlayerData(player);
            if (playerData == null) {
                return;
            }
            playerData.updateCombat();
        }
    }

    @EventHandler
    public void healthRegen(EntityRegainHealthEvent e) {
        CustomHealthRegen customHealthRegen = playerDataManager.getCustomHealthRegen();
        if (customHealthRegen.isDisableVanillaRegen()) {
            if (e.getEntityType() == EntityType.PLAYER && e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                e.setCancelled(true);
            }
        }
    }

}
