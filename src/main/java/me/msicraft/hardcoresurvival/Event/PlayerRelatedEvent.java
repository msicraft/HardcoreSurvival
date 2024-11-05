package me.msicraft.hardcoresurvival.Event;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.DecoratedPotInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PlayerRelatedEvent implements Listener {

    private static PlayerRelatedEvent instance = null;

    public static PlayerRelatedEvent getInstance() {
        if (instance == null) {
            instance = new PlayerRelatedEvent();
        }
        return instance;
    }

    private final HardcoreSurvival plugin;

    private PlayerRelatedEvent() {
        this.plugin = HardcoreSurvival.getPlugin();
    }

    private boolean disableBoneMeal = false;
    private double mendingEnchantChance = 0.0;
    private boolean disableBedExplode = false;
    private boolean disableUse;
    private final Set<Material> disableUseMaterials = new HashSet<>();
    private int shieldCooldownTick = -1;
    private double reduceArrowDamage = 1;

    private final Map<Enchantment, Double> extraEnchantItemDamageMap = new HashMap<>();
    private final Map<String, String> changeWorldMessageMap = new HashMap<>();

    public void reloadVariables() {
        FileConfiguration config = plugin.getConfig();

        this.disableBoneMeal = config.contains("Setting.DisableBoneMeal") && config.getBoolean("Setting.DisableBoneMeal");
        this.mendingEnchantChance = config.contains("Setting.MendingEnchantChance") ? config.getDouble("Setting.MendingEnchantChance") : 0.0;
        this.disableBedExplode = config.contains("Setting.DisableBedExplode") && config.getBoolean("Setting.DisableBedExplode");
        this.disableUse = config.contains("Setting.DisableUse.Enabled") && config.getBoolean("Setting.DisableUse.Enabled");
        this.shieldCooldownTick = config.contains("Setting.ShieldCooldownTick") ? config.getInt("Setting.ShieldCooldownTick") : -1;
        config.getStringList("Setting.DisableUse.List").forEach(materialName -> {
            Material material = Material.getMaterial(materialName.toUpperCase());
            if (material != null) {
                disableUseMaterials.add(material);
            } else {
                MessageUtil.sendDebugMessage("DisableUse-Can't Load Material", "Material: " + materialName);
            }
        });
        this.reduceArrowDamage = config.contains("Setting.ReduceArrowDamage") ? config.getDouble("Setting.ReduceArrowDamage") : 1;

        ConfigurationSection extraEnchantSection = config.getConfigurationSection("Setting.ExtraEnchantItemDamage");
        if (extraEnchantSection == null) {
            extraEnchantItemDamageMap.clear();
        } else {
            Set<String> enchantKey = extraEnchantSection.getKeys(false);
            for (String key : enchantKey) {
                String path = "Setting.ExtraEnchantItemDamage." + key;
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                if (enchantment == null) {
                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("ExtraEnchantItemDamage-UnknownEnchantment", "Key: " + key);
                    }
                    continue;
                }
                double multiplier = config.getDouble(path, 1.0);
                extraEnchantItemDamageMap.put(enchantment, multiplier);
            }
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ExtraEnchantItemDamage-Register", "Size: " + extraEnchantItemDamageMap.size());
            }
        }

        ConfigurationSection changeWorldMessageSection = config.getConfigurationSection("Setting.ChangeWorldMessage");
        if (changeWorldMessageSection == null) {
            changeWorldMessageMap.clear();
        } else {
            Set<String> keySet = changeWorldMessageSection.getKeys(false);
            for (String key : keySet) {
                String path = "Setting.ChangeWorldMessage." + key;
                String message = config.getString(path, null);
                changeWorldMessageMap.put(key, message);
            }
            if (plugin.useDebug()) {
                MessageUtil.sendDebugMessage("ChangeWorldMessage-Register", "Size: " + changeWorldMessageMap.size());
            }
        }
    }

    @EventHandler
    public void disableBoneMeal(PlayerInteractEvent e) {
        if (disableBoneMeal) {
            if (e.getPlayer().isOp()) {
                return;
            }
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemStack = e.getItem();
                if (itemStack != null && itemStack.getType() == Material.BONE_MEAL) {
                    e.setCancelled(true);

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Disable-BoneMeal", "Player: " + e.getPlayer().getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void mendingEnchant(EnchantItemEvent e) {
        if (e.getExpLevelCost() >= 30) {
            ItemStack itemStack = e.getItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (!itemMeta.hasEnchant(Enchantment.MENDING)) {
                    double random = Math.random();
                    boolean success = false;
                    if (random < mendingEnchantChance) {
                        success = true;
                        itemMeta.addEnchant(Enchantment.MENDING, 1, true);
                        itemStack.setItemMeta(itemMeta);
                    }

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("Player-EnchantItem-Mending",
                                "Player: " + e.getEnchanter().getName(),
                                "성공여부: " + success,
                                "랜덤: " + random + " | " + "설정확률: " + mendingEnchantChance);
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableBedExplode(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block == null) {
                return;
            }
            if (block.getType().name().contains("_BED")) {
                if (disableBedExplode) {
                    World world = player.getWorld();
                    if (world.isBedWorks()) {
                        return;
                    }
                    e.setCancelled(true);
                    block.breakNaturally();

                    if (plugin.useDebug()) {
                        MessageUtil.sendDebugMessage("DisableBedExplode",
                                "Player: " + player.getName(), "World: " + world.getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableUse(PlayerInteractEvent e) {
        if (disableUse) {
            Player player = e.getPlayer();
            if (player.isOp()) {
                return;
            }
            Action action = e.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                ItemStack itemStack = e.getItem();
                if (itemStack != null) {
                    if (disableUseMaterials.contains(itemStack.getType())) {

                        e.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "해당 아이템은 사용할 수 없습니다");

                        if (plugin.useDebug()) {
                            MessageUtil.sendDebugMessage("DisableUse",
                                    "Player: " + player.getName(), "Item: " + itemStack.getType().name());
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void disableTabComplete(PlayerCommandSendEvent e) {
        if (e.getPlayer().isOp()) {
            return;
        }
        e.getCommands().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void applyShieldCooldown(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (shieldCooldownTick != -1) {
                ItemStack itemStack = player.getActiveItem();
                if (itemStack.getType() == Material.SHIELD) {
                    player.setCooldown(Material.SHIELD, shieldCooldownTick);
                    player.clearActiveItem();

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta instanceof Damageable damageable) {
                        if (damageable.hasDamage()) {
                            damageable.setDamage(damageable.getDamage() + 1);
                        } else {
                            damageable.setDamage(1);
                        }
                        itemStack.setItemMeta(itemMeta);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void privateChestOpen(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            if (player.isOp()) {
                return;
            }
            Block chestBlock = e.getClickedBlock();
            if (chestBlock != null && plugin.getDeathPenaltyManager().isContainerMaterial(chestBlock.getType())) {
                if (chestBlock.getState() instanceof TileState tileState) {
                    PersistentDataContainer dataContainer = tileState.getPersistentDataContainer();
                    String owner = dataContainer.get(plugin.getDeathPenaltyManager().getBlockOwnerKey(), PersistentDataType.STRING);
                    if (owner != null) {
                        UUID ownerUUID = UUID.fromString(owner);
                        if (ownerUUID.equals(player.getUniqueId())) {
                            return;
                        }
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID);
                        if (offlinePlayer.isOnline()) {
                            PlayerData onlineOwnerData = plugin.getPlayerDataManager().getPlayerData(ownerUUID);
                            if ((boolean) onlineOwnerData.getPersonalOption(PersonalOption.PRIVATE_CHEST)) {
                                e.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "해당 상자는 잠겨있습니다");
                                return;
                            }
                        } else {
                            OfflinePlayerData offlinePlayerData = new OfflinePlayerData(ownerUUID);
                            offlinePlayerData.loadData();
                            if ((boolean) offlinePlayerData.getPersonalOption(PersonalOption.PRIVATE_CHEST)) {
                                e.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "해당 상자는 잠겨있습니다");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private final List<Material> fortuneBlockList = List.of(Material.COAL_ORE, Material.COPPER_ORE,
            Material.REDSTONE_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.ANCIENT_DEBRIS, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.NETHER_GOLD_ORE);

    @EventHandler
    public void nerfFortune(BlockDropItemEvent e) {
        Player player = e.getPlayer();
        Material material = e.getBlockState().getType();
        if (fortuneBlockList.contains(material)) {
            List<Item> dropList = e.getItems();
            if (!dropList.isEmpty()) {
                ItemStack handStack = player.getInventory().getItemInMainHand();
                ItemMeta itemMeta = handStack.getItemMeta();
                if (itemMeta == null) {
                    return;
                }
                int fortuneLevel = itemMeta.hasEnchant(Enchantment.FORTUNE) ? itemMeta.getEnchantLevel(Enchantment.FORTUNE) : 0;
                for (Item item : dropList) {
                    ItemStack itemStack = item.getItemStack();
                    if (fortuneLevel == 0) {
                        itemStack.setAmount(1);
                    } else {
                        int amount = 1;
                        for (int i = 0; i < fortuneLevel; i++) {
                            if (Math.random() < 0.05) {
                                amount++;
                            }
                        }
                        itemStack.setAmount(amount);
                        if (itemMeta instanceof Damageable damageable) {
                            int v = amount * 5;
                            if (damageable.hasDamage()) {
                                damageable.setDamage(damageable.getDamage() + v);
                            } else {
                                damageable.setDamage(v);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void disableCauldron(CauldronLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void reduceArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            if (e.getDamager() instanceof AbstractArrow abstractArrow) {
                if (abstractArrow.getShooter() instanceof Player) {
                    double reduceDamage = e.getDamage() * reduceArrowDamage;
                    e.setDamage(reduceDamage);
                }
            }
        }
    }


    @EventHandler
    public void extraEnchantItemDamage(PlayerItemDamageEvent e) {
        ItemStack itemStack = e.getItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            int damage = e.getDamage();
            Set<Enchantment> sets = extraEnchantItemDamageMap.keySet();
            for (Enchantment enchantment : sets) {
                if (itemMeta.hasEnchant(enchantment)) {
                    double extraD = extraEnchantItemDamageMap.get(enchantment);
                    damage = (int) (damage * extraD);
                }
            }
            e.setDamage(damage);
        }
    }

    @EventHandler
    public void disableTame(EntityTameEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void playerDisableContainerToBlock(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        if (inventory instanceof FurnaceInventory furnaceInventory) {
            ItemStack itemStack = furnaceInventory.getSmelting();
            if (MythicBukkit.inst().getItemManager().isMythicItem(itemStack)) {
                player.getInventory().addItem(itemStack);
                furnaceInventory.setSmelting(null);
            } else if (plugin.getCustomItemManager().getCustomItemInternalName(itemStack) != null) {
                player.getInventory().addItem(itemStack);
                furnaceInventory.setSmelting(null);
            }
        } else if (inventory instanceof DecoratedPotInventory decoratedPotInventory) {
            ItemStack itemStack = decoratedPotInventory.getItem();
            if (MythicBukkit.inst().getItemManager().isMythicItem(itemStack)) {
                player.getInventory().addItem(itemStack);
                decoratedPotInventory.setItem(null);
            } else if (plugin.getCustomItemManager().getCustomItemInternalName(itemStack) != null) {
                player.getInventory().addItem(itemStack);
                decoratedPotInventory.setItem(null);
            }
        }
    }

    @EventHandler
    public void playerChangeWorldMessage(PlayerChangedWorldEvent e) {
        Player player = e.getPlayer();
        String worldName = player.getWorld().getName();
        if (changeWorldMessageMap.containsKey(worldName)) {
            String message = changeWorldMessageMap.get(worldName);
            if (message != null) {
                player.sendMessage(MessageUtil.translateColorCodes(message));
            }
        }
    }

}
