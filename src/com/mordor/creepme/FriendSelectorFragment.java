package com.mordor.creepme;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
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
	AutoCompleteTextView textView = null;
	private ArrayAdapter<String> adapter;

	// Store contacts values in HashMap
	public Map<String, Contact> contactMap = new HashMap<String, Contact>();
	private static ContentResolver cr;

	EditText toNumber = null;
	String toNumberValue = "";

	private Creep mCreep;

	private Button mStartCreepButton;
	private EditText hrs;
	private EditText mins;
	private TextView nameTextView;
	private ImageView profileImageView;

	/* Builds main fragment view for FriendSelector */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// Inflates fragment layout into View
		View v = inflater.inflate(R.layout.fragment_friend_selector, parent,
				false);

		// Display home as up
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		mCreep = new Creep();

		// Initialize AutoCompleteTextView values

		textView = (AutoCompleteTextView) v.findViewById(R.id.toNumber);
		nameTextView = (TextView) v.findViewById(R.id.friend_nameText);

		// Create adapter (will change to custom, to display phone # as well)
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.two_line_list_item, android.R.id.text1,
				new ArrayList<String>());
		textView.setThreshold(1);

		// Set adapter to AutoCompleteTextView
		textView.setAdapter(adapter);
		textView.setOnItemSelectedListener(this);
		textView.setOnItemClickListener(this);

		// Read contact data and add data to ArrayAdapter
		// used by AutoCompleteTextView
		readContactData();

		hrs = (EditText) v.findViewById(R.id.hrs);
		mins = (EditText) v.findViewById(R.id.mins);

		// Defines and wires up button to set creep and begin verification
		mStartCreepButton = (Button) v.findViewById(R.id.selector_finalButton);
		mStartCreepButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// Set follow time, milliseconds
				mCreep.setFollowTime(parseFollowTime(v));

				// Set time made, milliseconds
				Date currDate = new Date();
				mCreep.setTimeMade(currDate.getTime());

				// Default to unchecked
				mCreep.setIsChecked(false);

				// You're the creep
				mCreep.setByYou(true);

				// Creep has not yet started
				mCreep.setIsStarted(false);
				mCreep.setIsComplete(false);

				if (mCreep.getName() == null) {
					Toast.makeText(getActivity(),
							"Please choose creep victim first!",
							Toast.LENGTH_SHORT).show();
					return;
				}

				MainActivity.sLab.addCreep(mCreep);

				Intent i = new Intent(getActivity(), MainActivity.class);
				getActivity().startActivity(i);
				/**
				 * query database - if phone number has app, send request,
				 * acknowledge request sent, and return to main.
				 *
				 * if phone number does not have app, ask if they want to send a
				 * text inviting them. If yes, send text, store pending request
				 * for set amount of time, and
				 */
			}
		});

		return v;
	}

	// Read phone contact name and phone numbers
	private void readContactData() {
		final String[] PROJECTION = new String[] {
		    ContactsContract.Contacts.PHOTO_ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER };

		cr = getActivity().getBaseContext().getContentResolver();
		Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
		if (cursor != null) {
		    try {
				// final int photoIndex = cursor
				// .getColumnIndex(ContactsContract.Contacts.PHOTO_ID);
				final int displayNameIndex = cursor
				    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				final int phoneIndex = cursor
				    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

				String name = "", phoneNumber = "";
				// Long id;
				while (cursor.moveToNext()) {
					// id = cursor.getLong(photoIndex);
					name = cursor.getString(displayNameIndex);
					phoneNumber = cursor.getString(phoneIndex);

					// If contact has multiple numbers, make an entry for each
					int j = 2;
					while (contactMap.get(name) != null) {
						name = name + " #" + Integer.toString(j);
					}

					// Update contact HashMap
					Contact c = new Contact();
					// c.setId(id);
					c.setName(name);
					c.setNumber(phoneNumber);
					contactMap.put(name, c);

					// Add contacts name to adapter
					adapter.add(name);
				}
		    } finally {
		        cursor.close();
		    }
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// Blank
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(getActivity().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
				.getWindowToken(), 0);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Get Array index value for selected name
		String name = arg0.getItemAtPosition(arg2).toString();
		Contact c = contactMap.get(name);

		// If name exist in HashMap
		if (contactMap.get(name) != null) {
			setCreepView(name, c.getId());
			mCreep.setName(name);
			mCreep.setNumber(c.getNumber());
		}

		InputMethodManager imm = (InputMethodManager) getActivity()
		    .getSystemService(getActivity().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
		    .getWindowToken(), 0);
	}

	/* Changes friend name text from default to reflect contact choice */
	private void setCreepView(String name, Long id) {
		nameTextView.setText(name);
		// Figure out how to get contact photo, set as imageview
	}

	private long parseFollowTime(View v) {
		long time;
		int hours, minutes;

		// Get values from EditTexts - default 05:00:00
		hours = Integer.parseInt(hrs.getText().toString());
		minutes = Integer.parseInt(mins.getText().toString());

		time = ((hours * 60) + minutes) * 60 * 1000; // to ms
		return time;
	}

	/* Builds the Activity Bar Menu */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main_options, menu);
	}

	/* Deals with Activity Bar and Menu item selections */
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
