package me.catand.spdnetserver;

import com.alibaba.fastjson2.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.data.Status;
import me.catand.spdnetserver.data.actions.*;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.DailyGameRecord;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.*;
import me.catand.spdnetserver.service.DailyChallengeService;
import me.catand.spdnetserver.service.PlayerPrefixService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class Handler {
	private final PlayerRepository playerRepository;
	private final GameRecordRepository gameRecordRepository;
	private final PlayerCatalogRepository playerCatalogRepository;
	private final PlayerBestiaryRepository playerBestiaryRepository;
	private final PlayerDocumentRepository playerDocumentRepository;
	private final DailyGameRecordRepository dailyGameRecordRepository;
	private final PlayerPrefixService playerPrefixService;
	private final DailyChallengeService dailyChallengeService;
	private SocketService socketService;
	private Sender sender;
	private Map<UUID, Player> playerMap;
	private ChatService chatService;

	public Handler(PlayerRepository playerRepository, GameRecordRepository gameRecordRepository,
	               PlayerCatalogRepository playerCatalogRepository, PlayerBestiaryRepository playerBestiaryRepository,
	               PlayerDocumentRepository playerDocumentRepository, DailyGameRecordRepository dailyGameRecordRepository,
	               PlayerPrefixService playerPrefixService, DailyChallengeService dailyChallengeService,
	               SocketService socketService, Sender sender, Map<UUID, Player> playerMap, ChatService chatService) {
		this.playerRepository = playerRepository;
		this.gameRecordRepository = gameRecordRepository;
		this.playerCatalogRepository = playerCatalogRepository;
		this.playerBestiaryRepository = playerBestiaryRepository;
		this.playerDocumentRepository = playerDocumentRepository;
		this.dailyGameRecordRepository = dailyGameRecordRepository;
		this.playerPrefixService = playerPrefixService;
		this.dailyChallengeService = dailyChallengeService;
		this.socketService = socketService;
		this.sender = sender;
		this.playerMap = playerMap;
		this.chatService = chatService;
	}

	// SPDNet: 获取玩家当前激活的前缀名称
	private String getPlayerPrefixName(String playerName) {
		return playerPrefixService.getActivePrefixName(playerName);
	}

	public void handleAchievement(Player player, CAchievement cAchievement) {
		// SPDNet: 从数据库重新加载 Player，确保数据最新
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法保存成就", player.getName());
			return;
		}

		// 确保成就集合不为 null
		if (dbPlayer.getAchievements() == null) {
			dbPlayer.setAchievements(new java.util.HashSet<>());
		}

		boolean hasAchievement = dbPlayer.getAchievements().contains(cAchievement.getBadgeEnumString());
		log.info("玩家{}获得了成就{}，是否已经获得：{}", player.getName(), cAchievement.getBadgeEnumString(), hasAchievement);

		if (!hasAchievement) {
			dbPlayer.getAchievements().add(cAchievement.getBadgeEnumString());
			playerRepository.save(dbPlayer);
			// 更新 playerMap 中的 Player 对象
			player.setAchievements(dbPlayer.getAchievements());
		}

		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastAchievement(new SAchievement(player.getName(), cAchievement.getBadgeEnumString(), hasAchievement, prefixName));
	}

	public void handleAnkhUsed(Player player, CAnkhUsed cAnkhUsed) {
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastAnkhUsed(new SAnkhUsed(player.getName(), cAnkhUsed.getCause(), cAnkhUsed.getUnusedBlessedAnkh(), cAnkhUsed.getUnusedUnblessedAnkh(), prefixName));
	}

	public void handleArmorUpdate(Player player, CArmorUpdate cArmorUpdate) {
		log.info("玩家{}装备了{}级护甲", player.getName(), cArmorUpdate.getArmorTier());
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastArmorUpdate(new SArmorUpdate(player.getName(), cArmorUpdate.getArmorTier(), prefixName));
	}

	public void handleChatMessage(Player player, CChatMessage cChatMessage) {
		log.info("玩家{}发送了消息：{}", player.getName(), cChatMessage.getMessage());
		// SPDNet: 使用客户端传来的时间，如果没有则使用服务端时间
		String prefixName = getPlayerPrefixName(player.getName());
		SChatMessage chatMessage = new SChatMessage(player.getName(), cChatMessage.getMessage(), cChatMessage.getTime(), prefixName);
		chatService.addMessage(player.getName(), cChatMessage.getMessage(), cChatMessage.getTime());
		sender.sendBroadcastChatMessage(chatMessage);
	}

	public void handleEnterDungeon(SocketIOClient client, Player player, CEnterDungeon cEnterDungeon) {
		Status status = cEnterDungeon.getStatus();
		player.setStatus(status);
		playerMap.put(client.getSessionId(), player);

		Integer dailyGroupIndex = cEnterDungeon.getDailyGroupIndex();
		Long dailySeed = cEnterDungeon.getDailySeed();
		String dailyRecordDate = cEnterDungeon.getDailyRecordDate();

		if (dailyGroupIndex != null && dailySeed != null) {
			LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

			if (!dailyChallengeService.validateSeed(today, dailyGroupIndex, dailySeed)) {
				log.warn("玩家{}尝试进入每日挑战但种子无效: 期望={}, 实际={}", player.getName(), dailyChallengeService.generateSeed(today, dailyGroupIndex), dailySeed);
			} else {
				Player dbPlayer = playerRepository.findByName(player.getName());
				if (dbPlayer == null) {
					log.error("玩家{}不存在，无法创建每日挑战记录", player.getName());
				} else {
					boolean hasExistingRecord = dailyGameRecordRepository.existsByPlayerAndRecordDateAndGroupIndexAndStatusIn(
						dbPlayer, today, dailyGroupIndex,
						Arrays.asList(DailyGameRecord.DailyRecordStatus.CREATED, DailyGameRecord.DailyRecordStatus.COMPLETED)
					);

					if (hasExistingRecord) {
						log.info("玩家{}已有今日组别{}的每日挑战记录，本次游玩不计入成绩", player.getName(), dailyGroupIndex);
					} else {
						DailyGameRecord dailyRecord = new DailyGameRecord();
						dailyRecord.setPlayer(dbPlayer);
						dailyRecord.setRecordDate(today);
						dailyRecord.setGroupIndex(dailyGroupIndex);
						dailyRecord.setSeed(dailySeed);
						dailyRecord.setStatus(DailyGameRecord.DailyRecordStatus.CREATED);
						dailyGameRecordRepository.save(dailyRecord);
						log.info("玩家{}创建了每日挑战记录: 组别={}, 种子={}", player.getName(), dailyGroupIndex, dailySeed);
					}
				}
			}
		}

		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastEnterDungeon(new SEnterDungeon(player.getName(), status, prefixName));
		log.info("玩家{}以{}挑进入了{}地牢第{}层", player.getName(), Challenges.countActiveChallenges(status.getChallenges()), status.getSeed(), status.getDepth());
	}

	public void handleError(Player player, CError cError) {
	}

	public void handleFloatingText(Player player, CFloatingText cFloatingText) {
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastFloatingText(new SFloatingText(
				player.getName(),
				cFloatingText.getColor(),
				cFloatingText.getText(),
				cFloatingText.getIcon(),
				cFloatingText.getHeroHP(),
				cFloatingText.getHeroShield(),
				cFloatingText.getHeroHT(),
				prefixName));
	}

	public void handleGameEnd(Player player, CGameEnd cGameEnd) {
		log.info("玩家{}{}了游戏，分数：{}", player.getName(), cGameEnd.getRecord().isWin() ? "通关" : "结束", cGameEnd.getRecord().getTotalScore());
		GameRecord gameRecord = cGameEnd.getRecord();
		gameRecord.setPlayer(player);
		gameRecordRepository.save(gameRecord);
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastGameEnd(new SGameEnd(player.getName(), gameRecord, prefixName));
	}

	public void handleDailyGameEnd(SocketIOClient client, Player player, CGameEnd cGameEnd, Integer groupIndex, Long dailySeed) {
		log.info("玩家{}{}了每日挑战游戏，组别：{}，分数：{}", player.getName(), cGameEnd.getRecord().isWin() ? "通关" : "结束", groupIndex, cGameEnd.getRecord().getTotalScore());

		LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

		if (!dailyChallengeService.validateSeed(today, groupIndex, dailySeed)) {
			log.warn("玩家{}提交的每日挑战种子无效: 期望={}, 实际={}", player.getName(), dailyChallengeService.generateSeed(today, groupIndex), dailySeed);
			handleGameEnd(player, cGameEnd);
			return;
		}

		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.warn("玩家{}不存在，作为普通模式处理", player.getName());
			handleGameEnd(player, cGameEnd);
			return;
		}

		DailyGameRecord dailyRecord = dailyGameRecordRepository.findByPlayerAndRecordDateAndGroupIndex(dbPlayer, today, groupIndex)
			.orElse(null);

		if (dailyRecord == null || dailyRecord.getStatus() != DailyGameRecord.DailyRecordStatus.CREATED) {
			log.warn("玩家{}没有进行中的每日挑战记录: 组别={}，作为普通模式处理", player.getName(), groupIndex);
			handleGameEnd(player, cGameEnd);
			return;
		}

		GameRecord gameRecord = cGameEnd.getRecord();
		gameRecord.setPlayer(player);
		gameRecord.setDaily(true);
		gameRecordRepository.save(gameRecord);

		dailyRecord.setGameRecord(gameRecord);
		dailyRecord.markCompleted();
		dailyGameRecordRepository.save(dailyRecord);

		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastGameEnd(new SGameEnd(player.getName(), gameRecord, prefixName));
		log.info("玩家{}每日挑战记录已更新: 组别={}, 分数={}, 胜利={}", player.getName(), groupIndex, gameRecord.getTotalScore(), gameRecord.isWin());
	}

	public void handleRequestDailyChallenge(SocketIOClient client, Player player, CRequestDailyChallenge cRequestDailyChallenge) {
		Integer groupIndex = cRequestDailyChallenge.getGroupIndex();
		if (groupIndex == null || groupIndex < 0 || groupIndex > 2) {
			log.warn("玩家{}请求的每日挑战组别无效: {}", player.getName(), groupIndex);
			sender.sendRejectDailyChallenge(client, new SRejectDailyChallenge(groupIndex, "无效的组别", false));
			return;
		}

		LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法查询每日挑战", player.getName());
			sender.sendRejectDailyChallenge(client, new SRejectDailyChallenge(groupIndex, "玩家不存在", false));
			return;
		}

		long seed = dailyChallengeService.generateSeed(today, groupIndex);
		int challenges = getChallengesForGroup(seed, groupIndex);

		boolean hasExistingRecord = dailyGameRecordRepository.existsByPlayerAndRecordDateAndGroupIndexAndStatusIn(
			dbPlayer, today, groupIndex,
			Arrays.asList(DailyGameRecord.DailyRecordStatus.CREATED, DailyGameRecord.DailyRecordStatus.COMPLETED)
		);

		log.info("玩家{}查询每日挑战: 组别={}, 种子={}, 已有记录={}", player.getName(), groupIndex, seed, hasExistingRecord);
		sender.sendAllowDailyChallenge(client, new SAllowDailyChallenge(groupIndex, seed, today.toString(), hasExistingRecord, challenges));
	}

	private int getChallengesForGroup(long seed, int groupIndex) {
		int[] minChallenges = {0, 4, 7};
		int[] maxChallenges = {3, 6, 9};

		int min = minChallenges[groupIndex];
		int max = maxChallenges[groupIndex];

		if (min == 0 && max == 0) {
			return 0;
		}

		java.util.Random random = new java.util.Random(seed);
		int count = min + random.nextInt(max - min + 1);

		if (count == 0) {
			return 0;
		}
		if (count == 9) {
			return 511;
		}

		int[] masks = {128, 256, 1, 2, 4, 8, 16, 32, 64};
		java.util.List<Integer> availableMasks = new java.util.ArrayList<>();
		for (int mask : masks) {
			availableMasks.add(mask);
		}
		java.util.Collections.shuffle(availableMasks, random);

		int result = 0;
		for (int i = 0; i < count; i++) {
			result += availableMasks.get(i);
		}
		return result;
	}

	public void handleGiveItem(Player player, CGiveItem cGiveItem) {
		String prefixName = getPlayerPrefixName(player.getName());
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cGiveItem.getTargetName())) {
				sender.sendGiveItem(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SGiveItem(player.getName(), cGiveItem.getItem(), prefixName));
			}
		});
	}

	public void handleHero(Player player, CHero cHero) {
		String prefixName = getPlayerPrefixName(player.getName());
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cHero.getSourceName())) {
				sender.sendHero(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SHero(player.getName(), cHero.getHero(), prefixName));
			}
		});
	}

	public void handleLeaveDungeon(Player player, CLeaveDungeon cLeaveDungeon) {
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastLeaveDungeon(new SLeaveDungeon(player.getName(), prefixName));
	}

	public void handlePlayerChangeFloor(Player player, CPlayerChangeFloor cPlayerChangeFloor) {
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastPlayerChangeFloor(new SPlayerChangeFloor(player.getName(), cPlayerChangeFloor.getDepth(), prefixName));
	}

	public void handlePlayerMove(Player player, CPlayerMove cPlayerMove) {
		String prefixName = getPlayerPrefixName(player.getName());
		sender.sendBroadcastPlayerMove(new SPlayerMove(player.getName(), cPlayerMove.getPos(), prefixName));
		log.info("玩家{}移动到了{}", player.getName(), cPlayerMove.getPos());
	}

	public void handleRequestLeaderboard(SocketIOClient client, CRequestLeaderboard cRequestLeaderboard) {
		if (cRequestLeaderboard.getSortCriteria() == null) {
			cRequestLeaderboard.setSortCriteria("id");
		}
		Sort sort;
		if (cRequestLeaderboard.getSortCriteria().equals("duration")) {
			sort = Sort.by(Sort.Direction.ASC, cRequestLeaderboard.getSortCriteria());
		} else {
			sort = Sort.by(Sort.Direction.DESC, cRequestLeaderboard.getSortCriteria());
		}
		Pageable pageable = PageRequest.of(cRequestLeaderboard.getPage() - 1, cRequestLeaderboard.getAmountPerPage(), sort);

		// SPDNet: 默认排除被ban玩家，如果bannedOnly=true则只显示被ban玩家
		Boolean bannedOnly = cRequestLeaderboard.getBannedOnly() != null ? cRequestLeaderboard.getBannedOnly() : false;

		Page<GameRecord> page = gameRecordRepository.findWithFilters(
				cRequestLeaderboard.getPlayerName(),
				cRequestLeaderboard.getWinOnly(),
				cRequestLeaderboard.getGameMode(),
				cRequestLeaderboard.getChallengeCount(),
				bannedOnly,
				pageable
		);
		// 显示第1页 共有10页 共有100条记录
		log.info("玩家{}请求了排行榜, 显示第{}页 共有{}页 共有{}条记录", playerMap.get(client.getSessionId()).getName(), page.getNumber(), page.getTotalPages(), page.getTotalElements());
		int totalPages = page.getTotalPages();
		int currentPage = page.getNumber();
		int totalElements = (int) page.getTotalElements();
		List<GameRecord> gameRecords = page.getContent();
		gameRecords.forEach(gameRecord -> {
			gameRecord.setPlayerName(gameRecord.getPlayer().getName());
		});
		List<String> gameRecordsString = gameRecords.stream().map(JSON::toJSONString).toList();
		sender.sendLeaderboard(client, new SLeaderboard(totalPages, currentPage + 1, totalElements, gameRecordsString));
	}

	public void handleRequestPlayerList(SocketIOClient client, CRequestPlayerList cRequestPlayerList) {
		sender.sendPlayerList(client, new SPlayerList(playerMap));
		log.info("玩家{}请求了玩家列表", playerMap.get(client.getSessionId()).getName());
	}

	public void handleViewHero(Player player, CViewHero cViewHero) {
		log.info("玩家{}请求查看玩家{}", player.getName(), cViewHero.getTargetName());
		String prefixName = getPlayerPrefixName(player.getName());
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cViewHero.getTargetName())) {
				sender.sendViewHero(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SViewHero(player.getName(), prefixName));
			}
		});
	}

	// SPDNet: 处理 Catalog 更新
	public void handleCatalogUpdate(Player player, CCatalogUpdate cCatalogUpdate) {
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法保存 Catalog 数据", player.getName());
			return;
		}

		me.catand.spdnetserver.entitys.PlayerCatalog catalog = playerCatalogRepository
				.findByPlayerIdAndCatalogTypeAndItemClass(
						dbPlayer.getId(),
						cCatalogUpdate.getCatalogType(),
						cCatalogUpdate.getItemClass())
				.orElse(new me.catand.spdnetserver.entitys.PlayerCatalog());

		catalog.setPlayerId(dbPlayer.getId());
		catalog.setCatalogType(cCatalogUpdate.getCatalogType());
		catalog.setItemClass(cCatalogUpdate.getItemClass());
		catalog.setSeen(cCatalogUpdate.isSeen());
		catalog.setUseCount(cCatalogUpdate.getUseCount());

		playerCatalogRepository.save(catalog);
		log.info("玩家{}更新了 Catalog 数据：{} - {}", player.getName(), cCatalogUpdate.getCatalogType(), cCatalogUpdate.getItemClass());
	}

	// SPDNet: 处理 Bestiary 更新
	public void handleBestiaryUpdate(Player player, CBestiaryUpdate cBestiaryUpdate) {
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法保存 Bestiary 数据", player.getName());
			return;
		}

		me.catand.spdnetserver.entitys.PlayerBestiary bestiary = playerBestiaryRepository
				.findByPlayerIdAndBestiaryTypeAndEntityClass(
						dbPlayer.getId(),
						cBestiaryUpdate.getBestiaryType(),
						cBestiaryUpdate.getEntityClass())
				.orElse(new me.catand.spdnetserver.entitys.PlayerBestiary());

		bestiary.setPlayerId(dbPlayer.getId());
		bestiary.setBestiaryType(cBestiaryUpdate.getBestiaryType());
		bestiary.setEntityClass(cBestiaryUpdate.getEntityClass());
		bestiary.setSeen(cBestiaryUpdate.isSeen());
		bestiary.setEncountered(cBestiaryUpdate.getEncountered());

		playerBestiaryRepository.save(bestiary);
		log.info("玩家{}更新了 Bestiary 数据：{} - {}", player.getName(), cBestiaryUpdate.getBestiaryType(), cBestiaryUpdate.getEntityClass());
	}

	// SPDNet: 处理 Document 更新
	public void handleDocumentUpdate(Player player, CDocumentUpdate cDocumentUpdate) {
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法保存 Document 数据", player.getName());
			return;
		}

		me.catand.spdnetserver.entitys.PlayerDocument document = playerDocumentRepository
				.findByPlayerIdAndDocumentTypeAndPageName(
						dbPlayer.getId(),
						cDocumentUpdate.getDocumentType(),
						cDocumentUpdate.getPageName())
				.orElse(new me.catand.spdnetserver.entitys.PlayerDocument());

		document.setPlayerId(dbPlayer.getId());
		document.setDocumentType(cDocumentUpdate.getDocumentType());
		document.setPageName(cDocumentUpdate.getPageName());
		document.setState(cDocumentUpdate.getState());

		playerDocumentRepository.save(document);
		log.info("玩家{}更新了 Document 数据：{} - {} state={}", player.getName(), cDocumentUpdate.getDocumentType(), cDocumentUpdate.getPageName(), cDocumentUpdate.getState());
	}

	// SPDNet: 加载玩家的 Journal 数据并发送给客户端
	public void loadAndSendJournals(SocketIOClient client, Player player) {
		Player dbPlayer = playerRepository.findByName(player.getName());
		if (dbPlayer == null) {
			log.error("玩家{}不存在，无法加载 Journal 数据", player.getName());
			return;
		}

		List<me.catand.spdnetserver.entitys.PlayerCatalog> catalogs = playerCatalogRepository.findByPlayerId(dbPlayer.getId());
		List<me.catand.spdnetserver.entitys.PlayerBestiary> bestiaries = playerBestiaryRepository.findByPlayerId(dbPlayer.getId());
		List<me.catand.spdnetserver.entitys.PlayerDocument> documents = playerDocumentRepository.findByPlayerId(dbPlayer.getId());

		List<SJournals.CatalogData> catalogDataList = new java.util.ArrayList<>();
		for (me.catand.spdnetserver.entitys.PlayerCatalog catalog : catalogs) {
			catalogDataList.add(new SJournals.CatalogData(catalog.getCatalogType(), catalog.getItemClass(), catalog.isSeen(), catalog.getUseCount()));
		}

		List<SJournals.BestiaryData> bestiaryDataList = new java.util.ArrayList<>();
		for (me.catand.spdnetserver.entitys.PlayerBestiary bestiary : bestiaries) {
			bestiaryDataList.add(new SJournals.BestiaryData(bestiary.getBestiaryType(), bestiary.getEntityClass(), bestiary.isSeen(), bestiary.getEncountered()));
		}

		List<SJournals.DocumentData> documentDataList = new java.util.ArrayList<>();
		for (me.catand.spdnetserver.entitys.PlayerDocument document : documents) {
			documentDataList.add(new SJournals.DocumentData(document.getDocumentType(), document.getPageName(), document.getState()));
		}

		sender.sendJournals(client, new SJournals(catalogDataList, bestiaryDataList, documentDataList));
		log.info("玩家{}的 Journal 数据已发送，包含 {} 个 Catalog, {} 个 Bestiary, {} 个 Document",
			player.getName(), catalogs.size(), bestiaries.size(), documents.size());
	}
}
