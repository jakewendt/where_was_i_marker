package com.jakewendt.wherewasimarker;

import com.jakewendt.wherewasimarker.R;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import java.io.InputStream;
import android.content.Intent;


//public class WhereWasIMarker extends Activity implements TextToSpeech.OnInitListener, View.OnClickListener  {
public class WhereWasIMarker extends Activity implements TextToSpeech.OnInitListener {

	private static final String TAG = "WhereWasI";
	private LocationManager locationManager; // = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	private TextToSpeech tts;
	static final int TTS_CHECK_CODE = 0;
 
	/* I need to learn the differences between public, private, final, static, etc. */
	public static final String PREFS_NAME = "MyPrefsFile";
	public String phone_number;
	public String device_id;
	public LinkedString status;
	public boolean mSilentMode = false;
	static final private int BACK_ID  = Menu.FIRST;
	static final private int CLEAR_ID = Menu.FIRST + 1;
	static final private int SETTINGS_ID = Menu.FIRST + 2;
	private TextView latitudeField;
	private TextView longitudeField;
	private TextView statusField;
	private TextView cogField;
	private TextView distanceField;
	private Location last_location;
	private Location bearing_location;
	private float last_bearing;
	private float distance;
/*
///	private HashMap phrases;
    HashMap hm = new HashMap();
    hm.put("Rohit", new Double(3434.34));
    hm.put("Mohit", new Double(123.22));
    hm.put("Ashish", new Double(1200.34));
    hm.put("Khariwal", new Double(99.34));
    hm.put("Pankaj", new Double(-19.34));
    
     hm.get("Rohit")
 */

	/**
	 * void com.jakewendt.wherewasimarker.WhereWasIMarker.onInit(int initStatus)
	 * 
	 * @Override
	 * 
	 * Specified by: onInit(...) in OnInitListener
	 * public abstract void onInit (int status)
	 * Since: API Level 4
	 * Called to signal the completion of the TextToSpeech engine initialization.
	 * 
	 * Parameters
	 * status	SUCCESS or ERROR.
	 */
	@Override
	public void onInit(int initStatus) {
		if (initStatus == TextToSpeech.SUCCESS)
		{
			tts.speak( "Program in-ish-e-ated",TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	/**
	 * void com.jakewendt.wherewasimarker.WhereWasIMarker.onCreate(Bundle savedInstanceState)
	 * 
	 * @Override
	 * Called when the activity is first created.
	 * 
	 * Overrides: onCreate(...) in Activity
	 * 
	 * Parameters:
	 * 	savedInstanceState
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
/* 
 * void android.app.Activity.onCreate(Bundle savedInstanceState)
 * 
 * protected void onCreate (Bundle savedInstanceState)
 * Since: API Level 1
 * Called when the activity is starting. This is where most initialization should go: calling setContentView(int) to 
 * inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI, calling 
 * managedQuery(android.net.Uri, String[], String, String[], String) to retrieve cursors for data being displayed, etc.
 * You can call finish() from within this function, in which case onDestroy() will be immediately called without any 
 * of the rest of the activity lifecycle (onStart(), onResume(), onPause(), etc) executing.
 * Derived classes must call through to the super class's implementation of this method. If they do not, 
 * an exception will be thrown.
 * 
 * Parameters
 * 	savedInstanceState	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
 * See Also
 * 	onStart()
 * 	onSaveInstanceState(Bundle)
 * 	onRestoreInstanceState(Bundle)
 * 	onPostCreate(Bundle)
 */		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		latitudeField  = (TextView) findViewById(R.id.latitude);
		longitudeField = (TextView) findViewById(R.id.longitude);
		cogField       = (TextView) findViewById(R.id.cog);
		distanceField  = (TextView) findViewById(R.id.distance);

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization completes.
		tts = new TextToSpeech( this, this );
		statusField = (TextView) findViewById(R.id.status);
		status = new LinkedString(tts,statusField);

		// get a handle on the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// public void requestLocationUpdates (String provider, long minTime, float minDistance, PendingIntent intent)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
			10, new LocationUpdateHandler());

		update_settings_view(this);

		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		device_id    = tManager.getDeviceId();
		phone_number = tManager.getLine1Number();

		((TextView) findViewById(R.id.device_id)).setText(device_id);
		((TextView) findViewById(R.id.phone_number)).setText(phone_number);
	}

	public void update_settings_view(Context context) {
		Settings.readSettings(context);
		((TextView) findViewById(R.id.name)).setText(Settings.username);
		((TextView) findViewById(R.id.email)).setText(Settings.email);
	}
	/**
	 * this inner class is the intent reciever that recives notifcations
	 * from the location provider about position updates, and then redraws
	 * the MapView with the new location centered.
	 * 
	 * Used for receiving notifications from the LocationManager when the location has changed. 
	 * These methods are called if the LocationListener has been registered with the location 
	 * manager service using the requestLocationUpdates(String, long, float, LocationListener) method.
	 */
	public class LocationUpdateHandler implements LocationListener {
//	
//		@Override
		public void onLocationChanged(Location location) {
			refresh_location(location);
		}

//		@Override
		public void onProviderDisabled(String provider) {}

//		@Override
		public void onProviderEnabled(String provider) {}

//		@Override
		public void onStatusChanged(String provider, int status,
			Bundle extras) {}
	}

	public void showLocation(View view) {
		switch (view.getId()) {
			case R.id.showlocation:
//				LocationManager 
//				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				
/*
 * Location android.location.LocationManager.getLastKnownLocation(String provider)
 * 
 * Returns a Location indicating the data from the last known location fix obtained from the given provider. 
 * This can be done without starting the provider. Note that this location could be out-of-date, 
 * for example if the device was turned off and moved to another location.
 * 
 * If the provider is currently disabled, null is returned.
 * 
 * Parameters
 * provider	the name of the provider
 * 
 * Returns
 * the last known location for the provider, or null				
 */				Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					refresh_location(location);
				} else {
					status.message("GPS not available");
					latitudeField.setText(status.toString());
					longitudeField.setText(status.toString());				
					cogField.setText(status.toString());				
				}
				break;
				
		}
	}

	/**
	 * void com.jakewendt.wherewasimarker.WhereWasIMarker.onStop()
	 * 
	 * @Override
	 * Overrides: onStop() in Activity
	 * protected void onStop ()
	 * Since: API Level 1
	 * Called when you are no longer visible to the user. You will next receive either onRestart(), onDestroy(), 
	 * or nothing, depending on later user activity.
	 * Note that this method may never be called, in low memory situations where the system does not have 
	 * enough memory to keep your activity's process running after its onPause() method is called.
	 * Derived classes must call through to the super class's implementation of this method. If they do not, 
	 * an exception will be thrown.
	 * 
	 * See Also
	 * 	onRestart()
	 * 	onResume()
	 * 	onSaveInstanceState(Bundle)
	 * 	onDestroy()
	 */
	@Override
	protected void onStop(){
		tts.speak("Self destruct sequence in-ish-e-ated",TextToSpeech.QUEUE_FLUSH, null);
		super.onStop();
	}

	@Override
	protected void onResume(){
		super.onResume();
		update_settings_view(this);
	}

	public void refresh_location(Location location){
		last_location = location;
		latitudeField.setText(Double.toString( location.getLatitude()));
		longitudeField.setText(Double.toString(location.getLongitude()));

//		some_previous_location.bearingTo (current_location)

//		perhaps save a bearing location every minute to compare to
/*
 * float 	distanceTo(Location dest)
 * Returns the approximate distance in meters between this location and the given location.
 * 
 * bull pucky.  
 * 
 * float 	bearingTo(Location dest)
 * Returns the approximate initial bearing in degrees East of true North when traveling 
 * 	along the shortest path between this location and the given location.
 */
		
		if( bearing_location == null ){
			bearing_location = last_location;
		}
		// Returns the approximate distance in meters between this location and the given location.
		distance = bearing_location.distanceTo(last_location);
		distanceField.setText(Float.toString(distance));
		if( distance > 0.01 ) {
			last_bearing = bearing_location.bearingTo(last_location);
			cogField.setText(Float.toString(last_bearing));
		} else {
//			last_bearing = bearing_location.bearingTo(last_location);
			cogField.setText("---");
		}
		bearing_location = last_location;	
//		tts.speak("Set location",TextToSpeech.QUEUE_FLUSH, null);
	}

	public void markLocation(View view) {

		switch (view.getId()) {
			case R.id.marklocation:
				if (last_location != null) {
/* The next parts happen so fast that this message is rarely seen. */
					status.message("Marking location");
					try {
						HttpClient httpclient = new DefaultHttpClient();
						List<NameValuePair> formparams = new ArrayList<NameValuePair>();
						formparams.add(new BasicNameValuePair("marker[latitude]", 
							Double.toString(last_location.getLatitude())));
						formparams.add(new BasicNameValuePair("marker[longitude]", 
							Double.toString(last_location.getLongitude())));
						formparams.add(new BasicNameValuePair("marker[direction]", 
								Double.toString(last_bearing)));
						formparams.add(new BasicNameValuePair("marker[name]", Settings.username));
						formparams.add(new BasicNameValuePair("marker[email]", Settings.email));
						formparams.add(new BasicNameValuePair("marker[phone_number]", phone_number));
						formparams.add(new BasicNameValuePair("marker[device_id]", device_id));

						UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
						HttpPost httppost = new HttpPost("http://wherewasi.jakewendt.com/markers");
						httppost.setEntity(entity);
						HttpResponse response = httpclient.execute(httppost);

						InputStream is = (InputStream) response.getEntity().getContent();
						Writer writer = new StringWriter();
						char[] buffer = new char[1024];
						Reader reader = new BufferedReader(
							new InputStreamReader(is, "UTF-8"));
						int n;
						while ((n = reader.read(buffer)) != -1) {
							writer.write(buffer, 0, n);
						}
						is.close();
						status.message(writer.toString());
					} catch (Exception e) {
						status.message("There was an exception");
						  StringWriter sw = new StringWriter();
						  e.printStackTrace(new PrintWriter(sw));
						statusField.setText(sw.toString());
						e.printStackTrace();
					}
				} else {
					status.message("GPS not available");
				}
				break;
		}
	}

	/**
	 * Replace my button click methods with this
	 */
/*	@Override
	public void onClick(View view) {
//		super.onClick();
		switch (view.getId()) {
			case R.id.showlocation:
				showLocation(view);
				break;
			case R.id.marklocation:
				markLocation(view);
				break;
//			case R.id.marklocation:
//				Intent vsettings = new Intent(this,VoiceRecognition.class);
//				startActivity(vsettings);
//				break;
		}
	}
*/


/*
 * All of that hidden menu stuff
 */

	/**
	* Called when your activity's options menu needs to be created.
	*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// We are going to create two menus. Note that we assign them
		// unique integer IDs, labels from our string resources, and
		// given them shortcuts.
		menu.add(0, BACK_ID, 0, R.string.back).setShortcut('0', 'b');
		menu.add(0, CLEAR_ID, 0, R.string.clear).setShortcut('1', 'c');
		menu.add(0, SETTINGS_ID, 0, "Settings").setShortcut('2', 's');

		return true;
	}
	/**
	* Called right before your activity's option menu is displayed.
	*/
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Before showing the menu, we need to decide whether the clear
		// item is enabled depending on whether there is text to clear.
		//        menu.findItem(CLEAR_ID).setVisible(mEditor.getText().length() > 0);

		return true;
	}
	/**
	* Called when a menu item is selected.
	*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case BACK_ID:
				finish();
				return true;
			case CLEAR_ID:
				//mEditor.setText("");
				return true;
			case SETTINGS_ID:
				Intent ssettings = new Intent(this,Settings.class);
				startActivity(ssettings);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
