package me.catand.spdnetserver.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import me.catand.spdnetserver.MailSender;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SPDNetGetkeyPlugin extends BotPlugin {
	@Autowired
	private PlayerRepository playerRepository;
	static MailSender mailSender = new MailSender("JDSALing@126.com", "JDSALing@126.com", "HJOHDUYCGDQOZYMH", "smtp.126.com");

	@Override
	public int onGroupMessage(Bot bot, GroupMessageEvent event) {
		if (!event.getRawMessage().contains("查询key")) {
			return MESSAGE_IGNORE;
		}
		MsgUtils sendMsg = new MsgUtils();
		List<String> arrayMsg = event.getArrayMsg().stream()
				.filter(msg -> msg.getType() == MsgTypeEnum.text)
				.map(ArrayMsg::getData)
				.map(map -> map.get("text"))
				.map(map -> map.replaceAll("\\s+", " "))
				.filter(text -> text.equals("查询key"))
				.toList();
		String[] args = arrayMsg.getFirst().split(" ");
		if (!args[0].equals("查询key")) {
			return MESSAGE_IGNORE;
		}
		long qq = event.getUserId();

		if (!playerRepository.existsByQq(qq)) {
			sendMsg.text("哥你还没注册呢");
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		Player player = playerRepository.findByQq(qq);

		try {
			mailSender.sendMail(player.getEmail(), "找到你的SPDNet key了", "你的SPDNet key是:\n" + player.getKey() + "\n别再搞丢了\n>:)");
			sendMsg.text("给" + player.getEmail() + "发送邮件成功, 看看收件箱");
		} catch (Exception e) {
			sendMsg = MsgUtils.builder().text("发送邮件失败,请再试一次,错误信息:\n" + e.getMessage());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
		return MESSAGE_BLOCK;
	}
}
