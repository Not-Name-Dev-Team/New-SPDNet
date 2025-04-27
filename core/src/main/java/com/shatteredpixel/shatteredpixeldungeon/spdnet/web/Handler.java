package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.alibaba.fastjson.JSON;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene.NetRankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.NLog;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.SPDUtils;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.sprites.NetHeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Player;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Status;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestPlayerList;
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
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.windows.NetWndPlayerInfo;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此类用于处理服务器发送的消息
 */
public class Handler {
	public static void handleAchievement(SAchievement achievement) {
		Badges.Badge badge = achievement.getBadge();
		if (achievement.isUnique()) {
			NLog.h(achievement.getName() + Messages.get(Badges.class, "new", badge.title() + " (" + badge.desc() + ")"));
		} else {
			NLog.h(achievement.getName() + Messages.get(Badges.class, "endorsed", badge.title()));
		}
	}

	public static void handleAnkhUsed(SAnkhUsed ankhUsed) {
		if (!ankhUsed.getName().equals(Net.name)) {
			Player player = Net.playerList.get(ankhUsed.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			NetHero player1 = NetHero.getPlayerFromDungeon(ankhUsed.getName());
			if (player1 != null) {
				player1.useAnkh(true, ankhUsed.getUnusedBlessedAnkh(), ankhUsed.getUnusedUnblessedAnkh());
			}
			if (ankhUsed.getUnusedBlessedAnkh() + ankhUsed.getUnusedUnblessedAnkh() == 0) {
				NLog.w(ankhUsed.getName() + "因为" + ankhUsed.getCause() + "用掉了他的最后一个十字架");
			}
			NLog.w(ankhUsed.getName() + "因为" + ankhUsed.getCause() + "用掉了他的十字架，" + "剩余十字架: " + (ankhUsed.getUnusedBlessedAnkh() + ankhUsed.getUnusedUnblessedAnkh()));
		}
	}

	public static void handleArmorUpdate(SArmorUpdate armorUpdate) {
		if (!armorUpdate.getName().equals(Net.name)) {
			Player player = Net.playerList.get(armorUpdate.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			Status status = player.getStatus();
			if (status == null) {
				return;
			}
			status.setArmorTier(armorUpdate.getArmorTier());
			player.setStatus(status);
			Net.playerList.put(armorUpdate.getName(), player);
			NetHero player1 = NetHero.getPlayerFromDungeon(armorUpdate.getName());
			if (player1 != null) {
				player1.tier = armorUpdate.getArmorTier();
				((NetHeroSprite) (player1.sprite)).updateArmor();
			}
		}

	}

	public static void handleHero(SHero hero) {
		Bundle bundle = Bundle.fromString(hero.getHero());
		NetHero player = new NetHero(hero.getTargetName());
		player.restoreFromBundleOverride(bundle);
		Game.runOnRenderThread(() -> ShatteredPixelDungeon.scene().add(new NetWndPlayerInfo(hero.getTargetName(), player)));
	}

	public static void handleChatMessage(SChatMessage chatMessage) {
		NLog.chat(chatMessage.getName(), chatMessage.getMessage());
	}

	public static void handleEnterDungeon(SEnterDungeon enterDungeon) {
		if (!enterDungeon.getName().equals(Net.name)) {
			Player player = Net.playerList.get(enterDungeon.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			player.setStatus(enterDungeon.getStatus());
			Net.playerList.put(enterDungeon.getName(), player);
			NetHero.addPlayerToDungeon(player);
			NLog.h(player.getName() + "以" +
					enterDungeon.getStatus().getGameModeEnum().getName().substring(0, 2) + "模式, " +
					SPDUtils.activeChallenges(enterDungeon.getStatus().getChallenges()) + "挑进入了地牢");
		}
	}

	public static void handleError(SError error) {
		NetWindow.error("服务器错误:" + error.getError());
		NLog.n("服务器错误:" + error.getError());
	}

	public static void handleExit(SExit exit) {
		if (!exit.getName().equals(Net.name)) {
			Player player = Net.playerList.get(exit.getName());
			if (player != null) {
				Net.playerList.remove(exit.getName());
				NetHero.removePlayerFromDungeon(exit.getName());
			}
			NLog.h(exit.getName() + " 下线了");
		}
	}

	public static void handleGiveItem(SGiveItem giveItem) {
		Item item = giveItem.getItemObject();
		if (item != null && ShatteredPixelDungeon.scene() instanceof GameScene) {
			if (NetInProgress.mode == Mode.IRONMAN) {
				NLog.h(giveItem.getName() + "想给你 " + item.name() + ", 可惜你是铁人");
				return;
			}
			item.doPickUp(Dungeon.hero);
			NLog.h(giveItem.getName() + "给了你" + item.name());
		}
	}

	public static void handleFloatingText(SFloatingText floatingText) {
		if (!floatingText.getName().equals(Net.name)) {
			NetHero player = NetHero.getPlayerFromDungeon(floatingText.getName());
			if (player != null) {
				// 溅血效果
				if (player.HP > floatingText.getHeroHP()) {
					player.sprite.bloodBurstA(player.sprite.center(), (player.HP - floatingText.getHeroHP()) * 2);
				}
				player.HP = floatingText.getHeroHP();
				player.shield = floatingText.getHeroShield();
				player.HT = floatingText.getHeroHT();
				player.sprite.showStatusWithIcon(floatingText.getColor(), floatingText.getText(), floatingText.getIcon());
			}
		}
	}

	public static void handleGameEnd(SGameEnd gameEnd) {
		GameRecord record = JSON.parseObject(gameEnd.getRecord(), GameRecord.class);
		NLog.w(gameEnd.getName() + "在" + Mode.valueOf(record.getGameMode()).getName() + record.getChallengeAmount() + "挑" + (record.isWin() ? "胜利" : "死亡, 到达了第" + record.getDepth() + "层"));

	}

	public static void handleInit(SInit init) {
		Net.seeds = new ConcurrentHashMap<>(init.getSeeds());
		Net.name = init.getName();
		NetWindow.showMotd(init.getMotd());


		// TODO 等GUI实现之后来这里更改种子逻辑 目前默认使用服务器给与的第一个种子
		Enumeration<String> keysEnumeration = Net.seeds.keys();
		ArrayList<String> keysList = Collections.list(keysEnumeration);
		if (!keysList.isEmpty()) {
			NetInProgress.seedName = keysList.get(0);
			NetInProgress.seed = Net.seeds.get(NetInProgress.seedName);
		}
	}

	public static void handleJoin(SJoin join) {
		if (!join.getName().equals(Net.name)) {
			Net.playerList.put(join.getName(), new Player(join.getQq(), join.getName(), join.getPower(), null));
			NLog.h(join.getName() + " 上线了");
		}
	}

	public static void handleLeaderboard(SLeaderboard leaderboard) {
		ArrayList<GameRecord> records = new ArrayList<>();
		if (ShatteredPixelDungeon.scene() instanceof NetRankingsScene) {
			try {
				List<String> recordsString = leaderboard.getGameRecords();
				for (String record : recordsString) {
					records.add(JSON.parseObject(record, GameRecord.class));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			((NetRankingsScene) ShatteredPixelDungeon.scene()).setRankings(leaderboard.getTotalPages(), leaderboard.getCurrentPage(), leaderboard.getTotalElements(), records);
		}
	}

	public static void handleLeaveDungeon(SLeaveDungeon leaveDungeon) {
		if (!leaveDungeon.getName().equals(Net.name)) {
			Player player = Net.playerList.get(leaveDungeon.getName());
			if (player != null) {
				player.setStatus(null);
				Net.playerList.put(leaveDungeon.getName(), player);
				NetHero.removePlayerFromDungeon(leaveDungeon.getName());
			}
		}
	}

	public static void handlePlayerChangeFloor(SPlayerChangeFloor playerChangeFloor) {
		if (!playerChangeFloor.getName().equals(Net.name)) {
			Player player = Net.playerList.get(playerChangeFloor.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			Status status = player.getStatus();
			if (status == null) {
				return;
			}
			status.setDepth(playerChangeFloor.getDepth());
			player.setStatus(status);
			Net.playerList.put(playerChangeFloor.getName(), player);
			NetHero.addPlayerToDungeon(player);
		}
	}

	public static void handlePlayerList(SPlayerList playerList) {
		Net.playerList.clear();
		for (Player player : playerList.getPlayers()) {
			Net.playerList.put(player.getName(), player);
		}
	}

	public static void handlePlayerMove(SPlayerMove playerMove) {
		if (!playerMove.getName().equals(Net.name)) {
			Player player = Net.playerList.get(playerMove.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			Status status = player.getStatus();
			if (status == null) {
				return;
			}
			status.setPos(playerMove.getPos());
			player.setStatus(status);
			Net.playerList.put(playerMove.getName(), player);
			// 如果这位玩家在当前地牢楼层
			NetHero player1 = NetHero.getPlayerFromDungeon(playerMove.getName());
			if (player1 != null) {
				player1.move(playerMove.getPos(), false);
			}

		}
	}

	public static void handleServerMessage(SServerMessage serverMessage) {
		NetWindow.message(serverMessage.getMessage());
	}

	public static void handleViewHero(SViewHero viewHero) {
		if (Dungeon.hero != null) {
			Bundle heroBundle = new Bundle();
			Dungeon.hero.storeInBundle(heroBundle);
			Sender.sendHero(new CHero(viewHero.getSourceName(), heroBundle.toString()));
		}
		NLog.h("你被" + viewHero.getSourceName() + "查看了");
	}

	/**
	 * 同步玩家列表
	 * 如果出现任何列表不同步的情况, 请调用此方法
	 */
	public static void syncPlayerList() {
		Sender.sendRequestPlayerList(new CRequestPlayerList());
		NetHero.syncWithCurrentLevel();
	}
}
