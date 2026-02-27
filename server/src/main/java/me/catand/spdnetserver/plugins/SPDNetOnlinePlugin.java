package me.catand.spdnetserver.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import me.catand.spdnetserver.SocketService;
import me.catand.spdnetserver.entitys.Player;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SPDNetOnlinePlugin extends BotPlugin {
	@Override
	public int onGroupMessage(Bot bot, GroupMessageEvent event) {
		if (!event.getRawMessage().contains("在线状况")) {
			return MESSAGE_IGNORE;
		}
		MsgUtils sendMsg = new MsgUtils();
		List<String> arrayMsg = event.getArrayMsg().stream()
				.filter(msg -> msg.getType() == MsgTypeEnum.text)
				.map(ArrayMsg::getData)
				.map(map -> map.get("text"))
				.map(map -> map.replaceAll("\\s+", " "))
				.filter(text -> text.startsWith("在线状况"))
				.toList();
		if (!arrayMsg.isEmpty()) {
			Map<UUID, Player> playerMap = SocketService.getInstance().getPlayerMap();
			if (playerMap.isEmpty()) {
				sendMsg.text("现在服务器里面一个人都没 :(");
			} else {
				sendMsg.text("当前在线玩家(" + playerMap.size() + "):\n");
				AtomicInteger counter = new AtomicInteger(1);
				playerMap.forEach((uuid, player) -> {
					if (counter.getAndIncrement() == playerMap.size()) {
						sendMsg.text(player.getName());
					} else {
						sendMsg.text(player.getName() + ", ");
					}
				});
			}

			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		} else {
			return MESSAGE_IGNORE;
		}


	}
}
