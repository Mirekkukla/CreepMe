package com.mordor.creepme;

import android.annotation.SuppressLint;
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

	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			TextView name = (TextView) findViewById(R.id.map_infoText);
			name.setText(extras.getString("name"));
			Double mLatVictim = extras.getDouble("lat");
			Double mLonVictim = extras.getDouble("lon");
			LatLng victim = new LatLng(mLatVictim, mLonVictim);
			dirPoints = ("http://maps.google.com/maps?f=&daddr="
					+ Double.toString(mLatVictim) + ", " + Double
					.toString(mLonVictim));

			// Get a handle to the Map Fragment
			final GoogleMap map = ((MapFragment) getFragmentManager()
					.findFragmentById(R.id.creepMapFragment)).getMap();

			map.setMyLocationEnabled(true);
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	        String provider = locationManager.GPS_PROVIDER;
	        Location location = locationManager.getLastKnownLocation(provider);

			map.moveCamera(CameraUpdateFactory.newLatLngZoom(victim, 13));
			if (location != null) {
				double mLatUser = location.getLatitude();
				double mLonUser = location.getLongitude();
				LatLng user = new LatLng(mLatUser, mLonUser);

				LatLngBounds.Builder builder = new LatLngBounds.Builder();
				final LatLngBounds bounds = builder.include(victim)
						.include(user)
						.build();

				try {
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
							50));
				} catch (Exception e) {
					// layout not yet initialized
			        final View mapView = getFragmentManager().findFragmentById(R.id.creepMapFragment).getView();
			        if (mapView.getViewTreeObserver().isAlive()) {
			            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			                @SuppressWarnings("deprecation")
			                @SuppressLint("NewApi")
			                // We check which build version we are using.
			                @Override
			                public void onGlobalLayout() {
			                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			                    } else {
			                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			                    }
												map.moveCamera(CameraUpdateFactory
														.newLatLngBounds(
																bounds, 50));
			                }
			            });
			        }
				}

				map.addMarker(new MarkerOptions().title("Victim")
						.snippet("< who yer creepin' >").position(victim));
			} else {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(victim, 13));
				map.addMarker(new MarkerOptions().title("Victim")
						.snippet("< who yer creepin' >").position(victim));
			}
		}

		// Check for compatibility, display home as up
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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

	public void getDirections(View v) {
		if (dirPoints != "") {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse(dirPoints));
			startActivity(intent);
		}

	}

}
