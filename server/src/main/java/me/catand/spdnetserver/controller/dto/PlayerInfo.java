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
    // SPDNet: 玩家状态信息 - 挑战数、层数、游戏模式等（使用DTO转换后的数据）
    private StatusDTO status;

    public PlayerInfo(String name, String role, boolean online) {
        this.name = name;
        this.role = role;
        this.online = online;
    }
}
