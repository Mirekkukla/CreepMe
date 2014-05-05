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
		this.creepsByYou = new ArrayList<Creep>();
		this.creepsOnYou = new ArrayList<Creep>();
		// Temporary list population for testing
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
			this.creepsByYou.add(c);
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
			this.creepsOnYou.add(c);
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
			return this.creepsByYou;
		} else {
			return this.creepsOnYou;
		}
	}

	public Creep getCreep(UUID id, Boolean isByYou) {
		if (isByYou) {
			for (Creep c : this.creepsByYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		} else {
			for (Creep c : this.creepsOnYou) {
				if (c.getId().equals(id)) {
					return c;
				}
			}
		}
		return null;
	}

	public void addCreep(Creep c) {
		if (c.isByYou()) {
			this.creepsByYou.add(c);
		} else if (!c.isByYou()) {
			this.creepsOnYou.add(c);
		}
	}

	// Removes the passed creep from it's ArrayList
	public void removeCreep(Creep c) {
		if (c.isByYou()) {
			this.creepsByYou.remove(this.creepsByYou.indexOf(c));
		} else if (!c.isByYou()) {
			this.creepsOnYou.remove(this.creepsOnYou.indexOf(c));
		}
	}

	// Removes all selected creeps from their ArrayLists
	public Boolean removeSelections() {
		Boolean removed = false;
		// Remove all checked creeps
		for (int i = 0; i < this.creepsOnYou.size(); i++) {
			if (this.creepsOnYou.get(i).getIsChecked()) {
				this.creepsOnYou.remove(i);
				removed = true;
				i--; // Because an element was just removed
			}
		}
		for (int i = 0; i < this.creepsByYou.size(); i++) {
			if (this.creepsByYou.get(i).getIsChecked()) {
				this.creepsByYou.remove(i);
				removed = true;
				i--; // Because an element was just removed
			}
		}

		// Set all remaining creeps to unchecked
		for (int i = 0; i < this.creepsOnYou.size(); i++) {
			this.creepsOnYou.get(i).setIsChecked(false);
		}
		for (int i = 0; i < this.creepsByYou.size(); i++) {
			this.creepsByYou.get(i).setIsChecked(false);
		}
		return removed;
	}

	// Checks for and removes all completed creeps
	public void checkForCompletions() {
		for (int i = 0; i < this.creepsOnYou.size(); i++) {
			if (this.creepsOnYou.get(i).getIsComplete()) {
				this.creepsOnYou.remove(i);
				if (i != 0)
					i--; // Because an element was removed
			}
		}
		for (int i = 0; i < this.creepsByYou.size(); i++) {
			if (this.creepsByYou.get(i).getIsComplete()) {
				this.creepsByYou.remove(i);
				if (i != 0)
					i--; // Because an element was removed
			}
		}
	}

}
