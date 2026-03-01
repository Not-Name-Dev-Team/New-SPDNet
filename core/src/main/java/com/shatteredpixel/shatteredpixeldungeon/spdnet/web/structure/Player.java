package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 玩家信息
 *
 * 修改：移除qq字段，将power改为role以匹配服务端
 * SPDNet: 添加前缀字段
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Player {
	private String name;
	private String role;
	private Status status;
	// SPDNet: 前缀系统 - 玩家前缀
	private String prefix;

	public Player(String name, String role, Status status) {
		this.name = name;
		this.role = role;
		this.status = status;
	}
}
