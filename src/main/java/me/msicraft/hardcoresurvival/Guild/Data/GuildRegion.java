package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.API.Data.Pair;
import me.msicraft.hardcoresurvival.HardcoreSurvival;
import me.msicraft.hardcoresurvival.Utils.GuiUtil;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class GuildRegion {

    public static final NamespacedKey GUILD_REGION_KEY = new NamespacedKey(HardcoreSurvival.getPlugin(), "GuildRegion");

    private final GuildSpawnLocation guildSpawnLocation;
    private final Map<String, List<Pair<Integer, Integer>>> buyRegionMap = new HashMap<>();
    private long lastRegionPayTime;

    private final Map<RegionOptions, Object> regionOptionsMap = new HashMap<>();

    //private int freeRegionBuyCount = 0;

    public GuildRegion(FileConfiguration config) {
        this.guildSpawnLocation = new GuildSpawnLocation(config);

        this.lastRegionPayTime = config.getLong("Guild.Region.LastRegionPayTime", -1);

        ConfigurationSection buyRegionSection = config.getConfigurationSection("Guild.Region.BuyRegion");
        if (buyRegionSection == null) {
            buyRegionMap.clear();
        } else {
            Set<String> worldKeys = buyRegionSection.getKeys(false);
            for (String worldName : worldKeys) {
                List<Pair<Integer, Integer>> list = new ArrayList<>();
                String path = "Guild.Region.BuyRegion." + worldName;
                config.getStringList(path).forEach(s -> {
                    String[] split = s.split(":");
                    int x = Integer.parseInt(split[0]);
                    int z = Integer.parseInt(split[1]);
                    list.add(new Pair<>(x, z));
                });
                buyRegionMap.put(worldName, list);
            }
        }

        RegionOptions[] regionOptions = RegionOptions.values();
        for (RegionOptions option : regionOptions) {
            String path = "Guild.Region.RegionOptions." + option.name();
            Object o = config.get(path, option.getBaseValue());
            regionOptionsMap.put(option, o);
        }
    }

    public GuildSpawnLocation getGuildSpawnLocation() {
        return guildSpawnLocation;
    }

    public int getBuyRegionCount() {
        int count = 0;
        Set<String> worldKeys = buyRegionMap.keySet();
        for (String worldName : worldKeys) {
            List<Pair<Integer, Integer>> list = buyRegionMap.get(worldName);
            count = count + list.size();
        }
        return count;
    }

    public Set<String> worldNameSets() {
        return buyRegionMap.keySet();
    }

    public List<Pair<Integer, Integer>> getBuyRegionList(String worldName) {
        if (buyRegionMap.containsKey(worldName)) {
            return buyRegionMap.get(worldName);
        }
        List<Pair<Integer, Integer>> list = new ArrayList<>();
        buyRegionMap.put(worldName, list);
        return list;
    }

    public List<GuildChunk> getGuildChunks() {
        List<GuildChunk> list = new ArrayList<>();
        Set<String> keys = buyRegionMap.keySet();
        for (String worldName : keys) {
            List<Pair<Integer, Integer>> pairList = getBuyRegionList(worldName);
            for (Pair<Integer, Integer> pair : pairList) {
                list.add(new GuildChunk(worldName, pair));
            }
        }
        return list;
    }

    public void addRegion(String worldName, Pair<Integer, Integer> pair) {
        List<Pair<Integer, Integer>> list = getBuyRegionList(worldName);
        list.add(pair);
        buyRegionMap.put(worldName, list);
    }

    public void addRegion(String worldName, Chunk chunk) {
        addRegion(worldName, new Pair<>(chunk.getX(), chunk.getZ()));
    }

    public void removeRegion(String worldName, int x, int z) {
        List<Pair<Integer, Integer>> list = getBuyRegionList(worldName);
        Iterator<Pair<Integer, Integer>> it = list.iterator();
        while (it.hasNext()) {
            Pair<Integer, Integer> pair = it.next();
            if (pair.getV1() == x && pair.getV2() == z) {
                it.remove();
                break;
            }
        }
    }

    public List<String> getBuyRegionToFormatList(String worldName) {
        if (buyRegionMap.containsKey(worldName)) {
            List<String> formatList = new ArrayList<>();
            buyRegionMap.get(worldName).forEach(pair -> {
                formatList.add(pair.getV1() + ":" + pair.getV2());
            });
            return formatList;
        }
        return GuiUtil.EMPTY_LORE;
    }

    public long getLastRegionPayTime() {
        return lastRegionPayTime;
    }

    public synchronized void setLastRegionPayTime(long lastRegionPayTime) {
        this.lastRegionPayTime = lastRegionPayTime;
    }

    public int getOverdueDay(boolean applyMax) {
        if (getLastRegionPayTime() == -1) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long overDueTime = (currentTime - getLastRegionPayTime()) / 1000;
        int overdueDay = (int) (overDueTime / HardcoreSurvival.getPlugin().getGuildManager().getRegionPaySeconds());
        if (applyMax) {
            int max = HardcoreSurvival.getPlugin().getGuildManager().getMaxShopPenalty();
            if (overdueDay > max) {
                overdueDay = max;
            }
        }
        return overdueDay;
    }

    public void setRegionOption(RegionOptions option, Object value) {
        regionOptionsMap.put(option, value);
    }

    public Object getRegionOption(RegionOptions option) {
        return regionOptionsMap.getOrDefault(option, option.getBaseValue());
    }

}
