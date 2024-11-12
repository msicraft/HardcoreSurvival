package me.msicraft.hardcoresurvival.PlayerData.Data;

import me.msicraft.hardcoresurvival.Guild.Menu.GuildGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionGui;
import me.msicraft.hardcoresurvival.Guild.Menu.GuildRegionOptionGui;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Menu.ItemBoxGui;
import me.msicraft.hardcoresurvival.Menu.Data.CustomGui;
import me.msicraft.hardcoresurvival.Menu.Data.GuiType;
import me.msicraft.hardcoresurvival.Menu.MenuGui;
import me.msicraft.hardcoresurvival.PlayerData.Task.CombatTask;
import me.msicraft.hardcoresurvival.PlayerData.Task.PlayerTask;
import me.msicraft.hardcoresurvival.Shop.Menu.ShopGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData extends OfflinePlayerData {

    private final Player player;
    private final Map<GuiType, CustomGui> customGuiMap = new HashMap<>();
    private final Map<String, Object> tempDataMap = new HashMap<>();
    private PlayerTask playerTask;
    private CombatTask combatTask;

    public PlayerData(UUID uuid, Player player) {
        super(uuid);
        this.player = player;
    }

    public void updateTask(int ticks) {
        if (playerTask != null) {
            playerTask.cancel();
        }
        playerTask = new PlayerTask(this);
        playerTask.runTaskTimer(HardcoreSurvival.getPlugin(), 0, ticks);
    }

    public CustomGui getCustomGui(GuiType guiType) {
        CustomGui customGui = null;
        if (customGuiMap.containsKey(guiType)) {
            customGui = customGuiMap.get(guiType);
        }
        if (customGui == null) {
            switch (guiType) {
                case MAIN -> {
                    customGui = new MenuGui(this);
                    customGuiMap.put(guiType, customGui);
                }
                case ITEM_BOX -> {
                    customGui = new ItemBoxGui(this);
                    customGuiMap.put(guiType, customGui);
                }
                case SHOP -> {
                    customGui = new ShopGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD -> {
                    customGui = new GuildGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD_REGION -> {
                    customGui = new GuildRegionGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                case GUILD_REGION_OPTIONS -> {
                    customGui = new GuildRegionOptionGui(HardcoreSurvival.getPlugin(), this);
                    customGuiMap.put(guiType, customGui);
                }
                default -> {
                    customGui = new MenuGui(this);
                    Bukkit.getConsoleSender().sendMessage(HardcoreSurvival.PREFIX + ChatColor.YELLOW + "플레이어: " + player.getName(),
                            ChatColor.YELLOW + "메뉴 생성중 기본값 사용이 발생하였습니다.");
                }
            }
        }
        return customGui;
    }

    public PlayerTask getPlayerTask() {
        return playerTask;
    }

    public Player getPlayer() {
        return player;
    }

    public void setTempData(String key, Object object) {
        tempDataMap.put(key, object);
    }

    public Object getTempData(String key) {
        return tempDataMap.getOrDefault(key, null);
    }

    public Object getTempData(String key, Object def) {
        Object object = getTempData(key);
        if (!hasTempData(key) || object == null) {
            return def;
        }
        return object;
    }

    public boolean hasTempData(String key) {
        return tempDataMap.containsKey(key);
    }

    public void removeTempData(String key) {
        tempDataMap.remove(key);
    }

    public void setCombatTask(CombatTask combatTask) {
        this.combatTask = combatTask;
    }

    public boolean isInCombat() {
        return this.combatTask != null;
    }

    public void updateCombat() {
        if (isInCombat()) {
            combatTask.update();
        } else {
            combatTask = new CombatTask(this);
        }
    }


}
