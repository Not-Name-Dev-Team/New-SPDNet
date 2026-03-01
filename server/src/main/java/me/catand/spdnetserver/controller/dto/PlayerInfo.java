package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerInfo {
    private String name;
    private String role;
    private boolean online;
    // SPDNet: 前缀系统 - 玩家前缀完整信息
    private PlayerPrefixDTO prefix;

    public PlayerInfo(String name, String role, boolean online) {
        this.name = name;
        this.role = role;
        this.online = online;
    }
}
