package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FriendSelectorFragment extends Fragment implements
    OnItemClickListener, OnItemSelectedListener {

	// Initialize AutoComplete variables
	private AutoCompleteTextView textView = null;
	private ArrayAdapter<String> adapter;

	// Store contacts values in HashMap
	public Map<String, Contact> contactMap = new HashMap<String, Contact>();

	private Creep creep;

	private Button startCreepButton;
	private EditText hrs;
	private EditText mins;
	private TextView nameTextView;
	private ImageView profileImageView;

	// Builds main fragment view for FriendSelector
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
	    Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// Inflates fragment layout into View
		View v = inflater.inflate(R.layout.fragment_friend_selector, parent, false);

		// Display home as up
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		this.creep = new Creep();

		// Initialize AutoCompleteTextView values
		this.textView = (AutoCompleteTextView) v
		    .findViewById(R.id.to_numberAutoText);
		this.nameTextView = (TextView) v.findViewById(R.id.friend_nameText);

		// Initialize profile picture ImageView to default
		this.profileImageView = (ImageView) v
		    .findViewById(R.id.selector_profileImage);
		this.profileImageView.setImageResource(R.drawable.profile_default);

		// Create adapter (will change to custom, to display phone # as well)
		this.adapter = new ArrayAdapter<String>(getActivity(),
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
		hrs = (EditText) v.findViewById(R.id.hrsEditText);
		mins = (EditText) v.findViewById(R.id.minsEditText);

		// Defines and wires up button to set creep and begin verification
		startCreepButton = (Button) v.findViewById(R.id.selector_finalButton);
		startCreepButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Set follow time, milliseconds
				creep.setFollowTime(parseFollowTime(v));

				// Set time made, milliseconds
				Date currDate = new Date();
				creep.setTimeMade(currDate.getTime());

				// Default to unchecked
				creep.setIsChecked(false);

				// You're the creeper
				creep.setIsByYou(true);

				// Creep has not yet started
				creep.setIsStarted(false);
				creep.setIsComplete(false);

				// Set GPS default to not enabled
				creep.setGpsEnabled(false);

				if (creep.getName() == null) {
					Toast.makeText(getActivity(), "Gotta choose creep victim first!",
					    Toast.LENGTH_SHORT).show();
					return;
				}

				if (parseFollowTime(v) == 0) {
					Toast.makeText(getActivity(),
					    "Creep duration must be longer than 00:00",
					    Toast.LENGTH_SHORT).show();
					return;
				}

				MainActivity.sLab.addCreep(creep);

				Intent i = new Intent(getActivity(), MainActivity.class);
				getActivity().startActivity(i);
				/**
				 * query database - if phone number has app, send request, acknowledge
				 * request sent, and return to main.
				 *
				 * if phone number does not have app, ask if they want to send a text
				 * inviting them. If yes, send text, store pending request for set
				 * amount of time, and
				 */
			}
		});

		return v;
	}

	// Read phone contact name and phone number(s)
	private void readContactData() {
		final String[] PROJECTION = new String[] {
		    ContactsContract.Contacts.DISPLAY_NAME,
		    ContactsContract.CommonDataKinds.Phone.NUMBER };

		ContentResolver cr = getActivity().getBaseContext().getContentResolver();
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

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
	    long arg3) {
		// Nothing
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
		InputMethodManager imm = (InputMethodManager) getActivity()
		    .getSystemService(getActivity().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
		    .getWindowToken(), 0);

	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos,
	    long arg3) {
		// Get Array index value for selected name
		String name = adapterView.getItemAtPosition(pos).toString();
		Contact contact = contactMap.get(name);

		// If name exists in HashMap, set as creep data
		if (contact != null) {
			this.creep.setName(name);
			this.creep.setNumber(contact.getNumber());
			setCreepView();
		}

		InputMethodManager imm = (InputMethodManager) getActivity()
		    .getSystemService(getActivity().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
		    .getWindowToken(), 0);
	}

	// Changes victim name, picture from default to reflect contact choice
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

	private long parseFollowTime(View v) {
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

		time = ((hours * 60) + minutes) * 60 * 1000; // to ms
		return time;
	}

	// Builds the Activity Bar Menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.friend_selector_options, menu);
	}

	// Deals with Activity Bar and Menu item selections
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (NavUtils.getParentActivityName(getActivity()) != null) {
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
