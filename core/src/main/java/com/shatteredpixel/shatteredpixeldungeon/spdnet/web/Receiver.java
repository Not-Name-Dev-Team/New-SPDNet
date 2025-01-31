package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import static com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net.getSocket;

import com.alibaba.fastjson.JSON;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene.NetRankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Events;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SAchievement;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SAnkhUsed;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SArmorUpdate;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SChatMessage;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SEnterDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SError;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SExit;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SFloatingText;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SGameEnd;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SGiveItem;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SInit;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SJoin;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SLeaderboard;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SLeaveDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SPlayerChangeFloor;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SPlayerList;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SPlayerMove;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SServerMessage;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.SViewHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.windows.NetWindow;
import com.watabou.noosa.Game;

import java.io.IOException;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * 此类用于接收并解析服务器发送的消息
 */
public class Receiver {
	public static void startAll() {
		Emitter.Listener onConnected = args -> {
		};
		Emitter.Listener onDisconnected = args -> {
			if (ShatteredPixelDungeon.scene() instanceof GameScene) {
				try {
					Dungeon.saveAll();
				} catch (IOException e) {
					ShatteredPixelDungeon.reportException(e);
				}
				Game.switchScene(TitleScene.class);
			} else if (ShatteredPixelDungeon.scene() instanceof NetRankingsScene) {
				Game.switchScene(TitleScene.class);
			}
			cancelAll();
			NetWindow.error("与服务器断开连接");
		};
		Emitter.Listener onConnectionError = args -> {
		};
		Emitter.Listener onAchievement = args -> {
			Handler.handleAchievement(JSON.parseObject(args[0].toString(), SAchievement.class));
		};
		Emitter.Listener onAnkhUsed = args -> {
			Handler.handleAnkhUsed(JSON.parseObject(args[0].toString(), SAnkhUsed.class));
		};
		Emitter.Listener onArmorUpdate = args -> {
			Handler.handleArmorUpdate(JSON.parseObject(args[0].toString(), SArmorUpdate.class));
		};
		Emitter.Listener onChatMessage = args -> {
			Handler.handleChatMessage(JSON.parseObject(args[0].toString(), SChatMessage.class));
		};
		Emitter.Listener onEnterDungeon = args -> {
			Handler.handleEnterDungeon(JSON.parseObject(args[0].toString(), SEnterDungeon.class));
		};
		Emitter.Listener onError = args -> {
			Handler.handleError(JSON.parseObject(args[0].toString(), SError.class));
		};
		Emitter.Listener onExit = args -> {
			Handler.handleExit(JSON.parseObject(args[0].toString(), SExit.class));
		};
		Emitter.Listener onFloatingText = args -> {
			Handler.handleFloatingText(JSON.parseObject(args[0].toString(), SFloatingText.class));
		};
		Emitter.Listener onGameEnd = args -> {
			Handler.handleGameEnd(JSON.parseObject(args[0].toString(), SGameEnd.class));
		};
		Emitter.Listener onGiveItem = args -> {
			Handler.handleGiveItem(JSON.parseObject(args[0].toString(), SGiveItem.class));
		};
		Emitter.Listener onHero = args -> {
			Handler.handleHero(JSON.parseObject(args[0].toString(), SHero.class));
		};
		Emitter.Listener onInit = args -> {
			Handler.handleInit(JSON.parseObject(args[0].toString(), SInit.class));
		};
		Emitter.Listener onJoin = args -> {
			Handler.handleJoin(JSON.parseObject(args[0].toString(), SJoin.class));
		};
		Emitter.Listener onLeaderboard = args -> {
			System.out.println(args[0].toString());
			Handler.handleLeaderboard(JSON.parseObject(args[0].toString(), SLeaderboard.class));
		};
		Emitter.Listener onLeaveDungeon = args -> {
			Handler.handleLeaveDungeon(JSON.parseObject(args[0].toString(), SLeaveDungeon.class));
		};
		Emitter.Listener onPlayerChangeFloor = args -> {
			Handler.handlePlayerChangeFloor(JSON.parseObject(args[0].toString(), SPlayerChangeFloor.class));
		};
		Emitter.Listener onPlayerList = args -> {
			Handler.handlePlayerList(JSON.parseObject(args[0].toString(), SPlayerList.class));
		};
		Emitter.Listener onPlayerMove = args -> {
			Handler.handlePlayerMove(JSON.parseObject(args[0].toString(), SPlayerMove.class));
		};
		Emitter.Listener onServerMessage = args -> {
			Handler.handleServerMessage(JSON.parseObject(args[0].toString(), SServerMessage.class));
		};
		Emitter.Listener onViewHero = args -> {
			Handler.handleViewHero(JSON.parseObject(args[0].toString(), SViewHero.class));
		};
		getSocket().on(Socket.EVENT_CONNECT, onConnected);
		getSocket().on(Socket.EVENT_DISCONNECT, onDisconnected);
		getSocket().on(Socket.EVENT_CONNECT_ERROR, onConnectionError);
		getSocket().on(Events.ACHIEVEMENT.getName(), onAchievement);
		getSocket().on(Events.ANKH_USED.getName(), onAnkhUsed);
		getSocket().on(Events.ARMOR_UPDATE.getName(), onArmorUpdate);
		getSocket().on(Events.CHAT_MESSAGE.getName(), onChatMessage);
		getSocket().on(Events.ENTER_DUNGEON.getName(), onEnterDungeon);
		getSocket().on(Events.ERROR.getName(), onError);
		getSocket().on(Events.EXIT.getName(), onExit);
		getSocket().on(Events.FLOATING_TEXT.getName(), onFloatingText);
		getSocket().on(Events.GAME_END.getName(), onGameEnd);
		getSocket().on(Events.GIVE_ITEM.getName(), onGiveItem);
		getSocket().on(Events.HERO.getName(), onHero);
		getSocket().on(Events.INIT.getName(), onInit);
		getSocket().on(Events.JOIN.getName(), onJoin);
		getSocket().on(Events.LEADERBOARD.getName(), onLeaderboard);
		getSocket().on(Events.LEAVE_DUNGEON.getName(), onLeaveDungeon);
		getSocket().on(Events.PLAYER_CHANGE_FLOOR.getName(), onPlayerChangeFloor);
		getSocket().on(Events.PLAYER_LIST.getName(), onPlayerList);
		getSocket().on(Events.PLAYER_MOVE.getName(), onPlayerMove);
		getSocket().on(Events.SERVER_MESSAGE.getName(), onServerMessage);
		getSocket().on(Events.VIEW_HERO.getName(), onViewHero);
	}

	public static void cancelAll() {
		getSocket().off();
	}
}
