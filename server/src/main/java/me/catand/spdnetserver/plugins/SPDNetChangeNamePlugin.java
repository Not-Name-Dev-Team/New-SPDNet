package me.catand.spdnetserver.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SPDNetChangeNamePlugin extends BotPlugin {
	@Autowired
	private PlayerRepository playerRepository;

	@Override
	public int onGroupMessage(Bot bot, GroupMessageEvent event) {
		if (!event.getRawMessage().contains("现在我叫")) {
			return MESSAGE_IGNORE;
		}
		MsgUtils sendMsg = new MsgUtils();
		List<String> arrayMsg = event.getArrayMsg().stream()
				.filter(msg -> msg.getType() == MsgTypeEnum.text)
				.map(ArrayMsg::getData)
				.map(map -> map.get("text"))
				.map(map -> map.replaceAll("\\s+", " "))
				.filter(text -> text.startsWith("现在我叫"))
				.toList();
		String[] args = arrayMsg.getFirst().split(" ");
		if (!args[0].equals("现在我叫") || args.length < 2) {
			sendMsg.text("参数错误, 正确格式\n现在我叫 [用户名]");
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		if (!playerRepository.existsByQq(event.getUserId())) {
			sendMsg.text("你还没注册 :( 直接去注册个号吧");
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		if (playerRepository.existsByName(args[1])) {
			Player existingPlayer = playerRepository.findByName(args[1]);
			sendMsg.text("用户名 " + args[1] + " 的账户已存在,QQ号为 " + existingPlayer.getQq());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		try {
			Player player = playerRepository.findByQq(event.getUserId());
			player.setName(args[1]);
			playerRepository.save(player);
			sendMsg.text("好的敖, " + args[1] + ", 记住你了敖, 你给我等着");
		} catch (Exception e) {
			sendMsg.text("数据库存储失败,错误信息:\n" + e.getMessage());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
		return MESSAGE_BLOCK;
	}
}
