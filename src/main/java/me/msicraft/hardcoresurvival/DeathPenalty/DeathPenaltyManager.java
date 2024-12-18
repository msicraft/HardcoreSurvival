package me.msicraft.hardcoresurvival.DeathPenalty;

import fr.maxlego08.zauctionhouse.api.AuctionItem;
import fr.maxlego08.zauctionhouse.api.AuctionManager;
import io.lumine.mythic.lib.api.item.NBTItem;
import me.msicraft.hardcoresurvival.DeathPenalty.Data.DeathPenaltyChestLog;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBox;
import me.msicraft.hardcoresurvival.ItemBox.Data.ItemBoxStack;
import me.msicraft.hardcoresurvival.ItemBox.ItemBoxManager;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class DeathPenaltyManager {

    public enum ActionType {
        DISABLED, CHEST, INVENTORY, BALANCE, ITEMBOX, AUCTION
    }

    private final HardcoreSurvival plugin;
    private boolean isEnabled = false;
    private double balancePercent = 0;
    private double removeChanceForSlot = 1.0;

    private final Map<String, Set<ActionType>> worldActionMap = new HashMap<>();

    public DeathPenaltyManager(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void reloadVariables() {
        this.isEnabled = plugin.getConfig().contains("Setting.DeathPenalty.Enabled") && plugin.getConfig().getBoolean("Setting.DeathPenalty.Enabled");
        this.balancePercent = plugin.getConfig().contains("Setting.DeathPenalty.BalancePercent") ? plugin.getConfig().getDouble("Setting.DeathPenalty.BalancePercent") : 0;
        this.removeChanceForSlot = plugin.getConfig().contains("Setting.DeathPenalty.RemoveChanceForSlot")? plugin.getConfig().getDouble("Setting.DeathPenalty.RemoveChanceForSlot") : 1.0;

        ConfigurationSection worldActionSection = plugin.getConfig().getConfigurationSection("Setting.DeathPenalty.WorldList");
        if (worldActionSection != null) {
            Set<String> worlds = worldActionSection.getKeys(false);
            for (String worldName : worlds) {
                List<String> actionList = plugin.getConfig().getStringList("Setting.DeathPenalty.WorldList." + worldName);
                Set<ActionType> sets;
                if (worldActionMap.containsKey(worldName)) {
                    sets = worldActionMap.get(worldName);
                    sets.clear();
                } else {
                    sets = new HashSet<>();
                }
                for (String actionS : actionList) {
                    ActionType actionType;
                    try {
                        actionType = ActionType.valueOf(actionS.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        actionType = ActionType.DISABLED;
                    }
                    sets.add(actionType);
                }
                worldActionMap.put(worldName, sets);

                if (plugin.useDebug()) {
                    MessageUtil.sendDebugMessage("DeathPenalty-WorldActionTypes", "WorldName: " + worldName, "ActionTypes: " + sets);
                }
            }
        } else {
            worldActionMap.clear();
        }
    }

    public boolean isContainerMaterial(Material material) {
        if (material == Material.ENDER_CHEST) {
            return false;
        }
        String name = material.name();
        return isContainerMaterial(name);
    }

    public boolean isContainerMaterial(String materialName) {
        return materialName.contains("CHEST") || materialName.contains("SHULKER_BOX") || materialName.contains("BARREL");
    }

    private final List<Integer> armorSlots = List.of(36, 37, 38, 39);

    public void applyDeathPenalty(PlayerData playerData) {
        String deathWorldName = (String) playerData.getData("DeathWorldName", "world");
        Player player = playerData.getPlayer();
        player.setFoodLevel((int) playerData.getData("DeathLastFoodLevel", 20));
        Set<ActionType> sets = getWorldActionTypes(deathWorldName);
        for (ActionType actionType : sets) {
            switch (actionType) {
                case DISABLED -> {
                }
                case INVENTORY -> {
                    player.setLevel(0);
                    player.setExp(0);
                    player.getEnderChest().clear();
                    int max = player.getInventory().getSize();
                    for (int i = 0; i<max; i++) {
                        ItemStack itemStack = player.getInventory().getItem(i);
                        if (itemStack != null && itemStack.getType() != Material.AIR) {
                            if (armorSlots.contains(i)) {
                                if (itemStack.getItemMeta() instanceof Damageable damageable) {
                                    NBTItem nbtItem = NBTItem.get(itemStack);
                                    int maxDamage;
                                    if (nbtItem.hasTag("MMOITEMS_MAX_ITEM_DAMAGE")) {
                                        maxDamage = (int) nbtItem.getDouble("MMOITEMS_MAX_ITEM_DAMAGE");
                                    } else {
                                        maxDamage = itemStack.getType().getMaxDurability();
                                    }
                                    int applyDamage = (int) (maxDamage * 0.2);
                                    if (damageable.hasDamage()) {
                                        applyDamage = damageable.getDamage() + applyDamage;
                                    }
                                    if (applyDamage > maxDamage) {
                                        applyDamage = maxDamage - 1;
                                    }
                                    damageable.setDamage(applyDamage);
                                    itemStack.setItemMeta(damageable);
                                }
                            } else {
                                if (Math.random() <= removeChanceForSlot) {
                                    player.getInventory().setItem(i, GuiUtil.AIR_STACK);
                                }
                            }
                        }
                    }
                }
                case BALANCE -> {
                    double balance = plugin.getEconomy().getBalance(player);
                    plugin.getEconomy().withdrawPlayer(player, balance);
                    int i = (int) (balance * balancePercent);
                    if (i < 0) {
                        i = 0;
                    }
                    plugin.getEconomy().depositPlayer(player, i);
                }
                case CHEST -> {
                    DeathPenaltyChestLog deathPenaltyChestLog = playerData.getDeathPenaltyChestLog();
                    Set<Location> locations = deathPenaltyChestLog.getChestLocationSets();
                    Set<Location> ignoredLocations = new HashSet<>(locations.size());
                    for (Location location : locations) {
                        if (plugin.getGuildManager().isInOwnGuildRegion(location, player, true)) {
                            ignoredLocations.add(location);
                            continue;
                        }
                        Block block = location.getBlock();
                        String materialName = block.getType().name();
                        if (materialName.contains("CHEST")) {
                            Chest chest = (Chest) block.getState();
                            chest.getBlockInventory().clear();
                            block.setType(Material.AIR);
                        } else if (materialName.contains("SHULKER_BOX")) {
                            ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                            shulkerBox.getInventory().clear();
                            block.setType(Material.AIR);
                        } else if (materialName.contains("BARREL")) {
                            Barrel barrel = (Barrel) block.getState();
                            barrel.getInventory().clear();
                            block.setType(Material.AIR);
                        }
                    }
                    deathPenaltyChestLog.reset();
                    for (Location location : ignoredLocations) {
                        deathPenaltyChestLog.addLocation(location);
                    }
                }
                case ITEMBOX -> {
                    ItemBox itemBox = playerData.getItemBox();
                    Iterator<ItemBoxStack> it = itemBox.getList().iterator();
                    while (it.hasNext()) {
                        ItemBoxStack itemBoxStack = it.next();
                        if (itemBoxStack.isUnlimitedExpiredTime()) {
                            continue;
                        }
                        it.remove();
                    }
                }
                case AUCTION -> {
                    AuctionManager auctionManager = plugin.getAuctionManager();
                    List<AuctionItem> list = auctionManager.getItems(player);
                    for (AuctionItem auctionItem : list) {
                        ItemStack itemStack = auctionItem.getItemStack();
                        itemStack.setAmount(0);
                    }
                }
            }
        }
        playerData.setData("DeathWorldName", null);
        playerData.setData("DeathLastFoodLevel", null);

        if (plugin.useDebug()) {
            MessageUtil.sendDebugMessage("Apply DeathPenalty",
                    "DeathDamageType: "
                            + (playerData.getTempData("DeathDamageType") != null ? playerData.getTempData("DeathDamageType") : "Unknown"),
                    "WorldName: " + deathWorldName + " | Player: " + player.getName(), "ActionTypes: " + sets);
        }
    }

    public void sendChestLogToItemBox(PlayerData playerData) {
        ItemBoxManager itemBoxManager = plugin.getItemBoxManager();
        DeathPenaltyChestLog deathPenaltyChestLog = playerData.getDeathPenaltyChestLog();
        deathPenaltyChestLog.getChestLocationSets().forEach(location -> {
            Block block = location.getBlock();
            if (location.getWorld() != null) {
                String materialName = block.getType().name();
                if (materialName.contains("CHEST")) {
                    Chest chest = (Chest) block.getState();
                    ItemStack[] itemStacks = chest.getBlockInventory().getContents();
                    for (ItemStack itemStack : itemStacks) {
                        itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", -1);
                    }
                    block.setType(Material.AIR);
                } else if (materialName.contains("SHULKER_BOX")) {
                    ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                    ItemStack[] itemStacks = shulkerBox.getInventory().getContents();
                    for (ItemStack itemStack : itemStacks) {
                        itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", -1);
                    }
                    block.setType(Material.AIR);
                } else if (materialName.contains("BARREL")) {
                    Barrel barrel = (Barrel) block.getState();
                    ItemStack[] itemStacks = barrel.getInventory().getContents();
                    for (ItemStack itemStack : itemStacks) {
                        itemBoxManager.sendItemStackToItemBox(playerData, itemStack, "[시스템]", -1);
                    }
                    block.setType(Material.AIR);
                }
            }
        });
        deathPenaltyChestLog.reset();
    }

    private final Vector xVector = new Vector(1, 0, 0);
    private final Vector xVector2 = new Vector(-1, 0, 0);
    private final Vector zVector = new Vector(0, 0, 1);
    private final Vector zVector2 = new Vector(0, 0, -1);

    public Location getOtherChestLocation(Block block, org.bukkit.block.data.type.Chest chestTypeData) {
       String materialName = block.getType().name();
       if (materialName.contains("CHEST")) {
           BlockFace blockFace = chestTypeData.getFacing();
           org.bukkit.block.data.type.Chest.Type chestType = chestTypeData.getType();
           Location location = null;
           switch (blockFace) {
               case EAST -> {
                   if (chestType == org.bukkit.block.data.type.Chest.Type.LEFT) {
                       location = block.getLocation().add(zVector);
                   } else {
                       location = block.getLocation().add(zVector2);
                   }
               }
               case NORTH -> {
                   if (chestType == org.bukkit.block.data.type.Chest.Type.LEFT) {
                       location = block.getLocation().add(xVector);
                   } else {
                       location = block.getLocation().add(xVector2);
                   }
               }
               case WEST -> {
                   if (chestType == org.bukkit.block.data.type.Chest.Type.LEFT) {
                       location = block.getLocation().add(zVector2);
                   } else {
                       location = block.getLocation().add(zVector);
                   }
               }
               case SOUTH -> {
                   if (chestType == org.bukkit.block.data.type.Chest.Type.LEFT) {
                       location = block.getLocation().add(xVector2);
                   } else {
                       location = block.getLocation().add(xVector);
                   }
               }
           }
           return location;
       }
       return null;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String locationToFormat(Location location) {
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ();
    }

    public Location formatToLocation(String format) {
        String[] split = format.split(":");
        String worldName = split[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        double x = Double.parseDouble(split[1]);
        double y = Double.parseDouble(split[2]);
        double z = Double.parseDouble(split[3]);
        return new Location(world, x, y, z);
    }

    public static final NamespacedKey CHEST_OWNER_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "ChestOwner");

    public String getChestOwner(Block block) {
        if (block != null && isContainerMaterial(block.getType())) {
            if (block.getState() instanceof TileState tileState) {
                PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
                if (dataContainer.has(CHEST_OWNER_KEY, PersistentDataType.STRING)) {
                    return dataContainer.get(CHEST_OWNER_KEY, PersistentDataType.STRING);
                }
            }
        }
        return null;
    }

    public Set<ActionType> getWorldActionTypes(String worldName) {
        return worldActionMap.getOrDefault(worldName, Collections.emptySet());
    }

}
