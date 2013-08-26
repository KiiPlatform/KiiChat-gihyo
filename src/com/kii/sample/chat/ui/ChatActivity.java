package com.kii.sample.chat.ui;

import java.util.List;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatMessage;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.adapter.MessageListAdapter;
import com.kii.sample.chat.ui.util.Logger;
import com.kii.sample.chat.ui.util.ProgressDialogFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * メッセージの送受信を行うチャット画面です。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatActivity extends FragmentActivity {
	
	public static final String INTENT_GROUP_URI = "group_uri";
	
	private Vibrator vibrator;
	private ListView listView;
	private MessageListAdapter adapter;
	private EditText editMessage;
	private ImageButton btnSend;
	private KiiGroup kiiGroup;
	private Long lastGotTime;
	private final BroadcastReceiver handleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateMessage(false);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		this.vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		this.adapter = new MessageListAdapter(this, KiiUser.getCurrentUser());
		this.listView = (ListView)findViewById(R.id.list_view);
		this.listView.setAdapter(this.adapter);
		this.editMessage = (EditText)findViewById(R.id.edit_message);
		this.editMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(editMessage.getText().toString())) {
					btnSend.setEnabled(false);
				} else {
					btnSend.setEnabled(true);
				}
			}
		});
		this.btnSend = (ImageButton)findViewById(R.id.button_send);
		this.btnSend.setEnabled(false);
		this.btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSend.setEnabled(false);
				final ChatMessage message = new ChatMessage(kiiGroup);
				message.setMessage(editMessage.getText().toString());
				message.setSenderUri(KiiUser.getCurrentUser().toUri().toString());
				new SendMessageTask().execute(message);
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(this.handleMessageReceiver, new IntentFilter(ApplicationConst.ACTION_MESSAGE_RECEIVED));
		String uri = getIntent().getStringExtra(INTENT_GROUP_URI);
		this.kiiGroup = KiiGroup.createByUri(Uri.parse(uri));
		updateMessage(true);
	}
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.handleMessageReceiver);
	}
	private void updateMessage(boolean showProgress) {
		new GetMessageTask(showProgress).execute();
	}
	private class SendMessageTask extends AsyncTask<ChatMessage, Void, Void> {
		@Override
		protected Void doInBackground(ChatMessage... params) {
			try {
				params[0].getKiiObject().save();
			} catch (Exception e) {
				Logger.e("failed to send messsage", e);
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void v) {
			editMessage.setText("");
		}
	}
	/**
	 * 
	 */
	private class GetMessageTask extends AsyncTask<Void, Void, List<ChatMessage>> {
		private final boolean showProgress;
		private GetMessageTask(boolean showProgress) {
			this.showProgress = showProgress;
		}
		@Override
		protected void onPreExecute() {
			if (this.showProgress) {
				ProgressDialogFragment.show(getSupportFragmentManager(), "Chat", "Loading...");
			}
		}
		@Override
		protected List<ChatMessage> doInBackground(Void... params) {
			try {
				ChatRoom chatRoom = new ChatRoom(kiiGroup);
				List<ChatMessage> messages = null;
				if (lastGotTime == null) {
					messages = chatRoom.getMessageList();
				} else {
					messages = chatRoom.getMessageList(lastGotTime);
				}
				if (messages.size() > 0) {
					lastGotTime = messages.get(messages.size() - 1).getKiiObject().getCreatedTime();
				}
				return messages;
			} catch (Exception e) {
				return null;
			}
		}
		@Override
		protected void onPostExecute(List<ChatMessage> messages) {
			if (messages != null) {
				adapter.addAll(messages);
				adapter.notifyDataSetChanged();
			} else {
				// TODO:ERROR
			}
			if (this.showProgress) {
				ProgressDialogFragment.hide(getSupportFragmentManager());
			} else {
				vibrator.vibrate(500);
			}
			listView.setSelection(listView.getCount());
		}
	}
}
