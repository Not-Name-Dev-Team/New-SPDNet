package me.catand.spdnetserver;

import com.corundumstudio.socketio.SocketIOClient;
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

	public Sender(SocketIOServer server) {
		this.server = server;
	}

	/**
	 * SPDNet: 向与源玩家同一个地牢（seed 相同）的在线客户端广播，并排除源连接本身。
	 * 用于移动、浮动文字这类高频个性化事件，避免无用回环和跨地牢打扰。
	 *
	 * 丢信息防护：客户端（NetHero.addPlayerToDungeon）会按"同地牢+同楼层"二次过滤渲染，
	 * 因此这里只跳过"明确处于不同地牢（已知 seed 不同）"的接收者；
	 * 对状态未知（status 为 null）的接收者照发，交给客户端自行过滤，
	 * 避免因服务端状态尚未同步（如刚进地牢、换层后）导致漏发。
	 *
	 * @param eventName   事件名
	 * @param data        事件数据
	 * @param sourceClient 发起者连接（排除它）
	 * @param sourceStatus 发起者状态，若为空则退化为"广播给所有人（排除发起者）"
	 * @param playerMap   在线玩家表（sessionId -> Player），用于判断接收者所在的地牢
	 */
	private void broadcastInSameDungeonExcept(String eventName, Object data, SocketIOClient sourceClient,
	                                          Status sourceStatus, Map<UUID, Player> playerMap) {
		for (SocketIOClient c : server.getAllClients()) {
			// 排除发起者，避免回环（客户端本就忽略自己发的消息）
			if (sourceClient != null && c.getSessionId().equals(sourceClient.getSessionId())) {
				continue;
			}
			// 发起者不在任何地牢时退化为全量广播（排除发起者）
			if (sourceStatus == null) {
				c.sendEvent(eventName, data);
				continue;
			}
			Player p = playerMap == null ? null : playerMap.get(c.getSessionId());
			Status s = p == null ? null : p.getStatus();
			// 只跳过"明确处于不同地牢"的玩家；状态未知(null)时仍发送，避免因状态未同步丢信息
			if (s != null && s.getSeed() != sourceStatus.getSeed()) {
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
		// SPDNet: 浮动文字只发给同地牢的其他玩家，排除发起者
		broadcastInSameDungeonExcept(Events.FLOATING_TEXT.getName(), data, sourceClient, sourceStatus, playerMap);
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
		// SPDNet: 移动只发给同地牢的其他玩家，排除发起者
		broadcastInSameDungeonExcept(Events.PLAYER_MOVE.getName(), data, sourceClient, sourceStatus, playerMap);
	}

	public void sendBroadcastServerMessage(SServerMessage data) {
		server.getBroadcastOperations().sendEvent(Events.SERVER_MESSAGE.getName(), data);
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
