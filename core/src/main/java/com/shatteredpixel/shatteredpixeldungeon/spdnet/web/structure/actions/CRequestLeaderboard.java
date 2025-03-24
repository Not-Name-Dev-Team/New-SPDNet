package com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRequestLeaderboard {
	// 筛选指定玩家
	private String playerName;
	// 无挑战筛选的时候值为 999
	private Integer challengeCount;
	// 只显示通关了的的记录
	private Boolean winOnly;
	// 游戏模式 不筛选选择 ALL
	private String gameMode;
	// 排序方式
	private String sortCriteria; // "score", "duration", "id"
	// 排行榜的第几页
	private int page;
	// 排行榜的每页有多少名
	private int amountPerPage;
}