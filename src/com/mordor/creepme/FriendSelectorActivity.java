package com.mordor.creepme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.core.CloudBackendAsync;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;

public class FriendSelectorActivity extends Activity implements
    OnItemClickListener, OnItemSelectedListener {

	// Initialize AutoComplete variables
	private AutoCompleteTextView textView = null;
	private ArrayAdapter<String> adapter;
	
	// Initialize CloudBackendAsync
	private CloudBackendAsync cloudAsync;

	// Store contacts values in HashMap
	private Map<String, Contact> contactMap = new HashMap<String, Contact>();

	// Initialize general variables
	private Creep creep;
	private EditText hrs;
	private EditText mins;
	private TextView nameTextView;
	private ImageView profileImageView;

	// Initialize notification variables
	private NotificationManager myNotificationManager;
	private final int notificationId = 111;
	private int numMessages = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Inflates fragment layout into View
		setContentView(R.layout.activity_friend_selector);

		// Display home as up
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.creep = new Creep();
		
		// Initialize cloudAsync
		cloudAsync = new CloudBackendAsync(this.getApplication());

		// Initialize AutoCompleteTextView values
		this.textView = (AutoCompleteTextView) findViewById(R.id.to_numberAutoText);
		this.nameTextView = (TextView) findViewById(R.id.friend_nameText);

		// Initialize profile picture ImageView to default
		this.profileImageView = (ImageView) findViewById(R.id.selector_profileImage);
		this.profileImageView.setImageResource(R.drawable.profile_default);

		// Create adapter (will change to custom, to display phone # as well)
		this.adapter = new ArrayAdapter<String>(this,
		    android.R.layout.two_line_list_item, android.R.id.text1,
		    new ArrayList<String>());
		this.textView.setThreshold(1);

		// Set adapter to AutoCompleteTextView
		this.textView.setAdapter(this.adapter);
		this.textView.setOnItemSelectedListener(this);
		this.textView.setOnItemClickListener(this);

		// Read contact data and add data to ArrayAdapter
		// used by AutoCompleteTextView
		readContactData();

		// Set EditText variables
		hrs = (EditText) findViewById(R.id.hrsEditText);
		mins = (EditText) findViewById(R.id.minsEditText);
	}

	/* Reads phone contact name and phone number(s) */
	private void readContactData() {
		final String[] PROJECTION = new String[] {
		    ContactsContract.Contacts.DISPLAY_NAME,
		    ContactsContract.CommonDataKinds.Phone.NUMBER };

		ContentResolver cr = this.getBaseContext().getContentResolver();
		Cursor cursor = cr.query(
		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null,
		    null, null);
		if (cursor != null) {
			try {
				final int displayNameIndex = cursor
				    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				final int phoneIndex = cursor
				    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

				String name = "", phoneNumber = "";
				while (cursor.moveToNext()) {
					name = cursor.getString(displayNameIndex);
					phoneNumber = cursor.getString(phoneIndex);

					// If contact has multiple numbers, make an entry for each
					int j = 2;
					while (contactMap.get(name) != null) {
						// Differentiate between each entry's name
						name = name + " #" + Integer.toString(j);
					}
					
					// Update contact HashMap
					Contact contact = new Contact();
					contact.setName(name);
					contact.setNumber(phoneNumber);
					contactMap.put(name, contact);

					// Add contacts name to adapter
					this.adapter.add(name);
				}
			} finally {
				cursor.close();
			}
		}
	}

	private String formatNumber(String num) {	
		StringBuilder sb = new StringBuilder(num);
		
		//Reduce string to only digits
		for(int i = 0; i < sb.length(); i++) {
			if(!Character.isDigit(sb.charAt(i))) {
				sb.deleteCharAt(i);
				i--;
			}
		}
		
		// Remove 0's or 1's from the beginning of the number
		for(int j = 0; j < sb.length(); j++) {
			if(sb.charAt(j) == '0' || sb.charAt(j) == '1') {
				sb.deleteCharAt(j);
				j--;
			} else {
				break;
			}
		}
		
	  //Check length of number
		if(sb.length() != 10) {
			Toast.makeText(this, "Invalid contact number: " + sb.toString(), Toast.LENGTH_SHORT).show();
			return null;
		}

		return sb.toString();
  }

	/* Action taken when an auto-complete list item is selected */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
	    long arg3) {
		// Nothing
	}

	/* Action taken when no auto-complete list item is selected */
	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
		InputMethodManager imm = (InputMethodManager) this
		    .getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);

	}

	/* Action taken when an auto-complete list item is clicked */
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos,
	    long arg3) {
		// Get Array index value for selected name
		String name = adapterView.getItemAtPosition(pos).toString();
		Contact contact = contactMap.get(name);
		
		// Format phone number to 10-digit
		this.creep.setNumber(formatNumber(contact.getNumber()));
		if(this.creep.getNumber() == null) {
			contact = null;
		}
		
		// If name exists in HashMap, set as creep data
		if (contact != null) {
			this.creep.setName(name);
			setCreepView();
		}

		InputMethodManager imm = (InputMethodManager) this
		    .getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	/* Changes victim name, picture from default to reflect contact choice */
	private void setCreepView() {
		nameTextView.setText(this.creep.getName());
		if (creep.getProfilePic() != null) {
			// Victim has profile picture, change from default
			// this.profileImageView.setImageResource(R.drawable.profile_default);
		} else {
			creep.setProfilePic(((BitmapDrawable) profileImageView.getDrawable())
			    .getBitmap());
		}
	}

	/* Parses follow time from EditText hour/minute fields to milliseconds */
	private long parseFollowTime() {
		long time;
		int hours, minutes;

		// Get values from EditTexts - default 05:00:00
		// If text field is empty, value is 0
		if (!hrs.getText().toString().isEmpty()) {
			hours = Integer.parseInt(hrs.getText().toString());
		} else {
			hours = 0;
		}

		if (!mins.getText().toString().isEmpty()) {
			minutes = Integer.parseInt(mins.getText().toString());
		} else {
			minutes = 0;
		}

		time = ((hours * 60) + minutes) * 60 * 1000; // to milliseconds
		return time;
	}

	/* Builds the Activity Bar Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.friend_selector_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (NavUtils.getParentActivityName(this) != null) {
					NavUtils.navigateUpFromSameTask(this);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* Example of a notification */
	protected void displayNotification() {
		// Invoking the default notification service
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("New Creep Request");
		mBuilder.setContentText("Creep Request From Yourself");
		mBuilder.setTicker("New Creep Received!");
		mBuilder.setSmallIcon(R.drawable.ic_launcher);
		mBuilder.setAutoCancel(true);

		// Increase notification number every time a new notification arrives
		mBuilder.setNumber(numMessages++);

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.putExtra("notificationId", notificationId);

		// This ensures that navigating backward from the Activity leads out of the
		// app to Home page
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

		// Adds the back stack for the Intent
		stackBuilder.addParentStack(MainActivity.class);

		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
		    PendingIntent.FLAG_ONE_SHOT // can only be used once
		    );

		// start the activity when the user clicks the notification text
		mBuilder.setContentIntent(resultPendingIntent);
		myNotificationManager = (NotificationManager) this
		    .getSystemService(Context.NOTIFICATION_SERVICE);

		// pass the Notification object to the system
		myNotificationManager.notify(notificationId, mBuilder.build());
	}

	/* Action taken on Creep Them button click */
	public void sendCreep(View v) {
		// Set follow time, milliseconds
		creep.setDuration(parseFollowTime());

		// Set time made, milliseconds
		Date currDate = new Date();
		creep.setTimeMade(currDate.getTime());

		// Default to unchecked
		creep.setIsChecked(false);

		// You're the creeper
		creep.setIsByYou(true);

		// Creep has not yet started
		creep.setIsStarted(true);
		Date now = new Date();
		creep.setTimeStarted((Long)now.getTime());
		creep.setIsComplete(false);

		// Set GPS default to not enabled
		creep.setGpsEnabled(false);

		if (creep.getName() == null) {
			Toast.makeText(this, "Gotta choose creep victim first!",
			    Toast.LENGTH_SHORT).show();
			return;
		}

		if (parseFollowTime() == 0) {
			Toast.makeText(this, "Creep duration must be longer than 00:00",
			    Toast.LENGTH_SHORT).show();
			return;
		}

		/**
		 * query database - if phone number has app, send request, acknowledge
		 * request sent, and return to main. Request includes: phone number,
		 * requested duration
		 * 
		 * if phone number does not have app, ask if they want to send a text
		 * inviting them. If yes, send text and return to main.
		 */
		
		// Add creep to cloud
		String creepCloudId = addCreepToCloud();
		creep.setCloudId(creepCloudId);

		// Add creep to sLab
		MainActivity.sLab.addCreep(creep);
		
		// Send push notification to victim
		pushToVictim();
	
		Intent i = new Intent(this, MainActivity.class);
		i.putExtra("source", "FriendSelectorActivity");
		i.putExtra("creepCloudId", creepCloudId);
		i.putExtra("creepId", creep.getId());
		this.startActivity(i);
	}

	private void pushToVictim() {
	  // TODO Auto-generated method stub
	  
  }
	
	private String addCreepToCloud() {
		// Create a response handler that will receive the result or an error
		CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
		  @Override
		  public void onComplete(CloudEntity results) {
		  	// It worked
		  }
					
			@Override
			public void onError(final IOException exception) {
				Log.e("ERROR", "Exception error updating CloudEntity, FSA");
			}
		};
		
		// Set current time Long
		Date now = new Date();
		Long currT = now.getTime();
		
		CloudEntity ce = new CloudEntity("Creep");
		ce.put("creeper", MainActivity.sPhoneNumber);
		ce.put("victim", creep.getNumber());
		ce.put("duration", Long.toString(creep.getDuration()));
		ce.put("time_started", Long.toString(currT));
		ce.put("is_started", creep.getIsStarted());
		ce.put("creep_uuid", UUID.randomUUID().toString());
		cloudAsync.insert(ce, handler);
		return (String)ce.get("creep_uuid");
  }
}
