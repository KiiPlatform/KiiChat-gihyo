package com.kii.sample.chat.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.kii.cloud.storage.GeoPoint;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.callback.KiiObjectCallBack;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.query.KiiQueryResult;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.model.IUser;
import com.kii.sample.chat.ui.ConfirmAddFriendDialogFragment.OnAddFriendListener;
import com.kii.sample.chat.ui.adapter.UserListAdapter;
import com.kii.sample.chat.ui.loader.AbstractAsyncTaskLoader;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;

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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NearFriendListFragment extends ListFragment implements
		LoaderCallbacks<List<UserStub>>, OnItemClickListener, OnAddFriendListener {

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
		UserStub stub = (UserStub) getListAdapter().getItem(position);
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
		UserStub stub = (UserStub) getListAdapter().getItem(position);
		// Save object in chat friend.
		ChatFriend cf = new ChatFriend(new ChatUser(stub.getUri(),
				stub.getUsername(), stub.getEmail()));
		SimpleProgressDialogFragment pdf = SimpleProgressDialogFragment.newInstance();
		pdf.show(getFragmentManager(), SimpleProgressDialogFragment.TAG);
		cf.getKiiObject().save(new KiiObjectCallBack() {
			@Override
			public void onSaveCompleted(int token, KiiObject object,
					Exception exception) {
				DialogFragment prgDialog = (DialogFragment) getFragmentManager()
						.findFragmentByTag(SimpleProgressDialogFragment.TAG);
				prgDialog.dismiss();
				if (exception != null) {
					Toast.makeText(getActivity(), "Failed to add friend: "
							+ exception.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public Loader<List<UserStub>> onCreateLoader(int id, Bundle args) {
		String exclEmail = args.getString("exclEmail");
		double lat = args.getDouble("latitude");
		double lon = args.getDouble("longitude");
		GeoPoint center = new GeoPoint(lat, lon);
		return new UserStubLoader(getActivity(), exclEmail, center);
	}

	@Override
	public void onLoadFinished(Loader<List<UserStub>> loader,
			final List<UserStub> data) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				((UserListAdapter) NearFriendListFragment.this.getListAdapter())
						.setData(data);
				DialogFragment pdf = (DialogFragment) ((FragmentActivity) getActivity())
						.getSupportFragmentManager().findFragmentByTag(
								SimpleProgressDialogFragment.TAG);
				pdf.dismissAllowingStateLoss();
			}
		});

	}

	@Override
	public void onLoaderReset(Loader<List<UserStub>> loader) {
		// Nothing to do.
	}

}

// TODO: remove this mock.
class UserStub implements IUser {

	private String username;
	private String email;
	private Uri userUri;

	public UserStub(String username, String email, Uri userUri) {
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

// TODO: remove this mock.
class UserStubLoader extends AbstractAsyncTaskLoader<List<UserStub>> {

	private String exclEmail;
	private GeoPoint center;
	public UserStubLoader(Context context, String exclEmail, GeoPoint center) {
		super(context);
		this.exclEmail = exclEmail;
		this.center = center;
	}

	@Override
	public List<UserStub> loadInBackground() {
		ArrayList<UserStub> ret = new ArrayList<UserStub>();
		KiiClause geoQuery = KiiClause.geoDistance("currentLocation",
				this.center, 1000, "distance");
		KiiQuery query = new KiiQuery(geoQuery);
		query.setLimit(10);
		query.sortByAsc("_calculated.distance");
		KiiQueryResult<KiiObject> result;
		try {
			result = Kii.bucket(ApplicationConst.LOCATIONBUCKET).query(query);
			List<KiiObject> qres = result.getResult();
			for (KiiObject obj : qres) {
				String email = obj.getString("email");
				if (email.equalsIgnoreCase(this.exclEmail))
					continue;
				String username = obj.getString("username");
				Uri userUri = obj.getUri("userUri");
				UserStub stub = new UserStub(username, email, userUri);
				ret.add(stub);
			}
			return ret;
		} catch (AppException e) {
			// TODO: Show error.
			e.printStackTrace();
			return ret;
		} catch (IOException e) {
			// TODO: Show error.
			e.printStackTrace();
			return ret;
		}
	}

}
