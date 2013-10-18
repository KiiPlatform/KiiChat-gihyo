package com.kii.sample.chat.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.kii.cloud.storage.GeoPoint;
import com.kii.sample.chat.model.IUser;
import com.kii.sample.chat.ui.ConfirmAddFriendDialogFragment.OnAddFriendListener;
import com.kii.sample.chat.ui.adapter.UserListAdapter;
import com.kii.sample.chat.ui.loader.AbstractAsyncTaskLoader;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;

public class NearFriendListFragment extends ListFragment implements
		LoaderCallbacks<List<SimpleUser>>, OnItemClickListener, OnAddFriendListener {

	private Handler handler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler(Looper.getMainLooper());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		UserListAdapter adp = new UserListAdapter(getActivity());
		this.setListAdapter(adp);
		this.getListView().setOnItemClickListener(this);
	}

	public static NearFriendListFragment newInstance() {
		return new NearFriendListFragment();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		JSONObject userJSON = new JSONObject();
		SimpleUser stub = (SimpleUser) getListAdapter().getItem(position);
		try {
			userJSON.put("username", stub.getUsername());
			userJSON.put("email", stub.getEmail());
		} catch (JSONException e) {
			// Wont' happens.
			return;
		}
		ConfirmAddFriendDialogFragment cadf = ConfirmAddFriendDialogFragment
				.newInstance(this, userJSON, position);
		cadf.show(getFragmentManager(), "ConfirmAddFriendDialogFragment");
	}

	@Override
	public void onFriendAdded(int position) {
		SimpleUser user = (SimpleUser) getListAdapter().getItem(position);
		SimpleProgressDialogFragment pdf = SimpleProgressDialogFragment.newInstance();
		pdf.show(getFragmentManager(), SimpleProgressDialogFragment.TAG);

		// TODO: LiveCoding: 選択ユーザを友達リストに保存する処理の実装。
		// 1. 選択された近隣ユーザの情報をオブジェクトに格納して、chat_friendsバケットに保存する。
		// {
		// "username": {username of selected user},
		// "email": {email of selected user},
		// "uri": {uri of selected user}
		// }
		// 2. 完了したらプログレスダイアログを消去する。エラー発生時はToastを表示する。

	}

	@Override
	public Loader<List<SimpleUser>> onCreateLoader(int id, Bundle args) {
		String exclEmail = args.getString("exclEmail");
		double lat = args.getDouble("latitude");
		double lon = args.getDouble("longitude");
		GeoPoint center = new GeoPoint(lat, lon);
		return new SimpleUserLoader(getActivity(), exclEmail, center);
	}

	@Override
	public void onLoadFinished(Loader<List<SimpleUser>> loader,
			final List<SimpleUser> data) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				DialogFragment pdf = (DialogFragment) ((FragmentActivity) getActivity())
						.getSupportFragmentManager().findFragmentByTag(
								SimpleProgressDialogFragment.TAG);
				pdf.dismiss();
				if (data == null) {
					Toast.makeText(getActivity(), "Failed to update list.",
							Toast.LENGTH_LONG).show();
					return;
				}
				((UserListAdapter) NearFriendListFragment.this.getListAdapter())
						.setData(data);
			}
		});

	}

	@Override
	public void onLoaderReset(Loader<List<SimpleUser>> loader) {
		// Nothing to do.
	}

}

class SimpleUser implements IUser {

	private String username;
	private String email;
	private Uri userUri;

	public SimpleUser(String username, String email, Uri userUri) {
		this.username = username;
		this.email = email;
		this.userUri = userUri;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getEmail() {
		return this.email;
	}

	@Override
	public String getUri() {
		return this.userUri.toString();
	}

}

class SimpleUserLoader extends AbstractAsyncTaskLoader<List<SimpleUser>> {

	private String exclEmail;
	private GeoPoint center;
	public SimpleUserLoader(Context context, String exclEmail, GeoPoint center) {
		super(context);
		this.exclEmail = exclEmail;
		this.center = center;
	}

	@Override
	public List<SimpleUser> loadInBackground() {
		ArrayList<SimpleUser> ret = new ArrayList<SimpleUser>();
		// TODO: LiveCoding: GeoQueryの実装。
		// 1. currentLocationフィールドに対するGeoDistanceクエリを作成する。(半径1Km)
		// 2. _calculated.distance で昇順にソート
		// 3. 自分の情報を除いて検索結果をretに詰める。 取得エラーの場合はnullを返す。
		return ret;
	}

}
