package com.mordor.creepme;

import java.util.ArrayList;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class CreepMapActivity extends Activity {
	private String dirPoints;
	private ArrayList<UUID> victimsList;
	private Location location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		// Initialize map builder
		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		// Get a handle to the Map Fragment
		final GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(
		    R.id.creep_mapFragment)).getMap();
		map.setMyLocationEnabled(true);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		location = locationManager.getLastKnownLocation(provider);

		// Get passed in extras
		if (getIntent().getSerializableExtra("victimsList") != null) {
			@SuppressWarnings("unchecked")
			ArrayList<UUID> list = (ArrayList<UUID>) getIntent()
			    .getSerializableExtra("victimsList");

			// Don't know why I have to do this, but using victimsList 3 lines up
			// doesn't work
			victimsList = list;

			// Initialize header text
			TextView nameTextView = (TextView) findViewById(R.id.map_infoText);
			nameTextView.setText("");
			for (int i = 0; i < victimsList.size(); i++) {
				// Get the creep
				Creep c = MainActivity.sLab.getCreep(victimsList.get(i));

				// Set creep location to builder
				builder.include(new LatLng(c.getLatitude(), c.getLongitude()));

				// Update header TextView with name of creep
				if (i != 0) {
					nameTextView.setText(nameTextView.getText() + ", " + c.getName());
				} else {
					nameTextView.setText(c.getName());
				}
			}

			// Update all creep locations on map
			updateLocations(map);

			// If more than one person is being mapped, remove Get Directions button
			if (victimsList.size() != 1) {
				View v = findViewById(R.id.directionsButton);
				((ViewManager) v.getParent()).removeView(v);
			}

			// Check if user location is available
			if (location != null) {
				// Add user location to bounds
				builder.include(new LatLng(location.getLatitude(), location
				    .getLongitude()));
			}
			// Zoom in on the defined bounds
			zoomInOnCreeps(map, builder);
		}

		// Display home as up in Activity Bar
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	/* Zooms map view in to bounds defined to include all creeps */
	private void zoomInOnCreeps(final GoogleMap map,
	    final LatLngBounds.Builder builder) {
		// Set bounds
		final LatLngBounds bounds = builder.build();
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
						    mapView.getViewTreeObserver()
						        .removeOnGlobalLayoutListener(this);
						    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 75));
					    }
				    });
			}
		}

	}

	/* Updates location markers for all creeps in victimsList */
	private void updateLocations(GoogleMap map) {
		// Remove all existing markers, etc.
		map.clear();

		// Update all creep positions
		for (int i = 0; i < victimsList.size(); i++) {
			// Get the creep
			Creep c = MainActivity.sLab.getCreep(victimsList.get(i));

			// Get creep location
			LatLng creepLocation = new LatLng(c.getLatitude(), c.getLongitude());

			// Add marker that shows victim's status and name when you click on it
			String creepTag;
			if (!c.isByYou()) {
				creepTag = "Creeper";
				map.addMarker(new MarkerOptions()
				    .title(creepTag)
				    .snippet(c.getName())
				    .position(creepLocation)
				    .icon(
				        BitmapDescriptorFactory
				            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			} else {
				creepTag = "Victim";
				map.addMarker(new MarkerOptions().title(creepTag).snippet(c.getName())
				    .position(creepLocation));
			}

			// Set victim/creeper's location as destination point for Directions
			this.dirPoints = ("http://maps.google.com/maps?f=&daddr="
			    + Double.toString(c.getLatitude()) + ", " + Double.toString(c
			    .getLongitude()));
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

	// Action taken on Get Directions button click
	public void getDirections(View v) {
		if (this.dirPoints != "") {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
			    Uri.parse(this.dirPoints));
			startActivity(intent);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check for GPS enabled on resume
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
		    .setMessage("Your GPS seems to be disabled, do you want to enable it?")
		    .setCancelable(false)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    startActivity(new Intent(
				        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			    }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    dialog.cancel();
				    finish();
			    }
		    });
		final AlertDialog alert = builder.create();
		alert.show();
	}

}
