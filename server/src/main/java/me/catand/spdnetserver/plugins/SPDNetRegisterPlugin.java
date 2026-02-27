package me.catand.spdnetserver.plugins;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import me.catand.spdnetserver.MailSender;
import me.catand.spdnetserver.repositories.PlayerRepository;
import me.catand.spdnetserver.entitys.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;

@Component
public class SPDNetRegisterPlugin extends BotPlugin {
	@Autowired
	private PlayerRepository playerRepository;
	static MailSender mailSender = new MailSender("JDSALing@126.com", "JDSALing@126.com", "HJOHDUYCGDQOZYMH", "smtp.126.com");

	@Override
	public int onGroupMessage(Bot bot, GroupMessageEvent event) {
		if (!event.getRawMessage().contains("地牢注册")){
			return MESSAGE_IGNORE;
		}
		MsgUtils sendMsg = new MsgUtils();
		List<String> arrayMsg = event.getArrayMsg().stream()
				.filter(msg -> msg.getType() == MsgTypeEnum.text)
				.map(ArrayMsg::getData)
				.map(map -> map.get("text"))
				.map(map -> map.replaceAll("\\s+", " "))
				.filter(text -> text.startsWith("地牢注册"))
				.toList();
		String[] args = arrayMsg.getFirst().split(" ");
		if (!args[0].equals("地牢注册") || args.length < 2) {
			sendMsg.text("参数错误, 正确格式\n地牢注册 [用户名]");
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		long qq;
		try {
			qq = event.getUserId();
		} catch (Exception e) {
			sendMsg.text("你这QQ不太行 :/\n" + args[2]);
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		Player player = new Player();
		player.setQq(qq);
		player.setName(args[1]);
		player.setKey(DigestUtils.md5DigestAsHex(("这是一个加盐前缀:QQ号码:" + qq).getBytes()).substring(8, 24));
		if (qq == 3047354896L || qq == 2735951230L || qq == 2427968603L) {
			player.setPower("admin");
		} else {
			player.setPower("normal");
		}
		player.setEmail(qq + "@qq.com");

		if (playerRepository.existsByQq(player.getQq())) {
			Player existingPlayer = playerRepository.findByQq(player.getQq());
			sendMsg.text("QQ号 " + player.getQq() + " 的账户已存在,用户名为 " + existingPlayer.getName());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		if (playerRepository.existsByName(player.getName())) {
			Player existingPlayer = playerRepository.findByName(player.getName());
			sendMsg.text("用户名 " + player.getName() + " 的账户已存在,QQ号为 " + existingPlayer.getQq());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		if (playerRepository.existsByKey(player.getKey())) {
			Player existingPlayer = playerRepository.findByKey(player.getKey());
			sendMsg.text("此Key已存在\n用户名：" + existingPlayer.getName() + "\nQQ号:" + existingPlayer.getQq() + "\n这条消息按说不可能存在,这意味着撞md5了");
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}

		try {
			playerRepository.save(player);
		} catch (Exception e) {
			sendMsg.text("数据库存储失败,错误信息:\n" + e.getMessage());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		try {
			mailSender.sendMail(player.getEmail(), "你好" + "，你的SPDNet key已送达！如果没有请到垃圾箱查看。", "你的SPDNet key是:\n" + player.getKey() + "\n请妥善保管,不要泄露给他人!");
			sendMsg.text("给" + player.getEmail() + "发送邮件成功,请查收");
		} catch (Exception e) {
			sendMsg = MsgUtils.builder().text("发送邮件失败,请再试一次,错误信息:\n" + e.getMessage());
			bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
			return MESSAGE_BLOCK;
		}
		bot.sendGroupMsg(event.getGroupId(), sendMsg.build(), false);
		return MESSAGE_BLOCK;
	}
}
