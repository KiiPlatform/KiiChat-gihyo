package com.kii.sample.chat.ui;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.kii.cloud.storage.GeoPoint;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.app.AppException;
import com.kii.cloud.storage.exception.app.BadRequestException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.cloud.storage.exception.app.ForbiddenException;
import com.kii.cloud.storage.exception.app.NotFoundException;
import com.kii.cloud.storage.exception.app.UnauthorizedException;
import com.kii.cloud.storage.exception.app.UndefinedException;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class NearFriendListActivity extends FragmentActivity {

	private static final String TAG = "NearFriendListActivity";
	private static final String PREFKEY_LOCOBJ = "PREFKEY_LOCOBJ";
	private Location cachedLoc = null;

	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_near_friend);
		updateList();
		Button btnUpdate = (Button) findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateList();
			}
		});
	}

	public void updateList() {
		ProgressDialogFragment pdf = ProgressDialogFragment.newInstance();
		pdf.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (cachedLoc == null) {
					cachedLoc = getCurrentLocation();
				}
				try {
					if (isLocObjUriSaved()) {
						updateLocationObj(cachedLoc);
					} else {
						uploadLocationObj(cachedLoc);
					}
					NearFriendListFragment target = (NearFriendListFragment) getSupportFragmentManager()
							.findFragmentById(R.id.nearFriendListFragment);

					KiiUser user = Kii.user();
					Bundle b = new Bundle();
					b.putString("exclEmail", user.getEmail());
					b.putDouble("latitude", cachedLoc.getLatitude());
					b.putDouble("longitude", cachedLoc.getLongitude());
					target.getLoaderManager().initLoader(0, b, target);
				} catch (AppException e) {
					// TODO: handle error.
					e.printStackTrace();
				} catch (IOException e) {
					// TODO: handle error.
					e.printStackTrace();
				}
			}
		}).start();

	}

	private String getSavedLocObjUri() {
		SharedPreferences prefs = this.getSharedPreferences(TAG, MODE_PRIVATE);
		return prefs.getString(PREFKEY_LOCOBJ, null);
	}

	private boolean isLocObjUriSaved() {
		SharedPreferences prefs = this.getSharedPreferences(TAG, MODE_PRIVATE);
		return prefs.contains(PREFKEY_LOCOBJ);
	}

	private void saveLocObjUri(Uri objUri) {
		SharedPreferences prefs = this.getSharedPreferences(TAG, MODE_PRIVATE);
		prefs.edit().putString(PREFKEY_LOCOBJ, objUri.toString()).commit();
	}

	private KiiObject updateLocationObj(Location loc)
			throws BadRequestException, ConflictException, ForbiddenException,
			NotFoundException, UnauthorizedException, UndefinedException,
			IOException {
		GeoPoint gp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		KiiObject target = KiiObject
				.createByUri(Uri.parse(getSavedLocObjUri()));
		target.set("currentLocation", gp);
		target.save();
		return target;
	}

 	private KiiObject uploadLocationObj(Location loc) throws BadRequestException,
			ConflictException, ForbiddenException, NotFoundException,
			UnauthorizedException, UndefinedException, IOException {
		GeoPoint gp = new GeoPoint(loc.getLatitude(), loc.getLongitude());
		KiiUser user = KiiUser.getCurrentUser();
		KiiObject obj = Kii.bucket(ApplicationConst.LOCATIONBUCKET).object();
		obj.set("email", user.getEmail());
		obj.set("username", user.getDisplayname());
		obj.set("userUri", user.toUri());
		obj.set("currentLocation", gp);
		obj.save();
		saveLocObjUri(obj.toUri());
		return obj;
	}

	private Location getCurrentLocation(){
		LocationManager mLocationManager = 
				(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setHorizontalAccuracy(Criteria.ACCURACY_COARSE);
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Location> locRef = new AtomicReference<Location>();
		LocationListener listener = new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				latch.countDown();
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO: confirm whether this is right.
				latch.countDown();
			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onLocationChanged(Location location) {
				cachedLoc = location;
				locRef.set(location);
				latch.countDown();
			}
		};
		mLocationManager.requestSingleUpdate(criteria, listener,
				getMainLooper());
		try {
			long start = System.currentTimeMillis();
			latch.await();
			long end = System.currentTimeMillis();
			Log.v(TAG, "get location takes " + (end - start) / 1000 + "sec.");
			if (cachedLoc != null) {
				Log.v(TAG, "location: lat " + cachedLoc.getLatitude()
						+ " lon: " + cachedLoc.getLongitude());
			}
		} catch (InterruptedException e) {
			Log.v(TAG, "Getting location timed out.");
		}
		return locRef.get();
	}

}
