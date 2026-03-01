package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SPDNet: 玩家前缀信息DTO
 * 包含完整的前缀样式信息
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPrefixDTO {
	private String name;
	private String displayText;
	private String color;
	private String backgroundColor;
}
