package com.kii.sample.chat.ui;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.ui.util.ProgressDialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

/**
 * �A�v���P�[�V�����N�����ɌĂ΂��A�N�e�B�r�e�B�ł��B
 * �K�v�ɉ����Ď����I�ɃT�C���C���������s���܂��B
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class MainActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// SharedPreferences��AccessToken���ۑ�����Ă��邩�`�F�b�N����
		String token = PreferencesManager.getStoredAccessToken();
		if (!TextUtils.isEmpty(token)) {
			// �ۑ�����Token�Ń��O�C�������s����
			ProgressDialogFragment.show(getSupportFragmentManager(), "Login", "Processing...");
			KiiUser.loginWithToken(new KiiUserCallBack() {
				@Override
				public void onLoginCompleted(int token, KiiUser user, Exception e) {
					if (e == null) {
						// �T�C���C���������̓`���b�g��ʂɑJ��
						Intent intent = new Intent(MainActivity.this, ChatMainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						// �T�C���C�����s���̓T�C���C����ʂɑJ��
						PreferencesManager.setStoredAccessToken("");
						Intent intent = new Intent(MainActivity.this, SigninActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					ProgressDialogFragment.hide(getSupportFragmentManager());
				}
			},token);
		} else {
			// Token���ۑ�����Ă��Ȃ��ꍇ�̓T�C���C����ʂɑJ��
			Intent intent = new Intent(MainActivity.this, SigninActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
}
