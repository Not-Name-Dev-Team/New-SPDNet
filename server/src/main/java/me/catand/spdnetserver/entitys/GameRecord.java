package me.catand.spdnetserver.entitys;

import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.Challenges;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JSONField(serialize = false)
	private long id;
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

	private String hero;
	private String badges;
	private String handlers;
	private int challenges;
	@JSONField(serialize = false, name = "challenge_amount")
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
	@ManyToOne
	@JoinColumn(name = "player_id")
	@JSONField(serialize = false)
	private Player player;
	@Transient
	@JSONField(name = "player_name")
	private String playerName;

	public void setchallenges(int challenges) {
		this.challenges = challenges;
		this.challengeAmount = Challenges.countActiveChallenges(challenges);
	}
}
