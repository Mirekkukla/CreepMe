package com.mordor.creepme;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.content.Context;

public class CreepLab {
  public final ArrayList<Creep> creepsByYou;
  public final ArrayList<Creep> creepsOnYou;

  private static CreepLab sCreepLab;
  private List<Creep> deletedCreeps;
  private List<Creep> updatedCreeps;
  
  private Boolean isEmpty;

  private CreepLab(Context appContext) {
    this.creepsByYou = new ArrayList<Creep>();
    this.creepsOnYou = new ArrayList<Creep>();
    this.deletedCreeps = new ArrayList<Creep>();
    this.updatedCreeps = new ArrayList<Creep>();
    this.isEmpty = true;

    // Temporary list population for testing
    for (int i = 0; i < 2; i++) {
      Creep c = new Creep();
      c.setName("Sparkles #" + (i + 1));
      c.setDuration((i + 1) * 1000 * 60 * 60);
      c.setGpsEnabled(true);
      c.setLatitude(40.0176 - (i + 1) * .01);
      c.setLongitude(-105.2797 - (i + 1) * .001);
      c.setIsByYou(false);
      c.setIsStarted(false);
      c.setIsSubscribed(true);
      c.setNumber("8005556969");
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
  
  /* Gets list of creeps by specified phone number */
  public List<Creep> getCreeps(String number) {
    List<Creep> creeps = new ArrayList<Creep>();
    for (Creep c : this.creepsByYou) {
      if(c.getNumber().equals(number)) {
        creeps.add(c);
      }
    }

    for (Creep c : this.creepsOnYou) {
      if(c.getNumber().equals(number)) {
        creeps.add(c);
      }
    }
    return creeps;
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
    deletedCreeps.add(c);
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
        deletedCreeps.add(this.creepsOnYou.get(i));
        this.creepsOnYou.remove(i);
        i--;
      }
    }
    for (int i = 0; i < this.creepsByYou.size(); i++) {
      if (this.creepsByYou.get(i).getIsComplete()) {
        deletedCreeps.add(this.creepsByYou.get(i));
        this.creepsByYou.remove(i);
        i--;
      }
    }
  }

  public Boolean isEmpty() {
    if(this.creepsOnYou.size() == 0 && this.creepsByYou.size() == 0) {
      this.isEmpty = true;
    } else {
      this.isEmpty = false;
    }
    return this.isEmpty;
  }
  
  public List<Creep> getDeletedCreepsList() {
    return this.deletedCreeps;
  }
  
  public void clearDeletedCreepsList() {
    this.deletedCreeps.clear();
  }
  
  public List<Creep> getUpdatedCreeps() {
    return this.updatedCreeps;
  }
  
  public void addUpdatedCreep(Creep c) {
    this.updatedCreeps.add(c);
  }
  
  public void clearUpdatedCreepsList() {
    updatedCreeps.clear();
  }
}
