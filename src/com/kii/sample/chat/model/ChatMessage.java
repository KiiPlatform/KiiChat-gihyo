package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;

/**
 * チャットのメッセージを表します。
 * グループスコープのデータとしてKiiCloudに保存され、チャットに参加しているメンバーのみが参照することができます。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatMessage extends KiiObjectWrapper {
	
	private static final String FIELD_MESSAGE = "message";
	private static final String FIELD_SENDER_URI = "sender_uri";
	
	public ChatMessage(KiiGroup kiiGroup) {
		super(ChatRoom.getBucket(kiiGroup).object());
	}
	public ChatMessage(KiiObject message) {
		super(message);
	}
	public static KiiQuery createQuery() {
		return createQuery(null);
	}
	public static KiiQuery createQuery(Long modifiedSinceTime) {
		KiiQuery query = null;
		if (modifiedSinceTime != null) {
			query = new KiiQuery(
				KiiClause.and(
					KiiClause.notEquals(FIELD_MESSAGE, ""),
					KiiClause.greaterThan(FIELD_CREATED, modifiedSinceTime)
				)
			);
		} else {
			query = new KiiQuery(KiiClause.notEquals(FIELD_MESSAGE, ""));
		}
		query.sortByAsc(FIELD_CREATED);
		return query;
	}
	
	
	
	
	
	public String getMessage() {
		return getString(FIELD_MESSAGE);
	}
	public void setMessage(String message) {
		set(FIELD_MESSAGE, message);
	}
	public String getSenderUri() {
		return getString(FIELD_SENDER_URI);
	}
	public void setSenderUri(String uri) {
		set(FIELD_SENDER_URI, uri);
	}
}