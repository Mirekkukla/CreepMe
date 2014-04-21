package com.mordor.creepme;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
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
	private CountDownTimer counter;
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

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Do something
			}

		});

		holder.checkBox.setChecked(false);
		// holder.profilePic.setImageDrawable(creepData.get(position).getProfilePic());
		holder.name.setText(creepData.get(position).getName());
		// holder.timeLeft.setText(creepData.get(position).getFollowTime())
		tv = holder.timeLeft;
		counter = new MyCountDownTimer(creepData.get(position).getFollowTime(),
				1000);
		counter.start();
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

	public class MyCountDownTimer extends CountDownTimer {
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisToFinish) {
			int sec = (int) (millisToFinish / 1000) % 60;
			int min = (int) ((millisToFinish / (1000 * 60)) % 60);
			int hr = (int) ((millisToFinish / (1000 * 60 * 60)) % 24);
			String text = (Integer.toString(hr) + ":" + Integer.toString(min)
					+ ":" + Integer.toString(sec));
			tv.setText(text);
		}

		@Override
		public void onFinish() {
			// end creep
			tv.setText("--:--:--");
		}
	}
}
