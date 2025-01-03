package me.msicraft.hardcoresurvival.Shop.Data;

import me.msicraft.hardcoresurvival.Shop.ShopManager;
import me.msicraft.hardcoresurvival.Utils.MathUtil;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class ShopItem {

    public enum ItemType {
        VANILLA, MYTHICMOBS, CUSTOM_ITEM, ORAXEN, MMOITEMS
    }

    public enum Category {
        ALL("전체", 0), NONE("기타", 1),
        INGOT("광석", 2), FARM("농사", 3),
        EQUIPMENT("장비", 4), CONSUMABLE("소모품", 5),
        ECONOMY("화폐", 6);

        private final String displayName;
        private final int order;

        Category(String displayName, int order) {
            this.displayName = displayName;
            this.order = order;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getOrder() {
            return order;
        }

    }

    private final String id;
    private ItemStack itemStack;

    private ItemType itemType;
    private boolean useStaticPrice = false;
    private boolean unlimitStock = false;
    private int stock = 0;
    private int basePrice = -1;
    private int price = -1;

    private boolean disableSell = false;

    private Category category;

    public ShopItem(ItemType itemType, boolean useStaticPrice, boolean unlimitStock,
                    ItemStack itemStack, String id, int stock, int basePrice, int price, boolean disableSell, Category category) {
        this.itemType = itemType;
        this.useStaticPrice = useStaticPrice;
        this.unlimitStock = unlimitStock;
        this.itemStack = itemStack;
        this.id = id;
        this.stock = stock;
        this.basePrice = basePrice;
        this.price = price;
        this.disableSell = disableSell;
        this.category = category;
    }

    public void updatePrice(ShopManager shopManager) {
        if (useStaticPrice) {
            return;
        }
        if (basePrice < 0) {
            return;
        }

        int maxPrice = (int) (basePrice * shopManager.getMaxPricePercent());
        int minPrice = (int) (basePrice * shopManager.getMinPricePercent());
        if (minPrice < 0) {
            minPrice = 1;
        }

        double changeV = shopManager.getPerValueChangeMaxPercent();
        double randomChangeValue = MathUtil.getRangeRandomDouble(changeV, -changeV);
        price = (int) (price + (price * randomChangeValue));
        if (price < minPrice) {
            price = minPrice;
        }
        if (price > maxPrice) {
            price = maxPrice;
        }
    }

    public void resetPrice() {
        if (useStaticPrice) {
            return;
        }
        if (basePrice < 0) {
            return;
        }

        price = basePrice;
    }

    public String getChangePercentString() {
        String changePercent;
        if (price < basePrice) {
            int c = (int) ((1 - ((double) price / basePrice)) * 100.0);
            changePercent =  ChatColor.BOLD + "" + ChatColor.BLUE + "(-" + c + "%)";
        } else if (price > basePrice) {
            int c = (int) ((((double) price / basePrice) - 1.0) * 100.0);
            changePercent = ChatColor.BOLD + "" + ChatColor.RED + "(+" + c + "%)";
        } else {
            changePercent = ChatColor.BOLD + "" + ChatColor.WHITE + "(+0%)";
        }
        return changePercent;
    }

    public boolean hasEnoughStock(int stock) {
        if (unlimitStock) {
            return true;
        }
        return this.stock >= stock;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public boolean isUseStaticPrice() {
        return useStaticPrice;
    }

    public void setUseStaticPrice(boolean useStaticPrice) {
        this.useStaticPrice = useStaticPrice;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getId() {
        return id;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void addStock(int stock) {
        this.stock = this.stock + stock;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
    }

    public int getPrice(boolean isPerPrice) {
        if (isPerPrice) {
            return price / itemStack.getAmount();
        }
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isUnlimitStock() {
        return unlimitStock;
    }

    public void setUnlimitStock(boolean unlimitStock) {
        this.unlimitStock = unlimitStock;
    }

    public boolean isDisableSell() {
        return disableSell;
    }

    public void setDisableSell(boolean disableSell) {
        this.disableSell = disableSell;
    }

    public String asString() {
        return "ShopItem{" +
                "id='" + id + '\'' +
                ", itemType=" + itemType.name() +
                ", useStaticPrice=" + useStaticPrice +
                ", unlimitStock=" + unlimitStock +
                ", stock=" + stock +
                ", basePrice=" + basePrice +
                ", price=" + price +
                ", DisableSell=" + disableSell +
                ", Category=" + category.getDisplayName() + "}";
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

}
