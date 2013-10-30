package com.kii.sample.chat.ui;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask.OnInitializeListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * サインイン画面のアクティビティです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SigninActivity extends FragmentActivity implements OnInitializeListener{
	
	private TextView textNewAccount;
	private Button btnSignin;
	private CheckBox checkRemember;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		this.textNewAccount = (TextView)findViewById(R.id.text_new_account);
		this.checkRemember = (CheckBox)findViewById(R.id.check_remember);
		this.btnSignin = (Button)findViewById(R.id.button_signin);
		
		this.btnSignin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// サインイン画面を表示する
				SigninDialogFragment signinFragment = SigninDialogFragment.newInstance(SigninActivity.this);
				signinFragment.show(getSupportFragmentManager(), SigninDialogFragment.TAG);
			}
		});
		this.textNewAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// サインアップ画面を表示する
				SignupDialogFragment signupFragment = SignupDialogFragment.newInstance(SigninActivity.this);
				signupFragment.show(getSupportFragmentManager(), "signup");
			}
		});
	}
	@Override
	public void onInitializeCompleted() {
		moveToChatMain();
	}
	private void moveToChatMain() {
		ChatRoom.ensureSubscribedBucket(KiiUser.getCurrentUser());
		Intent intent = new Intent(SigninActivity.this, ChatMainActivity.class);
		startActivity(intent);
	}
}
