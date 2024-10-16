package me.msicraft.hardcoresurvival.Menu.Data;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CustomGuiManager {

    private final List<UUID> viewers = new ArrayList<>();

    public void addViewer(UUID uuid) {
        viewers.add(uuid);
    }

    public void addViewer(Player player) {
        addViewer(player.getUniqueId());
    }

    public void removeViewer(UUID uuid) {
        viewers.remove(uuid);
    }

    public void removeViewer(Player player) {
        removeViewer(player.getUniqueId());
    }

    public List<UUID> getViewers() {
        return viewers;
    }

    public void removeAll() {
        viewers.clear();
    }

}
