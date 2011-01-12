
package com.jakewendt.wherewasimarker;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.view.View.OnClickListener;
import android.widget.TextView;
//import android.widget.Button;
import android.content.SharedPreferences;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

// import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
// import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import java.util.List;
// import java.io.IOException;
// import java.io.OutputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.UnsupportedEncodingException;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class WhereWasIMarker extends Activity {

	private LocationManager locationManager;

	public static final String PREFS_NAME = "MyPrefsFile";

    public String name;
    public String email;
    public String phone_number;
    public String device_id;
    
    public boolean mSilentMode = false;
    
    static final private int BACK_ID = Menu.FIRST;
    static final private int CLEAR_ID = Menu.FIRST + 1;
	public TextView latitudeField;
	public TextView longitudeField;
	public Location last_location;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		latitudeField  = (TextView) findViewById(R.id.latitude);
		longitudeField = (TextView) findViewById(R.id.longitude);

        // get a handle on the location manager
        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // public void requestLocationUpdates (String provider, long minTime, float minDistance, PendingIntent intent)
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
        		0, new LocationUpdateHandler());

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        name  = settings.getString("name", "Jake's Where was I Marker");
        email = settings.getString("email", "jake@example.com");
        
        ((TextView) findViewById(R.id.name)).setText(name);
        ((TextView) findViewById(R.id.email)).setText(email);
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		device_id = tManager.getDeviceId();
		phone_number = tManager.getLine1Number();

        ((TextView) findViewById(R.id.device_id)).setText(device_id);
        ((TextView) findViewById(R.id.phone_number)).setText(phone_number);
	}
    
    // this inner class is the intent reciever that recives notifcations
    // from the location provider about position updates, and then redraws
    // the MapView with the new location centered.
    public class LocationUpdateHandler implements LocationListener {
    	@Override
    	public void onLocationChanged(Location location) {
    		refresh_location(location);
    	}

    	@Override
    	public void onProviderDisabled(String provider) {}

    	@Override
    	public void onProviderEnabled(String provider) {}

    	@Override
    	public void onStatusChanged(String provider, int status,
    			Bundle extras) {}
    }

    
	public void showLocation(View view) {
		switch (view.getId()) {
		case R.id.showlocation:
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				refresh_location(location);
			} else {
				latitudeField.setText("GPS not available");
				longitudeField.setText("GPS not available");
			}
			break;
		}

	}


	
	
    @Override
    protected void onStop(){
       super.onStop();

      // We need an Editor object to make preference changes.
      // All objects are from android.content.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putString("name", name);
      editor.putString("email", email);
      
      // Commit the edits!
      editor.commit();
    }


    public void refresh_location(Location location){
    	last_location = location;
		latitudeField.setText(Double.toString( location.getLatitude()));
		longitudeField.setText(Double.toString(location.getLongitude()));
    }
    
    public void markLocation(View view) {
    	switch (view.getId()) {
    		case R.id.marklocation:
    			if (last_location != null) {

	                ((TextView) findViewById(R.id.status)).setText(
	                		"Marking location");
	    			try {
		                HttpClient httpclient = new DefaultHttpClient();
		                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		                formparams.add(new BasicNameValuePair("marker[latitude]", 
		                		Double.toString(last_location.getLatitude())));
		                formparams.add(new BasicNameValuePair("marker[longitude]", 
		                		Double.toString(last_location.getLongitude())));
		                formparams.add(new BasicNameValuePair("marker[name]", name));
		                formparams.add(new BasicNameValuePair("marker[email]", email));
		                formparams.add(new BasicNameValuePair("marker[phone_number]", phone_number));
		                formparams.add(new BasicNameValuePair("marker[device_id]", device_id));
	
		                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
		                HttpPost httppost = new HttpPost("http://wherewasi.jakewendt.com/markers");
		                httppost.setEntity(entity);
		                HttpResponse response = httpclient.execute(httppost);
	
		                InputStream is = response.getEntity().getContent();
		                Writer writer = new StringWriter();
		                char[] buffer = new char[1024];
		                try {
		                    Reader reader = new BufferedReader(
		                            new InputStreamReader(is, "UTF-8"));
		                    int n;
		                    while ((n = reader.read(buffer)) != -1) {
		                        writer.write(buffer, 0, n);
		                    }
		                } finally {
		                    is.close();
		                }
		                
		                ((TextView) findViewById(R.id.status)).setText(
		                		writer.toString());
	    			}catch (Exception e) {
	    				e.printStackTrace();
	    			}
    			} else {
    				((TextView) findViewById(R.id.status)).setText("GPS not available");
    			}
    			break;
    	}
    }

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    
    
/*  Hidden menu stuff */

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
 //           mEditor.setText("");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}