package com.kii.sample.chat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.text.TextUtils;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.chat.ui.util.Logger;

/**
 * チャットルームを表します。
 * ユーザがチャットを開始すると、チャットのメンバーでKiiGroupが作成されます。
 * そのグループスコープのバケツとしてchat_roomが作成されます。
 * ユーザはこのchat_roomバケツにメッセージを保存します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatRoom {
	
	private static final String BUCKET_NAME = "chat_room";
	public static final String CAHT_GROUP_URI = "chat_group_uri";
	
	public static KiiBucket getBucket(KiiGroup kiiGroup) {
		return kiiGroup.bucket(BUCKET_NAME);
	}
	public static String getChatRoomName(KiiUser user, ChatFriend chatFriend) throws Exception {
		ChatUser me = ChatUser.findByUri(user.toUri());
		List<String> members = new ArrayList<String>();
		members.add(me.getUsername());
		members.add(chatFriend.getUsername());
		Collections.sort(members);
		return TextUtils.join(",", members);
	}
	
	public static String getUniqueKey(KiiGroup kiiGroup) {
		try {
			kiiGroup.refresh();
			List<KiiUser> members = kiiGroup.listMembers();
			return getUniqueKey(members);
		} catch (Exception e) {
			return null;
		}
	}
	public static String getUniqueKey(KiiUser user, ChatFriend friend) {
		List<KiiUser> members = new ArrayList<KiiUser>();
		members.add(user);
		members.add(KiiUser.createByUri(Uri.parse(friend.getUri())));
		return getUniqueKey(members);
	}
	public static String getUniqueKey(List<KiiUser> members) {
		List<String> memberUri = new ArrayList<String>();
		for (KiiUser member : members) {
			memberUri.add(member.toUri().toString());
		}
		Collections.sort(memberUri);
		return TextUtils.join("_", memberUri);
	}
	
	private final KiiBucket kiiBucket;
	
	public ChatRoom(KiiGroup kiiGroup) {
		this.kiiBucket = getBucket(kiiGroup);
	}
	
	/**
	 * チャットルーム内の全てのメッセージを取得します。
	 * 
	 * @return　昇順にソートされたメッセージリスト
	 */
	public List<ChatMessage> getMessageList() {
		return this.queryMessageList(ChatMessage.createQuery());
	}
	/**
	 * 指定した日時以降に作成されたチャットルーム内のメッセージを取得します。
	 * 
	 * @param modifiedSinceTime
	 * @return 昇順にソートされたメッセージリスト
	 */
	public List<ChatMessage> getMessageList(long modifiedSinceTime) {
		return this.queryMessageList(ChatMessage.createQuery(modifiedSinceTime));
	}
	private List<ChatMessage> queryMessageList(KiiQuery query) {
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			List<KiiObject> results = this.kiiBucket.query(query).getResult();
			for (KiiObject o : results) {
				messages.add(new ChatMessage(o));
			}
		} catch (Exception e) {
			Logger.e("Unable to list messages", e);
		}
		return messages;
	}
}
