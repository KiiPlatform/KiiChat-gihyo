package com.kii.sample.chat;

/**
 * KiiChatアプリケーション共通で使用する定数を管理します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public final class ApplicationConst {
	
	// TODO:APP_IDとAPP_KEYがKiiCloudでアプリケーションを作成した際に発行された値に書き換えてください。
	public static final String APP_ID = "5db741d4";
	public static final String APP_KEY = "7b9a135db251fc849b21237d3ccc3ab9";
	// TODO:SENDER_IDはGCMの設定を有効にした際に発行された値に書き換えてください。
	public static final String SENDER_ID = "1012419078893";

	public static final String TOPIC_INVITE_NOTIFICATION = "InviteNotification";
	public static final String ACTION_CHAT_STARTED = "com.kii.sample.chat.ACTION_CHAT_STARTED";
	public static final String ACTION_MESSAGE_RECEIVED = "com.kii.sample.chat.ACTION_MESSAGE_RECEIVED";
	public static final String EXTRA_MESSAGE = "com.kii.sample.chat.EXTRA_MESSAGE";
	
	private ApplicationConst() {
	}
}
