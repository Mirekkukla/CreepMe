package com.mordor.creepme;

import android.support.v4.app.Fragment;

public class FriendSelectorActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new FriendSelectorFragment();
	}

}