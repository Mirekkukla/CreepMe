package com.mordor.creepme;

import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;

public class Creep {
  private String name;
  private String number;
  private Bitmap profilePic;
  private long duration;
  private long timeRemaining;
  private final UUID id;
  private long timeMade;
  private long timeStarted;
  private Boolean isByYou;
  private Boolean isChecked;
  private Boolean isStarted;
  private Boolean isComplete;
  private Boolean gpsEnabled;
  private Boolean isSubscribed;
  private Double latitude;
  private Double longitude;
  private String cloudId;

  /* Initializes UUID when creating a new Creep */
  public Creep() {
    this.id = UUID.randomUUID();
    this.gpsEnabled = false;
    this.isChecked = false;
    this.isStarted = false;
    this.isComplete = false;
    this.isSubscribed = false;
    this.timeMade = (new Date()).getTime();
    this.timeStarted = this.timeMade;
  }

  public void setCloudId(String ce) {
    this.cloudId = ce;
  }

  public String getCloudId() {
    return this.cloudId;
  }

  public Boolean getIsByYou() {
    return this.isByYou;
  }

  public void setIsByYou(Boolean bool) {
    this.isByYou = bool;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNumber() {
    return this.number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public long getDuration() {
    return this.duration;
  }

  public void setDuration(long time) {
    this.duration = time;
  }

  /* Returns time remaining in milliseconds */
  public long getTimeRemaining() {
    Date now = new Date();
    this.timeRemaining = this.getDuration()
        - (now.getTime() - this.timeStarted);
    return this.timeRemaining;
  }

  public Bitmap getProfilePic() {
    return this.profilePic;
  }

  public void setProfilePic(Bitmap pic) {
    this.profilePic = pic;
  }

  public Boolean gpsEnabled() {
    return this.gpsEnabled;
  }

  public void setGpsEnabled(Boolean bool) {
    this.gpsEnabled = bool;
  }

  public UUID getId() {
    return this.id;
  }

  public void setTimeMade(long time) {
    this.timeMade = time;
  }

  public long getTimeMade() {
    return this.timeMade;
  }

  public long getTimeStarted() {
    return this.timeStarted;
  }

  public void setTimeStarted(long time) {
    this.timeStarted = time;
  }

  public void setIsChecked(Boolean bool) {
    this.isChecked = bool;
  }

  public Boolean getIsChecked() {
    return this.isChecked;
  }

  public void setIsStarted(Boolean bool) {
    this.isStarted = bool;
  }

  public Boolean getIsStarted() {
    return this.isStarted;
  }

  public void setIsComplete(Boolean bool) {
    this.isComplete = bool;
  }

  public Boolean getIsComplete() {
    return this.isComplete;
  }

  public void setLatitude(Double lat) {
    this.latitude = lat;
  }

  public Double getLatitude() {
    return this.latitude;
  }

  public void setLongitude(Double lng) {
    this.longitude = lng;
  }

  public Double getLongitude() {
    return this.longitude;
  }

  public Boolean getIsSubscribed() {
    return this.isSubscribed;
  }

  public void setIsSubscribed(Boolean isSubscribed) {
    this.isSubscribed = isSubscribed;
  }

}