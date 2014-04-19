package com.mordor.creepme;

import java.util.UUID;

import android.media.Image;

public class Creep {
	private String mName;
	private String mNumber;
	private Image mProfilePic;
	private int mFollowTime;
	private int mTimeRemaining;
	private final UUID mId;

	public Creep() {
		mId = UUID.randomUUID();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public UUID getId() {
		return mId;
	}
}
