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
import com.kii.sample.chat.ui.util.GCMUtils;
import com.kii.sample.chat.ui.util.Logger;
import com.kii.sample.chat.ui.util.ProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * �T�C���A�b�v��ʂ̃t���O�����g�ł��B
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
		// android:hint�Ŏw�肵��������̃t�H���g�𐧌䂷��ׂ�xml��textPassword�̎w������Ȃ��ŃR�[�h����ݒ肷��
		this.editPassword.setTransformationMethod(new PasswordTransformationMethod());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Create new account");
		builder.setPositiveButton(R.string.button_signup, this);
		builder.setNegativeButton(R.string.button_cancel, this);
		builder.setView(view);
		return builder.show();
	}
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// �u�T�C���A�b�v�v�{�^���������ꂽ�ꍇ�̏���
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
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				dismiss();
				break;
		}
	}
	/**
	 * �o�b�N�O���E���h��Signup�̏��������s���܂��B
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
			ProgressDialogFragment.show(getFragmentManager(), "Signup", "Processing...");
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				// TODO:�T�C���A�b�v�����͈ȉ���5�̏�������Ȃ邪�A�r���Ŏ��s�����ꍇ�A���g���C�⃍�[���o�b�N�̏������K�v
				// 1.KiiUer��register����
				// 2.Push��install
				// 3.User Topic�̍쐬
				// 4.User Topic�ւ�ACL�̐ݒ�
				// 5.Topic�̍w��
				KiiUser.Builder builder = KiiUser.builderWithEmail(email);
				KiiUser kiiUser = builder.build();
				kiiUser.register(password);
				Logger.i("registered user uri=" + kiiUser.toUri().toString());
				// �o�^����KiiUser��ChatUser�Ƃ���App�X�R�[�v�̃o�P�c�ɕۑ����Ă����i�����p�j
				ChatUser user = new ChatUser(kiiUser.toUri().toString(), username, email);
				user.getKiiObject().save();
				// GCM�̐ݒ�
				String registrationId = GCMUtils.register();
				KiiUser.pushInstallation().install(registrationId);
				// �T�[�o����v�b�V���ʒm����M����ׂɁA������p�̃g�s�b�N���쐬����
				// ���̃g�s�b�N�͑��̑S�Ẵ��[�U�ɏ������݌�����^��
				// ���̃��[�U���������`���b�g�����o�[�ǉ��������Ƃ�ʒm����ׂɎg�p����
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
			ProgressDialogFragment.hide(getFragmentManager());
			if (result) {
				// �T�C���A�b�v����������ɍs��ꂽ�ꍇ�́A�R�[���o�b�N���\�b�h�ŌĂяo�����ɒʒm����
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
