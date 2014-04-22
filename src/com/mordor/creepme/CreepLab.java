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
		for (int i = 0; i < 2; i++) {
				Date now = new Date();
				Creep c = new Creep();
			c.setTimeMade(now.getTime());
				c.setName("Your Friend #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setByYou(true);
			c.setIsChecked(false);
			mCreepsByYou.add(c);
			}
		for (int i = 0; i < 8; i++) {
				Date now = new Date();
				Creep c = new Creep();
			c.setTimeMade(now.getTime());
				c.setName("Hot Chick #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setByYou(false);
			c.setIsChecked(false);
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

	public void addCreep(Creep c) {
		if (c.isByYou()) {
			mCreepsByYou.add(c);
		} else if (!c.isByYou()) {
			mCreepsOnYou.add(c);
		} else {
			// c doesn't exist
		}
	}

	public void removeCreep(Creep c) {
		if(c.isByYou()) {
			mCreepsByYou.remove(mCreepsByYou.indexOf(c));
		} else if (!c.isByYou()) {
			mCreepsOnYou.remove(mCreepsOnYou.indexOf(c));
		} else {
			// c doesn't exist
		}
	}

	public void removeSelections() {
		// Remove all checked creeps
		for (int i = 0; i < mCreepsOnYou.size(); i++) {
			if (mCreepsOnYou.get(i).getIsChecked()) {
				mCreepsOnYou.remove(i);
				i--;
			}
		}
		for (int i = 0; i < mCreepsByYou.size(); i++) {
			if (mCreepsByYou.get(i).getIsChecked()) {
				mCreepsByYou.remove(i);
				i--;
			}
		}

		// Set all remaining creeps to unchecked
		for (int i = 0; i < mCreepsOnYou.size(); i++) {
			mCreepsOnYou.get(i).setIsChecked(false);
		}
		for (int i = 0; i < mCreepsByYou.size(); i++) {
			mCreepsByYou.get(i).setIsChecked(false);
		}
	}

}
