package com.mordor.creepme;

import android.support.v4.app.Fragment;

public class CreepMapActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new CreepMapFragment();
	}

}