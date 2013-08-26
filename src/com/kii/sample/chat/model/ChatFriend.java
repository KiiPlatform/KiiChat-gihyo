package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;

/**
 * Chat友達を表します。
 * ユーザスコープのデータとしてKiiCloudに保存され、他のユーザが他人の友達リストを参照することはできません。
 * このデータは友達追加時に作成されます。
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
