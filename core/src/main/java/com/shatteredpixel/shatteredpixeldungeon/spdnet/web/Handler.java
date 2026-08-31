package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import com.alibaba.fastjson.JSON;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.spdnetbutcopy.scene.NetRankingsScene;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.NLog;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.PrefixUtils;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.utils.SPDUtils;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.actors.NetHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.sprites.NetHeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Player;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Status;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestPlayerList;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.events.*;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.ui.scene.DailyChallengeDetailWindow;
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
		String displayName = PrefixUtils.formatNameWithPrefix(achievement.getName(), achievement.getPrefix());
		if (achievement.isUnique()) {
			NLog.h(displayName + Messages.get(Badges.class, "new", badge.title() + " (" + badge.desc() + ")"));
		} else {
			NLog.h(displayName + Messages.get(Badges.class, "endorsed", badge.title()));
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
			String displayName = PrefixUtils.formatNameWithPrefix(ankhUsed.getName(), ankhUsed.getPrefix());
			if (ankhUsed.getUnusedBlessedAnkh() + ankhUsed.getUnusedUnblessedAnkh() == 0) {
				NLog.w(displayName + "因为" + ankhUsed.getCause() + "用掉了他的最后一个十字架");
			}
			NLog.w(displayName + "因为" + ankhUsed.getCause() + "用掉了他的十字架，" + "剩余十字架: " + (ankhUsed.getUnusedBlessedAnkh() + ankhUsed.getUnusedUnblessedAnkh()));
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
		// SPDNet: 源端 hero 数据无效/为空(如对方在主菜单且无英雄)时，直接跳过，视为正常情况而非崩溃
		if (bundle == null) {
			NLog.w("查看 " + hero.getTargetName() + " 的英雄数据无效或被截断");
			return;
		}
		// SPDNet: 将反序列化与窗口创建统一放到渲染线程执行。
		// 还原过程会临时替换全局 Dungeon.hero(见 NetHero.withGlobalHero)，若在 socket 线程
		// 进行会与游戏线程产生数据竞争；放到渲染线程即可安全地与游戏循环串行。
		// 注意：此处刻意不捕获异常，让还原失败以未捕获异常的形式从渲染线程向上抛出，
		// 以便 Android 端 Firebase/Crashlytics 能收到致命崩溃报告(桌面端则由 DesktopLauncher
		// 的全局未捕获处理器兜底弹窗)。快捷键状态清理见 NetHero.restoreFromBundleOverride 内的 try/finally。
		Game.runOnRenderThread(() -> {
			NetHero player = new NetHero(hero.getTargetName());
			player.restoreFromBundleOverride(bundle);
			ShatteredPixelDungeon.scene().add(new NetWndPlayerInfo(hero.getTargetName(), player));
		});
	}

	public static void handleChatMessage(SChatMessage chatMessage) {
		// SPDNet: 使用服务端传来的时间显示聊天消息，并显示前缀
		String displayName = PrefixUtils.formatNameWithPrefix(chatMessage.getName(), chatMessage.getPrefix());
		NLog.chat(displayName, chatMessage.getMessage(), chatMessage.getTime());
	}

	public static void handleEnterDungeon(SEnterDungeon enterDungeon) {
		if (!enterDungeon.getName().equals(Net.name)) {
			Player player = Net.playerList.get(enterDungeon.getName());
			if (player == null) {
				syncPlayerList();
				return;
			}
			player.setStatus(enterDungeon.getStatus());
			player.setPrefix(enterDungeon.getPrefix());
			Net.playerList.put(enterDungeon.getName(), player);
			NetHero.addPlayerToDungeon(player);
			String displayName = PrefixUtils.formatNameWithPrefix(enterDungeon.getName(), enterDungeon.getPrefix());
			NLog.h(displayName + "以" +
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
			String displayName = PrefixUtils.formatNameWithPrefix(exit.getName(), exit.getPrefix());
			NLog.h(displayName + " 下线了");
		}
	}

	public static void handleGiveItem(SGiveItem giveItem) {
		if (NetInProgress.isDailyChallenge()) {
			String displayName = PrefixUtils.formatNameWithPrefix(giveItem.getName(), giveItem.getPrefix());
			NLog.h(displayName + "想给你物品，但每日挑战模式下无法接收物品");
			return;
		}
		Item item = giveItem.getItemObject();
		if (item != null && ShatteredPixelDungeon.scene() instanceof GameScene) {
			if (NetInProgress.mode == Mode.IRONMAN) {
				String displayName = PrefixUtils.formatNameWithPrefix(giveItem.getName(), giveItem.getPrefix());
				NLog.h(displayName + "想给你 " + item.name() + ", 可惜你是铁人");
				return;
			}
			item.doPickUp(Dungeon.hero);
			String displayName = PrefixUtils.formatNameWithPrefix(giveItem.getName(), giveItem.getPrefix());
			NLog.h(displayName + "给了你" + item.name());
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
		String displayName = PrefixUtils.formatNameWithPrefix(gameEnd.getName(), gameEnd.getPrefix());
		NLog.w(displayName + "在" + Mode.valueOf(record.getGameMode()).getName() + record.getChallengeAmount() + "挑" + (record.isWin() ? "胜利" : "死亡, 到达了第" + record.getDepth() + "层"));

	}

	public static void handleInit(SInit init) {
		Net.seeds = new ConcurrentHashMap<>(init.getSeeds());
		Net.name = init.getName();
		NetWindow.showMotd(init.getMotd());

		// SPDNet: 从服务器加载云端成就
		Badges.loadFromCloud(init.getAchievements());

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
			Player player = new Player(join.getName(), join.getRole(), null);
			player.setPrefix(join.getPrefix());
			Net.playerList.put(join.getName(), player);
			String displayName = PrefixUtils.formatNameWithPrefix(join.getName(), join.getPrefix());
			NLog.h(displayName + " 上线了");
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

	// SPDNet: 地牢留言(Ping)系统 - 留言创建成功后的聊天通报。与"xxx进入地牢"一致，
	// 渲染进聊天窗口(NLog.h 高亮通报)，而非服务端弹窗。
	public static void handleNoteNotify(SServerMessage noteNotify) {
		NLog.h(noteNotify.getMessage());
	}

	public static void handleJournals(SJournals journals) {
		// SPDNet: 从服务器加载 Journal 数据
		Journal.loadFromCloud(journals.getCatalogs(), journals.getBestiaries(), journals.getDocuments());
	}

	public static void handleAllowDailyChallenge(SAllowDailyChallenge allowDailyChallenge) {
		Game.runOnRenderThread(() -> {
			Game.scene().add(new DailyChallengeDetailWindow(
				allowDailyChallenge.getGroupIndex(),
				allowDailyChallenge.getSeed(),
				allowDailyChallenge.getRecordDate(),
				allowDailyChallenge.isHasExistingRecord(),
				allowDailyChallenge.getChallenges()
			));
		});
	}

	public static void handleRejectDailyChallenge(SRejectDailyChallenge rejectDailyChallenge) {
		NetInProgress.resetDailyChallenge();
		String reason = rejectDailyChallenge.getReason();
		NetWindow.error("每日挑战: " + reason);
		NLog.n("每日挑战被拒绝: " + reason);
	}

	public static void handleViewHero(SViewHero viewHero) {
		if (Dungeon.hero != null) {
			Bundle heroBundle = new Bundle();
			Dungeon.hero.storeInBundle(heroBundle);
			// SPDNet: 地牢留言(Ping)系统 - forNote 模式下回 CHero 时带 forNote=true，服务端拦截落库，
			// 不回传触发查看窗；且本端不打"你被查看"提示（留言取快照是静默的）。
			CHero ch = new CHero(viewHero.getSourceName(), heroBundle.toString());
			ch.setForNote(viewHero.isForNote());
			Sender.sendHero(ch);
		}
		// SPDNet: forNote 模式静默，不提示"你被查看"
		if (!viewHero.isForNote()) {
			String displayName = PrefixUtils.formatNameWithPrefix(viewHero.getSourceName(), viewHero.getPrefix());
			NLog.h("你被" + displayName + "查看了");
		}
	}

	/**
	 * SPDNet: 地牢留言(Ping)系统 - 处理进/换层单播 REPLACE 与同层 DELTA 增量。
	 * 仅在 seed+depth 与当前层一致时才更新缓存与 Overlay；运行在 socket 线程，
	 * 缓存更新与 setData 统一回渲染线程执行（与 hero 查看同款处理）。
	 */
	public static void handleNoteList(SNoteList noteList) {
		// 铁人模式同种子的留言本就互不可见，无需额外过滤；直接按 seed+depth 过滤
		if (noteList.getSeed() != Dungeon.seed || noteList.getDepth() != Dungeon.depth) {
			return;
		}
		String mode = noteList.getMode();
		List<String> notes = noteList.getNotes();
		List<Integer> likedIds = noteList.getMyLikedIds();
		Game.runOnRenderThread(() -> {
			if ("REPLACE".equals(mode)) {
				NetNoteStore.replace(notes, likedIds);
			} else if ("DELTA_ADD".equals(mode)) {
				if (notes != null && !notes.isEmpty()) {
					NetNoteStore.upsert(notes.get(0));
				}
			} else if ("DELTA_REMOVE".equals(mode)) {
				if (notes != null && !notes.isEmpty()) {
					int id = JSON.parseObject(notes.get(0)).getIntValue("id");
					NetNoteStore.remove(id);
				}
			}
			// overlay 可能尚未创建（极端时序），做空指针容忍
			if (GameScene.noteOverlay != null) {
				GameScene.noteOverlay.setData();
			}
		});
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
