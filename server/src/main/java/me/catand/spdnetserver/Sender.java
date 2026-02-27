package me.catand.spdnetserver;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.extern.slf4j.Slf4j;
import me.catand.spdnetserver.data.events.*;

@Slf4j
public class Sender {
	private SocketIOServer server;

	public Sender(SocketIOServer server) {
		this.server = server;
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

	public void sendBroadcastFloatingText(SFloatingText data) {
		server.getBroadcastOperations().sendEvent(Events.FLOATING_TEXT.getName(), data);
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

	public void sendBroadcastPlayerMove(SPlayerMove data) {
		server.getBroadcastOperations().sendEvent(Events.PLAYER_MOVE.getName(), data);
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
}
