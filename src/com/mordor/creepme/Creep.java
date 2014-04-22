package com.mordor.creepme;

import java.util.Date;
import java.util.UUID;

import android.graphics.Bitmap;

public class Creep {
	private String mName;
	private String mNumber;
	private Bitmap mProfilePic;
	private long mFollowTime;
	private long mTimeRemaining;
	private final UUID mId;
	private long mTimeMade;
	private long mTimeStarted;
	public Boolean mIsByYou;
	public Boolean mIsChecked;

	public Creep() {
		mId = UUID.randomUUID();
	}

	public Boolean isByYou() {
		return mIsByYou;
	}

	public void setByYou(Boolean bool) {
		mIsByYou = bool;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String number) {
		mNumber = number;
	}

	public long getFollowTime() {
		return mFollowTime;
	}

	public void setFollowTime(long time) {
		mFollowTime = time;
	}

	/**
	 * Returns follow time remaining in milliseconds
	 */
	public long getTimeRemaining() {
		Date now = new Date();
		mTimeRemaining = now.getTime() - mTimeStarted;
		return mTimeRemaining;
	}

	public Bitmap getProfilePic() {
		return mProfilePic;
	}

	public void setProfilePic(Bitmap pic) {
		mProfilePic = pic;
	}

	public UUID getId() {
		return mId;
	}

	public void setTimeMade(long time) {
		mTimeMade = time;
	}

	public long getTimeMade() {
		return mTimeMade;
	}

	public long getTimeStarted() {
		return mTimeStarted;
	}

	public void setTimeStarted(long time) {
		mTimeStarted = time;
	}

	public void setIsChecked(Boolean bool) {
		mIsChecked = bool;
	}

	public Boolean getIsChecked() {
		return mIsChecked;
	}
}
