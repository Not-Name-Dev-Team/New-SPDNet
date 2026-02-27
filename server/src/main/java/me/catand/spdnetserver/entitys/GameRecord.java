package me.catand.spdnetserver.entitys;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.Challenges;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"player"})
public class GameRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private long id;
	private String cause;
	private boolean win;
	private int score;
	@JsonProperty("class")
	@JSONField(name = "class")
	private String heroClass;
	private int tier;
	private int level;
	private int depth;
	private boolean ascending;
	private String date;
	private String version;
	@JsonProperty("net_version")
	@JSONField(name = "net_version")
	private String netVersion;
	@JsonProperty("game_mode")
	@JSONField(name = "game_mode")
	private String gameMode;

	private String hero;
	private String badges;
	private String handlers;
	private int challenges;
	@JsonIgnore
	@JsonProperty("challenge_amount")
	@JSONField(name = "challenge_amount")
	private int challengeAmount;
	@JsonProperty("game_version")
	@JSONField(name = "game_version")
	private int gameVersion;
	private long seed;
	@JsonProperty("custom_seed")
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
	@JsonProperty("prog_score")
	@JSONField(name = "prog_score")
	private int progScore;
	@JsonProperty("item_val")
	@JSONField(name = "item_val")
	private int itemVal;
	@JsonProperty("tres_score")
	@JSONField(name = "tres_score")
	private int tresScore;
	@JsonProperty("flr_expl_")
	@JSONField(name = "flr_expl_")
	private String flrExpl;
	@JsonProperty("expl_score")
	@JSONField(name = "expl_score")
	private int explScore;
	@JsonProperty("boss_scores")
	@JSONField(name = "boss_scores")
	private int[] bossScores;
	@JsonProperty("tot_boss")
	@JSONField(name = "tot_boss")
	private int totBoss;
	@JsonProperty("quest_scores")
	@JSONField(name = "quest_scores")
	private int[] questScores;
	@JsonProperty("tot_quest")
	@JSONField(name = "tot_quest")
	private int totQuest;
	@JsonProperty("win_mult")
	@JSONField(name = "win_mult")
	private float winMult;
	@JsonProperty("chal_mult")
	@JSONField(name = "chal_mult")
	private float chalMult;
	@JsonProperty("total_score")
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
	@ManyToOne
	@JoinColumn(name = "player_id")
	@JsonIgnore
	private Player player;
	@Transient
	@JsonProperty("player_name")
	@JSONField(name = "player_name")
	private String playerName;

	public void setchallenges(int challenges) {
		this.challenges = challenges;
		this.challengeAmount = Challenges.countActiveChallenges(challenges);
	}
}
