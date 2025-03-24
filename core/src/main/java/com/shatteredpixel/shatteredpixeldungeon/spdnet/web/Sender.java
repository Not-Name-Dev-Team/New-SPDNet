package com.shatteredpixel.shatteredpixeldungeon.spdnet.web;

import static com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net.getSocket;

import com.alibaba.fastjson.JSON;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.Mode;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.NetInProgress;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.Actions;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CAchievement;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CAnkhUsed;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CArmorUpdate;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CChatMessage;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CEnterDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CError;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CFloatingText;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CGameEnd;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CGiveItem;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CHero;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CLeaveDungeon;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CPlayerChangeFloor;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CPlayerMove;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestLeaderboard;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CRequestPlayerList;
import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.structure.actions.CViewHero;

/**
 * 此类用于发送消息给服务器
 */
public class Sender {
	public static void sendAchievement(CAchievement achievement) {
		getSocket().emit(Actions.ACHIEVEMENT.getName(), JSON.toJSONString(achievement));
	}

	public static void sendAnkhUsed(CAnkhUsed ankhUsed) {
		getSocket().emit(Actions.ANKH_USED.getName(), JSON.toJSONString(ankhUsed));
	}

	public static void sendArmorUpdate(CArmorUpdate armorUpdate) {
		if (NetInProgress.mode == null || NetInProgress.mode == Mode.IRONMAN) {
			return;
		}
		getSocket().emit(Actions.ARMOR_UPDATE.getName(), JSON.toJSONString(armorUpdate));
	}

	public static void sendChatMessage(CChatMessage message) {
		getSocket().emit(Actions.CHAT_MESSAGE.getName(), JSON.toJSONString(message));
	}

	public static void sendEnterDungeon(CEnterDungeon enterDungeon) {
		getSocket().emit(Actions.ENTER_DUNGEON.getName(), JSON.toJSONString(enterDungeon));
	}

	public static void sendError(CError message) {
		getSocket().emit(Actions.ERROR.getName(), JSON.toJSONString(message));
	}

	public static void sendFloatingText(CFloatingText floatingText) {
		if (NetInProgress.mode == null || NetInProgress.mode == Mode.IRONMAN) {
			return;
		}
		getSocket().emit(Actions.FLOATING_TEXT.getName(), JSON.toJSONString(floatingText));
	}

	public static void sendGameEnd(CGameEnd gameEnd) {
		getSocket().emit(Actions.GAME_END.getName(), JSON.toJSONString(gameEnd));
	}

	public static void sendGiveItem(CGiveItem giveItem) {
		getSocket().emit(Actions.GIVE_ITEM.getName(), JSON.toJSONString(giveItem));
	}

	public static void sendHero(CHero hero) {
		getSocket().emit(Actions.HERO.getName(), JSON.toJSONString(hero));
	}

	public static void sendLeaveDungeon(CLeaveDungeon leaveDungeon) {
		getSocket().emit(Actions.LEAVE_DUNGEON.getName(), "{}");
	}

	public static void sendPlayerChangeFloor(CPlayerChangeFloor playerChangeFloor) {
		if (NetInProgress.mode == null || NetInProgress.mode == Mode.IRONMAN) {
			return;
		}
		getSocket().emit(Actions.PLAYER_CHANGE_FLOOR.getName(), JSON.toJSONString(playerChangeFloor));
	}

	public static void sendPlayerMove(CPlayerMove playerMove) {
		if (NetInProgress.mode == null || NetInProgress.mode == Mode.IRONMAN) {
			return;
		}
		getSocket().emit(Actions.PLAYER_MOVE.getName(), JSON.toJSONString(playerMove));
	}

	public static void sendRequestLeaderboard(CRequestLeaderboard requestLeaderboard) {
		getSocket().emit(Actions.REQUEST_LEADERBOARD.getName(), JSON.toJSONString(requestLeaderboard));
	}

	public static void sendRequestPlayerList(CRequestPlayerList requestPlayerList) {
		getSocket().emit(Actions.REQUEST_PLAYER_LIST.getName(), "{}");
	}

	public static void sendViewHero(CViewHero viewHero) {
		getSocket().emit(Actions.VIEW_HERO.getName(), JSON.toJSONString(viewHero));
	}
}
