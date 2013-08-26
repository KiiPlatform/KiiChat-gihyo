package com.kii.sample.chat.ui.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.ui.util.Logger;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class FriendListLoader extends AbstractAsyncTaskLoader<List<ChatFriend>> {
	
	public FriendListLoader(Context context) {
		super(context);
	}
	@Override
	public List<ChatFriend> loadInBackground() {
		List<ChatFriend> friends = new ArrayList<ChatFriend>();
		try {
			KiiBucket friendsBucket = ChatFriend.getBucket();
			List<KiiObject> results = friendsBucket.query(new KiiQuery()).getResult();
			for (KiiObject friend : results) {
				friends.add(new ChatFriend(friend));
			}
			return friends;
		} catch (Exception e) {
			Logger.e("Unable to list users", e);
			return friends;
		}
	}
}
