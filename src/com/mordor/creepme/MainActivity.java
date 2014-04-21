package com.mordor.creepme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

public class MainActivity extends Activity {

	/* Builds view for Main */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Define ListViews locally, linked to layout
		ListView lv1 = (ListView) findViewById(R.id.who_you_creepingList);
		ListView lv2 = (ListView) findViewById(R.id.who_creeping_youList);

		// Set custom array adapter to display list items in each ListView
		lv1.setAdapter(new CreepListAdapter(this, R.layout.creep_list_element,
				CreepLab.get(this).getCreeps(true)));
		lv2.setAdapter(new CreepListAdapter(this, R.layout.creep_list_element,
				CreepLab.get(this).getCreeps(false)));

	}

	/* Defines and activates intent that opens FriendSelector activity */
	public void newFriendSelector(View v) {
		Intent i = new Intent(this, FriendSelector.class);
		startActivity(i);
	}

	/* Builds the Activity Bar Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.fragment_main_options, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
