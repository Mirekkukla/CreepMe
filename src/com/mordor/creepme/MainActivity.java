package com.mordor.creepme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.cloud.backend.core.CloudBackendAsync;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Filter;

public class MainActivity extends Activity {
	private static final String TAG = "com.mordor.creepme.MainActivity";
	public static CreepLab sLab;
	public static String sPhoneNumber;
	private CreepListAdapter adp1;
	private CreepListAdapter adp2;
	private Handler timerHandler;
	private final int timeInterval = 1000; // Update interval, milliseconds
	public int refreshInterval; // Creep info refresh, milliseconds
	private int refreshTime;
	private Map<String, Contact> contactMap;

	// CloudBackendAsync variable
	private CloudBackendAsync cloudAsync;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

    // Initialize CloudBackendAsync
    cloudAsync = new CloudBackendAsync(this.getApplication());
		
		// Initialize contactMap
	  contactMap = new HashMap<String, Contact>();
	  
	  // Get user's phone number
    TelephonyManager telManager = (TelephonyManager) this
        .getSystemService(Context.TELEPHONY_SERVICE);
    sPhoneNumber = telManager.getLine1Number();
    //sPhoneNumber = "3076904811";
    
    // Get contact data
    readContactData();
		
	  if(sLab == null) {
	      
	    // Set session's creep manager
	    sLab = CreepLab.get(this);
	      
	    // Retrieve Creeps from Cloud
	    getCloudCreeps();
	  }

		// Initialize timer handler
		timerHandler = new Handler();
		refreshTime = 0;
		refreshInterval = 60; // Will come from settings page

		// Define ListViews locally, linked to layout
		ListView lv1 = (ListView) findViewById(R.id.who_you_creepingList);
		ListView lv2 = (ListView) findViewById(R.id.who_creeping_youList);

		// Assign adapters
		this.adp1 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(true));
		this.adp2 = new CreepListAdapter(this, R.layout.creep_list_element,
		    sLab.getCreeps(false));

		// Set custom array adapter to display list items in each ListView
		lv1.setAdapter(this.adp1);
		lv2.setAdapter(this.adp2);
		
		adp1.notifyDataSetChanged();
		adp2.notifyDataSetChanged();
	}

	private void getCloudCreeps() {
		CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
			@SuppressWarnings("unchecked")
      @Override
      public void onComplete(List<CloudEntity> results) {
			  Log.i("Comments", results.size() + " Creeps found in Cloud");
        if(results.size() > 0) {
        	// Creeps are out there...
        	for(int i = 0; i < results.size(); i++) {
        		// Add each creep to lab
        		CloudEntity ce = results.get(i);
        		Creep c = new Creep();
        		c.setDuration(Long.parseLong(((String)ce.get("duration"))));
        		if(((String)ce.get("creeper")).equals(sPhoneNumber)) {
        			c.setIsByYou(true);
        		} else {
        			c.setIsByYou(false);
        		}
        		c.setNumber((String)ce.get("victim"));
        		c.setIsStarted((Boolean)ce.get("is_started"));
        		Log.i("Comments", "is started: " + c.getIsStarted().toString());
        		c.setTimeStarted(Long.parseLong(((String)ce.get("time_started"))));
        		c.setIsChecked(false);
        		c.setGpsEnabled(false);
        		c.setCloudId((String)ce.get("creep_uuid"));
        		
        		// TODO Subscribe to location updates from other number
        	
        		if(c.getTimeRemaining() > 0) {
        		  Log.i("Comments", "Adding creep...");
        			// Creep is still valid
        			c.setIsComplete(false);
        			if(contactMap.get(c.getNumber()).getName() != null) {
        			  c.setName((contactMap.get(c.getNumber())).getName());
        			} else {
        			  c.setName("Unknown number");
        			}
        			sLab.addCreep(c);
        			trackCreepChanges((String)ce.get("creep_uuid"), c.getId());
        		} else {
        		  Log.i("Comments", "removing complete creep...");
        			// Creep is complete
        			@SuppressWarnings("rawtypes")
              CloudCallbackHandler removalHandler = new CloudCallbackHandler() {
								
  							@Override
  							public void onError(final IOException exception) {
  								Log.e("ERROR", "Exception error deleting CloudEntity, MA");
  							}

  							@Override
                public void onComplete(Object results) {
  	              // Do nothing - Creep has been deleted
                }
  						};
  						// Remove CloudEntity from Cloud
  						cloudAsync.delete(ce, removalHandler);
        		}
        	}
        } else {
        	Log.i("Comments", "No existing creeps");
        }
      }
			
			@Override
			public void onError(final IOException exception) {
				Log.e("ERROR", "Exception error updating CloudEntity, MA");
			}
		};
		CloudQuery cq2 = new CloudQuery("Creep");
		//cq2.setFilter(Filter.or(Filter.eq("creeper", sPhoneNumber), Filter.eq("victim", sPhoneNumber)));
		cloudAsync.list(cq2, handler);
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
					// Format phone number to 10-digit
					phoneNumber = formatNumber(phoneNumber);
					
					// If number is valid
					if(phoneNumber != null) {						
						// Update contact HashMap
						Contact contact = new Contact();
						contact.setName(name);
						contact.setNumber(phoneNumber);
						contactMap.put(phoneNumber, contact);
					}					
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
			return null;
		}
		return sb.toString();
  }

	/* Action taken on Add New Creep Button click */
	public void newFriendSelector(View v) {
		// Defines intent for new creep activity
		Intent i = new Intent(this, FriendSelectorActivity.class);

		// Opens new creep activity
		startActivity(i);
	}

	/* Action taken on Cancel All Selections Button click */
	public void cancelSelections(View v) {
		try {
		  Boolean removed = false;
		  for(int i = 0; i < sLab.creepsOnYou.size(); i++) {
		    if(sLab.creepsOnYou.get(i).getIsChecked()) {
		      deleteFromCloud(sLab.creepsOnYou.get(i));
		      sLab.removeCreep(sLab.creepsOnYou.get(i));
		      removed = true;
		      i--;
		    }
		  }
		  for(int i = 0; i < sLab.creepsByYou.size(); i++) {
		    if(sLab.creepsByYou.get(i).getIsChecked()) {
          deleteFromCloud(sLab.creepsByYou.get(i));
          sLab.removeCreep(sLab.creepsByYou.get(i));
          removed = true;
          i--;
        }
		  }
		  
			// If nothing gets removed, nothing was selected
			if (!removed) {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at cancelSelections()");
		}

		// Update listView elements
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();
	}

	/* Action taken on Map All Selections Button click */
	public void mapSelections(View v) {
		// Check for GPS enabled
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Check for GPS enabled. If not...
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			// Dialog and no map
			buildAlertMessageNoGps();
		}

		try {
			ArrayList<UUID> selections = sLab.selectedCreeps();
			if (selections.size() != 0) {
				for(int j = 0; j < selections.size(); j++) {
					if(sLab.getCreep(selections.get(j)).getIsStarted() == false) {
						selections.remove(j);
						j--;
					}
				}
				if(selections.size() == 0) {
					Toast.makeText(this, "Pending Creeps cannot be mapped", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent i = new Intent(this, CreepMapActivity.class);
				i.putExtra("victimsList", selections);
				this.startActivity(i);
			} else {
				Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception error at mapSelections()");
		}
	}

	/* Implements timer to update listView elements */
	Runnable listViewUpdater = new Runnable() {
		@Override
		public void run() {
			sLab.checkForCompletions();

			// Check whether to refresh UI
			if (refreshTime == 0 || refreshTime == refreshInterval) {
				if (refreshTime == 0) {
					// Refresh UI on 0
				}
				if (refreshTime == refreshInterval) {
					// Reached refresh interval - reset
					refreshTime = -1;
				}
			}
			refreshTime++;

			// Update UI
			adp1.notifyDataSetChanged();
			adp2.notifyDataSetChanged();

			timerHandler.postDelayed(listViewUpdater, timeInterval);
		}
	};

	/* Starts listView update timer */
	private void startTimer() {
		listViewUpdater.run();
	}

	/* Stops listView update timer */
	private void stopTimer() {
		this.timerHandler.removeCallbacks(listViewUpdater);
	}

	private void trackCreepChanges(String cloudId, final UUID creepId) {
		// Create a response handler that will receive the result or an error
		CloudCallbackHandler<List<CloudEntity>> trackHandler = new CloudCallbackHandler<List<CloudEntity>>() {
			@Override
			public void onComplete(List<CloudEntity> results) {
				Log.i("Comments", "Update from Cloud - results size: " + results.size());
				Creep localCreep = sLab.getCreep(creepId);
				if(results.size() > 0) {
					// Creep has been altered - update aspects from cloud
					Log.i("Comments", "Updating from cloud");
					CloudEntity ce = results.get(0);
					localCreep.setDuration(Long.parseLong(((String)ce.get("duration"))));
					localCreep.setTimeStarted(Long.parseLong(((String)ce.get("time_started"))));
					localCreep.setIsStarted((Boolean)ce.get("is_started"));
				} else {
				  Log.i("Comments", "Creep not found in cloud");
				}
			}
						
			@Override
			public void onError(final IOException exception) {
				Log.e("ERROR", "Exception error updating CloudEntity, MA");
			}
		};
	 
		CloudQuery cq = new CloudQuery("Creep");
		cq.setFilter(Filter.eq("creep_uuid", cloudId));
		cq.setScope(Scope.FUTURE_AND_PAST);
		cloudAsync.list(cq, trackHandler);
  }
	
	/* Builds GPS not enabled alert message and provides option to re-enable */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Alert dialog blocks activity from running and opening map,
		// allows user to go direct to enable screen
		builder
		    .setMessage("Your GPS seems to be disabled, do you want to enable it?")
		    .setCancelable(false)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(final DialogInterface dialog, final int id) {
				    startActivity(new Intent(
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

	/* Action taken when activity is resumed */
	@Override
	public void onResume() {
		super.onResume();
		if(getIntent().getExtras() != null) {
		  if(((String)getIntent().getExtras().get("source")).equals("FriendSelectorActivity")) {
		    trackCreepChanges((String)getIntent().getExtras().get("creepCloudId"), (UUID)getIntent().getExtras().get("creepId"));
		  }
		}
		// Update lists on activity resume
		this.adp1.notifyDataSetChanged();
		this.adp2.notifyDataSetChanged();

		// If there's an instance of CreepMapActivity open, kill it
		if (CreepMapActivity.getInstance() != null) {
			CreepMapActivity.getInstance().finish();
		}

		// ListView update timer restarted
		startTimer();
	}

	/* Action taken when activity is paused or destroyed */
	@Override
	public void onPause() {
		stopTimer();
		super.onPause();
	}

	/* Builds the Activity Bar Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.main_options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Deals with Activity Bar and Menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View v = findViewById(android.R.id.content);
		switch (item.getItemId()) {
			case R.id.action_map_selections:
				// Opens new map activity with selected creeps mapped
				mapSelections(v);
				return true;
			case R.id.action_delete_selections:
				// Cancels selected creeps
				cancelSelections(v);
				return true;
			case R.id.action_add_creep:
				// Opens new creep activity
				newFriendSelector(v);
				return true;
			case R.id.action_settings:
				// Open settings activity
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void deleteFromCloud(Creep c) {
	  // Create a response handler that will receive the result or an error
    CloudCallbackHandler<List<CloudEntity>> trackHandler = new CloudCallbackHandler<List<CloudEntity>>() {
      @SuppressWarnings("unchecked")
      @Override
      public void onComplete(List<CloudEntity> results) {
        if(results.size() > 0) {
          @SuppressWarnings("rawtypes")
          CloudCallbackHandler handler = new CloudCallbackHandler() {
                  
            @Override
            public void onError(final IOException exception) {
              Log.e("ERROR", "Exception error deleting CloudEntity, MA");
            }

            @Override
            public void onComplete(Object results) {
              // Auto-generated method stub
            }
          };
          cloudAsync.delete(results.get(0), handler);
        } else {
          Log.i("Comments", "Couldn't find specified CloudEntity");
        }
      }
            
      @Override
      public void onError(final IOException exception) {
        Log.e("ERROR", "Exception error finding CloudEntity, MA");
      }
    };
   
    CloudQuery cq = new CloudQuery("Creep");
    cq.setScope(Scope.PAST);
    cq.setFilter(Filter.eq("creep_uuid", c.getCloudId()));
    cloudAsync.list(cq, trackHandler);
	}
	
	public void eraseCloud(View v) {
	// Create a response handler that will receive the result or an error
			CloudCallbackHandler<List<CloudEntity>> trackHandler = new CloudCallbackHandler<List<CloudEntity>>() {
				@SuppressWarnings("unchecked")
        @Override
				public void onComplete(List<CloudEntity> results) {
					if(results.size() > 0) {
						@SuppressWarnings("rawtypes")
            CloudCallbackHandler handler = new CloudCallbackHandler() {
										
							@Override
							public void onError(final IOException exception) {
								Log.e("ERROR", "Exception error updating CloudEntity, MA");
							}

							@Override
              public void onComplete(Object results) {
	              // Auto-generated method stub
	              
              }
						};
						Log.i("Comments", results.size() + " Entries to be erased");
						cloudAsync.delete(results.get(0), handler);
					} else {
						Log.i("Comments", "Nothing to be erased");
					}
				}
							
				@Override
				public void onError(final IOException exception) {
					Log.e("ERROR", "Exception error updating CloudEntity, MA");
				}
			};
		 
			CloudQuery cq = new CloudQuery("Creep");
			cq.setScope(Scope.PAST);
			cloudAsync.list(cq, trackHandler);
	}
}
