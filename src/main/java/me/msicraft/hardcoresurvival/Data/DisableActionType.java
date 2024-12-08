package me.msicraft.hardcoresurvival.Data;

public enum DisableActionType {

    RIGHT_CLICK("RightClick"),
    LEFT_CLICK("LeftClick"),
    SHIFT_LEFT_CLICK("ShiftLeftClick"),
    SHIFT_RIGHT_CLICK("ShiftRightClick");

    private final String path;

    DisableActionType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
