package com.radar.niyo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.radar.niyo.data.AutoEvent;
import com.radar.niyo.data.CalendarHelper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;

public class LocationUpdaterIntentService extends IntentService implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener{
	
	private static final String LOG_TAG = LocationUpdaterIntentService.class.getSimpleName();
	public static final String USER_ASKING_PROPERTY = "user_asking";
	public static final String NEXT_EVENT_MODE = "next_event_mode";
	public static final String TRX_ID_PROPERTY = "trx_id";
	private LocationClient mLocationClient;
	private String mUserAsking;
	private String mTrxId;
	private Boolean mNextEventMode;

	public LocationUpdaterIntentService() {
		super("locationUpdater");
		
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		mUserAsking = intent.getStringExtra(USER_ASKING_PROPERTY);
		mTrxId = intent.getStringExtra(TRX_ID_PROPERTY);
		mNextEventMode = intent.getBooleanExtra(NEXT_EVENT_MODE, false);
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		
		ClientLog.d(LOG_TAG, "handle intent with userAsking "+mUserAsking);

	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		
		Location location = mLocationClient.getLastLocation();
		if (location == null) {
			ClientLog.e(LOG_TAG, "location is null!!");
			return;
		}
		
		if (mNextEventMode) {
			doNextEventMode(location);
		}
		else {
			long updateTime = location.getTime();
			String userAnswering = SettingsManager.getString(this, RadarActivity.USER_EMAIL);
			ServiceCaller caller = new ServiceCaller() {
				
				@Override
				public void success(Object data) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void failure(Object data, String description) {
					// TODO Auto-generated method stub
					
				}
			};
			GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
			String imageUrl = SettingsManager.getString(this, GoogleAuthTask.PROFILE_IMAGE_URL);
			
			AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			Integer ringerMode = audioManager.getRingerMode();
			
			String url = NetworkUtilities.BASE_URL+"/answerPosition?user_asking="+mUserAsking+
					"&user_answering="+userAnswering+
					"&latitude="+location.getLatitude()+
					"&longitude="+location.getLongitude()+
					"&update_time="+updateTime+
					"&image_url="+imageUrl+
					"&trx_id="+mTrxId+
					"&ring="+ringerMode;
			
			ClientLog.d(LOG_TAG, "sending location update with url "+url);
			
			task.execute(url);
		}
		
	}

	private void doNextEventMode(Location location) {
		
		AutoEvent nextEvent = CalendarHelper.getNextEvent(this);

		Boolean isEventFired = SettingsManager.getBoolean(this, nextEvent.getEventId().toString(), false);
		if (nextEvent != null && !isEventFired) {
			String eventLat = nextEvent.getLat();
			String eventLon = nextEvent.getLon();
			String fromLat = Double.toString(location.getLatitude());
			String fromLon = Double.toString(location.getLongitude());
			
			String title = "Traffic for "+nextEvent.getTitle();
			
			GcmBroadcastReceiver.runWazeRequest(fromLat, fromLon, eventLat, eventLon, getApplicationContext(), title);
			SettingsManager.setBoolean(this, nextEvent.getEventId().toString(), true);
		}

		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
