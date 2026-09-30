package me.catand.spdnetserver.data.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.data.Data;
import me.catand.spdnetserver.data.Player;

/**
 * SPDNet: 在线玩家列表下发。
 *
 * 元素类型是传输用的 {@link Player} DTO（不是 JPA 实体），因此这里只需做一次字段搬运，
 * 不必再把实体序列化成 JSON 字符串、再解析回 JSONObject 来补字段。
 *
 * 注：实体与本类的 DTO 同名（都叫 Player），故下方一律用全限定名引用实体，避免歧义。
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SPlayerList extends Data {
	private List<Player> players;

	public SPlayerList(Map<UUID, me.catand.spdnetserver.entitys.Player> playersMap) {
		players = new ArrayList<>(playersMap.size());
		for (me.catand.spdnetserver.entitys.Player entity : playersMap.values()) {
			Player player = new Player();
			player.setName(entity.getName());
			// role 用 displayName（"管理员"/"玩家"/"封禁"），与 SJoin 及客户端 Role 常量保持一致
			player.setRole(entity.getRole() == null ? null : entity.getRole().getDisplayName());
			player.setStatus(entity.getStatus());
			player.setPrefix(entity.getPrefixName());
			players.add(player);
		}
	}
}
