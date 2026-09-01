package com.shatteredpixel.shatteredpixeldungeon.spdnet;

import com.shatteredpixel.shatteredpixeldungeon.spdnet.web.Net;

/**
 * 用来存储某些变量
 * 比如玩家当前选择的模式
 * 玩家当前选择的服务器种子
 */
public class NetInProgress {
	public static Mode mode = Mode.FUN;
	public static String seedName = "";
	public static long seed;
	public static int dailyGroupIndex = -1;
	public static String dailyRecordDate = null;
	public static int dailyChallenges = 0;

	public static boolean isDailyChallenge() {
		return mode == Mode.DAILY && dailyGroupIndex >= 0 && dailyGroupIndex <= 2;
	}

	public static void resetDailyChallenge() {
		dailyGroupIndex = -1;
		dailyRecordDate = null;
		dailyChallenges = 0;
	}

	/**
	 * 切换到指定模式，同时清空每日挑战残留状态，并将种子复位为默认的娱乐种子。
	 * 用于模式选择、连接初始化以及一局游戏结束后的状态清理，
	 * 避免每日挑战的种子/组别残留到下一局导致种子类别错乱。
	 */
	public static void switchToMode(Mode newMode) {
		mode = newMode;
		resetDailyChallenge();
		seedName = "seedFUN";
		seed = Net.seeds.getOrDefault("seedFUN", 0L);
	}

	/**
	 * 一局游戏结束后恢复默认状态，确保下一局以娱乐模式+娱乐种子开局。
	 */
	public static void resetForNextGame() {
		switchToMode(Mode.FUN);
	}

	public static String getDailySeedKey() {
		if (isDailyChallenge()) {
			return "dailyGroup" + dailyGroupIndex;
		}
		return null;
	}
}
