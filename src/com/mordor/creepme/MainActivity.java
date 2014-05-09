package com.mordor.creepme;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "com.mordor.creepme.MainActivity";
	public static CreepLab sLab;
	public static String sPhoneNumber;
	private CreepListAdapter adp1;
	private CreepListAdapter adp2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sLab = CreepLab.get(this);

		// Get user's phone number
		TelephonyManager telManager = (TelephonyManager) this
		    .getSystemService(Context.TELEPHONY_SERVICE);
		sPhoneNumber = telManager.getLine1Number();

		// Define ListViews locally, linked to layout
		ListView lv1 = (ListView) findViewById(R.id.who_you_creepingList);
		ListView lv2 = (ListView) findViewById(R.id.who_creeping_youList);

		// Assign adapters
		this.adp1 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(true));
		this.adp2 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(false));

		// Set custom array adapter to display list items in each ListView
		lv1.setAdapter(this.adp1);
		lv2.setAdapter(this.adp2);

		implementListViewTimer();

	}

	// Defines and activates intent that opens FriendSelector activity
	public void newFriendSelector(View v) {
		Intent i = new Intent(this, FriendSelectorActivity.class);
		startActivity(i);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Update lists on activity resume
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();
	}

	// Action taken on Cancel All Selections Button click
	public void cancelSelections(View v) {
		try {
			// If nothing gets removed, nothing was selected
			if (!sLab.removeSelections()) {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at cancelSelections()");
		}
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();
	}

	// Action taken on Map All Selections Button click
	public void mapSelections() {
		// Check for GPS enabled
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		}

		try {
			ArrayList<UUID> selections = sLab.selectedCreeps();
			if (selections.size() != 0) {
				Intent i = new Intent(this, CreepMapActivity.class);
				i.putExtra("victimsList", selections);
				this.startActivity(i);
			} else {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at mapSelections()");
		}
	}

	// Builds the Activity Bar Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	// Deals with Activity Bar and Menu item selections
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View v = findViewById(android.R.id.content);
		switch (item.getItemId()) {
		case R.id.action_map_selections:
			mapSelections();
			return true;
		case R.id.action_map_selections_text:
			mapSelections();
			return true;
		case R.id.action_delete_selections:
			cancelSelections(v);
			return true;
		case R.id.action_add_creep:
			newFriendSelector(v);
			return true;
		case R.id.action_settings:
			// Open settings page
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Starts a timer to update timers every second
	public void implementListViewTimer() {
		// Timer counts down every second, by 1000 ms intervals
		new CountDownTimer(1000, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {
				sLab.checkForCompletions();
				adp1.notifyDataSetChanged();
				adp2.notifyDataSetChanged();
				// Restarts every second
				implementListViewTimer();
			}

		}.start();
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
			    }
		    });
		final AlertDialog alert = builder.create();
		alert.show();
	}
}
