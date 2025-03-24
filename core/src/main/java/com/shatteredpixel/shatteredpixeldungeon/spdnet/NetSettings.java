package com.shatteredpixel.shatteredpixeldungeon.spdnet;

import static com.watabou.utils.GameSettings.getString;
import static com.watabou.utils.GameSettings.put;

/**
 * 用来存储某些长期保存的变量
 */
public class NetSettings {
	public static String DEFAULT_KEY = "debug";
	public static final String KEY_AUTH_KEY = "net_auth_key";

	public static void setKey(String value) {
		put(KEY_AUTH_KEY, value);
	}

	public static String getKey() {
		return getString(KEY_AUTH_KEY, DEFAULT_KEY);
	}
}
