package me.catand.spdnetserver.data;

import lombok.Getter;

@Getter
public enum Mode {
	IRONMAN("铁人模式", "没有玩家之间的实质性交互，使用你的技巧争夺排行榜上的高分，证明自己是地牢高手。"),
	FUN("娱乐模式", "随便摸，乐就行了。 : )"),
	DAILY("每日挑战", "每天都有新的地牢，每天都有新排行榜");
	private final String name;
	private final String description;

	Mode(String name, String description) {
		this.name = name;
		this.description = description;
	}
}