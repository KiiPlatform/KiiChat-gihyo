package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiObject;

public class ChatMessageFactory {
	public static ChatMessage createInstance(KiiObject message) {
		String m = message.getString("message");
		if (m != null && m.startsWith(ChatStamp.PREFIX_STAMP)) {
			return new ChatStamp(message);
		}
		return new ChatMessage(message);
	}
}
