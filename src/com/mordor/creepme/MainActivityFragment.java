package com.mordor.creepme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivityFragment extends Fragment {
	private Button mCreepNewFriendButton;

	/* Builds main fragment view for Main */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.fragment_main, parent, false);

		// Button to add new friend to "Who You're Creeping" list
		mCreepNewFriendButton = (Button) v
				.findViewById(R.id.creep_new_friendButton);
		mCreepNewFriendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), FriendSelector.class);
				startActivity(i);
			}
		});

		return v;
	}

	/* Builds the Activity Bar Menu */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main_options, menu);
	}
}
