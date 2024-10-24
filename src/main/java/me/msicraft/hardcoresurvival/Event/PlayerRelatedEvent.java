package me.msicraft.hardcoresurvival.Event;

import me.msicraft.hardcoresurvival.DeathPenalty.Event.DeathPenaltyRelatedEvent;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.OfflinePlayerData;
import me.msicraft.hardcoresurvival.PlayerData.Data.PersonalOption;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import me.msicraft.hardcoresurvival.Utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final List<Material> disableUseMaterials = new ArrayList<>();
    private int shieldCooldownTick = -1;

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
                    if (random <= mendingEnchantChance) {
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
            Action action = e.getAction();
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                ItemStack itemStack = e.getItem();
                if (itemStack != null) {
                    if (disableUseMaterials.contains(itemStack.getType())) {
                        Player player = e.getPlayer();
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
                if (player.getActiveItem().getType() == Material.SHIELD) {
                    player.setCooldown(Material.SHIELD, shieldCooldownTick);
                    player.clearActiveItem();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void privateChest(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Player player = e.getPlayer();
            Block chestBlock = e.getClickedBlock();
            if (chestBlock != null) {
                if (chestBlock.getState() instanceof TileState tileState) {
                    String owner = tileState.getPersistentDataContainer().get(DeathPenaltyRelatedEvent.BLOCK_OWNER_KEY, PersistentDataType.STRING);
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
                int fortuneLevel = itemMeta.hasEnchant(Enchantment.FORTUNE) ? itemMeta.getEnchantLevel(Enchantment.FORTUNE) : 0;
                double extraChance = 0.05 * fortuneLevel;
                for (Item item : dropList) {
                    ItemStack itemStack = item.getItemStack();
                    if (fortuneLevel == 0) {
                        itemStack.setAmount(1);
                    } else {
                        int amount = 1;
                        for (int i = 0; i < fortuneLevel; i++) {
                            if (Math.random() < extraChance) {
                                amount++;
                            }
                        }
                        itemStack.setAmount(amount);
                    }
                }
            }
        }
    }

}
