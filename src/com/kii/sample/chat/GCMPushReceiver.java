package com.kii.sample.chat;

import java.util.List;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushMessageBundleHelper.MessageType;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.ReceivedMessage;
import com.kii.cloud.storage.exception.GroupOperationException;
import com.kii.sample.chat.model.ChatMessage;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.ChatActivity;
import com.kii.sample.chat.util.Logger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Gravity;
import android.widget.Toast;

/**
 * GCMからのプッシュ通知を受信するレシーバーです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class GCMPushReceiver extends BroadcastReceiver {
	
	private NotificationManager notificationManager;
	public GCMPushReceiver() {
		this.notificationManager = (NotificationManager)KiiChatApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		final Handler handler = new Handler(context.getMainLooper());
		Logger.i("received push message");
		if (KiiUser.getCurrentUser() == null) {
			// ログイン中でない場合は何もしない
			return;
		}
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			final Bundle extras = intent.getExtras();
			final ReceivedMessage message = PushMessageBundleHelper.parse(extras);
			MessageType type = message.pushMessageType();
			switch (type) {
				case DIRECT_PUSH:
					Logger.i("received DIRECT_PUSH");
					return;
				case PUSH_TO_APP:
					Logger.i("received PUSH_TO_APP");
					try {
						// 参加中のChatに新規メッセージが投稿された場合
						Logger.i("received PUSH_TO_USER");
						new Thread(new Runnable() {
							@Override
							public void run() {
								
								
								try {
									final KiiUser sender = ((PushToAppMessage)message).getSender();
									final KiiObject obj = ((PushToAppMessage)message).getKiiObject();
									obj.refresh();
									final ChatMessage chatMessage = new ChatMessage(obj);
									final KiiGroup kiiGroup = KiiGroup.createByUri(Uri.parse(chatMessage.getGroupUri()));
									// 自分がメンバーでないChatRoomは無視する
									if (isMember(kiiGroup)) {
										if (isForeground(context)) {
											sendBroadcast(context, ApplicationConst.ACTION_MESSAGE_RECEIVED, chatMessage.getKiiObject().toJSON().toString());
										} else if (!sender.equals(KiiUser.getCurrentUser())) {
											// KiiChatがバックグラウンドにいる場合は通知バーにメッセージを表示する。
											showNotificationArea(chatMessage, kiiGroup);
											// さらにメッセージをToastで表示する。
											handler.post(new Runnable() {
												@Override
												public void run() {
													Toast toast = Toast.makeText(context, chatMessage.getMessage(), Toast.LENGTH_LONG);
													toast.setGravity(Gravity.CENTER, 0, 0);
													toast.show();
												}
											});
										}
									}
								} catch (Exception e) {
									Logger.e("Unable to subscribe group bucket", e);
								}
							}
						}).start();
					} catch (Exception e) {
						Logger.e("Unable to get the KiiObject", e);
					}
					break;
				case PUSH_TO_USER:
					Logger.i("received PUSH_TO_USER");
					// 他のユーザが自分とChatを開始した場合
					// 対象のChat用バケットを購読してメッセージをプッシュ通知してもらう状態にする
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String groupUri =  extras.getString(ChatRoom.CAHT_GROUP_URI);
								KiiGroup kiiGroup = KiiGroup.createByUri(Uri.parse(groupUri));
								// 自分がメンバーでないChatRoomは無視する
								if (isMember(kiiGroup)) {
									KiiBucket chatBucket = ChatRoom.getBucket(kiiGroup);
									KiiUser.getCurrentUser().pushSubscription().subscribeBucket(chatBucket);
									sendBroadcast(context, ApplicationConst.ACTION_CHAT_STARTED, groupUri);
								}
							} catch (Exception e) {
								Logger.e("Unable to subscribe group bucket", e);
							}
						}
					}).start();
					break;
			}
		}
	}
	/**
	 * ログイン中のユーザが指定されたグループに所属しているか判定します。
	 * 
	 * @param kiiGroup
	 * @return
	 * @throws GroupOperationException
	 */
	private boolean isMember(KiiGroup kiiGroup) throws GroupOperationException {
		if (KiiUser.getCurrentUser() != null) {
			kiiGroup.refresh();
			List<KiiUser> members = kiiGroup.listMembers();
			for (KiiUser member : members) {
				if (KiiUser.getCurrentUser().toUri().equals(member.toUri())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * プッシュ通知を受信したことをBroadcast Intentを使ってActivityに通知します。
	 * 
	 * @param context
	 * @param action
	 * @param message
	 */
	private void sendBroadcast(Context context, String action, String message) {
		Intent intent = new Intent(action);
		intent.putExtra(ApplicationConst.EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
	
	
	/**
	* Shows a received message in the notification area.
	*
	* @param chatMessage
	* @param kiiGroup
	*/
	private void showNotificationArea(ChatMessage chatMessage, KiiGroup kiiGroup) {
		if (this.notificationManager != null) {
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(KiiChatApplication.getContext())
			.setSmallIcon(R.drawable.launcher)
			.setContentTitle("New message received")
			.setContentText(chatMessage.getMessage());
			Intent resultIntent = new Intent(KiiChatApplication.getContext(), ChatActivity.class);
			resultIntent.putExtra(ChatActivity.INTENT_GROUP_URI, kiiGroup.toUri().toString());
		
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(KiiChatApplication.getContext());
			stackBuilder.addParentStack(ChatActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
			notificationBuilder.setContentIntent(resultPendingIntent);
			this.notificationManager.notify(0, notificationBuilder.build());
		}
	}
	/**
	* Checks if the KiiChat is on foreground.
	*
	* @param context
	* @return
	*/
	private boolean isForeground(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> processInfoList = am.getRunningAppProcesses();
		for(RunningAppProcessInfo info : processInfoList){
			if(info.processName.equals(context.getPackageName()) && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
				return true;
			}
		}
		return false;
	}

}
