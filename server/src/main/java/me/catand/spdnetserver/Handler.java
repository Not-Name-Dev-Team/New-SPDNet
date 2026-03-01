package me.catand.spdnetserver;

import com.alibaba.fastjson2.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.data.Status;
import me.catand.spdnetserver.data.actions.*;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
	private SocketService socketService;
	private Sender sender;
	private Map<UUID, Player> playerMap;
	private ChatService chatService;

	public Handler(PlayerRepository playerRepository, GameRecordRepository gameRecordRepository, 
	               PlayerCatalogRepository playerCatalogRepository, PlayerBestiaryRepository playerBestiaryRepository,
	               PlayerDocumentRepository playerDocumentRepository,
	               SocketService socketService, Sender sender, Map<UUID, Player> playerMap, ChatService chatService) {
		this.playerRepository = playerRepository;
		this.gameRecordRepository = gameRecordRepository;
		this.playerCatalogRepository = playerCatalogRepository;
		this.playerBestiaryRepository = playerBestiaryRepository;
		this.playerDocumentRepository = playerDocumentRepository;
		this.socketService = socketService;
		this.sender = sender;
		this.playerMap = playerMap;
		this.chatService = chatService;
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
		
		sender.sendBroadcastAchievement(new SAchievement(player.getName(), cAchievement.getBadgeEnumString(), hasAchievement));
	}

	public void handleAnkhUsed(Player player, CAnkhUsed cAnkhUsed) {
		sender.sendBroadcastAnkhUsed(new SAnkhUsed(player.getName(), cAnkhUsed.getCause(), cAnkhUsed.getUnusedBlessedAnkh(), cAnkhUsed.getUnusedUnblessedAnkh()));
	}

	public void handleArmorUpdate(Player player, CArmorUpdate cArmorUpdate) {
		log.info("玩家{}装备了{}级护甲", player.getName(), cArmorUpdate.getArmorTier());
		sender.sendBroadcastArmorUpdate(new SArmorUpdate(player.getName(), cArmorUpdate.getArmorTier()));
	}

	public void handleChatMessage(Player player, CChatMessage cChatMessage) {
		log.info("玩家{}发送了消息：{}", player.getName(), cChatMessage.getMessage());
		// SPDNet: 使用客户端传来的时间，如果没有则使用服务端时间
		SChatMessage chatMessage = new SChatMessage(player.getName(), cChatMessage.getMessage(), cChatMessage.getTime());
		chatService.addMessage(player.getName(), cChatMessage.getMessage(), cChatMessage.getTime());
		sender.sendBroadcastChatMessage(chatMessage);
	}

	public void handleEnterDungeon(SocketIOClient client, Player player, CEnterDungeon cEnterDungeon) {
		Status status = cEnterDungeon.getStatus();
		player.setStatus(status);
		playerMap.put(client.getSessionId(), player);
		sender.sendBroadcastEnterDungeon(new SEnterDungeon(player.getName(), status));
		log.info("玩家{}以{}挑进入了{}地牢第{}层", player.getName(), Challenges.countActiveChallenges(status.getChallenges()), status.getSeed(), status.getDepth());
	}

	public void handleError(Player player, CError cError) {
	}

	public void handleFloatingText(Player player, CFloatingText cFloatingText) {
		sender.sendBroadcastFloatingText(new SFloatingText(
				player.getName(),
				cFloatingText.getColor(),
				cFloatingText.getText(),
				cFloatingText.getIcon(),
				cFloatingText.getHeroHP(),
				cFloatingText.getHeroShield(),
				cFloatingText.getHeroHT()));
	}

	public void handleGameEnd(Player player, CGameEnd cGameEnd) {
		log.info("玩家{}{}了游戏，分数：{}", player.getName(), cGameEnd.getRecord().isWin() ? "通关" : "结束", cGameEnd.getRecord().getTotalScore());
		GameRecord gameRecord = cGameEnd.getRecord();
		gameRecord.setPlayer(player);
		gameRecordRepository.save(gameRecord);
		sender.sendBroadcastGameEnd(new SGameEnd(player.getName(), gameRecord));
	}

	public void handleGiveItem(Player player, CGiveItem cGiveItem) {
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cGiveItem.getTargetName())) {
				sender.sendGiveItem(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SGiveItem(player.getName(), cGiveItem.getItem()));
			}
		});
	}

	public void handleHero(Player player, CHero cHero) {
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cHero.getSourceName())) {
				sender.sendHero(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SHero(player.getName(), cHero.getHero()));
			}
		});
	}

	public void handleLeaveDungeon(Player player, CLeaveDungeon cLeaveDungeon) {
		sender.sendBroadcastLeaveDungeon(new SLeaveDungeon(player.getName()));
	}

	public void handlePlayerChangeFloor(Player player, CPlayerChangeFloor cPlayerChangeFloor) {
		sender.sendBroadcastPlayerChangeFloor(new SPlayerChangeFloor(player.getName(), cPlayerChangeFloor.getDepth()));
	}

	public void handlePlayerMove(Player player, CPlayerMove cPlayerMove) {
		sender.sendBroadcastPlayerMove(new SPlayerMove(player.getName(), cPlayerMove.getPos()));
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

		Page<GameRecord> page = gameRecordRepository.findWithFilters(
				cRequestLeaderboard.getPlayerName(),
				cRequestLeaderboard.getWinOnly(),
				cRequestLeaderboard.getGameMode(),
				cRequestLeaderboard.getChallengeCount(),
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
		playerMap.forEach((uuid, player1) -> {
			if (player1.getName().equals(cViewHero.getTargetName())) {
				sender.sendViewHero(socketService.getServer().getNamespace("/spdnet").getClient(uuid), new SViewHero(player.getName()));
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

		me.catand.spdnetserver.entitys.PlayerCatalog catalog = new me.catand.spdnetserver.entitys.PlayerCatalog();
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

		me.catand.spdnetserver.entitys.PlayerBestiary bestiary = new me.catand.spdnetserver.entitys.PlayerBestiary();
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

		me.catand.spdnetserver.entitys.PlayerDocument document = new me.catand.spdnetserver.entitys.PlayerDocument();
		document.setPlayerId(dbPlayer.getId());
		document.setDocumentType(cDocumentUpdate.getDocumentType());
		document.setPageName(cDocumentUpdate.getPageName());
		document.setFound(cDocumentUpdate.isFound());

		playerDocumentRepository.save(document);
		log.info("玩家{}更新了 Document 数据：{} - {}", player.getName(), cDocumentUpdate.getDocumentType(), cDocumentUpdate.getPageName());
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
			documentDataList.add(new SJournals.DocumentData(document.getDocumentType(), document.getPageName(), document.isFound()));
		}

		sender.sendJournals(client, new SJournals(catalogDataList, bestiaryDataList, documentDataList));
		log.info("玩家{}的 Journal 数据已发送，包含 {} 个 Catalog, {} 个 Bestiary, {} 个 Document",
			player.getName(), catalogs.size(), bestiaries.size(), documents.size());
	}
}
