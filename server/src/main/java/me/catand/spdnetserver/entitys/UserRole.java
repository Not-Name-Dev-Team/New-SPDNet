package me.catand.spdnetserver.entitys;

public enum UserRole {
    USER("玩家"),
    ADMIN("管理员"),
    BANNED("封禁");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
