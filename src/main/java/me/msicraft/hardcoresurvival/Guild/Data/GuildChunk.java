package me.msicraft.hardcoresurvival.Guild.Data;

import me.msicraft.hardcoresurvival.API.Data.Pair;

public class GuildChunk {

    private final String worldName;
    private final Pair<Integer, Integer> chunkPair;

    public GuildChunk(String worldName, Pair<Integer, Integer> chunkPair) {
        this.worldName = worldName;
        this.chunkPair = chunkPair;
    }

    public String getWorldName() {
        return worldName;
    }

    public Pair<Integer, Integer> getChunkPair() {
        return chunkPair;
    }

}
