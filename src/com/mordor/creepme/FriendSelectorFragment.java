package com.mordor.creepme;

import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FriendSelectorFragment extends Fragment {
	private static final int REQUEST_CONTACT = 2;

	private Creep mCreep;

	private Button mFriendButton;
	private Button mStartCreepButton;
	private EditText hrs;
	private EditText mins;

	/* Builds main fragment view for FriendSelector */
	@TargetApi(11)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		// Inflates fragment layout into View
		View v = inflater.inflate(R.layout.fragment_friend_selector, parent,
				false);

		// Check for compatibility, display home as up
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mCreep = new Creep();

		// Defines and wires up button linked to contacts app to choose friend
		mFriendButton = (Button)v.findViewById(R.id.friend_selectorButton);
		mFriendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(i, REQUEST_CONTACT);
			}
		});

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

				// Set profile pic
				// ImageView iv = (ImageView) v
				// .findViewById(R.id.selector_profilePic);
				// Drawable drawable = iv.getDrawable();
				// Bitmap bitmap =
				// Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				// drawable.getIntrinsicHeight(), Config.ARGB_8888);
				// mCreep.setProfilePic(bitmap);


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

	/* Uses contact list to find friend when mFriendButton pressed */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK) return;

		if (requestCode == REQUEST_CONTACT) {
			Uri contactUri = data.getData();
			String[] queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME };

			Cursor c = getActivity().getContentResolver().query(contactUri,
					queryFields, null, null, null);

			if(c.getCount() == 0) {
				c.close();
				return;
			}

			mCreep = new Creep();
			c.moveToFirst();
			String name = c.getString(0);
			mCreep.setName(name);
			// mCreep.setNumber(number)
			setCreepView(name);
			c.close();
		}
	}

	/* Changes friend name text from default to reflect contact choice */
	private void setCreepView(String text) {
		TextView textView = (TextView) getView().findViewById(
				R.id.friend_nameText);
		textView.setText(text);
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
