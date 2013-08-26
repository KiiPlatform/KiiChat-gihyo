package com.kii.sample.chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatMessage;

public class MessageListAdapter extends AbstractArrayAdapter<ChatMessage> {
	
	private static final int ROW_SELF = 0;
	private static final int ROW_FRIEND = 1;
	
	static class ViewHolder {
		int position;
		TextView text;
	}
	
	private final LayoutInflater inflater;
	private final String userUri;
	
	public MessageListAdapter(Context context, KiiUser kiiUser) {
		super(context, R.layout.chat_message_me);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.userUri = kiiUser.toUri().toString();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		ChatMessage chatMessage = this.getItem(position);
		if (convertView == null) {
			if (getRowType(chatMessage) == ROW_SELF) {
				convertView = this.inflater.inflate(R.layout.chat_message_me, parent, false);
			} else {
				convertView = this.inflater.inflate(R.layout.chat_message_friend, parent, false);
			}
			holder = new ViewHolder();
			holder.text = (TextView)convertView.findViewById(R.id.row_message);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		holder.position = position;
		String message = chatMessage.getMessage() == null ? "" : chatMessage.getMessage();
		holder.text.setText(message);
		return convertView;
	}
	@Override
	public int getViewTypeCount() {
		// ListViewに表示する行の種類は「自分のメッセージ」「友達のメッセージ」の２種類あるので2を返す。
		return 2;
	}
	@Override
	public int getItemViewType(int position) {
		// 与えられた位置の行が、「自分のメッセージ」か「友達のメッセージ」かを判定する
		return getRowType(getItem(position));
	}
	private int getRowType(ChatMessage chatMessage) {
		if (TextUtils.equals(this.userUri, chatMessage.getSenderUri())) {
			return ROW_SELF;
		} else {
			return ROW_FRIEND;
		}
	}
}
