package com.mordor.creepme;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CreepMapFragment extends Fragment {
	private Context context;
	private TextView lat, lon;
	private LocationManager locationManager;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		// Check for compatibility, display home as up
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		context = getActivity();

		lat = (TextView) view.findViewById(R.id.latitudeText);
		lon = (TextView) view.findViewById(R.id.longitudeText);

		locationManager = (LocationManager) getActivity()
				.getSystemService(context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				updateLocation(location);
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 100, 0, locationListener);

		return view;
	}

	/* Builds the Activity Bar Menu */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main_options, menu);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateLocation(Location location) {
		lat.setText(Double.toString(location.getLatitude()));
		lon.setText(Double.toString(location.getLongitude()));
		Log.i("here", "changed latlong");
	}
}
