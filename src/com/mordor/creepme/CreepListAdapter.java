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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CreepListAdapter extends
    ArrayAdapter<Creep> {
  private final Context context;
  private final int layoutResourceId;
  private final ArrayList<Creep> creepData;
  private TextView tv;

  public CreepListAdapter(Context context, int layoutResourceId, ArrayList<Creep> creepData) {
    super(context, layoutResourceId, creepData);
    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.creepData = creepData;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final Creep c = this.creepData.get(position);
    CreepHolder holder = null;

    if (convertView == null) {
      LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
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
          .findViewById(R.id.follow_timeText);
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
        // Check for GPS enabled
        final LocationManager manager = (LocationManager) context
            .getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
          buildAlertMessageNoGps();
          return;
        }
        if (c.getIsStarted() == true) {
          // Open map with creep location
          ArrayList<UUID> victim = new ArrayList<UUID>();
          victim.add(c.getId());
          Intent i = new Intent(context, CreepMapActivity.class);
          i.putExtra("victimsList", victim);
          context.startActivity(i);
        } else if(c.getIsByYou()) {
          Toast.makeText(context, "Pending Creeps cannot be mapped", Toast.LENGTH_SHORT).show();
          return;
        } else {
          // Is Pending creep on you, waiting for response
          buildAlertMessageCreepPending(c);
        }
      }
    });

    // Handle CheckBox actions, states
    holder.checkBox.setChecked(c.getIsChecked());
    holder.checkBox.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        c.setIsChecked(!c.getIsChecked());
      }
    });

    // Set profile picture ImageView
    if (c.getProfilePic() != null) {
      holder.profilePic.setImageBitmap(c.getProfilePic());
    } else {
      holder.profilePic.setImageResource(R.drawable.profile_default);
    }

    // Set name TextView
    holder.name.setText(c.getName());

    // Update time remaining TextView
    this.tv = holder.timeLeft;

    if (this.tv != null) {
      String text = "pending";
      long millisToFinish = c.getTimeRemaining();
      if (c.getIsStarted() == true) {
        if (millisToFinish > 0) {
          // Convert millisToFinish to readable string
          int sec = (int) (millisToFinish / 1000) % 60;
          int min = (int) ((millisToFinish / (1000 * 60)) % 60);
          int hr = (int) ((millisToFinish / (1000 * 60 * 60)) % 24);
          String seconds = Integer.toString(sec);
          String minutes = Integer.toString(min);
          String hours = Integer.toString(hr);
          
          if (seconds.length() < 2) {
            seconds = "0" + seconds;
          }
          
          if (minutes.length() < 2) {
            minutes = "0" + minutes;
          }
          
          text = (hours + ":" + minutes + ":" + seconds);
        } else {
          // Creep is complete
          c.setIsComplete(true);
          text = "--:--:--";
        }
      }
      this.tv.setText(text);
    }

    // Set GPS enabled ImageView
    if (c.gpsEnabled()) {
      holder.gps.setImageResource(R.drawable.gps_check_dark);
    } else {
      holder.gps.setImageResource(R.drawable.gps_x_dark);
    }

    return convertView;
  }
  
  /* Builds Pending Creep Yes/No dialog with option to change duration */
  private void buildAlertMessageCreepPending(final Creep c) {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    LayoutInflater inf = LayoutInflater.from(context);
    final View durationEditView = inf.inflate(R.layout.duration_editor, null);
    
    final EditText hourEditor = (EditText) durationEditView.findViewById(R.id.edit_hrEditText);
    final EditText minEditor = (EditText) durationEditView.findViewById(R.id.edit_minEditText);
    
    String min = Integer.toString((int) ((c.getDuration() / (1000 * 60)) % 60));
    if(min.length() < 2) {
      min = "0" + min;
    }
    String hr = Integer.toString((int) ((c.getDuration() / (1000 * 60 * 60)) % 24));
    if(hr.length() < 2) {
      hr = "0" + hr;
    }
    
    hourEditor.setText(hr, TextView.BufferType.EDITABLE);
    minEditor.setText(min, TextView.BufferType.EDITABLE);
    
    builder
      .setTitle("Pending Creep from " + c.getName())
      .setMessage(c.getName() + " wants to creep you!  Do you allow this?")
      .setCancelable(false)
      .setView(durationEditView)
      .setPositiveButton(
          "Yes",
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
              Long duration = Long.parseLong(hourEditor.getText().toString())*60*60*1000
                  + Long.parseLong(minEditor.getText().toString())*60*1000;
              if(duration == 0) {
                Toast.makeText(context, "Duration set to 00:00 - cancelling creep", Toast.LENGTH_SHORT).show();
              }
              c.setDuration(duration); 
              c.setIsStarted(true);
              c.setTimeStarted((new Date()).getTime());
              MainActivity.sLab.addUpdatedCreep(c);
              /**
               * TODO Send push notification to creeper that their creep has been acceptedF
               */
              dialog.cancel();
            }
          })
      .setNegativeButton(
          "No", 
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
              MainActivity.sLab.removeCreep(c);
              dialog.cancel();
            }
          });
    final AlertDialog alert = builder.create();
    alert.show();
  }

  /* Builds GPS not enabled alert message and provides option to re-enable */
  private void buildAlertMessageNoGps() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    // Alert dialog blocks activity from running and opening map,
    // allows user to go direct to enable screen
    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
        .setCancelable(false)
        .setPositiveButton(
            "Yes",
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int id) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
              }
            })
        .setNegativeButton(
            "No",
            new DialogInterface.OnClickListener() {
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
