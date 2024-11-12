package me.msicraft.hardcoresurvival.Guild.Data;

public enum RegionOptions {

    PRIVATE_CHEST("상자 잠금", false),
    BLOCK_PLACE("외부인 블록 설치 금지", false),
    BLOCK_BREAK("외부인 블록 파괴 금지", false);

    private final String displayName;
    private final Object baseValue;

    RegionOptions(String displayName, Object baseValue) {
        this.displayName = displayName;
        this.baseValue = baseValue;
    }

    public Object getBaseValue() {
        return baseValue;
    }

    public String getDisplayName() {
        return displayName;
    }

}
