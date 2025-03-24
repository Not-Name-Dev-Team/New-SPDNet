package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure;

import lombok.Getter;

/**
 * 消息接收类型
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
	VIEW_HERO("viewHero");


	private final String name;

	Events(String name) {
		this.name = name;
	}

}
