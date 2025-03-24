package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure;

import lombok.Getter;

/**
 * 消息发送类型
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
	VIEW_HERO("viewHero");
	private final String name;

	Actions(String name) {
		this.name = name;
	}
}
