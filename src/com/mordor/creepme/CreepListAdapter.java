package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class CreepListAdapter extends ArrayAdapter<Creep> {
	private final Context context;
	private final int layoutResourceId;
	private final ArrayList<Creep> creepData;
	private TextView tv;

	public CreepListAdapter(Context context, int layoutResourceId,
			ArrayList<Creep> creepData) {
		super(context, layoutResourceId, creepData);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.creepData = creepData;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		CreepHolder holder = null;

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);

			holder = new CreepHolder();

			convertView.setClickable(true);
			convertView.setFocusable(true);

			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.cancelBox);
			holder.profilePic = (ImageView) convertView
					.findViewById(R.id.profile_picImage);
			holder.name = (TextView) convertView
					.findViewById(R.id.friend_nameText);
			holder.timeLeft = (TextView) convertView
					.findViewById(R.id.follow_time);
			holder.gps = (ImageView) convertView
					.findViewById(R.id.gps_enabledImage);

			convertView.setTag(holder);

		} else {
			holder = (CreepHolder) convertView.getTag();
		}

		// Handle clicking on ListView item
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (creepData.get(position).isByYou()) {
					Intent i = new Intent(context, CreepMapActivity.class);
					i.putExtra("lat", creepData.get(position).getLatitude());
					i.putExtra("lon", creepData.get(position).getLongitude());
					i.putExtra("name", creepData.get(position).getName());
					context.startActivity(i);
				} else {
					Intent i = new Intent(context, CreepMapActivity.class);
					i.putExtra("lat", creepData.get(position).getLatitude());
					i.putExtra("lon", creepData.get(position).getLongitude());
					i.putExtra("name", creepData.get(position).getName());
					context.startActivity(i);
				}

			}

		});

		// Handle CheckBox actions, states
		holder.checkBox.setChecked(creepData.get(position).getIsChecked());
		holder.checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				creepData.get(position).setIsChecked(!creepData.get(position).getIsChecked());
			}
		});

		// holder.profilePic.setImageDrawable(creepData.get(position).getProfilePic());
		holder.name.setText(creepData.get(position).getName());

		// Update creep time left
		tv = holder.timeLeft;
		Date currT = new Date();
		if (!creepData.get(position).getIsStarted()) {
			creepData.get(position).setIsStarted(true);
			creepData.get(position).setTimeStarted(currT.getTime());
		}
		if (tv != null) {
			long millisToFinish = creepData.get(position).getFollowTime()
					- (currT.getTime() - creepData.get(position)
							.getTimeStarted());
			if (millisToFinish > 0) {
				int sec = (int) (millisToFinish / 1000) % 60;
				int min = (int) ((millisToFinish / (1000 * 60)) % 60);
				int hr = (int) ((millisToFinish / (1000 * 60 * 60)) % 24);
				String seconds = Integer.toString(sec);
				String minutes = Integer.toString(min);
				String hours = Integer.toString(hr);
				if (seconds.length() < 2)
					seconds = "0" + seconds;
				if (minutes.length() < 2)
					minutes = "0" + minutes;

				String text = (hours + ":" + minutes + ":" + seconds);
				tv.setText(text);
			} else {
				creepData.get(position).setIsComplete(true);
				tv.setText("--:--:--");
			}
		}

		// holder.gps.setImageDrawable(creepData.get(position).get)

		return convertView;
	}

	static class CreepHolder {
		CheckBox checkBox;
		ImageView profilePic;
		TextView name;
		TextView timeLeft;
		ImageView gps;
	}
}
