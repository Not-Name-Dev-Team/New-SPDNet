package me.catand.spdnetserver;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnet.protocol.Events;
import me.catand.spdnetserver.data.Status;
import me.catand.spdnetserver.data.events.*;
import me.catand.spdnetserver.entitys.Player;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class Sender {
	private SocketIOServer server;
	// SPDNet: /spdnet 命名空间。netty-socketio 的 server.getAllClients() 只返回根命名空间("/")的客户端
	// （真实客户端都连 /spdnet，传入的是空的），导致所有基于 getAllClients() 的同层 DELTA 广播全落空；
	// 直发(client.sendEvent)与全局通告(getBroadcastOperations)不受影响。因此这里必须遍历命名空间客户端。
	private SocketIONamespace spdNetNamespace;

	public Sender(SocketIOServer server, SocketIONamespace spdNetNamespace) {
		this.server = server;
		this.spdNetNamespace = spdNetNamespace;
	}

	/**
	 * SPDNet: 向与源玩家同一个地牢（seed 相同，可选同楼层 depth）的在线客户端广播。
	 * 泛化自 broadcastInSameDungeonExcept，新增 excludeSender 开关与 seed+depth 双重匹配。
	 *
	 * 丢信息防护：移动/浮动文字（matchDepth=false）沿用"跳过明确不同 seed、状态未知照发"语义，
	 * 由客户端（NetHero.addPlayerToDungeon）二次过滤渲染。
	 *
	 * 留言（matchDepth=true）语义不同：发起者必在地牢内，逐接收者必须"同 seed && 同 depth"；
	 * 接收者状态未知(null)视为无法比较，一律跳过（与移动的未知照发相反）。
	 * 边界：sourceStatus==null 且 matchDepth=true 时静默拒绝（不广播）——留言若被广播到所有地牢将产生串扰。
	 *
	 * @param eventName     事件名
	 * @param data          事件数据
	 * @param sourceClient  发起者连接（excludeSender=true 时排除）
	 * @param sourceStatus  发起者状态，null 且 matchDepth=true 时静默拒绝；null 且 matchDepth=false 时退化为"除发送者广播给所有人"
	 * @param playerMap     在线玩家表（sessionId -> Player）
	 * @param matchDepth    是否要求接收者与发起者同 seed 且同 depth
	 * @param excludeSender 是否排除发起者连接
	 */
	public void broadcastToDungeon(String eventName, Object data, SocketIOClient sourceClient,
	                               Status sourceStatus, Map<UUID, Player> playerMap,
	                               boolean matchDepth, boolean excludeSender) {
		// 留言(需同层)但发起者不在任何地牢 → 静默拒绝，绝不广播到所有地牢
		if (matchDepth && sourceStatus == null) {
			return;
		}
		for (SocketIOClient c : spdNetNamespace.getAllClients()) {
			// 排除发起者，避免回环（客户端本就忽略自己发的消息）
			if (excludeSender && sourceClient != null && c.getSessionId().equals(sourceClient.getSessionId())) {
				continue;
			}
			// 仅移动/浮动文字分支会走到这里：发起者无状态退化为全量广播（排除发起者）
			if (sourceStatus == null) {
				c.sendEvent(eventName, data);
				continue;
			}
			Player p = playerMap == null ? null : playerMap.get(c.getSessionId());
			Status s = p == null ? null : p.getStatus();
			if (matchDepth) {
				// 留言：接收者必须在同 seed 同 depth，未知状态跳过
				if (s == null || s.getSeed() != sourceStatus.getSeed() || s.getDepth() != sourceStatus.getDepth()) {
					continue;
				}
			} else {
				// 移动/浮动文字：只跳过"明确不同地牢"的玩家；状态未知(null)照发，交给客户端过滤
				if (s != null && s.getSeed() != sourceStatus.getSeed()) {
					continue;
				}
			}
			c.sendEvent(eventName, data);
		}
	}

	/**
	 * SPDNet: 向指定 (seed, depth) 层内所有玩家广播（不做发送者排除，天然含创建者回显）。
	 * 用于地牢留言 DELTA 广播——尤其 PLAYER 快照回填时源连接状态已不可靠，故直接按目标层目标匹配。
	 */
	public void broadcastToLayer(String eventName, Object data, long seed, int depth, Map<UUID, Player> playerMap) {
		for (SocketIOClient c : spdNetNamespace.getAllClients()) {
			Player p = playerMap == null ? null : playerMap.get(c.getSessionId());
			Status s = p == null ? null : p.getStatus();
			if (s == null || s.getSeed() != seed || s.getDepth() != depth) {
				continue;
			}
			c.sendEvent(eventName, data);
		}
	}

	public void sendBroadcastAchievement(SAchievement data) {
		server.getBroadcastOperations().sendEvent(Events.ACHIEVEMENT.getName(), data);
	}

	public void sendBroadcastAnkhUsed(SAnkhUsed data) {
		server.getBroadcastOperations().sendEvent(Events.ANKH_USED.getName(), data);
	}

	public void sendBroadcastArmorUpdate(SArmorUpdate data) {
		server.getBroadcastOperations().sendEvent(Events.ARMOR_UPDATE.getName(), data);
	}

	public void sendBroadcastChatMessage(SChatMessage sChatMessage) {
		server.getBroadcastOperations().sendEvent(Events.CHAT_MESSAGE.getName(), sChatMessage);
	}

	public void sendBroadcastEnterDungeon(SEnterDungeon data) {
		server.getBroadcastOperations().sendEvent(Events.ENTER_DUNGEON.getName(), data);
	}

	public void sendBroadcastError(SError data) {
		server.getBroadcastOperations().sendEvent(Events.ERROR.getName(), data);
	}

	public void sendBroadcastExit(SExit data) {
		server.getBroadcastOperations().sendEvent(Events.EXIT.getName(), data);
	}

	public void sendBroadcastGiveItem(SGiveItem data) {
		server.getBroadcastOperations().sendEvent(Events.GIVE_ITEM.getName(), data);
	}

	public void sendBroadcastFloatingText(SocketIOClient sourceClient, Status sourceStatus, Map<UUID, Player> playerMap, SFloatingText data) {
		// SPDNet: 浮动文字只发给同地牢的其他玩家，排除发起者（不需同层）
		broadcastToDungeon(Events.FLOATING_TEXT.getName(), data, sourceClient, sourceStatus, playerMap, false, true);
	}

	public void sendBroadcastGameEnd(SGameEnd data) {
		server.getBroadcastOperations().sendEvent(Events.GAME_END.getName(), data);
	}

	public void sendBroadcastJoin(SJoin data) {
		server.getBroadcastOperations().sendEvent(Events.JOIN.getName(), data);
	}

	public void sendBroadcastPlayerChangeFloor(SPlayerChangeFloor data) {
		server.getBroadcastOperations().sendEvent(Events.PLAYER_CHANGE_FLOOR.getName(), data);
	}

	public void sendBroadcastLeaveDungeon(SLeaveDungeon data) {
		server.getBroadcastOperations().sendEvent(Events.LEAVE_DUNGEON.getName(), data);
	}

	public void sendBroadcastPlayerMove(SocketIOClient sourceClient, Status sourceStatus, Map<UUID, Player> playerMap, SPlayerMove data) {
		// SPDNet: 移动只发给同地牢的其他玩家，排除发起者（不需同层）
		broadcastToDungeon(Events.PLAYER_MOVE.getName(), data, sourceClient, sourceStatus, playerMap, false, true);
	}

	// SPDNet: 地牢留言(Ping)系统 - 向单个客户端下发留言列表（进/换层单播 REPLACE）
	public void sendNoteList(SocketIOClient client, SNoteList data) {
		client.sendEvent(Events.NOTE_LIST.getName(), data);
	}

	public void sendBroadcastServerMessage(SServerMessage data) {
		server.getBroadcastOperations().sendEvent(Events.SERVER_MESSAGE.getName(), data);
	}

	// SPDNet: 地牢留言(Ping)系统 - 留言创建成功后的聊天通报（NOTE_NOTIFY）。
	// 复用 SServerMessage 承载提示文本，但客户端按聊天通报渲染(NLog.h)，不走弹窗。
	public void sendBroadcastNoteNotify(SServerMessage data) {
		server.getBroadcastOperations().sendEvent(Events.NOTE_NOTIFY.getName(), data);
	}

	public void sendInit(SocketIOClient client, SInit data) {
		client.sendEvent(Events.INIT.getName(), data);
	}

	public void sendPlayerList(SocketIOClient client, SPlayerList data) {
		client.sendEvent(Events.PLAYER_LIST.getName(), data);
	}

	public void sendHero(SocketIOClient client, SHero data) {
		client.sendEvent(Events.HERO.getName(), data);
	}

	public void sendViewHero(SocketIOClient client, SViewHero data) {
		client.sendEvent(Events.VIEW_HERO.getName(), data);
	}

	public void sendGiveItem(SocketIOClient client, SGiveItem data) {
		client.sendEvent(Events.GIVE_ITEM.getName(), data);
	}

	public void sendLeaderboard(SocketIOClient client, SLeaderboard data) {
		client.sendEvent(Events.LEADERBOARD.getName(), data);
	}

	public void sendAllowDailyChallenge(SocketIOClient client, SAllowDailyChallenge data) {
		client.sendEvent(Events.ALLOW_DAILY_CHALLENGE.getName(), data);
	}

	public void sendRejectDailyChallenge(SocketIOClient client, SRejectDailyChallenge data) {
		client.sendEvent(Events.REJECT_DAILY_CHALLENGE.getName(), data);
	}

	public void sendError(SocketIOClient client, SError data) {
		client.sendEvent(Events.ERROR.getName(), data);
	}

	// SPDNet: 发送 Journal 数据给客户端
	public void sendJournals(SocketIOClient client, SJournals data) {
		client.sendEvent(Events.JOURNALS.getName(), data);
	}
}
