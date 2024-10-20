package me.msicraft.hardcoresurvival.CustomItem.Item;

import me.msicraft.hardcoresurvival.CustomItem.Data.CustomItem;
import me.msicraft.hardcoresurvival.CustomItem.File.CustomItemDataFile;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.PlayerData.Data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class RollBackScroll extends CustomItem {

    public static final NamespacedKey ROLLBACK_SCROLL_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "CustomItem_RollBackScroll");

    public RollBackScroll(String id, CustomItemDataFile customItemDataFile) {
        super(id, customItemDataFile);
    }

    @Override
    public void rightClick(PlayerData playerData, ItemStack useItemStack) {
        Player player = playerData.getPlayer();
        if (playerData.isInCombat()) {
            player.sendMessage(ChatColor.RED + "전투중에는 사용 불가능합니다");
            return;
        }
        PersistentDataContainer useDataContainer = useItemStack.getItemMeta().getPersistentDataContainer();
        if (!useDataContainer.has(ROLLBACK_SCROLL_KEY)) {
            player.sendMessage(ChatColor.RED + "저장위치가 존재하지 않습니다");
            return;
        }
        String locationString = useDataContainer.get(ROLLBACK_SCROLL_KEY, PersistentDataType.STRING);
        if (locationString == null) {
            player.sendMessage(ChatColor.RED + "{none-1} 에러가 발생했습니다. 관리자에게 문의해주세요");
            return;
        }
        Location location = formatToString(locationString);
        if (!location.getWorld().getName().equals(player.getWorld().getName())) {
            player.sendMessage(ChatColor.RED + "같은 월드내에서만 이동가능합니다");
            return;
        }
        player.teleport(location);
        useItemStack.setAmount(useItemStack.getAmount() - 1);
    }

    @Override
    public void leftClick(PlayerData playerData, ItemStack useItemStack) {
        PersistentDataContainer useDataContainer = useItemStack.getItemMeta().getPersistentDataContainer();
        Player player = playerData.getPlayer();
        if (useDataContainer.has(ROLLBACK_SCROLL_KEY)) {
            player.sendMessage(ChatColor.RED + "이미 저장위치가 존재합니다");
            return;
        }
        Location location = player.getLocation();
        ItemStack clone = useItemStack.clone();
        ItemMeta cloneMeta = clone.getItemMeta();
        PersistentDataContainer dataContainer = cloneMeta.getPersistentDataContainer();
        dataContainer.set(ROLLBACK_SCROLL_KEY, PersistentDataType.STRING, locationToFormat(location));
        List<Component> lore = cloneMeta.lore();
        lore.add(Component.text(""));
        lore.add(Component.text(ChatColor.GRAY + "저장위치 ->"));
        lore.add(Component.text(ChatColor.GRAY + "월드: " + HardcoreSurvival.getPlugin().getWorldManager().getCurrentWorldName(location.getWorld().getName())));
        lore.add(Component.text(ChatColor.GRAY + "XYZ: " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ()));
        cloneMeta.lore(lore);
        cloneMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
        cloneMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        clone.setItemMeta(cloneMeta);

        useItemStack.setAmount(useItemStack.getAmount() - 1);
        player.getInventory().addItem(clone);
        player.sendMessage(ChatColor.GREEN + "현재 위치가 저장되었습니다");
    }

    private String locationToFormat(Location location) {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private Location formatToString(String format) {
        String[] split = format.split(":");
        String worldName = split[0];
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);
        return new Location(Bukkit.getServer().getWorld(worldName), x, y, z);
    }

}
