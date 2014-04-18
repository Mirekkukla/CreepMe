package com.mordor.creepme;

import android.support.v4.app.Fragment;

public class FriendSelector extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new FriendSelectorFragment();
	}

}
