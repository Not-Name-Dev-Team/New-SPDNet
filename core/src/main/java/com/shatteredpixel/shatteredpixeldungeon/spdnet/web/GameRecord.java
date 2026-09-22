package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.alibaba.fastjson.annotation.JSONField;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameRecord {
	private String cause;
	private boolean win;
	private int score;
	@JSONField(name = "class")
	private String heroClass;
	private int tier;
	private int level;
	private int depth;
	private boolean ascending;
	private String date;
	private String version;
	@JSONField(name = "net_version")

	private String netVersion;
	@JSONField(name = "game_mode")
	private String gameMode;

	private Hero hero;
	private String badges;
	private String handlers;
	private int challenges;
	@JSONField(name = "challenge_amount")
	private int challengeAmount;
	@JSONField(name = "game_version")
	private int gameVersion;
	private long seed;
	@JSONField(name = "custom_seed")
	private String customSeed;
	private boolean daily;
	private boolean dailyReplay;

	private int gold;
	private int maxDepth;
	private int maxAscent;
	private int enemiesSlain;
	private int foodEaten;
	private int potionsCooked;
	private int priranhas;
	private int ankhsUsed;
	@JSONField(name = "prog_score")
	private int progScore;
	@JSONField(name = "item_val")
	private int itemVal;
	@JSONField(name = "tres_score")
	private int tresScore;
	@JSONField(name = "flr_expl_")
	private String flrExpl;
	@JSONField(name = "flr_expl")
	private String flrExplOld;
	@JSONField(name = "expl_score")
	private int explScore;
	@JSONField(name = "boss_scores")
	private int[] bossScores;
	@JSONField(name = "tot_boss")
	private int totBoss;
	@JSONField(name = "quest_scores")
	private int[] questScores;
	@JSONField(name = "tot_quest")
	private int totQuest;
	@JSONField(name = "win_mult")
	private float winMult;
	@JSONField(name = "chal_mult")
	private float chalMult;
	@JSONField(name = "total_score")
	private int totalScore;
	private int upgradesUsed;
	private int sneakAttacks;
	private int thrownAssists;
	private int spawnersAlive;
	private float duration;
	private boolean qualifiedForNoKilling;
	private boolean qualifiedForBossRemainsBadge;
	private boolean qualifiedForBossChallengeBadge;
	private boolean amuletObtained;
	private boolean won;
	private boolean ascended;
	@JSONField(name = "player_name")
	private String playerName;

	public String desc() {
		if (win) {
			if (ascending) {
				return Messages.get(Rankings.Record.class, "ascended");
			} else {
				return Messages.get(Rankings.Record.class, "won");
			}
		} else if (getClass(cause) == null) {
			return Messages.get(Rankings.Record.class, "something");
		} else {
			String result = Messages.get(getClass(cause), "rankings_desc", (Messages.get(getClass(cause), "name")));
			if (result.contains(Messages.NO_TEXT_FOUND)) {
				return Messages.get(Rankings.Record.class, "something");
			} else {
				return result;
			}
		}
	}

	public Class getClass(String cause) {
		String clName = cause.replace("class ", "");
		if (!clName.isEmpty()) {
			return Reflection.forName(clName);
		}
		return null;
	}

	// SPDNet 症状21：反序列化阶段的原生 hero bundle 串。打榜查看需要按记录重填全局快捷栏，
	// 但该串在 parse 阶段用于 setHero() 时已消费（restoreFromBundle），若不保留则无法再取回槽位。
	// 仅在本地反序列化时由 setHero 写入，不回传给服务端。
	@JSONField(serialize = false, deserialize = false)
	private transient String heroRawBundle;

	public void setHero(String hero) {
		// 保留原始 hero bundle，供浏览时重填快捷栏（restoreHeroWithQuickslot）
		this.heroRawBundle = hero;

		// SPDNet 症状21：parse 阶段仍用 skipQuickslotUpdate=true 防止批量榜单反序列化时
		// 用各记录的槽位污染本机全局快捷栏（且先于本机真实游戏已存档场景）。
		boolean wasSkipQuickslotUpdate = Belongings.skipQuickslotUpdate;
		Belongings.skipQuickslotUpdate = true;
		
		Hero heroObject = new Hero();
		heroObject.restoreFromBundle(Bundle.fromString(hero));
		this.hero = heroObject;
		
		// 恢复原来的状态
		Belongings.skipQuickslotUpdate = wasSkipQuickslotUpdate;
	}

	/**
	 * SPDNet 症状21：排行榜浏览视图（NetWndRanking.loadGameData）需要展示上榜英雄的快捷栏。
	 * setHero() 在反序列化阶段为不影响本机快捷栏刻意跳过槽位写入（bundleRestoring 时的
	 * Item.restoreFromBundle 短路），这里以 skipQuickslotUpdate=false 重新还原一次，
	 * 让 Item.restoreFromBundle 把各槽位写回全局 Dungeon.quickslot —— 与本地
	 * Rankings.Record.loadGameData 走 data.get(HERO) 的 bundleRestoring 路径语义一致。
	 */
	public Hero restoreHeroWithQuickslot() {
		if (heroRawBundle == null) return hero;
		Bundle bundle = Bundle.fromString(heroRawBundle);
		if (bundle == null) return hero;
		boolean wasSkipQuickslotUpdate = Belongings.skipQuickslotUpdate;
		Belongings.skipQuickslotUpdate = false;
		try {
			Hero heroObject = new Hero();
			heroObject.restoreFromBundle(bundle);
			this.hero = heroObject;
			return heroObject;
		} finally {
			Belongings.skipQuickslotUpdate = wasSkipQuickslotUpdate;
		}
	}
}
