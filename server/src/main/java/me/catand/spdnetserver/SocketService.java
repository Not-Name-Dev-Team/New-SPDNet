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
import me.catand.spdnetserver.service.NoteService;
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
@Autowired
	private NoteService noteService;
	private SocketIOServer server;
	private Map<UUID, Player> playerMap = new ConcurrentHashMap<>();
	// SPDNet: 玩家名 -> sessionId 索引，避免反复遍历 playerMap 找人（O(N) → O(1)）
	private Map<String, UUID> nameToSessionId = new ConcurrentHashMap<>();
	// SPDNet: 会话表写操作的临界区。playerMap 与 nameToSessionId 是同一份会话状态的两个投影，
	// 必须在同一临界区内成对更新；否则会出现两类矛盾状态：
	//   1) 按 sessionId 取不到人 → 事件派发 NPE（且跳过该事件的副作用）
	//   2) 按名字指向已失效连接 / 索引被旧连接的回调误删 → 重复登录误判、幽灵在线
	// 仅在连接建立/断开这类低频路径上持锁，事件派发是只读查表，不持锁。
	private final Object sessionLock = new Object();
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
		sender = new Sender(server, spdNetNamespace);
		handler = new Handler(playerRepository, gameRecordRepository,
		                      playerCatalogRepository, playerBestiaryRepository, playerDocumentRepository,
		                      dailyGameRecordRepository, playerPrefixService, dailyChallengeService,
		                      this, sender, playerMap, chatService, noteService);
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

			// SPDNet: 会话登记临界区。重复登录判定与"清理幽灵后放行"必须在同一临界区内完成，
			// 否则两个并发连接可能都通过存活检查、互相覆盖索引（后写者留下前写者的孤儿会话）。
			UUID ghostSessionId = null;
			Player ghost = null;
			synchronized (sessionLock) {
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
					// SPDNet 症状22：先取出旧 Player 对象再移除，随后在锁外按正常断线语义补发 EXIT，
					// 否则其它客户端/本地玩家列表/地牢层会残留旧 status 与旧 NetHero 精灵。
					ghost = playerMap.remove(existingSessionId);
					nameToSessionId.remove(player.getName());
					ghostSessionId = existingSessionId;
				}
				playerMap.put(client.getSessionId(), player);
				nameToSessionId.put(player.getName(), client.getSessionId());
			}
			if (ghostSessionId != null) {
				// 锁外执行副作用：清空待补快照草稿并广播退出（含清理该玩家的移动降频记录）
				handler.handleDisconnect(ghost);
				String ghostPrefix = playerPrefixService.getActivePrefixName(player.getName());
				sender.sendBroadcastExit(new SExit(player.getName(), ghostPrefix));
			}
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
		spdNetNamespace.addDisconnectListener(client -> unregisterSession(client.getSessionId()));
		// SPDNet: 需要玩家上下文的事件统一走 onPlayerEvent——会话已注销时静默丢弃，
		// 避免 playerMap.get() 返回 null 后在各 handler 内解引用抛 NPE
		// （NPE 会中断该事件的副作用：清 status / 广播 EXIT / 清待补快照草稿）。
		onPlayerEvent(Actions.ACHIEVEMENT, (client, player, data) ->
				handler.handleAchievement(player, JSON.parseObject(data, CAchievement.class)));
		onPlayerEvent(Actions.ANKH_USED, (client, player, data) ->
				handler.handleAnkhUsed(player, JSON.parseObject(data, CAnkhUsed.class)));
		onPlayerEvent(Actions.ARMOR_UPDATE, (client, player, data) ->
				handler.handleArmorUpdate(player, JSON.parseObject(data, CArmorUpdate.class)));
		onPlayerEvent(Actions.CHAT_MESSAGE, (client, player, data) ->
				handler.handleChatMessage(player, JSON.parseObject(data, CChatMessage.class)));
		onPlayerEvent(Actions.ENTER_DUNGEON, (client, player, data) ->
				handler.handleEnterDungeon(client, player, JSON.parseObject(data, CEnterDungeon.class)));
		onPlayerEvent(Actions.ERROR, (client, player, data) ->
				handler.handleError(player, JSON.parseObject(data, CError.class)));
		onPlayerEvent(Actions.FLOATING_TEXT, (client, player, data) ->
				handler.handleFloatingText(client, player, JSON.parseObject(data, CFloatingText.class)));
		onPlayerEvent(Actions.GAME_END, (client, player, data) -> {
			JSONObject cGameEndJson = JSON.parseObject(data, JSONObject.class);
			CGameEnd gameEnd = new CGameEnd(JSONObject.parseObject(cGameEndJson.getString("record"), GameRecord.class));
			if (cGameEndJson.containsKey("dailyGroupIndex") && cGameEndJson.getInteger("dailyGroupIndex") != null) {
				Integer dailyGroupIndex = cGameEndJson.getInteger("dailyGroupIndex");
				Long dailySeed = cGameEndJson.getLong("dailySeed");
				handler.handleDailyGameEnd(client, player, gameEnd, dailyGroupIndex, dailySeed);
			} else {
				handler.handleGameEnd(player, gameEnd);
			}
		});
		onPlayerEvent(Actions.GIVE_ITEM, (client, player, data) ->
				handler.handleGiveItem(player, JSON.parseObject(data, CGiveItem.class)));
		onPlayerEvent(Actions.HERO, (client, player, data) ->
				handler.handleHero(player, JSON.parseObject(data, CHero.class)));
		onPlayerEvent(Actions.LEAVE_DUNGEON, (client, player, data) ->
				handler.handleLeaveDungeon(player, JSON.parseObject(data, CLeaveDungeon.class)));
		onPlayerEvent(Actions.PLAYER_CHANGE_FLOOR, (client, player, data) ->
				handler.handlePlayerChangeFloor(client, player, JSON.parseObject(data, CPlayerChangeFloor.class)));
		onPlayerEvent(Actions.PLAYER_MOVE, (client, player, data) ->
				handler.handlePlayerMove(client, player, JSON.parseObject(data, CPlayerMove.class)));
		onPlayerEvent(Actions.VIEW_HERO, (client, player, data) ->
				handler.handleViewHero(player, JSON.parseObject(data, CViewHero.class)));
		// SPDNet: 地牢留言(Ping)系统 - 留言/点赞/删除路由
		onPlayerEvent(Actions.NOTE_CREATE, (client, player, data) ->
				handler.handleNoteCreate(client, player, JSON.parseObject(data, CNoteCreate.class)));
		onPlayerEvent(Actions.NOTE_LIKE, (client, player, data) ->
				handler.handleNoteLike(client, player, JSON.parseObject(data, CNoteId.class)));
		onPlayerEvent(Actions.NOTE_DELETE, (client, player, data) ->
				handler.handleNoteDelete(client, player, JSON.parseObject(data, CNoteId.class)));
		onPlayerEvent(Actions.REQUEST_DAILY_CHALLENGE, (client, player, data) ->
				handler.handleRequestDailyChallenge(client, player, JSON.parseObject(data, CRequestDailyChallenge.class)));
		// SPDNet: Journal 相关事件监听（事件名来自共享协议枚举）
		onPlayerEvent(Actions.CATALOG_UPDATE, (client, player, data) ->
				handler.handleCatalogUpdate(player, JSON.parseObject(data, CCatalogUpdate.class)));
		onPlayerEvent(Actions.BESTIARY_UPDATE, (client, player, data) ->
				handler.handleBestiaryUpdate(player, JSON.parseObject(data, CBestiaryUpdate.class)));
		onPlayerEvent(Actions.DOCUMENT_UPDATE, (client, player, data) ->
				handler.handleDocumentUpdate(player, JSON.parseObject(data, CDocumentUpdate.class)));

		// SPDNet: 不依赖玩家上下文的事件——handler 内部已自行处理无会话情形
		spdNetNamespace.addEventListener(Actions.REQUEST_LEADERBOARD.getName(), String.class, (client, data, ackSender) ->
				handler.handleRequestLeaderboard(client, JSON.parseObject(data, CRequestLeaderboard.class)));
		spdNetNamespace.addEventListener(Actions.REQUEST_PLAYER_LIST.getName(), String.class, (client, data, ackSender) ->
				handler.handleRequestPlayerList(client, JSON.parseObject(data, CRequestPlayerList.class)));

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

	/**
	 * SPDNet: 需要玩家上下文的事件处理器。
	 */
	@FunctionalInterface
	private interface PlayerEventHandler {
		void handle(SocketIOClient client, Player player, String data);
	}

	/**
	 * SPDNet: 注册"需要玩家上下文"的事件监听器。会话不存在时记录并丢弃该事件，
	 * 不把 null 传给 handler——这是 NPE 的唯一防线（原先 26 处 playerMap.get() 全部裸传）。
	 * 丢弃是安全的：会话已注销说明玩家已下线，其状态无需再同步。
	 */
	private void onPlayerEvent(Actions action, PlayerEventHandler body) {
		spdNetNamespace.addEventListener(action.getName(), String.class, (client, data, ackSender) -> {
			Player player = playerMap.get(client.getSessionId());
			if (player == null) {
				log.debug("丢弃来自已注销会话的事件: action={}, sessionId={}", action.getName(), client.getSessionId());
				return;
			}
			body.handle(client, player, data);
		});
	}

	/**
	 * SPDNet: 注销会话并对外广播退出（幂等）。返回 true 表示本次调用真正注销了该会话。
	 * playerMap 与 nameToSessionId 在同一临界区内成对更新，并采用条件移除：
	 * 仅当名字索引仍指向本会话时才删除，避免旧连接的回调误删新会话的索引。
	 */
	private boolean unregisterSession(UUID sessionId) {
		Player player;
		boolean nameIndexStillMine;
		synchronized (sessionLock) {
			player = playerMap.remove(sessionId);
			if (player == null) {
				return false;
			}
			nameIndexStillMine = nameToSessionId.remove(player.getName(), sessionId);
		}
		// 锁外执行副作用：清理该玩家的待补快照草稿（含占位行）
		handler.handleDisconnect(player);
		if (nameIndexStillMine) {
			String activePrefixName = playerPrefixService.getActivePrefixName(player.getName());
			sender.sendBroadcastExit(new SExit(player.getName(), activePrefixName));
			log.info("玩家已断开连接: " + player.getName() + ", " + sessionId);
		} else {
			log.info("玩家{}的旧会话已断开（已被新会话接管，跳过退出广播）: {}", player.getName(), sessionId);
		}
		return true;
	}

	/**
	 * SPDNet: 回收半开（幽灵）连接。网络抖动可能让客户端通道实际已死、但 disconnect 回调迟迟不触发，
	 * 导致该玩家长期占位：他人列表残留其旧 status、地牢层残留 NetHero 精灵，且本人无法重登。
	 * 这里按心跳周期巡检 playerMap，对已无存活通道的会话按正常断线语义清理并广播退出。
	 */
	@Scheduled(fixedDelay = 30000)
	public void reapDeadSessions() {
		if (spdNetNamespace == null) {
			return;
		}
		for (UUID sessionId : playerMap.keySet()) {
			SocketIOClient client = spdNetNamespace.getClient(sessionId);
			if (client == null || !client.isChannelOpen()) {
				if (unregisterSession(sessionId)) {
					log.info("回收失效会话（半开连接）: sessionId={}", sessionId);
				}
			}
		}
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
