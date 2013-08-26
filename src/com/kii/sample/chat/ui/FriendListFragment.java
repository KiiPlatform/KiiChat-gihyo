package com.kii.sample.chat.ui;

import java.util.List;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.ui.adapter.UserListAdapter;
import com.kii.sample.chat.ui.loader.FriendListLoader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 友達一覧を表示する画面のフラグメントです。
 * この画面はタブに表示されます。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class FriendListFragment extends ListFragment implements LoaderCallbacks<List<ChatFriend>>, OnItemClickListener {
	
	public static FriendListFragment newInstance() {
		return new FriendListFragment();
	}
	
	private UserListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);
		this.adapter = new UserListAdapter(getActivity());
		this.setListAdapter(this.adapter);
		this.setListShown(false);
		this.getListView().setOnItemClickListener(this);
		this.getLoaderManager().initLoader(0, state, this);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.user_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
			case R.id.menu_add_friend:
				intent = new Intent(getActivity(), AddFriendActivity.class);
				startActivity(intent);
				return true;
			case R.id.menu_reload:
				this.getLoaderManager().restartLoader(0, null, this);
				return true;
			case R.id.menu_signout:
				// TODO:ログアウト処理を共通化
				PreferencesManager.setStoredAccessToken("");
				KiiUser.logOut();
				intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public Loader<List<ChatFriend>> onCreateLoader(int id, Bundle bundle) {
		return new FriendListLoader(getActivity());
	}
	@Override
	public void onLoadFinished(Loader<List<ChatFriend>> loader, List<ChatFriend> data) {
		this.adapter.setData(data);
		if (isResumed()) {
			this.setListShown(true);
		} else {
			this.setListShownNoAnimation(true);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<ChatFriend>> loader) {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChatFriend friend = (ChatFriend)parent.getItemAtPosition(position);
		Toast.makeText(getActivity(), friend.getEmail() + " : " + friend.getUri(), Toast.LENGTH_SHORT).show();
		// TODO:ダイアログを表示して、そこからチャット画面に遷移
	}
}
