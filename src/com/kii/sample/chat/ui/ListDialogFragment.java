package com.kii.sample.chat.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ListDialogFragment extends DialogFragment {

	public static final String TAG = "ListDialogFragment";
	ListView listView;
	OnItemClickListener listener;

	public static ListDialogFragment newInstance(int listViewLayoutId,
			int titleResId, int iconResId, int requestId) {
		ListDialogFragment frag = new ListDialogFragment();
		Bundle b = new Bundle();
		b.putInt("layoutId", listViewLayoutId);
		b.putInt("titleResId", titleResId);
		b.putInt("iconResId", iconResId);
		b.putInt("requestId", requestId);
		frag.setArguments(b);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int layoutId = getArguments().getInt("layoutId");
		int titleResId = getArguments().getInt("titleResId");
		int iconResId = getArguments().getInt("iconResId");
		final int requestId = getArguments().getInt("requestId");
		LayoutInflater inflater = getActivity().getLayoutInflater();
		listView = (ListView) inflater.inflate(layoutId, null);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {

				ListDialogFragmentCallback lfc = null;
				Fragment pf = getTargetFragment();
				if (pf != null) {
					lfc = (ListDialogFragmentCallback) pf;
				} else {
					Activity ac = getActivity();
					if (ac != null) {
						lfc = (ListDialogFragmentCallback) ac;
					}
				}
				if (lfc != null) {
					lfc.onListDialogItemClicked(parent, view, pos, id,
							requestId);
				}
			}
		});

		return new AlertDialog.Builder(getActivity()).setIcon(iconResId)
				.setTitle(titleResId).setView(listView).create();
	}

	public interface ListDialogFragmentCallback {
		public void onListDialogItemClicked(AdapterView<?> parent, View view,
				int pos, long id, int requestId);
	}

}
