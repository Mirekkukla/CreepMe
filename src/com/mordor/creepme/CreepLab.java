package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
  public final ArrayList<Creep> creepsByYou;
  public final ArrayList<Creep> creepsOnYou;

  private static CreepLab sCreepLab;

  private CreepLab(Context appContext) {
    this.creepsByYou = new ArrayList<Creep>();
    this.creepsOnYou = new ArrayList<Creep>();

    // Temporary list population for testing
    for (int i = 0; i < 2; i++) {
      Date now = new Date();
      Creep c = new Creep();
      c.setTimeMade(now.getTime());
      c.setName("Hot Chick #" + (i + 1));
      c.setDuration((i + 1) * 1000 * 60 * 60);
      c.setTimeStarted(now.getTime());
      c.setGpsEnabled(true);
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
  
  /* Gets any creep by cloud ID */
  public Creep getCreep(String id) {
    for (Creep c : this.creepsByYou) {
      if(c.getCloudId() != null) {
        if (c.getCloudId().equals(id)) {
          return c;
        }
      }
    }

    for (Creep c : this.creepsOnYou) {
      if(c.getCloudId() != null) {
        if (c.getCloudId().equals(id)) {
          return c;
        }
      }
    }
    return null;
  }

  /* Adds new creep to relevant list */
  public void addCreep(Creep c) {
    if (c.getIsByYou()) {
      this.creepsByYou.add(c);
    } else if (!c.getIsByYou()) {
      this.creepsOnYou.add(c);
    }
  }

  /* Removes creep from relevant list */
  public void removeCreep(Creep c) {
    if (c.getIsByYou()) {
      this.creepsByYou.remove(this.creepsByYou.indexOf(c));
    } else if (!c.getIsByYou()) {
      this.creepsOnYou.remove(this.creepsOnYou.indexOf(c));
    }
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
        if(i != 0) i--;
      }
    }
    for (int i = 0; i < this.creepsByYou.size(); i++) {
      if (this.creepsByYou.get(i).getIsComplete()) {
        this.creepsByYou.remove(i);
        if(i != 0) i--;
      }
    }
  }

}
