package me.catand.spdnetserver;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.corundumstudio.socketio.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnet.protocol.Actions;
import me.catand.spdnet.protocol.Events;
import me.catand.spdnetserver.data.actions.*;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.GameRecord;
import me.catand.spdnetserver.entitys.Player;
import me.catand.spdnetserver.entitys.UserRole;
import me.catand.spdnetserver.repositories.GameRecordRepository;
import me.catand.spdnetserver.repositories.DailyGameRecordRepository;
import me.catand.spdnetserver.repositories.PlayerRepository;
import me.catand.spdnetserver.repositories.PlayerCatalogRepository;
import me.catand.spdnetserver.repositories.PlayerBestiaryRepository;
import me.catand.spdnetserver.repositories.PlayerDocumentRepository;
import me.catand.spdnetserver.service.PlayerPrefixService;
import me.catand.spdnetserver.service.DailyChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	private DailyGameRecordRepository dailyGameRecordRepository;
	@Autowired
	private PlayerCatalogRepository playerCatalogRepository;
	@Autowired
	private PlayerBestiaryRepository playerBestiaryRepository;
	@Autowired
	private PlayerDocumentRepository playerDocumentRepository;
	@Autowired
	private SpdProperties spdProperties;
	@Autowired
private ChatService chatService;
@Autowired
private PlayerPrefixService playerPrefixService;
@Autowired
private DailyChallengeService dailyChallengeService;
private SocketIOServer server;
	private Map<UUID, Player> playerMap = new ConcurrentHashMap<>();
	// SPDNet: 玩家名 -> sessionId 索引，避免反复遍历 playerMap 找人（O(N) → O(1)）
	private Map<String, UUID> nameToSessionId = new ConcurrentHashMap<>();
	private Sender sender;
	private Handler handler;
	private SocketIONamespace spdNetNamespace;
	public static ConcurrentHashMap<String, Long> seeds = new ConcurrentHashMap<>();
	private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
		config.setPort(32814);

		// SPDNet: 缩短心跳与超时，加速回收网络抖动产生的僵尸连接，避免"幽灵玩家"占位导致无法重登
		config.setPingInterval(15000);
		config.setPingTimeout(45000);

		instance = this;
		server = new SocketIOServer(config);
		spdNetNamespace = server.addNamespace("/spdnet");
		if (seeds.isEmpty()) {
			seeds.put("seedFUN", getNoonTimestamp());
		}
		seeds.putAll(dailyChallengeService.getDailySeeds());
		server.start();
		startAll();
		sender = new Sender(server);
		handler = new Handler(playerRepository, gameRecordRepository,
		                      playerCatalogRepository, playerBestiaryRepository, playerDocumentRepository,
		                      dailyGameRecordRepository, playerPrefixService, dailyChallengeService,
		                      this, sender, playerMap, chatService);
	}

	@PreDestroy
	public void stopServer() {
		server.stop();
	}

	private void startAll() {
		spdNetNamespace.addConnectListener(client -> {
			HandshakeData handshakeData = client.getHandshakeData();
			LinkedHashMap<String, String> authTokenMap = (LinkedHashMap) handshakeData.getAuthToken();
			String name = null;
			String password = null;
			if (authTokenMap != null) {
				name = authTokenMap.get("name");
				password = authTokenMap.get("password");
			}
			String nameQuery = handshakeData.getSingleUrlParam("name");
			String passwordQuery = handshakeData.getSingleUrlParam("password");
			String spdVersion = handshakeData.getSingleUrlParam("SPDVersion");
			String netVersion = handshakeData.getSingleUrlParam("NetVersion");
			if (name == null) {
				name = nameQuery;
			}
			if (password == null) {
				password = passwordQuery;
			}

			if (name == null || password == null) {
				client.sendEvent(Events.ERROR.getName(), new SError("请提供用户名和密码"));
				log.info("连接失败: 缺少认证信息, " + client.getSessionId());
				client.disconnect();
				return;
			}

			Player player = playerRepository.findByName(name);
			if (player == null) {
				client.sendEvent(Events.ERROR.getName(), new SError("用户名或密码错误"));
				log.info("连接失败: 用户不存在, " + name + ", " + client.getSessionId());
				client.disconnect();
				return;
			}

			if (!passwordEncoder.matches(password, player.getPassword())) {
				client.sendEvent(Events.ERROR.getName(), new SError("用户名或密码错误"));
				log.info("连接失败: 密码错误, " + name + ", " + client.getSessionId());
				client.disconnect();
				return;
			}

			if (player.getRole() == UserRole.BANNED) {
				client.sendEvent(Events.ERROR.getName(), new SError("账号已被封禁"));
				log.info("连接失败: 账号已封禁, " + name + ", " + client.getSessionId());
				client.disconnect();
				return;
			}

			if (!(spdProperties.getVersion().equals(spdVersion) && (spdProperties.getNetVersion().equals(netVersion) || netVersion.equals(spdProperties.getNetVersion() + "-INDEV")))) {
				client.sendEvent(Events.ERROR.getName(), new SError("版本不匹配"));
				log.info("连接失败: 版本不匹配, 破碎版本: " + spdVersion + ", Net版本: " + netVersion + ", " + client.getSessionId());
				client.disconnect();
				return;
			}

			// SPDNet: 用 nameToSessionId 索引判断重复登录，替代 O(N) 遍历 playerMap
			UUID existingSessionId = nameToSessionId.get(player.getName());
			if (existingSessionId != null) {
				SocketIOClient existingClient = spdNetNamespace.getClient(existingSessionId);
				// 仅当旧连接仍存活时判定为真正的重复登录
				if (existingClient != null && existingClient.isChannelOpen()) {
					client.sendEvent(Events.ERROR.getName(), new SError(player.getName() + "已登录, 重复登录"));
					log.info("连接失败: " + player.getName() + "已登录, 重复登录, " + client.getSessionId());
					client.disconnect();
					return;
				}
				// 旧连接已失效（网络抖动产生的幽灵连接），清理占位后放行新连接
				log.info("玩家{}存在失效的旧连接({})，清理后允许重新登录", player.getName(), existingSessionId);
				playerMap.remove(existingSessionId);
				nameToSessionId.remove(player.getName());
			}
			playerMap.put(client.getSessionId(), player);
			nameToSessionId.put(player.getName(), client.getSessionId());
			// SPDNet: 更新最后登录时间和IP
			player.setLastLoginAt(LocalDateTime.now());
			player.setLastLoginIp(getClientIp(client));
			playerRepository.save(player);
			// SPDNet: 确保玩家成就集合不为 null，如果为 null 则初始化并保存到数据库
			if (player.getAchievements() == null) {
				player.setAchievements(new java.util.HashSet<>());
				playerRepository.save(player);
				log.info("玩家{}成就集合初始化", player.getName());
			}
			sender.sendInit(client, new SInit(player.getName(), spdProperties.getMotd(), seeds, player.getAchievements()));
			// SPDNet: 发送 Journal 数据给客户端
			handler.loadAndSendJournals(client, player);
			// SPDNet: 获取玩家当前激活的前缀
			String activePrefixName = playerPrefixService.getActivePrefixName(player.getName());
			player.setPrefixName(activePrefixName);
			sender.sendBroadcastJoin(new SJoin(player.getName(), player.getRole().getDisplayName(), activePrefixName));
			sender.sendPlayerList(client, new SPlayerList(playerMap));
			log.info("玩家已连接: " + player.getName() + ", " + client.getSessionId());
		});
		spdNetNamespace.addDisconnectListener(client -> {
			Player player = playerMap.get(client.getSessionId());
			if (player != null) {
				playerMap.remove(client.getSessionId());
				nameToSessionId.remove(player.getName());
				// SPDNet: 清理移动降频记录，避免无界增长
				handler.removePlayerMoveThrottle(player.getName());
				// SPDNet: 获取玩家当前激活的前缀
				String activePrefixName = playerPrefixService.getActivePrefixName(player.getName());
				sender.sendBroadcastExit(new SExit(player.getName(), activePrefixName));
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
			handler.handleFloatingText(client, playerMap.get(client.getSessionId()), JSON.parseObject(data, CFloatingText.class));
		});
		spdNetNamespace.addEventListener(Actions.GAME_END.getName(), String.class, (client, data, ackSender) -> {
			JSONObject cGameEndJson = JSON.parseObject(data, JSONObject.class);
			CGameEnd gameEnd = new CGameEnd(JSONObject.parseObject(cGameEndJson.getString("record"), GameRecord.class));
			if (cGameEndJson.containsKey("dailyGroupIndex") && cGameEndJson.getInteger("dailyGroupIndex") != null) {
				Integer dailyGroupIndex = cGameEndJson.getInteger("dailyGroupIndex");
				Long dailySeed = cGameEndJson.getLong("dailySeed");
				handler.handleDailyGameEnd(client, playerMap.get(client.getSessionId()), gameEnd, dailyGroupIndex, dailySeed);
			} else {
				handler.handleGameEnd(playerMap.get(client.getSessionId()), gameEnd);
			}
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
			handler.handlePlayerMove(client, playerMap.get(client.getSessionId()), JSON.parseObject(data, CPlayerMove.class));
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
		spdNetNamespace.addEventListener(Actions.REQUEST_DAILY_CHALLENGE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleRequestDailyChallenge(client, playerMap.get(client.getSessionId()), JSON.parseObject(data, CRequestDailyChallenge.class));
		});

		// SPDNet: Journal 相关事件监听（事件名来自共享协议枚举）
		spdNetNamespace.addEventListener(Actions.CATALOG_UPDATE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleCatalogUpdate(playerMap.get(client.getSessionId()), JSON.parseObject(data, CCatalogUpdate.class));
		});
		spdNetNamespace.addEventListener(Actions.BESTIARY_UPDATE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleBestiaryUpdate(playerMap.get(client.getSessionId()), JSON.parseObject(data, CBestiaryUpdate.class));
		});
		spdNetNamespace.addEventListener(Actions.DOCUMENT_UPDATE.getName(), String.class, (client, data, ackSender) -> {
			handler.handleDocumentUpdate(playerMap.get(client.getSessionId()), JSON.parseObject(data, CDocumentUpdate.class));
		});

	}

	@Scheduled(cron = "0 30 0 * * ?")
	public void doSomething() {
		seeds.clear();
		seeds.put("seedFUN", getNoonTimestamp());
		dailyChallengeService.resetDailySeeds();
		seeds.putAll(dailyChallengeService.getDailySeeds());
		sender.sendBroadcastError(new SError("换种子了嗷"));
		spdNetNamespace.getAllClients().forEach(ClientOperations::disconnect);
	}

	private long getNoonTimestamp() {
		LocalDateTime todayNoon = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).withHour(12).withMinute(0).withSecond(0).withNano(0);
		ZonedDateTime zdt = todayNoon.atZone(ZoneId.of("Asia/Shanghai"));
		return zdt.toInstant().toEpochMilli();
	}

	public void kickPlayer(String name) {
		SocketIOClient client = getClientByName(name);
		if (client != null) {
			client.sendEvent(Events.ERROR.getName(), new SError("你已被踢出服务器"));
			client.disconnect();
		}
	}

	/**
	 * SPDNet: 通过玩家名获取在线连接，使用 nameToSessionId 索引，O(1) 查找。
	 * 替代原先遍历整个 playerMap 再 getClient 的做法。
	 */
	public SocketIOClient getClientByName(String name) {
		UUID uuid = nameToSessionId.get(name);
		if (uuid == null) {
			return null;
		}
		return spdNetNamespace.getClient(uuid);
	}

	public void broadcastMessage(String message) {
		sender.sendBroadcastServerMessage(new SServerMessage(message));
	}

	public void broadcastChatMessage(String name, String message) {
		// SPDNet: 使用服务端时间作为系统广播消息的时间
		sender.sendBroadcastChatMessage(new SChatMessage(name, message, ""));
	}

	// SPDNet: 获取客户端IP地址
	private String getClientIp(SocketIOClient client) {
		if (client.getHandshakeData() == null) {
			return "unknown";
		}
		String ip = client.getHandshakeData().getHttpHeaders().get("X-Forwarded-For");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = client.getHandshakeData().getHttpHeaders().get("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = client.getHandshakeData().getHttpHeaders().get("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = client.getRemoteAddress().toString();
			// 移除端口号
			if (ip != null && ip.contains(":")) {
				ip = ip.substring(0, ip.lastIndexOf(":"));
			}
		}
		// 如果有多个IP，取第一个
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}
		return ip;
	}
}
