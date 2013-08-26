package com.kii.sample.chat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushMessageBundleHelper.MessageType;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.ReceivedMessage;
import com.kii.sample.chat.model.ChatMessage;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.util.Logger;
import com.kii.sample.chat.ui.util.ToastUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * GCM����̃v�b�V���ʒm����M���郌�V�[�o�[�ł��B
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class GCMPushReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		Logger.i("received push message");
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			final Bundle extras = intent.getExtras();
			ReceivedMessage message = PushMessageBundleHelper.parse(extras);
			MessageType type = message.pushMessageType();
			switch (type) {
				case DIRECT_PUSH:
					Logger.i("received DIRECT_PUSH");
					return;
				case PUSH_TO_APP:
					Logger.i("received PUSH_TO_APP");
					ToastUtils.showShort(context, "received message");
					try {
						// �Q������Chat�ɐV�K���b�Z�[�W�����e���ꂽ�ꍇ
						Logger.i("received PUSH_TO_USER");
//						KiiUser sender = message.getSender();
//						if (TextUtils.equals(KiiUser.getCurrentUser().toUri().toString(),sender.toUri().toString())) {
//							// �����N���̃v�b�V���ʒm�͖�������
//							return;
//						}
						ChatMessage chatMessage = new ChatMessage(((PushToAppMessage)message).getKiiObject());
						this.sendBroadcast(context, ApplicationConst.ACTION_MESSAGE_RECEIVED, chatMessage.getKiiObject().toJSON().toString());
					} catch (Exception e) {
						Logger.e("Unable to get the KiiObject", e);
					}
					break;
				case PUSH_TO_USER:
					Logger.i("received PUSH_TO_USER");
					ToastUtils.showShort(context, "received new chat");
					// ���̃��[�U��������Chat���J�n�����ꍇ
					// �Ώۂ�Chat�p�o�P�c���w�ǂ��ă��b�Z�[�W���v�b�V���ʒm���Ă��炤��Ԃɂ���
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String groupUri =  extras.getString(ChatRoom.CAHT_GROUP_URI);
								KiiGroup kiiGroup = KiiGroup.createByUri(Uri.parse(groupUri));
								kiiGroup.refresh();
								KiiBucket chatBucket = ChatRoom.getBucket(kiiGroup);
								KiiUser.getCurrentUser().pushSubscription().subscribeBucket(chatBucket);
								sendBroadcast(context, ApplicationConst.ACTION_CHAT_STARTED, groupUri);
							} catch (Exception e) {
								Logger.e("Unable to subscribe group bucket", e);
							}
						}
					}).start();
					break;
			}
		}
	}
	private void sendBroadcast(Context context, String action, String message) {
		Intent intent = new Intent(action);
		intent.putExtra(ApplicationConst.EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}
