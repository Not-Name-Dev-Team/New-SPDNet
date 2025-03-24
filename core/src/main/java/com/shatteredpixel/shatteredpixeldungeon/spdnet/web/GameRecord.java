package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.alibaba.fastjson.annotation.JSONField;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
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
	@JSONField(name = "flr_expl")
	private String flrExpl;
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
	private int duration;
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

	public void setHero(String hero) {
		Hero heroObject = new Hero();
		heroObject.restoreFromBundle(Bundle.fromString(hero));
		this.hero = heroObject;
	}
}
