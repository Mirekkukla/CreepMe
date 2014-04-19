package com.mordor.creepme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivityFragment extends Fragment {
	private Button mFindNewFriendButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.fragment_main, parent, false);

		// Button to add new friend to "Who You're Creeping" list
		mFindNewFriendButton = (Button)v.findViewById(R.id.find_new_friend_button);
		mFindNewFriendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), FriendSelector.class);
				startActivity(i);
			}
		});

		return v;
	}
}
