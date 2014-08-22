package com.radar.niyo;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.ArrayList;

//import org.json.JSONArray;
//import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.radar.niyo.data.Place;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
//import android.content.Intent;
//import android.content.IntentFilter;
import android.util.Log;

//import com.niyo.data.JsonFetchIntentService;

public class NiyoApplication extends Application {

	private static final String LOG_TAG = NiyoApplication.class.getSimpleName();
	private static Boolean s_logEnabled = true;
	GoogleCloudMessaging gcm;
	
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME =
            "onServerExpirationTimeMs";
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
	
	public static final String SENDER_ID = "663161706726";
	String mRegid;
	
	public static boolean isLogEnabled() {
		return s_logEnabled;
	}
	
	@Override
	public void onCreate(){
		
		super.onCreate();
		
		mRegid = getRegistrationId();
		ClientLog.d(LOG_TAG, "mRegId is: "+mRegid);
        
        if (mRegid.length() == 0) {
            registerBackground();
        }
        
//		fetchData();
//		fetchFoursquareVenues();
//		setupProximityAlerts();
        createPlaces();
	}
	
	private void createPlaces() {
		
		SettingsManager.setString(this, Place.HOME_LAT, "32.188912");
		SettingsManager.setString(this, Place.HOME_LON, "34.896700");
		
		SettingsManager.setString(this, Place.WORK_LAT, "32.130576");
		SettingsManager.setString(this, Place.WORK_LON, "34.893403");
		
		SettingsManager.setString(this, Place.FERBER_LAT, "32.187530");
		SettingsManager.setString(this, Place.FERBER_LON, "34.927225");
		
	}
	
	private String getRegistrationId() {
		String registrationId = SettingsManager.getString(this, PROPERTY_REG_ID);
		
		if (registrationId == null || registrationId.length() == 0) {
	        Log.v(LOG_TAG, "Registration not found.");
	        return "";
	    }
	    // check if app was updated; if so, it must clear registration id to
	    // avoid a race condition if GCM sends a message
	    int registeredVersion = SettingsManager.getInt(this, PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    
	    int currentVersion = getAppVersion(this);
	    ClientLog.d(LOG_TAG, "getRegistrationId registeredVersion: "+registeredVersion+" currentVersion: "+currentVersion);
	    if (registeredVersion != currentVersion || isRegistrationExpired()) {
	        Log.v(LOG_TAG, "App version changed or registration expired.");
	        return "";
	    }
	    
	    ClientLog.d(LOG_TAG, "registration id found "+registrationId);
	    return registrationId;
	}
	
	private void setRegistrationId(Context context, String regId) {
	    int appVersion = getAppVersion(context);
	    Log.v(LOG_TAG, "Saving regId on app version " + appVersion);
	    SettingsManager.setString(this, PROPERTY_REG_ID, regId);
	    SettingsManager.setInt(this, PROPERTY_APP_VERSION, appVersion);
	    long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

	    Log.v(LOG_TAG, "Setting registration expiry time to " +
	            new Timestamp(expirationTime));
	    SettingsManager.setLong(this, PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
	}
	
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	private boolean isRegistrationExpired() {
		
	    // checks if the information is not stale
	    long expirationTime =
	            SettingsManager.getLong(this, PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
	    return System.currentTimeMillis() > expirationTime;
	}
	
	private void registerBackground() {
		
		final Application context = this;
		
	    new AsyncTask<Void, Void, String>() {
	    	
	    	@Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                mRegid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration id=" + mRegid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the message
	                // using the 'from' address in the message.

	                // Save the regid - no need to register again.
	                setRegistrationId(context, mRegid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
	        }

	    	@Override
	        protected void onPostExecute(String msg) {
	            ClientLog.d(LOG_TAG, msg + "\n");
	        }
	    }.execute(null, null, null);
	}

}
