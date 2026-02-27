package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 玩家加入事件
 * 
 * 修改：移除qq字段，将power改为role以匹配服务端
 * 服务端字段：name, role
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SJoin extends Data {
	private String name;
	private String role;
}
