package com.kii.sample.chat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * SharedPreferencesを扱うためのヘルパークラスです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class PreferencesManager {
	interface Key {
		static final String ACCESS_TOKEN = "token";
		static final String SENDER_ID = "sender_id";
	}
	
	public static void setStoredAccessToken(String token) {
		setString(Key.ACCESS_TOKEN, token);
	}
	
	public static String getStoredAccessToken() {
		return getSharedPreferences().getString(Key.ACCESS_TOKEN, null);
	}
	private static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(KiiChatApplication.getContext());
	}
	private static void setString(String key, String value) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}
//	private static void setInt(String key, int value) {
//		Editor editor = getSharedPreferences().edit();
//		editor.putInt(key, value);
//		editor.commit();
//	}
//	private static void setLong(String key, long value) {
//		Editor editor = getSharedPreferences().edit();
//		editor.putLong(key, value);
//		editor.commit();
//	}
//	private static void setBoolean(String key, boolean value) {
//		Editor editor = getSharedPreferences().edit();
//		editor.putBoolean(key, value);
//		editor.commit();
//	}
//	private static void setFloat(String key, float value) {
//		Editor editor = getSharedPreferences().edit();
//		editor.putFloat(key, value);
//		editor.commit();
//	}
//	private static void setObject(String key, Object value) {
//		setString(key, value.toString());
//	}
}
