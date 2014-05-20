package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
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
		Random randomGenerator = new Random();
		int by = randomGenerator.nextInt(8);
		int on = randomGenerator.nextInt(8);
		for (int i = 0; i < by; i++) {
			Date now = new Date();
			Creep c = new Creep();
			c.setTimeMade(now.getTime());
			c.setName("Your Friend #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setTimeStarted(now.getTime());
			if ((i & 1) == 0) {
				c.setGpsEnabled(true);
			} else {
				c.setGpsEnabled(false);
			}
			c.setLatitude(40.0176 + i * .01);
			c.setLongitude(-105.2797 + i * .001);
			c.setIsByYou(true);
			c.setIsChecked(false);
			c.setIsStarted(true);
			c.setIsComplete(false);
			this.creepsByYou.add(c);
		}
		for (int i = 0; i < on; i++) {
			Date now = new Date();
			Creep c = new Creep();
			c.setTimeMade(now.getTime());
			c.setName("Hot Chick #" + (i + 1));
			c.setFollowTime((i + 1) * 1000 * 60 * 60);
			c.setTimeStarted(now.getTime());
			if ((i & 1) == 0) {
				c.setGpsEnabled(true);
			} else {
				c.setGpsEnabled(false);
			}
			c.setLatitude(40.0176 - (i + 1) * .01);
			c.setLongitude(-105.2797 - (i + 1) * .001);
			c.setIsByYou(false);
			c.setIsChecked(false);
			c.setIsStarted(true);
			c.setIsComplete(false);
			this.creepsOnYou.add(c);
		}
	}

	/* Builds new creep manager */
	public static CreepLab get(Context c) {
		if (sCreepLab == null) {
			sCreepLab = new CreepLab(c.getApplicationContext());
		}
		return sCreepLab;
	}

	/* Returns list of all relevant creeps */
	public ArrayList<Creep> getCreeps(Boolean isByYou) {
		if (isByYou) {
			return this.creepsByYou;
		} else {
			return this.creepsOnYou;
		}
	}

	/* Gets any creep by UUID */
	public Creep getCreep(UUID id) {
		for (Creep c : this.creepsByYou) {
			if (c.getId().equals(id)) {
				return c;
			}
		}

		for (Creep c : this.creepsOnYou) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		return null;
	}

	/* Adds new creep to relevant list */
	public void addCreep(Creep c) {
		if (c.isByYou()) {
			this.creepsByYou.add(c);
		} else if (!c.isByYou()) {
			this.creepsOnYou.add(c);
		}
	}

	/* Removes creep from relevant list */
	public void removeCreep(Creep c) {
		if (c.isByYou()) {
			this.creepsByYou.remove(this.creepsByYou.indexOf(c));
		} else if (!c.isByYou()) {
			this.creepsOnYou.remove(this.creepsOnYou.indexOf(c));
		}
		/**
		 * Notify server creep was removed
		 */
	}

	/* Removes all selected creeps from their lists */
	public Boolean removeSelections() {
		Boolean removed = false;
		// Remove all checked creeps
		for (int i = 0; i < this.creepsOnYou.size(); i++) {
			if (this.creepsOnYou.get(i).getIsChecked()) {
				removeCreep(this.creepsOnYou.get(i));
				removed = true;
				i--; // Because an element was just removed
			}
		}
		for (int i = 0; i < this.creepsByYou.size(); i++) {
			if (this.creepsByYou.get(i).getIsChecked()) {
				removeCreep(this.creepsByYou.get(i));
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

	/* Returns list of all selected creeps' UUIDs */
	public ArrayList<UUID> selectedCreeps() {
		ArrayList<UUID> selections = new ArrayList<UUID>();
		// Add all checked creeps
		for (int i = 0; i < this.creepsByYou.size(); i++) {
			Creep c = this.creepsByYou.get(i);
			if (c.getIsChecked()) {
				selections.add(c.getId());
				// Set unchecked once added to map
				c.setIsChecked(false);
			}
		}
		for (int i = 0; i < this.creepsOnYou.size(); i++) {
			Creep c = this.creepsOnYou.get(i);
			if (c.getIsChecked()) {
				selections.add(c.getId());
				// Set unchecked once added to map
				c.setIsChecked(false);
			}
		}
		return selections;
	}

	/* Checks for and removes all completed creeps */
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
