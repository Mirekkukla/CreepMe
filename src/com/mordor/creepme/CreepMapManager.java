package com.mordor.creepme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class CreepMapManager {
	public static final String ACTION_LOCATION = "com.mordor.creepme.ACTION_LOCATION";

	private static CreepMapManager sMapManager;
	private final Context mAppContext;
	private final LocationManager mLocationManager;

	private CreepMapManager(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager) mAppContext
				.getSystemService(Context.LOCATION_SERVICE);

	}

	public static CreepMapManager get(Context c) {
		if (sMapManager == null) {
			sMapManager = new CreepMapManager(c.getApplicationContext());
		}
		return sMapManager;
	}

	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		Intent broadcast = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}

	public void startLocationUpdates() {
		String provider = LocationManager.GPS_PROVIDER;

		// Get the last known location and broadcast it if you have one
		Location lastKnown = mLocationManager.getLastKnownLocation(provider);
		if (lastKnown != null) {
			// Reset the time to now...tricksy hobbitses
			lastKnown.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnown);
		}

		// Start updates from the location manager
		PendingIntent pi = getLocationPendingIntent(false);
		// Request location, minimum delta of 1000ms, 1 meters
		mLocationManager.requestLocationUpdates(provider, 1000, 1, pi);
	}

	private void broadcastLocation(Location location) {
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
		mAppContext.sendBroadcast(broadcast);
	}

	public void stopLocationUpdates() {
		PendingIntent pi = getLocationPendingIntent(false);
		if (pi != null) {
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}

	public boolean isTrackingLocation() {
		return getLocationPendingIntent(false) != null;
	}
}
