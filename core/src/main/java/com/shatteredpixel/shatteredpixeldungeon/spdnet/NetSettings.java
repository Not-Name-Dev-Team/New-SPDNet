package com.shatteredpixel.shatteredpixeldungeon.spdnet;

import static com.watabou.utils.GameSettings.getString;
import static com.watabou.utils.GameSettings.put;

/**
 * 用来存储某些长期保存的变量
 */
public class NetSettings {
	public static final String KEY_AUTH_NAME = "net_auth_name";
	public static final String KEY_AUTH_PASSWORD = "net_auth_password";

	public static void setName(String value) {
		put(KEY_AUTH_NAME, value);
	}

	public static String getName() {
		return getString(KEY_AUTH_NAME, "");
	}

	public static void setPassword(String value) {
		put(KEY_AUTH_PASSWORD, value);
	}

	public static String getPassword() {
		return getString(KEY_AUTH_PASSWORD, "");
	}

	public static boolean hasCredentials() {
		return !getName().isEmpty() && !getPassword().isEmpty();
	}

	public static void clearCredentials() {
		put(KEY_AUTH_NAME, "");
		put(KEY_AUTH_PASSWORD, "");
	}
}
