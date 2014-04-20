package com.mordor.creepme;

import java.util.Date;
import java.util.UUID;

import android.media.Image;

public class Creep {
	private String mName;
	private String mNumber;
	private Image mProfilePic;
	private long mFollowTime;
	private long mTimeRemaining;
	private final UUID mId;
	private Date mDateMade;
	private Date mDateStarted;

	public Creep() {
		mId = UUID.randomUUID();
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
		mTimeRemaining = now.getTime() - mDateStarted.getTime();
		return mTimeRemaining;
	}

	public Image getProfilePic() {
		return mProfilePic;
	}

	public void setProfilePic(Image pic) {
		mProfilePic = pic;
	}

	public UUID getId() {
		return mId;
	}

	public void setDateMade(Date date) {
		mDateMade = date;
	}

	public Date getDateMade() {
		return mDateMade;
	}

	public void setDateStarted(Date date) {
		mDateStarted = date;
	}

	public Date getDateStarted() {
		return mDateStarted;
	}
}
