package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
	private final ArrayList<Creep> mCreepsByYou;
	private final ArrayList<Creep> mCreepsOnYou;

	private static CreepLab sCreepLab;

	private CreepLab(Context appContext) {
		mCreepsByYou = new ArrayList<Creep>();
		mCreepsOnYou = new ArrayList<Creep>();
		// Temp list population
			for (int i = 0; i < 3; i++) {
				Date now = new Date();
				Creep c = new Creep();
				c.setDateStarted(now);
				c.setName("Your Friend #" + (i + 1));
				c.setFollowTime(i * 1000 * 60 * 60 * 5);
				mCreepsByYou.add(c);
			}
			for (int i = 0; i < 5; i++) {
				Date now = new Date();
				Creep c = new Creep();
				c.setDateStarted(now);
				c.setName("Hot Chick #" + (i + 1));
				c.setFollowTime(i * 1000 * 60 * 60 * 5);
				mCreepsOnYou.add(c);
			}
	}

	public static CreepLab get(Context c) {
		if (sCreepLab == null) {
			sCreepLab = new CreepLab(c.getApplicationContext());
		}
		return sCreepLab;
	}

	public ArrayList<Creep> getCreeps(Boolean byYou) {
		if (byYou) {
			return mCreepsByYou;
		} else {
			return mCreepsOnYou;
		}
	}

	public Creep getCreep(UUID id, Boolean byYou) {
		if (byYou) {
			for (Creep c : mCreepsByYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		} else {
			for (Creep c : mCreepsOnYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		}
		return null;
	}
}
