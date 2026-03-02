package com.shatteredpixel.shatteredpixeldungeon.spdnet;

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

	public static String getDailySeedKey() {
		if (isDailyChallenge()) {
			return "dailyGroup" + dailyGroupIndex;
		}
		return null;
	}
}
