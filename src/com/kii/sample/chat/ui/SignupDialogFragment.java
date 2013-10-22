package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;

import com.kii.cloud.storage.KiiACL;
import com.kii.cloud.storage.KiiACL.TopicAction;
import com.kii.cloud.storage.KiiACLEntry;
import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
import com.kii.cloud.storage.KiiPushSubscription;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.GCMUtils;
import com.kii.sample.chat.util.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * サインアップ画面のフラグメントです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SignupDialogFragment extends DialogFragment implements OnClickListener {
	
	public interface OnSignupListener {
		public void onSignupCompleted();
	}
	
	public static SignupDialogFragment newInstance(OnSignupListener onSignupListener) {
		SignupDialogFragment dialog = new SignupDialogFragment();
		dialog.setOnSignupListener(onSignupListener);
		return dialog;
	}
	
	private WeakReference<OnSignupListener> onSignupListener;
	private EditText editName;
	private EditText editEmail;
	private EditText editPassword;
	
	private void setOnSignupListener(OnSignupListener onSignupListener) {
		this.onSignupListener = new WeakReference<OnSignupListener>(onSignupListener);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {  
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_signup, null, false);
		
		this.editName = (EditText)view.findViewById(R.id.edit_name);
		this.editEmail = (EditText)view.findViewById(R.id.edit_email);
		this.editPassword = (EditText)view.findViewById(R.id.edit_password);
		// android:hintで指定した文字列のフォントを制御する為にxmlでtextPasswordの指定をしないでコードから設定する
		this.editPassword.setTransformationMethod(new PasswordTransformationMethod());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Create new account");
		builder.setPositiveButton(R.string.button_signup, null);
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.setView(view);
		AlertDialog dialog = builder.show();
		Button buttonOK = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		buttonOK.setOnClickListener(this);
		return dialog;
	}
	
	@Override
	public void onClick(View v) {
		final String username = editName.getText().toString();
		final String email = editEmail.getText().toString();
		final String password = editPassword.getText().toString();
		if (TextUtils.isEmpty(username)) {
			ToastUtils.showShort(getActivity(), "Please input username");
			return;
		}
		if (TextUtils.isEmpty(email)) {
			ToastUtils.showShort(getActivity(), "Please input email");
			return;
		}
		if (TextUtils.isEmpty(password)) {
			ToastUtils.showShort(getActivity(), "Please input password");
			return;
		}
		new SignupTask(username, email, password).execute();
	}
	/**
	 * バックグラウンドでSignupの処理を実行します。
	 */
	private class SignupTask extends AsyncTask<Void, Void, Boolean> {
		
		private final String username;
		private final String email;
		private final String password;
		
		private SignupTask(String username, String email, String password) {
			this.username = username;
			this.email = email;
			this.password = password;
		}
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getFragmentManager(), "Signup", "Processing...");
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// TODO:サインアップ処理は以下の5つの処理からなるが、途中で失敗した場合、リトライやロールバックの処理が必要
				// 1.KiiUerのregister処理
				// 2.Pushのinstall
				// 3.User Topicの作成
				// 4.User TopicへのACLの設定
				// 5.Topicの購読
				KiiUser.Builder builder = KiiUser.builderWithEmail(email);
				KiiUser kiiUser = builder.build();
				kiiUser.setDisplayname(username);
				kiiUser.register(password);
				Logger.i("registered user uri=" + kiiUser.toUri().toString());
				// 登録したKiiUserをChatUserとしてAppスコープのバケツに保存しておく（検索用）
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
				return true;
			} catch (Exception e) {
				Logger.e("failed to sign up", e);
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			SimpleProgressDialogFragment.hide(getFragmentManager());
			if (result) {
				// サインアップ処理が正常に行われた場合は、コールバックメソッドで呼び出し元に通知する
				OnSignupListener listener = onSignupListener.get();
				if (listener != null) {
					listener.onSignupCompleted();
				}
			} else {
				ToastUtils.showShort(getActivity(), "Unable to sign up");
			}
			dismiss();
		}
	}
}
