package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * SPDNet: 前缀拥有者DTO
 * 包含玩家信息和获得前缀的时间
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrefixOwnerDTO {
	private Long id;
	private String playerName;
	private boolean active;
	private LocalDateTime assignedAt;
}
