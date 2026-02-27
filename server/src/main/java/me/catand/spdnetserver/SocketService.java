package me.catand.spdnetserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.corundumstudio.socketio.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.data.actions.*;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.repositories.GameRecordRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Service
public class SocketService {
	private static SocketService instance;
	@Autowired
	private PlayerRepository playerRepository;
	@Autowired
	private GameRecordRepository gameRecordRepository;
	@Autowired
	private SpdProperties spdProperties;
	private SocketIOServer server;
	private Map<UUID, Player> playerMap = new ConcurrentHashMap<>();
	private Sender sender;
	private Handler handler;
	private SocketIONamespace spdNetNamespace;
	public static ConcurrentHashMap<String, Long> seeds = new ConcurrentHashMap<>();

	public static SocketService getInstance() {
		if (instance == null) {
			synchronized (SocketService.class) {
				if (instance == null) {
					instance = new SocketService();
				}
			}
		}
		return instance;
	}

	@PostConstruct
	public void init() {
		Configuration config = new Configuration();
		config.setHostname("0.0.0.0");
		config.setPort(21687);

		instance = this;
		server = new SocketIOServer(config);
		spdNetNamespace = server.addNamespace("/spdnet");
		if (seeds.isEmpty()) {
			seeds.put("seedFUN", getNoonTimestamp());
		}
		server.start();
		startAll();
		sender = new Sender(server);
		handler = new Handler(playerRepository, gameRecordRepository, this, sender, playerMap);
	}

	@PreDestroy
	public void stopServer() {
		server.stop();
	}

	/**
	 * 启动所有事件
	 */
	private void startAll() {
		spdNetNamespace.addConnectListener(client -> {
			HandshakeData handshakeData = client.getHandshakeData();
			// 检测Socket.IO设置的AuthToken
			LinkedHashMap<String, String> authTokenMap = (LinkedHashMap) handshakeData.getAuthToken();
			String authToken = null;
			if (authTokenMap != null && authTokenMap.containsKey("token")) {
				authToken = authTokenMap.get("token");
			}
			// 检测Query参数中的token
			String authTokenQuery = handshakeData.getSingleUrlParam("token");
			String spdVersion = handshakeData.getSingleUrlParam("SPDVersion");
			String netVersion = handshakeData.getSingleUrlParam("NetVersion");
			if (authToken == null) {
				authToken = authTokenQuery;
			}
			if (!playerRepository.existsByKey(authToken)) {
				client.sendEvent(Events.ERROR.getName(), new SError("Key无效"));
				log.info("连接失败: Key无效, " + authToken + ", " + client.getSessionId());
				client.disconnect();
			} else if (!(spdProperties.getVersion().equals(spdVersion) && (spdProperties.getNetVersion().equals(netVersion) || netVersion.equals(spdProperties.getNetVersion() + "-INDEV")))) {
				client.sendEvent(Events.ERROR.getName(), new SError("版本不匹配"));
				log.info("连接失败: 版本不匹配, 破碎版本: " + spdVersion + ", Net版本: " + netVersion + ", " + client.getSessionId());
				client.disconnect();
			} else {
				Player player = playerRepository.findByKey(authToken);
				final boolean[] isDuplicate = {false};
				playerMap.forEach((uuid, player1) -> {
					if (player1.getName().equals(player.getName())) {
						client.sendEvent(Events.ERROR.getName(), new SError(player.getName() + "已登录, 重复登录"));
						log.info("连接失败: " + player.getName() + "已登录, 重复登录, " + client.getSessionId());
						client.disconnect();
						isDuplicate[0] = true;
					}
				});
				if (isDuplicate[0]) {
					return;
				}
				playerMap.put(client.getSessionId(), player);
				sender.sendInit(client, new SInit(player.getName(), spdProperties.getMotd(), seeds));
				sender.sendBroadcastJoin(new SJoin(player.getQq(), player.getName(), player.getPower()));
				sender.sendPlayerList(client, new SPlayerList(playerMap));
				log.info("玩家已连接: " + player.getName() + ", " + client.getSessionId());
			}
		});
		spdNetNamespace.addDisconnectListener(client -> {
			Player player = playerMap.get(client.getSessionId());
			if (player != null) {
				playerMap.remove(client.getSessionId());
				sender.sendBroadcastExit(new SExit(player.getName()));
				log.info("玩家已断开连接: " + player.getName() + ", " + client.getSessionId());
			}
		});
		spdNetNamespace.addEventListener(Actions.ACHIEVEMENT.getName(), String.class, (client, data, ackSender) -> {
			handler.handleAchievement(playerMap.get(client.getSessionId()), JSON.parseObject(data, CAchievement.class));
		});
		spdNetNamespace.addEventListener(Actions.ANKH_USED.getName(), String.class, (client, data, ackSender) -> {
			handler.handleAnkhUsed(playerMap.get(client.getSessionId()), JSON.parseObject(data, CAnkhUsed.class));
		});
		spdNetNamespace.addEventListener(Actions.ARMOR_UPDATE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleArmorUpdate(playerMap.get(client.getSessionId()), JSON.parseObject(data, CArmorUpdate.class));
		});
		spdNetNamespace.addEventListener(Actions.CHAT_MESSAGE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleChatMessage(playerMap.get(client.getSessionId()), JSON.parseObject(data, CChatMessage.class));
		});
		spdNetNamespace.addEventListener(Actions.ENTER_DUNGEON.getName(), String.class, (client, data, ackSender) -> {
			handler.handleEnterDungeon(client, playerMap.get(client.getSessionId()), JSON.parseObject(data, CEnterDungeon.class));
		});
		spdNetNamespace.addEventListener(Actions.ERROR.getName(), String.class, (client, data, ackSender) -> {
			handler.handleError(playerMap.get(client.getSessionId()), JSON.parseObject(data, CError.class));
		});
		spdNetNamespace.addEventListener(Actions.FLOATING_TEXT.getName(), String.class, (client, data, ackSender) -> {
			handler.handleFloatingText(playerMap.get(client.getSessionId()), JSON.parseObject(data, CFloatingText.class));
		});
		spdNetNamespace.addEventListener(Actions.GAME_END.getName(), String.class, (client, data, ackSender) -> {
			JSONObject cGameEndJson = JSON.parseObject(data, JSONObject.class);
			CGameEnd gameEnd = new CGameEnd(JSONObject.parseObject(cGameEndJson.getString("record"), GameRecord.class));
			handler.handleGameEnd(playerMap.get(client.getSessionId()), gameEnd);
		});
		spdNetNamespace.addEventListener(Actions.GIVE_ITEM.getName(), String.class, (client, data, ackSender) -> {
			handler.handleGiveItem(playerMap.get(client.getSessionId()), JSON.parseObject(data, CGiveItem.class));
		});
		spdNetNamespace.addEventListener(Actions.HERO.getName(), String.class, (client, data, ackSender) -> {
			handler.handleHero(playerMap.get(client.getSessionId()), JSON.parseObject(data, CHero.class));
		});
		spdNetNamespace.addEventListener(Actions.LEAVE_DUNGEON.getName(), String.class, (client, data, ackSender) -> {
			handler.handleLeaveDungeon(playerMap.get(client.getSessionId()), JSON.parseObject(data, CLeaveDungeon.class));
		});
		spdNetNamespace.addEventListener(Actions.PLAYER_CHANGE_FLOOR.getName(), String.class, (client, data, ackSender) -> {
			handler.handlePlayerChangeFloor(playerMap.get(client.getSessionId()), JSON.parseObject(data, CPlayerChangeFloor.class));
		});
		spdNetNamespace.addEventListener(Actions.PLAYER_MOVE.getName(), String.class, (client, data, ackSender) -> {
			handler.handlePlayerMove(playerMap.get(client.getSessionId()), JSON.parseObject(data, CPlayerMove.class));
		});
		spdNetNamespace.addEventListener(Actions.REQUEST_LEADERBOARD.getName(), String.class, (client, data, ackSender) -> {
			handler.handleRequestLeaderboard(client, JSON.parseObject(data, CRequestLeaderboard.class));
		});
		spdNetNamespace.addEventListener(Actions.REQUEST_PLAYER_LIST.getName(), String.class, (client, data, ackSender) -> {
			handler.handleRequestPlayerList(client, JSON.parseObject(data, CRequestPlayerList.class));
		});
		spdNetNamespace.addEventListener(Actions.VIEW_HERO.getName(), String.class, (client, data, ackSender) -> {
			handler.handleViewHero(playerMap.get(client.getSessionId()), JSON.parseObject(data, CViewHero.class));
		});

	}

	/**
	 * 定时更改种子
	 */
	@Scheduled(cron = "0 30 0 * * ?")
	public void doSomething() {
		seeds.clear();
		seeds.put("seedFUN", getNoonTimestamp());
		sender.sendBroadcastError(new SError("换种子了嗷"));
		server.getAllClients().forEach(ClientOperations::disconnect);
	}

	private long getNoonTimestamp() {
		LocalDateTime todayNoon = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).withHour(12).withMinute(0).withSecond(0).withNano(0);
		ZonedDateTime zdt = todayNoon.atZone(ZoneId.of("Asia/Shanghai"));
		return zdt.toInstant().toEpochMilli();
	}
}