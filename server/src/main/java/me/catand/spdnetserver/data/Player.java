package me.catand.spdnetserver.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SPDNet: 在线玩家列表的传输结构（PLAYER_LIST 事件的元素类型）。
 *
 * 与实体 {@link me.catand.spdnetserver.entitys.Player} 刻意分开：实体带 JPA 注解且持有
 * 密码/邮箱/成就等敏感或重量级字段，直接下发给客户端既会泄漏又能被 Jackson 序列化意外带出。
 * 这里只保留客户端真正需要渲染的字段，字段名与客户端
 * {@code com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Player} 一一对应。
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
