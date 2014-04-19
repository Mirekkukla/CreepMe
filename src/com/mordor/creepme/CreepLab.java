package com.mordor.creepme;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
	private final ArrayList<Creep> mCreeps;

	private static CreepLab sCreepLab;
	private final Context mAppContext;

	private CreepLab(Context appContext) {
		mAppContext = appContext;
		mCreeps = new ArrayList<Creep>();
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
