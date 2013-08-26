package com.kii.sample.chat.ui;

import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.R;
import com.kii.sample.chat.ui.SignupDialogFragment.OnSignupListener;
import com.kii.sample.chat.ui.util.Logger;
import com.kii.sample.chat.ui.util.ProgressDialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * サインイン画面のアクティビティです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SigninActivity extends FragmentActivity implements OnSignupListener{
	
	private TextView textNewAccount;
	private EditText editEmail;
	private EditText editPassword;
	private Button btnSignin;
	private CheckBox checkRemember;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		this.textNewAccount = (TextView)findViewById(R.id.text_new_account);
		this.editEmail = (EditText)findViewById(R.id.edit_email);
		this.editPassword = (EditText)findViewById(R.id.edit_password);
		// android:hintで指定した文字列のフォントを制御する為にxmlでtextPasswordの指定をしないでコードから設定する
		this.editPassword.setTransformationMethod(new PasswordTransformationMethod());
		this.checkRemember = (CheckBox)findViewById(R.id.check_remember);
		this.btnSignin = (Button)findViewById(R.id.button_signin);
		
		this.btnSignin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = editEmail.getText().toString();
				String password = editPassword.getText().toString();
				if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
					Toast.makeText(SigninActivity.this, "Please input email address and password", Toast.LENGTH_SHORT).show();
					return;
				}
				ProgressDialogFragment.show(getSupportFragmentManager(), "Login", "Processing...");
				KiiUser.logIn(new KiiUserCallBack() {
					@Override
					public void onLoginCompleted(int token, KiiUser user, Exception e) {
						ProgressDialogFragment.hide(getSupportFragmentManager());
						if (e != null) {
							// サインイン失敗時はToastを表示してサインイン画面に留まる
							Logger.e("Unable to login.", e);
							Toast.makeText(SigninActivity.this, "Unable to login", Toast.LENGTH_SHORT).show();
							return;
						}
						if (checkRemember.isChecked()) {
							// ログイン状態を保持する場合は、SharedPreferencesにAccessTokenを保存する
							Logger.i(user.getAccessToken());
							PreferencesManager.setStoredAccessToken(user.getAccessToken());
						}
						moveToChatMain();
					}
				}, email, password);
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
	public void onSignupCompleted() {
		moveToChatMain();
	}
	private void moveToChatMain() {
		Intent intent = new Intent(SigninActivity.this, ChatMainActivity.class);
		startActivity(intent);
	}
}
