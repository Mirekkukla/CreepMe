package com.mordor.creepme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

	// Activity taken on Cancel All Selections Button click
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

	// Builds the Activity Bar Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.fragment_main_options, menu);
		return super.onCreateOptionsMenu(menu);
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
}
