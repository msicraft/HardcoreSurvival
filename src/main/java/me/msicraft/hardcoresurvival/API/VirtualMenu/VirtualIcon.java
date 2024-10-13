package me.msicraft.hardcoresurvival.API.VirtualMenu;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class VirtualIcon {

    private final String displayName;

    public VirtualIcon(String displayName) {
        this.displayName = displayName;
    }

    public TextDisplay createIcon(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        return world.spawn(location, TextDisplay.class, entity -> {
            entity.text(Component.text(ChatColor.AQUA + displayName));
            entity.setInvisible(true);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(true);
            entity.setBackgroundColor(Color.WHITE);
            entity.setShadowed(false);
        });
    }

}
