package com.kii.sample.chat.ui.util;

import com.kii.sample.chat.KiiChatApplication;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
	
public class ProgressDialogFragment extends DialogFragment {
		public static final int BLANK = -1;
		private static final String FRAGMENT_TAG = "progress";
		private static final String ARGS_TITLE = "title";
		private static final String ARGS_MESSAGE = "message";
		
		private ProgressDialog progressDialog;
		
		public static void show(FragmentManager manager, int titleId, int messageId) {
			show(manager, getMessage(titleId), getMessage(messageId));
		}
		public static void show(FragmentManager manager, String title, String message) {
			ProgressDialogFragment dialog = newInstance(title, message);
			dialog.show(manager, FRAGMENT_TAG);
		}
		public static void update(FragmentManager manager, int messageId) {
			update(manager, getMessage(messageId));
		}
		public static void update(FragmentManager manager, String message) {
			ProgressDialogFragment dialog = (ProgressDialogFragment) manager.findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
			dialog.setMessage(message);
		}
		public static void hide(FragmentManager manager) {
			ProgressDialogFragment dialog = (ProgressDialogFragment) manager.findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);
			if (dialog == null) {
				return;
			}
			dialog.dismiss();
		}
		private static ProgressDialogFragment newInstance(String title, String message) {
			ProgressDialogFragment fragment = new ProgressDialogFragment();
			
			Bundle args = new Bundle();
			args.putString(ARGS_TITLE, title);
			args.putString(ARGS_MESSAGE, message);
			fragment.setArguments(args);
			return fragment;
		}
		private static String getMessage(int id) {
			if (id == BLANK) {
				return "";
			}
			return KiiChatApplication.getMessage(id);
		}
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle args = getArguments();
			String title = args.getString(ARGS_TITLE);
			String msg = args.getString(ARGS_MESSAGE);
			
			this.progressDialog = new ProgressDialog(getActivity());
			this.progressDialog.setTitle(title);
			this.progressDialog.setMessage(msg);
			this.progressDialog.setIndeterminate(true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			return this.progressDialog;
		}
		private void setMessage(String message) {
			this.progressDialog.setMessage(message);
		}
	}
