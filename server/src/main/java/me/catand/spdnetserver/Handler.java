package me.catand.spdnetserver;

import com.alibaba.fastjson2.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.data.Status;
import me.catand.spdnetserver.data.actions.*;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.GameRecordRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
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
	private SocketService socketService;
	private Sender sender;
	private Map<UUID, Player> playerMap;

	public Handler(PlayerRepository playerRepository, GameRecordRepository gameRecordRepository, SocketService socketService, Sender sender, Map<UUID, Player> playerMap) {
		this.playerRepository = playerRepository;
		this.gameRecordRepository = gameRecordRepository;
		this.socketService = socketService;
		this.sender = sender;
		this.playerMap = playerMap;
	}

	public void handleAchievement(Player player, CAchievement cAchievement) {
		boolean hasAchievement = playerRepository.hasAchievement(player.getName(), cAchievement.getBadgeEnumString());
		log.info("玩家{}获得了成就{}，是否已经获得：{}", player.getName(), cAchievement.getBadgeEnumString(), hasAchievement);
		if (!hasAchievement) {
			player.getAchievements().add(cAchievement.getBadgeEnumString());
			playerRepository.save(player);
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
		sender.sendBroadcastChatMessage(new SChatMessage(player.getName(), cChatMessage.getMessage()));
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
}
