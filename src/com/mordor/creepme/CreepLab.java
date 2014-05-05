package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
	private final ArrayList<Creep> creepsByYou;
	private final ArrayList<Creep> creepsOnYou;

	private static CreepLab sCreepLab;

	private CreepLab(Context appContext) {
		creepsByYou = new ArrayList<Creep>();
		creepsOnYou = new ArrayList<Creep>();
		// Temp list population for testing
		for (int i = 0; i < 7; i++) {
				Date now = new Date();
				Creep c = new Creep();
			c.setTimeMade(now.getTime());
				c.setName("Your Friend #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setTimeStarted(now.getTime());
			c.setLatitude(43.479786);
			c.setLongitude(-110.762334);
			c.setIsByYou(true);
			c.setIsChecked(false);
			c.setIsStarted(true);
			c.setIsComplete(false);
			creepsByYou.add(c);
			}
		for (int i = 0; i < 2; i++) {
				Date now = new Date();
				Creep c = new Creep();
			c.setTimeMade(now.getTime());
				c.setName("Hot Chick #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setTimeStarted(now.getTime());
			c.setLatitude(43.59);
			c.setLongitude(-110.8);
			c.setIsByYou(false);
			c.setIsChecked(false);
			c.setIsStarted(true);
			c.setIsComplete(false);
			creepsOnYou.add(c);
			}
	}

	public static CreepLab get(Context c) {
		if (sCreepLab == null) {
			sCreepLab = new CreepLab(c.getApplicationContext());
		}
		return sCreepLab;
	}

	public ArrayList<Creep> getCreeps(Boolean isByYou) {
		if (isByYou) {
			return creepsByYou;
		} else {
			return creepsOnYou;
		}
	}

	public Creep getCreep(UUID id, Boolean isByYou) {
		if (isByYou) {
			for (Creep c : creepsByYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		} else {
			for (Creep c : creepsOnYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		}
		return null;
	}

	public void addCreep(Creep c) {
		if (c.isByYou()) {
			creepsByYou.add(c);
		} else if (!c.isByYou()) {
			creepsOnYou.add(c);
		}
	}

	// Removes the passed creep from it's ArrayList
	public void removeCreep(Creep c) {
		if(c.isByYou()) {
			creepsByYou.remove(creepsByYou.indexOf(c));
		} else if (!c.isByYou()) {
			creepsOnYou.remove(creepsOnYou.indexOf(c));
		}
	}

	// Removes all selected creeps from their ArrayLists
	public Boolean removeSelections() {
		Boolean removed = false;
		// Remove all checked creeps
		for (int i = 0; i < creepsOnYou.size(); i++) {
			if (creepsOnYou.get(i).getIsChecked()) {
				creepsOnYou.remove(i);
				removed = true;
				i--; // Because an element was just removed
			}
		}
		for (int i = 0; i < creepsByYou.size(); i++) {
			if (creepsByYou.get(i).getIsChecked()) {
				creepsByYou.remove(i);
				removed = true;
				i--; // Because an element was just removed
			}
		}

		// Set all remaining creeps to unchecked
		for (int i = 0; i < creepsOnYou.size(); i++) {
			creepsOnYou.get(i).setIsChecked(false);
		}
		for (int i = 0; i < creepsByYou.size(); i++) {
			creepsByYou.get(i).setIsChecked(false);
		}
		return removed;
	}

	// Checks for and removes all completed creeps
	public void checkForCompletions() {
		for (int i = 0; i < creepsOnYou.size(); i++) {
			if (creepsOnYou.get(i).getIsComplete()) {
				creepsOnYou.remove(i);
				if (i != 0)
					i--; // Because an element was removed
			}
		}
		for (int i = 0; i < creepsByYou.size(); i++) {
			if (creepsByYou.get(i).getIsComplete()) {
				creepsByYou.remove(i);
				if (i != 0)
					i--; // Because an element was removed
			}
		}
	}

}
