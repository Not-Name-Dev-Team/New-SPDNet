package com.shatteredpixel.shatteredpixeldungeon.spdnet;

import java.util.Map;

/**
 * 用来存储某些变量
 * 比如玩家当前选择的模式
 * 玩家当前选择的服务器种子
 */
public class NetInProgress {
	public static Mode mode = Mode.FUN;
	public static String seedName;
	public static long seed;
}
