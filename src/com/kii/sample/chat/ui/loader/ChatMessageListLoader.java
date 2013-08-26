package com.kii.sample.chat.ui.loader;

import java.util.List;

import android.content.Context;

import com.kii.sample.chat.model.ChatMessage;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatMessageListLoader extends AbstractAsyncTaskLoader<List<ChatMessage>> {

	public ChatMessageListLoader(Context context) {
		super(context);
	}
	@Override
	public List<ChatMessage> loadInBackground() {
		return null;
	}

}
