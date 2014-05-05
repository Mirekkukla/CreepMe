package com.mordor.creepme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreepMapActivity extends Activity {
	private String dirPoints;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		// Get passed in victim name, lat/lng
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Double mLatVictim = extras.getDouble("lat");
			Double mLngVictim = extras.getDouble("lng");
			this.name = extras.getString("name");

			// Set header text
			TextView nameTextView = (TextView) findViewById(R.id.map_infoText);
			nameTextView.setText(this.name);

			// Set victim's location as destination point for Directions
			LatLng victim = new LatLng(mLatVictim, mLngVictim);
			this.dirPoints = ("http://maps.google.com/maps?f=&daddr="
			    + Double.toString(mLatVictim) + ", " + Double.toString(mLngVictim));

			// Get a handle to the Map Fragment
			final GoogleMap map = ((MapFragment) getFragmentManager()
			    .findFragmentById(R.id.creep_mapFragment)).getMap();
			map.setMyLocationEnabled(true);
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			String provider = LocationManager.GPS_PROVIDER;
			Location location = locationManager.getLastKnownLocation(provider);

			// Zoom in on an area including both user and victim locations
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(victim, 13));
			if (location != null) {
				double mLatUser = location.getLatitude();
				double mLngUser = location.getLongitude();
				LatLng user = new LatLng(mLatUser, mLngUser);

				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				final LatLngBounds bounds = builder.include(victim).include(user)
				    .build();

				try {
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 75));
				} catch (Exception e) {
					// layout not yet initialized
					final View mapView = getFragmentManager().findFragmentById(
					    R.id.creep_mapFragment).getView();
					if (mapView.getViewTreeObserver().isAlive()) {
						mapView.getViewTreeObserver().addOnGlobalLayoutListener(
						    new OnGlobalLayoutListener() {

							    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
							    @Override
							    public void onGlobalLayout() {
								    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(
								        this);
								    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
								        75));
							    }
						    });
					}
				}

				// Marker shows victim's name when you click on it
				map.addMarker(new MarkerOptions().title("Victim")
				    .snippet("< who yer creepin' >").position(victim));
			} else {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(victim, 13));
				map.addMarker(new MarkerOptions().title("Victim").snippet(this.name)
				    .position(victim));
			}
		}

		// Display home as up in Activity Bar
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(this) != null) {
				NavUtils.navigateUpFromSameTask(this);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Action taken on Get Directions button click
	public void getDirections(View v) {
		if (this.dirPoints != "") {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
			    Uri.parse(this.dirPoints));
			startActivity(intent);
		}

	}

}
