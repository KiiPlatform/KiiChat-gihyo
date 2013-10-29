package com.kii.sample.chat.ui.task;

import android.os.AsyncTask;

import com.kii.cloud.storage.KiiACL;
import com.kii.cloud.storage.KiiACL.TopicAction;
import com.kii.cloud.storage.KiiACLEntry;
import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
import com.kii.cloud.storage.KiiPushSubscription;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.util.GCMUtils;
import com.kii.sample.chat.util.Logger;

/**
 * バックグラウンドでチャットユーザーの初期化処理を実行します。
 */
public class ChatUserInitializeTask extends AsyncTask<Void, Void, Boolean> {
	
	public interface OnInitializeListener {
		public void onInitializeCompleted();
	}
	
	private final String username;
	private final String email;
	
	public ChatUserInitializeTask(String username, String email) {
		this.username = username;
		this.email = email;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			// ユーザーサインアップ後の処理は以下の5つの処理からなるが、途中で失敗した場合、リトライやロールバックの処理が必要
			// 1.AppScope Bucketへのチャットユーザー登録
			// 2.Pushのinstall
			// 3.User Topicの作成
			// 4.User TopicへのACLの設定
			// 5.Topicの購読
			KiiUser kiiUser = KiiUser.getCurrentUser();
			ChatUser user = new ChatUser(kiiUser.toUri().toString(), username, email);
			user.getKiiObject().save();
			// GCMの設定
			String registrationId = GCMUtils.register();
			KiiUser.pushInstallation().install(registrationId);
			// サーバからプッシュ通知を受信する為に、自分専用のトピックを作成する
			// このトピックは他の全てのユーザに書き込み権限を与え
			// 他のユーザが自分をチャットメンバー追加したことを通知する為に使用する
			KiiTopic topic = KiiUser.topic(ApplicationConst.TOPIC_INVITE_NOTIFICATION);
			topic.save();
			KiiACL acl = topic.acl();
			acl.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(), TopicAction.SEND_MESSAGE_TO_TOPIC, true));
			acl.save();
			KiiPushSubscription subscription = kiiUser.pushSubscription();
			subscription.subscribe(topic);
			// 初期化が完了していることを示すフラグをユーザーのカスタムフィールドに追加
			kiiUser.set("initialized", true);
			kiiUser.update();
			return true;
		} catch (Exception e) {
			Logger.e("Failed to initialize", e);
			return false;
		}
	}
}