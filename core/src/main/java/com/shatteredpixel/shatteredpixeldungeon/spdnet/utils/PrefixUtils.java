package com.shatteredpixel.shatteredpixeldungeon.spdnet.utils;

/**
 * SPDNet: 前缀系统工具类
 * 用于处理玩家前缀的显示
 */
public class PrefixUtils {

	/**
	 * 格式化带前缀的玩家名称
	 * 管理员直接在displayText中包含符号包裹，这里不再自动添加
	 */
	public static String formatNameWithPrefix(String name, String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return name;
		}
		// 直接拼接前缀和名称，前缀本身已包含符号包裹（如【VIP】）
		return prefix + name;
	}

	/**
	 * 格式化带前缀的玩家名称（简化版，用于日志等）
	 */
	public static String formatSimple(String name, String prefix) {
		if (prefix == null || prefix.isEmpty()) {
			return name;
		}
		return prefix + name;
	}
}
