package me.msicraft.hardcoresurvival.Command;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
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
                    return List.of("reload", "deathpenalty");
                }
                if (args.length == 2) {
                    String var = args[0];
                    if (var.equalsIgnoreCase("deathpenalty")) {
                        return List.of("setspawn", "chestlog");
                    }
                }
                if (args.length == 3) {
                    String var = args[0];
                    String var2 = args[1];
                    if (var.equalsIgnoreCase("deathpenalty")) {
                        if (var2.equalsIgnoreCase("chestlog")) {
                            return List.of("get", "log-to-ItemBox");
                        }
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
                    }
                }
            }
        }
        return null;
    }

}
