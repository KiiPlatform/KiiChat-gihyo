package com.kii.sample.chat.ui.loader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.util.Logger;

/**
 * {@link ChatUser}をchat_usersバケットから取得するローダーです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class UserListLoader extends AbstractAsyncTaskLoader<List<ChatUser>> {
	
	private String keyword;
	
	public UserListLoader(Context context, String keyword) {
		super(context);
		this.keyword = keyword;
	}
	@Override
	public List<ChatUser> loadInBackground() {
		List<ChatUser> users = new ArrayList<ChatUser>();
		try {
			List<ChatUser> results = ChatUser.searchByKeyword(keyword);
			for (ChatUser user : results) {
				// サインイン中のユーザは検索結果から除外する
				if (!TextUtils.equals(user.getUri(), KiiUser.getCurrentUser().toUri().toString())) {
					users.add(user);
				}
			}
		} catch (Exception e) {
			Logger.e("Unable to list users", e);
		}
		return users;
	}
}
