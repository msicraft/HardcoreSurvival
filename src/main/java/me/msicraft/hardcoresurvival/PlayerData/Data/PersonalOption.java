package me.msicraft.hardcoresurvival.PlayerData.Data;

public enum PersonalOption {

    DISPLAY_ACTIONBAR("액션바 표시", true),;

    private final String displayName;
    private final Object baseValue;

    PersonalOption(String displayName, Object baseValue) {
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