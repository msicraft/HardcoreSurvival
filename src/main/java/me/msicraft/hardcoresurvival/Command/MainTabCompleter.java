package me.msicraft.hardcoresurvival.Command;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Shop.Data.ShopItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainTabCompleter implements TabCompleter {

    private final HardcoreSurvival plugin;

    public MainTabCompleter(HardcoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (command.getName().equals("hardcoresurvival")) {
            if (sender.isOp()) {
                if (args.length == 1) {
                    return List.of("reload", "deathpenalty", "shop", "customitem", "streamer",
                            "itembox", "info", "broadcast", "save-data", "maintenance", "set-maintenance", "debug");
                }
                if (args.length == 2) {
                    switch (args[0]) {
                        case "deathpenalty" -> {
                            return List.of("chestlog");
                        }
                        case "shop" -> {
                            return List.of("register", "unregister", "setcenter", "edit", "price-update", "reset-price");
                        }
                        case "customitem" -> {
                            return List.copyOf(plugin.getCustomItemManager().getInternalNames());
                        }
                        case "streamer" -> {
                            return List.of("add", "remove", "list");
                        }
                        case "itembox" -> {
                            return List.of("give");
                        }
                        case "save-data" -> {
                            return List.of("shop");
                        }
                        case "debug" -> {
                            return List.of("change-nickname");
                        }
                    }
                }
                if (args.length == 3) {
                    String var = args[0];
                    String var2 = args[1];
                    if (var.equalsIgnoreCase("deathpenalty")) {
                        if (var2.equalsIgnoreCase("chestlog")) {
                            return List.of("get", "log-to-ItemBox");
                        }
                    } else if (var.equalsIgnoreCase("shop")) {
                        if (var2.equalsIgnoreCase("register") || var2.equalsIgnoreCase("unregister")
                                || var2.equalsIgnoreCase("edit")) {
                            return plugin.getShopManager().getInternalNameList();
                        }
                    } else if (var.equalsIgnoreCase("streamer")) {
                        if (var2.equalsIgnoreCase("remove")) {
                            List<String> list = new ArrayList<>();
                            plugin.getPlayerDataManager().getStreamerList().forEach(uuid -> {
                                list.add(uuid.toString());
                            });
                            return list;
                        }
                    } else if (var.equalsIgnoreCase("itembox")) {
                        List<String> list = new ArrayList<>();
                        list.add("all-players");
                        list.addAll(plugin.getPlayerDataManager().getPlayerFileNames());
                        return list;
                    }
                }
                if (args.length == 4) {
                    String var = args[0];
                    String var2 = args[1];
                    String var3 = args[2];
                    if (var.equalsIgnoreCase("deathpenalty")) {
                        if (var2.equalsIgnoreCase("chestlog")) {
                            if (var3.equalsIgnoreCase("log-to-ItemBox")) {
                                List<String> list = new ArrayList<>();
                                list.add("all-players");
                                list.addAll(plugin.getPlayerDataManager().getPlayerFileNames());
                                return list;
                            }
                        }
                    } else if (var.equalsIgnoreCase("shop")) {
                        if (var2.equalsIgnoreCase("register") || var2.equalsIgnoreCase("unregister")) {
                            List<String> list = new ArrayList<>();
                            for (ShopItem.ItemType itemType : ShopItem.ItemType.values()) {
                                list.add(itemType.name());
                            }
                            return list;
                        } else if (var2.equalsIgnoreCase("edit")) {
                            return List.of("UseStaticPrice", "UnlimitStock", "BasePrice", "Price", "Stock", "DisableSell");
                        }
                    } else if (var.equalsIgnoreCase("itembox")) {
                        return List.of("<expiredSeconds>");
                    }
                }
                if (args.length == 5) {
                    String var = args[0];
                    String var2 = args[1];
                    if (var.equalsIgnoreCase("shop")) {
                        if (var2.equalsIgnoreCase("register") || var2.equalsIgnoreCase("unregister")) {
                            return List.of("<basePrice>");
                        }
                    }
                }
            }
        }
        return null;
    }

}
