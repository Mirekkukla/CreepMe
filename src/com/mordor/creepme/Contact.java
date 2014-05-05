package com.mordor.creepme;

import android.graphics.Bitmap;

public class Contact {
	private String name;
	private String number;
	private Bitmap pic;
	private String id;

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Bitmap getPic() {
		return this.pic;
	}

	public void setPic(Bitmap pic) {
		this.pic = pic;
	}

}
