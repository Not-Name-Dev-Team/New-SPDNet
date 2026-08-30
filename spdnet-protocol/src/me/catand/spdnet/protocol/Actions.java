package me.catand.spdnet.protocol;

import lombok.Getter;

/**
 * SPDNet 共享协议：消息发送类型（客户端 → 服务端）
 *
 * SPDNet: 此文件是客户端(core)与服务端(server)共享的单一事实来源。
 * 修改事件名时只需改这里，两端编译自动同步，避免人工维护两份枚举导致漂移。
 */
@Getter
public enum Actions {
	ACHIEVEMENT("achievement"),
	ANKH_USED("ankhUsed"),
	ARMOR_UPDATE("armorUpdate"),
	CHAT_MESSAGE("chatMessage"),
	ENTER_DUNGEON("enterDungeon"),
	ERROR("error"),
	FLOATING_TEXT("floatingText"),
	GAME_END("gameEnd"),
	GIVE_ITEM("giveItem"),
	HERO("hero"),
	LEAVE_DUNGEON("leaveDungeon"),
	PLAYER_CHANGE_FLOOR("playerChangeFloor"),
	PLAYER_MOVE("playerMove"),
	REQUEST_LEADERBOARD("requestLeaderboard"),
	REQUEST_PLAYER_LIST("requestPlayerList"),
	REQUEST_DAILY_CHALLENGE("requestDailyChallenge"),
	VIEW_HERO("viewHero"),
	// SPDNet: Journal 相关事件（原为裸字符串，现纳入枚举统一管理）
	CATALOG_UPDATE("catalogUpdate"),
	BESTIARY_UPDATE("bestiaryUpdate"),
	DOCUMENT_UPDATE("documentUpdate");

	private final String name;

	Actions(String name) {
		this.name = name;
	}
}
