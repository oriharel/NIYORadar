package com.radar.niyo;

import com.radar.niyo.ClientLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RadarBroadcastReceiver extends BroadcastReceiver {
	
	public static final String FRIEND_LAT = "friend_lat";
	public static final String FRIEND_LON = "friend_lon";
	public static final String FRIEND_EMAIL = "friend_email";
	public static final String ACKNOWLEDGMENT = "ack";
	public static final String RINGER_MODE = "ringer_mode";
	public static final String FRIEND_UPDATE_TIME = "friend_update_time";
	public static final String FRIENDS_IMAGE_URL = "friend_image_url";
	public static final String RING_MODE_INT = "ring_mode";
	private RadarActivity mRadarActivity;
	private static final String LOG_TAG = RadarBroadcastReceiver.class.getSimpleName();
	
	public RadarBroadcastReceiver(RadarActivity radarActivity) {
		
		mRadarActivity = radarActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		ClientLog.d(LOG_TAG, "received intent");
		
		Boolean ack = intent.getBooleanExtra(ACKNOWLEDGMENT, false);
		String email = intent.getStringExtra(FRIEND_EMAIL);
		String ringerMode = intent.getStringExtra(RINGER_MODE);
		Integer ringerModeInt = intent.getIntExtra(RING_MODE_INT, -1);
		
		if (ack) {
			
			String text = email+" got your request and processing. the user is on "+ringerMode;
			mRadarActivity.updateDebugText(text);
		}
		else {
			Double lat = intent.getDoubleExtra(FRIEND_LAT, 0);
			Double lon = intent.getDoubleExtra(FRIEND_LON, 0);
			
			String updateMsg = intent.getStringExtra(FRIEND_UPDATE_TIME);
			String imageUrl = intent.getStringExtra(FRIENDS_IMAGE_URL);
			
			mRadarActivity.updateFriend(email, lat, lon, updateMsg, imageUrl, true, ringerModeInt);
		}
		

	}

}
