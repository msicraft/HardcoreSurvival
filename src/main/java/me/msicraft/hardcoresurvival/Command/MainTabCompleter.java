package me.msicraft.hardcoresurvival.Command;

import me.msicraft.hardcoresurvival.HardcoreSurvival;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                }
            }
        }
        return null;
    }

}
