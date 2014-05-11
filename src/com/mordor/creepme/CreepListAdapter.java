package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
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

		final Creep current = this.creepData.get(position);
		CreepHolder holder = null;

		if (convertView == null) {
			LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);

			holder = new CreepHolder();

			convertView.setClickable(true);
			convertView.setFocusable(true);

			holder.checkBox = (CheckBox) convertView.findViewById(R.id.cancelBox);
			holder.profilePic = (ImageView) convertView
			    .findViewById(R.id.profile_picImage);
			holder.name = (TextView) convertView.findViewById(R.id.friend_nameText);
			holder.timeLeft = (TextView) convertView
			    .findViewById(R.id.follow_timeText);
			holder.gps = (ImageView) convertView.findViewById(R.id.gps_enabledImage);

			convertView.setTag(holder);

		} else {
			holder = (CreepHolder) convertView.getTag();
		}

		// Handle clicking on ListView item
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check for GPS enabled
				final LocationManager manager = (LocationManager) context
				    .getSystemService(Context.LOCATION_SERVICE);
				if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					buildAlertMessageNoGps();
					return;
				}

				ArrayList<UUID> victim = new ArrayList<UUID>();
				victim.add(current.getId());
				Intent i = new Intent(context, CreepMapActivity.class);
				i.putExtra("victimsList", victim);
				context.startActivity(i);
			}

		});

		// Handle CheckBox actions, states
		holder.checkBox.setChecked(current.getIsChecked());
		holder.checkBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				current.setIsChecked(!current.getIsChecked());
			}
		});

		// Set profile picture ImageView
		if (current.getProfilePic() != null) {
			holder.profilePic.setImageBitmap(current.getProfilePic());
		} else {
			holder.profilePic.setImageResource(R.drawable.profile_default);
		}

		// Set name TextView
		holder.name.setText(current.getName());

		// Update time remaining TextView
		this.tv = holder.timeLeft;
		Date currT = new Date();
		if (!current.getIsStarted()) {
			current.setIsStarted(true);
			current.setTimeStarted(currT.getTime());
		}
		if (this.tv != null) {
			long millisToFinish = current.getFollowTime()
			    - (currT.getTime() - current.getTimeStarted());
			if (millisToFinish > 0) {
				// Convert millisToFinish to readable string
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
				this.tv.setText(text);
			} else {
				// Creep is complete
				current.setIsComplete(true);
				this.tv.setText("--:--:--");
			}
		}

		// Set GPS enabled ImageView
		if(current.gpsEnabled()) {
			// Their GPS is enabled
			holder.gps.setImageResource(R.drawable.gps_check);
		} else {
			// Their GPS is not enabled
			holder.gps.setImageResource(R.drawable.gps_x);
		}

		return convertView;
	}

	/* Builds GPS not enabled alert message and provides option to re-enable */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// Alert dialog blocks activity from running and opening map,
		// allows user to go direct to enable screen
		builder
		    .setMessage("Your GPS seems to be disabled, do you want to enable it?")
		    .setCancelable(false)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    context.startActivity(new Intent(
				        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			    }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    dialog.cancel();
			    }
		    });
		final AlertDialog alert = builder.create();
		alert.show();
	}

	/* Holds list element component views */
	static class CreepHolder {
		CheckBox checkBox;
		ImageView profilePic;
		TextView name;
		TextView timeLeft;
		ImageView gps;
	}
}
