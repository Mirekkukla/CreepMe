package com.mordor.creepme;

import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;

public class Creep {
	private String name;
	private String number;
	private Bitmap profilePic;
	private long followTime;
	private long timeRemaining;
	private final UUID id;
	private long timeMade;
	private long timeStarted;
	private Boolean isByYou;
	private Boolean isChecked;
	private Boolean isStarted;
	private Boolean isComplete;
	private Boolean gpsEnabled;
	private Double latitude;
	private Double longitude;

	/* Initializes UUID when creating a new Creep */
	public Creep() {
		this.id = UUID.randomUUID();
	}

	public Boolean isByYou() {
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

	public long getFollowTime() {
		return this.followTime;
	}

	public void setFollowTime(long time) {
		this.followTime = time;
	}

	/* Returns time remaining in milliseconds */
	public long getTimeRemaining() {
		Date now = new Date();
		this.timeRemaining = now.getTime() - this.timeStarted;
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

}