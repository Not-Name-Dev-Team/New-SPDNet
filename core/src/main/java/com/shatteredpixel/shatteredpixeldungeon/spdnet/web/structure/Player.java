package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 玩家信息
 * 
 * 修改：移除qq字段，将power改为role以匹配服务端
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Player {
	private String name;
	private String role;
	private Status status;
}
