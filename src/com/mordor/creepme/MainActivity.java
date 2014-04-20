package com.mordor.creepme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class MainActivity extends FragmentActivity {

	/* Builds main view for Main */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

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
