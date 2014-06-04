package com.mordor.creepme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
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

import com.google.cloud.backend.core.CloudBackendActivity;
import com.google.cloud.backend.core.CloudBackendMessaging;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Filter;

public class MainActivity extends CloudBackendActivity {
  
  /** CONSTANTS */
  /*UI update interval, milliseconds */
  private final int UPDATE_TIME_INTERVAL = 1000;
  /* Location update interval, milliseconds */
  private final int LOCATION_UPDATE_INTERVAL = 30000;
  /* Max location update interval allowed for up-to-date, milliseconds */
  private final int LOCATION_MAX_INTERVAL = LOCATION_UPDATE_INTERVAL * (11 / 10);
  /* MainActivity Log TAG */
  private static final String TAG = "com.mordor.creepme.MainActivity";
  
  /** MainActivity global variables */
  public static CreepLab sLab;
  public static String sPhoneNumber;
  
  /** MainActivity local variables */
  private CreepListAdapter adp1;
  private CreepListAdapter adp2;
  private Handler timerHandler;
  private int timeSinceLastUpdate;
  private Map<String, Contact> contactMap;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Initialize contactMap
    contactMap = new HashMap<String, Contact>();
    timeSinceLastUpdate = LOCATION_UPDATE_INTERVAL;

    // Get user's phone number
    TelephonyManager telManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    sPhoneNumber = telManager.getLine1Number();
    //sPhoneNumber = "3076904811"; // TODO Can change your number for testing

    // Get contact data
    readContactData();

    if (sLab == null) {
      // Set session's creep manager
      sLab = CreepLab.get(this);

      // Retrieve Creeps from Cloud
      getCloudCreeps();      
    }

    // Initialize timer handler
    timerHandler = new Handler();

    // Define ListViews locally, linked to layout
    ListView lv1 = (ListView) findViewById(R.id.who_you_creepingList);
    ListView lv2 = (ListView) findViewById(R.id.who_creeping_youList);

    // Assign adapters
    this.adp1 = new CreepListAdapter(this, R.layout.creep_list_element, sLab.getCreeps(true));
    this.adp2 = new CreepListAdapter(this, R.layout.creep_list_element, sLab.getCreeps(false));

    // Set custom array adapter to display list items in each ListView
    lv1.setAdapter(this.adp1);
    lv2.setAdapter(this.adp2);

    adp1.notifyDataSetChanged();
    adp2.notifyDataSetChanged();
  }

  private void getCloudCreeps() {
    CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
      @Override
      public void onComplete(List<CloudEntity> results) {
        Log.i("Comments", results.size() + " relevant creeps found in Cloud");
        if (results.size() > 0) {
          // Relevant creeps are out there...
          for (int i = 0; i < results.size(); i++) {
            // Add each creep to lab
            CloudEntity ce = results.get(i);
            Creep c = sLab.getCreep((String) ce.get("creep_uuid"));
            if(c == null) {
              c = new Creep();
              if (((String) ce.get("creeper")).equals(sPhoneNumber)) {
                c.setIsByYou(true);
              } else {
                c.setIsByYou(false);
              }
              c.setNumber((String) ce.get("victim"));
              c.setCloudId((String) ce.get("creep_uuid"));
              sLab.addCreep(c);
            }           
            c.setDuration(Long.parseLong(((String) ce.get("duration"))));
            c.setIsStarted((Boolean) ce.get("is_started"));
            c.setTimeStarted(Long.parseLong(((String) ce.get("time_started"))));

            if (c.getTimeRemaining() > 0) {
              // Creep is still valid
              c.setIsComplete(false);
              if (contactMap.get(c.getNumber()).getName() != null) {
                c.setName((contactMap.get(c.getNumber())).getName());
              } else {
                c.setName(reformatNumber(c.getNumber()));
              }
            } else {
              // Creep has timed out - remove it
              Log.i("Comments", "Removing timed-out creep");
              deleteCreepFromCloud(c);
            }
          }
        } else {
          // Do nothing - no relevant creeps in the Cloud
        }
      }

      @Override
      public void onError(final IOException exception) {
        Log.e("TAG", "IOException error updating creeps from Cloud");
      }
    };
    
    CloudQuery cq = new CloudQuery("Creep");
    cq.setScope(Scope.FUTURE_AND_PAST);
    //cq.setFilter(Filter.eq("creeper", sPhoneNumber));
    //cq.setFilter(Filter.or(Filter.eq("creeper", sPhoneNumber), Filter.eq("victim", sPhoneNumber)));
    getCloudBackend().list(cq, handler);
  }

  /* Reads phone contact name and phone number(s) */
  private void readContactData() {
    final String[] PROJECTION = new String[] {
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER };

    ContentResolver cr = this.getBaseContext().getContentResolver();
    Cursor cursor = cr.query(ContactsContract
        .CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION, null, null, null);
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
          if (phoneNumber != null) {
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
    
    // Reduce string to only digits
    for (int i = 0; i < sb.length(); i++) {
      if (!Character.isDigit(sb.charAt(i))) {
        sb.deleteCharAt(i);
        i--;
      }
    }
    
    // Remove 0's or 1's from the beginning of the number
    for (int i = 0; i < sb.length(); i++) {
      if (sb.charAt(i) == '0' || sb.charAt(i) == '1') {
        sb.deleteCharAt(i);
        i--;
      } else {
        break;
      }
    }
    
    // Check length of number
    if (sb.length() != 10) {
      return null;
    }
    return sb.toString();
  }
  
  /* Reformats a 10-digit phone number to (xxx) xxx-xxxx format */
  private String reformatNumber(String num) {
    num = "(" + num.substring(0, 3) + ") " + num.substring(3, 6) + "-" + num.substring(6, 10);
    return num;
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
      List<Creep> list = new ArrayList<Creep>();
      for (int i = 0; i < sLab.creepsByYou.size(); i++) {
        Creep c = sLab.creepsByYou.get(i);
        if (c.getIsChecked()) {
          // Remove from both local list and Cloud
          list.add(c);
          deleteCreepFromCloud(c);
          sLab.removeCreep(c);
          removed = true;
          i--;
        }
      }
      for (int i = 0; i < sLab.creepsOnYou.size(); i++) {
        Creep c = sLab.creepsOnYou.get(i);
        if (c.getIsChecked()) {
          // Remove from both local list and Cloud
          list.add(c);
          deleteCreepFromCloud(c);
          sLab.removeCreep(c);
          removed = true;
          i--;
        }
      }
      if(removed) {
        unsubscribeFromLocationUpdates(list);
      } else {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "Exception error cancelling selected creeps");
    }

    // Update listView elements
    this.adp1.notifyDataSetChanged();
    this.adp2.notifyDataSetChanged();
  }

  /* Action taken on Map All Selections Button click */
  public void mapSelections(View v) {
    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    // Check for GPS enabled. If not...
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      // Dialog and no map
      buildAlertMessageNoGps();
    }

    try {
      ArrayList<UUID> selections = sLab.selectedCreeps();
      if (selections.size() != 0) {
        for (int i = 0; i < selections.size(); i++) {
          if (sLab.getCreep(selections.get(i)).getIsStarted() == false) {
            selections.remove(i);
            i--;
          }
        }
        if (selections.size() == 0) {
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
      Log.e(TAG, "Exception error at mapping selected creeps");
    }
  }

  /* Implements timer to update listView elements */
  Runnable listViewUpdater = new Runnable() {
    // Update UI
    @Override
    public void run() {
      // Subscribe to location updates from any new creeps
      for(int i = 0; i < sLab.getCreeps(true).size(); i++) {
        Creep c = sLab.getCreeps(true).get(i);
        if(c.getIsStarted() && !c.getIsSubscribed()) {
          subscribeToLocationUpdates(c.getNumber());
        }
      }
      for(int i = 0; i < sLab.getCreeps(false).size(); i++) {
        Creep c = sLab.getCreeps(false).get(i);
        if(c.getIsStarted() && !c.getIsSubscribed()) {
          subscribeToLocationUpdates(c.getNumber());
        }
      }
      // Remove any completed creeps
      sLab.checkForCompletions();  
      
      // 
      if(timeSinceLastUpdate >= LOCATION_UPDATE_INTERVAL) {
        if(!sLab.isEmpty()) {
          publishLocationUpdate();
        }
        timeSinceLastUpdate = 0;
      } else {
        timeSinceLastUpdate = timeSinceLastUpdate + 1000;
      }
      
      // Unsubscribe from any deleted creeps, reset list
      unsubscribeFromLocationUpdates(sLab.getDeletedCreepsList());
      sLab.clearDeletedCreepsList();
      
      // Update any updated creeps, reset list
      updateCreeps(sLab.getUpdatedCreeps());
      sLab.clearUpdatedCreepsList();
      
      adp1.notifyDataSetChanged();
      adp2.notifyDataSetChanged();

      timerHandler.postDelayed(listViewUpdater, UPDATE_TIME_INTERVAL);
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

  /* Adds a locally-created creep to the cloud */
  private void addCreepToCloud(Creep c) {
    c.setCloudId(UUID.randomUUID().toString());
    
    // Create a response handler that will receive the result or an error
    CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
      @Override
      public void onComplete(CloudEntity results) {
        // Do nothing - it worked
      }

      @Override
      public void onError(final IOException exception) {
        Log.e("TAG", "IOException error adding creep to Cloud");
      }
    };

    // Set current time Long
    Date now = new Date();
    Long currT = now.getTime();

    CloudEntity ce = new CloudEntity("Creep");
    ce.put("creeper", sPhoneNumber);
    ce.put("victim", c.getNumber());
    ce.put("duration", Long.toString(c.getDuration()));
    ce.put("time_started", Long.toString(currT));
    ce.put("is_started", c.getIsStarted());
    ce.put("creep_uuid", UUID.randomUUID().toString());
    getCloudBackend().insert(ce, handler);
  }
  
  /* Deletes a creep from the cloud */
  public void deleteCreepFromCloud(Creep c) {
    // Create a response handler that will receive the result or an error
    CloudCallbackHandler<List<CloudEntity>> trackHandler = new CloudCallbackHandler<List<CloudEntity>>() {
      @SuppressWarnings("unchecked")
      @Override
      public void onComplete(
          List<CloudEntity> results) {
        if (results.size() > 0) {
          @SuppressWarnings("rawtypes")
          CloudCallbackHandler handler = new CloudCallbackHandler() {
            @Override
            public void onComplete(Object results) {
              // Do nothing - creep was successfully deleted
            }
            
            @Override
            public void onError(final IOException exception) {
              Log.e("TAG", "IOException error deleting creep from the Cloud");
            }
          };  
          getCloudBackend().delete(results.get(0), handler);
        } else {
          // Couldn't find the specified creep in the cloud
          Log.i("Comments", "Couldn't find creep to be deleted in Cloud");
        }
      }

      @Override
      public void onError(final IOException exception) {
        Log.e("TAG", "IOException error finding creep in Cloud");
      }
    };

    CloudQuery cq = new CloudQuery("Creep");
    cq.setScope(Scope.PAST);
    cq.setFilter(Filter.eq("creep_uuid", c.getCloudId()));
    getCloudBackend().list(cq, trackHandler);
  }
  
  /* Starts publishing your location update */
  private void publishLocationUpdate() {
    // Get your current lat/long
    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
    String provider = locationManager.getBestProvider(criteria, true);
    Location location = locationManager.getLastKnownLocation(provider);
    
    if(location != null) {
      // Set up GCM publish
      CloudBackendMessaging cbm = new CloudBackendMessaging(this);
      CloudEntity ce = cbm.createCloudMessage(sPhoneNumber);
      ce.put("last_update", location.getTime());
      ce.put("latitude", location.getLatitude());
      ce.put("longitude", location.getLongitude());
      Log.i("Comments", "User latitude: " + Double.toString(location.getLatitude()));
      Log.i("Comments", "User longitude: " + Double.toString(location.getLongitude()));
      cbm.sendCloudMessage(ce);
    } else {
      Log.i("Comments", "No personal location available");
    }
  }
  
  /* Subscribes user to GCM location updates with input phone number as topicId */
  private void subscribeToLocationUpdates(final String number) {
    // Create a response handler that will received the result
    CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
      @Override
      public void onComplete(List<CloudEntity> messages) {
        Log.i("Comments", "Subscribing to messages from: " + number);
        // Get most recent message
        if(messages.size() != 0) {
          CloudEntity ce = messages.get(0);
          
          // Get lat/long from CloudEntity
          Double lat = (Double) ce.get("latitude");
          Double lon = (Double) ce.get("longitude");
          if(lat == null || lon == null) {
            // Lat/Long is bad
            Log.e(TAG, "Bad lat/long from Cloud message for: " + number);
            return;
          }
          
          // Get time CloudEntity was created (location updated)
          Long timeCreated = ((Date) ce.get("_createdAt")).getTime();
          Long currT = (new Date()).getTime();
          
          // Get List of creeps corresponding to input number
          List<Creep> creeps = sLab.getCreeps(number);
          
          for(int i = 0; i < creeps.size(); i++) {
            Creep c = creeps.get(i);
            c.setLatitude(lat);
            c.setLongitude(lon);
            Log.i("Comments", "Creep latitude: " + lat.toString());
            Log.i("Comments", "Creep longitude" + lon.toString());
            
            if(c.getIsStarted()) {
              // Update GPS Enabled (location current) indicator
              if((currT - timeCreated) < LOCATION_MAX_INTERVAL) {
                c.setGpsEnabled(true);
              } else {
                c.setGpsEnabled(false);
              }
            }
            c.setIsSubscribed(true);
          }
          adp1.notifyDataSetChanged();
          adp2.notifyDataSetChanged();
        }
      }
    };
    
    // Set up GCM subscribe.  Only retrieves latest message
    CloudBackendMessaging cbm = new CloudBackendMessaging(this);
    cbm.subscribeToCloudMessage(number, handler, 1);
  }
  
  /* Unsubscribes user from GCM location updates */
  private void unsubscribeFromLocationUpdates(List<Creep> list) {
    CloudBackendMessaging cbm = new CloudBackendMessaging(this);
    // Unsubscribe from location updates for passed-in creeps
    for(int i = 0; i < list.size(); i++) {
      // Unsubscribe creep from location updates
      if(list.get(i).getIsSubscribed()) {
        cbm.unsubscribeFromCloudMessage(list.get(i).getNumber());
        list.get(i).setIsSubscribed(false);
      }
    }
  }
  
  /* Updates creeps in the cloud that have been changed locally */
  private void updateCreeps(List<Creep> list) {
    for(Creep c : list) {
      CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
        @Override
        public void onComplete(List<CloudEntity> results) {
          if(results.size() > 0) {
            CloudCallbackHandler<CloudEntity> updateHandler = new CloudCallbackHandler<CloudEntity>() {
              @Override
              public void onComplete(CloudEntity results) {
                // Successfully updated
              }
              
              @Override
              public void onError(final IOException exception) {
                Log.e("TAG", "IOException error updating creep in the Cloud");
              }
            };
            getCloudBackend().update(results.get(0), updateHandler);
          }
        }   
      };
      CloudQuery cq = new CloudQuery("Creep");
      cq.setFilter(Filter.eq("creep_uuid", c.getCloudId()));
      getCloudBackend().list(cq, handler);
    }
  }

  /* Action taken when activity is resumed */
  @Override
  public void onResume() {
    super.onResume();
    if (getIntent().getExtras() != null) {
      if (((String) getIntent().getExtras().get("source")).equals("FriendSelectorActivity")) {
        if((UUID) getIntent().getExtras().get("uuid") != null) {
          Creep c = sLab.getCreep((UUID)getIntent().getExtras().get("uuid"));
          addCreepToCloud(c);
        }
      }
    }
    // Update lists on activity resume
    this.adp1.notifyDataSetChanged();
    this.adp2.notifyDataSetChanged();

    // If there's an instance of CreepMapActivity open, kill it
    if (CreepMapActivity.getInstance() != null) {
      CreepMapActivity.getInstance().finish();
    }

    // ListView update timer (re)started
    startTimer();
  }

  /* Action taken when activity is paused or destroyed */
  @Override
  public void onPause() {
    stopTimer();
    super.onPause();
  }
  
  /* Builds GPS not enabled alert message and provides option to re-enable */
  private void buildAlertMessageNoGps() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    // Alert dialog blocks activity from running and opening map
    builder
        .setMessage("Your GPS seems to be disabled, do you want to enable it?")
        .setCancelable(false)
        .setPositiveButton(
            "Yes",
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(final DialogInterface dialog, final int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
    int itemId = item.getItemId();
    if (itemId == R.id.action_map_selections) {
      // Opens new map activity with selected creeps mapped
      mapSelections(v);
      return true;
    } else if (itemId == R.id.action_delete_selections) {
      // Cancels selected creeps
      cancelSelections(v);
      return true;
    } else if (itemId == R.id.action_add_creep) {
      // Opens new creep activity
      newFriendSelector(v);
      return true;
    } else if (itemId == R.id.action_settings) {
      // Open settings activity
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }
  
  /* Erases a single cloud entity...for debugging */
  public void eraseSingleCreepCloudEntity(View v) {
    // Create a response handler that will receive the result or an error
    CloudCallbackHandler<List<CloudEntity>> trackHandler = new CloudCallbackHandler<List<CloudEntity>>() {
      @SuppressWarnings("unchecked")
      @Override
      public void onComplete(
          List<CloudEntity> results) {
        if (results.size() > 0) {
          @SuppressWarnings("rawtypes")
          CloudCallbackHandler handler = new CloudCallbackHandler() {

            @Override
            public void onError(
                final IOException exception) {
              Log.e(
                  "TAG",
                  "Exception error updating CloudEntity, MA");
            }

            @Override
            public void onComplete(
                Object results) {
              // Auto-generated method stub

            }
          };
          Log.i(
              "Comments",
              results.size()
                  + " Entries to be erased");
          getCloudBackend().delete(
              results.get(0), handler);
        } else {
          Log.i("Comments",
              "Nothing to be erased");
          Log.i(
              "Comments",
              "1 Entry erased");
        }
      }

      @Override
      public void onError(
          final IOException exception) {
        Log.e(
            "TAG",
            "Exception error updating CloudEntity, MA");
      }
    };

    CloudQuery cq = new CloudQuery(
        "Creep");
    cq.setScope(Scope.PAST);
    getCloudBackend().list(cq,
        trackHandler);
  }
}
