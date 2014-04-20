package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
	private final ArrayList<Creep> mCreeps;

	private static CreepLab sCreepLab;
	private final Context mAppContext;

	private CreepLab(Context appContext) {
		mAppContext = appContext;
		mCreeps = new ArrayList<Creep>();
		// Temp list population
		for (int i = 0; i < 5; i++) {
			Date now = new Date();
			Creep c = new Creep();
			c.setDateStarted(now);
			c.setName("First, Last Name #" + i);
			c.setFollowTime(i * 1000 * 60 * 60 * 5);
			mCreeps.add(c);
		}
	}

	public static CreepLab get(Context c) {
		if (sCreepLab == null) {
			sCreepLab = new CreepLab(c.getApplicationContext());
		}
		return sCreepLab;
	}

	public ArrayList<Creep> getCreeps() {
		return mCreeps;
	}

	public Creep getCreep(UUID id) {
		for (Creep c : mCreeps) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		return null;
	}
}
