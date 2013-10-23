package com.kii.sample.chat.model;

import java.util.ArrayList;
import java.util.List;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.chat.util.Logger;

/**
 * チャット友達を表します。
 * ユーザスコープのデータとして友達追加時にKiiCloudに保存され、他のユーザが他人の友達リストを参照することはできません。
 * 本アプリケーションではチャットを開始する前にユーザを友達に追加する必要があります。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatFriend extends KiiObjectWrapper implements IUser {
	
	private static final String BUCKET_NAME = "chat_friends";
	private static final String FIELD_NAME = "username";
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_URI = "uri";  // KiiUser.toUri()が返した値
	
	public static KiiBucket getBucket() {
		return KiiUser.getCurrentUser().bucket(BUCKET_NAME);
	}
	/**
	 * ログイン中ユーザのチャット友達を全て取得します。
	 * 
	 * @return
	 */
	public static List<ChatFriend> list() {
		List<ChatFriend> friends = new ArrayList<ChatFriend>();
		try {
			KiiBucket friendsBucket = ChatFriend.getBucket();
			List<KiiObject> results = friendsBucket.query(new KiiQuery()).getResult();
			for (KiiObject friend : results) {
				friends.add(new ChatFriend(friend));
			}
			return friends;
		} catch (Exception e) {
			Logger.e("Unable to list friends", e);
			return friends;
		}
	}
	
	public ChatFriend(KiiObject friend) {
		super(friend);
	}
	public ChatFriend(ChatUser user) {
		super(getBucket().object());
		setUsername(user.getUsername());
		setEmail(user.getEmail());
		setUri(user.getUri());
	}
	@Override
	public String getUsername() {
		return getString(FIELD_NAME);
	}
	public void setUsername(String username) {
		set(FIELD_NAME, username);
	}
	@Override
	public String getEmail() {
		return getString(FIELD_EMAIL);
	}
	public void setEmail(String email) {
		set(FIELD_EMAIL, email);
	}
	@Override
	public String getUri() {
		return getString(FIELD_URI);
	}
	public void setUri(String uri) {
		set(FIELD_URI, uri);
	}
}
