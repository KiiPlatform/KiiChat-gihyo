package com.kii.sample.chat;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.Kii.Site;
import com.kii.sample.chat.ui.util.Logger;

import android.app.Application;
import android.content.Context;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class KiiChatApplication extends Application {
	
	private static Context context;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		// アプリケーション起動時にKiiSDKを初期化します。
		// TODO:Activity.onCreateで毎回呼ぶようにしたほうがいいかも。
		Logger.i("■■■ initialize KII SDK ■■■");
		Kii.initialize(ApplicationConst.APP_ID, ApplicationConst.APP_KEY, Site.JP);
	}
	public static Context getContext(){
		return context;
	}
	public static String getMessage(int msgId) {
		return context.getResources().getString(msgId);
	}
	public static String getFormattedMessage(int msgId, Object... args) {
		String message = context.getResources().getString(msgId);
		return String.format(message, args);
	}
}
