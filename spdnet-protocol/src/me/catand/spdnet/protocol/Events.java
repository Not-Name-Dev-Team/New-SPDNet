package me.catand.spdnet.protocol;

import lombok.Getter;

/**
 * SPDNet 共享协议：消息接收类型（服务端 → 客户端）
 *
 * SPDNet: 此文件是客户端(core)与服务端(server)共享的单一事实来源。
 * 修改事件名时只需改这里，两端编译自动同步，避免人工维护两份枚举导致漂移。
 */
@Getter
public enum Events {
	ACHIEVEMENT("achievement"),
	ANKH_USED("ankhUsed"),
	ARMOR_UPDATE("armorUpdate"),
	CHAT_MESSAGE("chatMessage"),
	ENTER_DUNGEON("enterDungeon"),
	ERROR("error"),
	EXIT("exit"),
	FLOATING_TEXT("floatingText"),
	GAME_END("gameEnd"),
	GIVE_ITEM("giveItem"),
	HERO("hero"),
	INIT("init"),
	JOIN("join"),
	LEADERBOARD("leaderboard"),
	LEAVE_DUNGEON("leaveDungeon"),
	PLAYER_CHANGE_FLOOR("playerChangeFloor"),
	PLAYER_LIST("playerList"),
	PLAYER_MOVE("playerMove"),
	SERVER_MESSAGE("serverMessage"),
	ALLOW_DAILY_CHALLENGE("allowDailyChallenge"),
	REJECT_DAILY_CHALLENGE("rejectDailyChallenge"),
	VIEW_HERO("viewHero"),
	// SPDNet: 地牢留言(Ping)系统 - 服务端 → 客户端（进/换层单播与同层广播）
	NOTE_LIST("noteList"),
	// SPDNet: 地牢留言(Ping)系统 - 留言创建成功后的聊天通报（渲染为聊天窗口通报而非弹窗）
	NOTE_NOTIFY("noteNotify"),
	// SPDNet: Journal 相关事件（原为裸字符串，现纳入枚举统一管理）
	JOURNALS("journals");

	private final String name;

	Events(String name) {
		this.name = name;
	}

}
