package me.catand.spdnetserver.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.catand.spdnetserver.entitys.GameRecord;

/**
 * SPDNet: 排行榜记录DTO
 * 包含游戏记录和玩家前缀信息
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardRecordDTO {
	// 游戏记录信息
	private Long id;
	private String cause;
	private boolean win;
	private int score;
	private String heroClass;
	private int tier;
	private int level;
	private int depth;
	private boolean ascending;
	private String date;
	private String version;
	private String netVersion;
	private String gameMode;
	private String hero;
	private String badges;
	private String handlers;
	private int challenges;
	private int challengeAmount;
	private int gameVersion;
	private long seed;
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
	private int progScore;
	private int itemVal;
	private int tresScore;
	private String flrExpl;
	private int explScore;
	private int[] bossScores;
	private int totBoss;
	private int[] questScores;
	private int totQuest;
	private float winMult;
	private float chalMult;
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

	// 玩家信息
	private String playerName;

	// SPDNet: 玩家前缀信息
	private PlayerPrefixDTO prefix;

	/**
	 * 从GameRecord创建DTO
	 */
	public static LeaderboardRecordDTO fromGameRecord(GameRecord record) {
		LeaderboardRecordDTO dto = new LeaderboardRecordDTO();
		dto.setId(record.getId());
		dto.setCause(record.getCause());
		dto.setWin(record.isWin());
		dto.setScore(record.getScore());
		dto.setHeroClass(record.getHeroClass());
		dto.setTier(record.getTier());
		dto.setLevel(record.getLevel());
		dto.setDepth(record.getDepth());
		dto.setAscending(record.isAscending());
		dto.setDate(record.getDate());
		dto.setVersion(record.getVersion());
		dto.setNetVersion(record.getNetVersion());
		dto.setGameMode(record.getGameMode());
		dto.setHero(record.getHero());
		dto.setBadges(record.getBadges());
		dto.setHandlers(record.getHandlers());
		dto.setChallenges(record.getChallenges());
		dto.setChallengeAmount(record.getChallengeAmount());
		dto.setGameVersion(record.getGameVersion());
		dto.setSeed(record.getSeed());
		dto.setCustomSeed(record.getCustomSeed());
		dto.setDaily(record.isDaily());
		dto.setDailyReplay(record.isDailyReplay());
		dto.setGold(record.getGold());
		dto.setMaxDepth(record.getMaxDepth());
		dto.setMaxAscent(record.getMaxAscent());
		dto.setEnemiesSlain(record.getEnemiesSlain());
		dto.setFoodEaten(record.getFoodEaten());
		dto.setPotionsCooked(record.getPotionsCooked());
		dto.setPriranhas(record.getPriranhas());
		dto.setAnkhsUsed(record.getAnkhsUsed());
		dto.setProgScore(record.getProgScore());
		dto.setItemVal(record.getItemVal());
		dto.setTresScore(record.getTresScore());
		dto.setFlrExpl(record.getFlrExpl());
		dto.setExplScore(record.getExplScore());
		dto.setBossScores(record.getBossScores());
		dto.setTotBoss(record.getTotBoss());
		dto.setQuestScores(record.getQuestScores());
		dto.setTotQuest(record.getTotQuest());
		dto.setWinMult(record.getWinMult());
		dto.setChalMult(record.getChalMult());
		dto.setTotalScore(record.getTotalScore());
		dto.setUpgradesUsed(record.getUpgradesUsed());
		dto.setSneakAttacks(record.getSneakAttacks());
		dto.setThrownAssists(record.getThrownAssists());
		dto.setSpawnersAlive(record.getSpawnersAlive());
		dto.setDuration(record.getDuration());
		dto.setQualifiedForNoKilling(record.isQualifiedForNoKilling());
		dto.setQualifiedForBossRemainsBadge(record.isQualifiedForBossRemainsBadge());
		dto.setQualifiedForBossChallengeBadge(record.isQualifiedForBossChallengeBadge());
		dto.setAmuletObtained(record.isAmuletObtained());
		dto.setWon(record.isWon());
		dto.setAscended(record.isAscended());

		// 设置玩家名称
		if (record.getPlayer() != null) {
			dto.setPlayerName(record.getPlayer().getName());
		} else {
			dto.setPlayerName(record.getPlayerName());
		}

		return dto;
	}
}
