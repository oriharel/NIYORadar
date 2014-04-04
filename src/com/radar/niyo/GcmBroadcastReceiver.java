package com.radar.niyo;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class GcmBroadcastReceiver extends BroadcastReceiver {

	static final String LOG_TAG = GcmBroadcastReceiver.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;
	public static final String TRAFFIC_REPORT = "traffic_report";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    Context ctx;
    @Override
    public void onReceive(Context context, Intent intent) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        ctx = context;
        String message = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(message)) {
//            sendNotification("Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(message)) {
//            sendNotification("Deleted messages on server: " +
//                    intent.getExtras().toString());
        } else {
//            sendNotification("Received: " + intent.getExtras().toString());
        }
        
        ClientLog.d(LOG_TAG, "got push with msg "+intent.getExtras().toString());
        
			
		String gcmType = intent.getStringExtra("Type");
		
		if (gcmType.equals("req")) {
			//fire an update location intent
			
			String userAsking = intent.getStringExtra("user_asking");
			String trxId = intent.getStringExtra("trx_id");
			
			ClientLog.d(LOG_TAG, "got location request from "+userAsking);
			
			Intent serviceIntent = new Intent(context, LocationUpdaterIntentService.class);
			serviceIntent.putExtra(LocationUpdaterIntentService.USER_ASKING_PROPERTY, userAsking);
			serviceIntent.putExtra(LocationUpdaterIntentService.TRX_ID_PROPERTY, trxId);
			context.startService(serviceIntent);
			
			ServiceCaller caller = new ServiceCaller() {
				
				@Override
				public void success(Object data) {
					
					ClientLog.d(LOG_TAG, "acknowledge succeeded");
				}
				
				@Override
				public void failure(Object data, String description) {
					
					ClientLog.e(LOG_TAG, "failed to acknowledge");
				}
			};
			String askee = SettingsManager.getString(context, RadarActivity.USER_EMAIL);
			
			AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			Integer ringerMode = audioManager.getRingerMode();
			
			String url = NetworkUtilities.BASE_URL+"/ack?user_asking="+userAsking+"&user_answering="+askee+"&trx_id="+trxId+"&ring="+ringerMode;
			GenericHttpRequestTask httpTask = new GenericHttpRequestTask(caller);
			httpTask.execute(url);
		}
		
		else if (gcmType.equals("ack")) {
			ClientLog.d(LOG_TAG, "registration performed successfully");
			
			Intent updateIntent = new Intent("com.niyo.updateFriend");
			
			String userAnswering = intent.getStringExtra("user_answering");
			String trxId = intent.getStringExtra("trx_id");
			String ringerModeStr = intent.getStringExtra("ring");
			ClientLog.d(LOG_TAG, "your friend, "+userAnswering+" is acknowledging trxId is: "+trxId+" ringer is "+ringerModeStr);
			
			Integer ringerMode = -1;
			
			try { 
				ringerMode = Integer.parseInt(ringerModeStr); 
		    } catch(NumberFormatException e) { 
		        ClientLog.e(LOG_TAG, "error, "+ringerModeStr+" cannot be parsed to a number"); 
		    }
			
			String friendlyRingerMode = "";
			
			switch(ringerMode) {
				case AudioManager.RINGER_MODE_SILENT:
				{
					friendlyRingerMode = "Silent";
					break;
				}
				case AudioManager.RINGER_MODE_NORMAL:
				{
					friendlyRingerMode = "Normal";
					break;
				}
				case AudioManager.RINGER_MODE_VIBRATE:
				{
					friendlyRingerMode = "Vibrate";
				}
					
			}
			
			
			updateIntent.putExtra(RadarBroadcastReceiver.FRIEND_EMAIL, userAnswering);
			updateIntent.putExtra(RadarBroadcastReceiver.ACKNOWLEDGMENT, true);
			updateIntent.putExtra(RadarBroadcastReceiver.RINGER_MODE, friendlyRingerMode);
			context.sendBroadcast(updateIntent);
		}
		
		else if (gcmType.equals("res")) {
			
			String userAnswering = intent.getStringExtra("user_answering");
			String lat = intent.getStringExtra("latitude");
			String lon = intent.getStringExtra("longitude");
			String updateTimeStr = intent.getStringExtra("updateTime");
			String friendImageUrl = intent.getStringExtra("imageUrl");
			String trxId = intent.getStringExtra("trx_id");
			String ringModeStr = intent.getStringExtra("ring");
			
			Integer ringerMode = -1;
			
			try { 
				ringerMode = Integer.parseInt(ringModeStr); 
		    } catch(NumberFormatException e) { 
		        ClientLog.e(LOG_TAG, "error, "+ringModeStr+" cannot be parsed to a number"); 
		    }
			
			Long updateTimeInMillis;
			Intent updateIntent = new Intent("com.niyo.updateFriend");
			
			try {
				updateTimeInMillis = Long.valueOf(updateTimeStr);
				Calendar now = Calendar.getInstance();
				long diff = now.getTimeInMillis() - updateTimeInMillis;
				
				ClientLog.d(LOG_TAG, "diff time is "+diff);
				
				long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
				
				String updateTime = "Updated: "+diffInMinutes+" min. ago";
				updateIntent.putExtra(RadarBroadcastReceiver.FRIEND_UPDATE_TIME, updateTime);
			}
			catch(Exception e) {
				ClientLog.e(LOG_TAG, "Error in parsgin update time "+updateTimeStr);
			}
			
			
			
			ClientLog.d(LOG_TAG, "your friend, "+userAnswering+" is in lat: "+lat+" and lon: "+lon+" trxId is: "+trxId);
			
			
			updateIntent.putExtra(RadarBroadcastReceiver.FRIEND_EMAIL, userAnswering);
			updateIntent.putExtra(RadarBroadcastReceiver.FRIEND_LAT, Double.valueOf(lat));
			updateIntent.putExtra(RadarBroadcastReceiver.FRIEND_LON, Double.valueOf(lon));
			updateIntent.putExtra(RadarBroadcastReceiver.FRIENDS_IMAGE_URL, friendImageUrl);
			updateIntent.putExtra(RadarBroadcastReceiver.RING_MODE_INT, ringerMode);
			context.sendBroadcast(updateIntent);
		}
		
		else if (gcmType.equals("traffic")) {
			String payload = intent.getStringExtra("payload");
			ClientLog.d(LOG_TAG, "got traffic with: "+payload);
			
			ServiceCaller caller = new ServiceCaller() {
				
				@Override
				public void success(Object data) {
					
					ClientLog.d(LOG_TAG, "got answer from heroku with "+data);
					
					try {
						JSONArray jsonData = new JSONArray((String)data);
						
						for(int i=0;i<jsonData.length();i++){
							
							JSONObject route = (JSONObject)jsonData.get(i);
							String routeTime = (String)route.get("route-time");
							String routeName = (String)route.get("route-name");
							
							String routeTimeClean = routeTime.replaceAll("\\D+","");
							
							int routeTimeInt = Integer.valueOf(routeTimeClean);
							
							if (routeTimeInt > 20) {
								sendNotification(routeTimeInt+" mins on "+routeName, "dummy");
							}
							
						}
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				@Override
				public void failure(Object data, String description) {
					ClientLog.e(LOG_TAG, "got failure from heroku");
					
				}
			};
			
			GenericHttpRequestTask trafficAlertTask = new GenericHttpRequestTask(caller);
			
			ClientLog.d(LOG_TAG, "going to call heroku now");
			trafficAlertTask.execute("http://calm-fortress-7680.herokuapp.com/traffic");
			
			
		}
        
    }

    // Put the GCM message into a notification and post it.
    private void sendNotification(String msg, String payload) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent trafficIntent = new Intent(ctx, RadarActivity.class);
        trafficIntent.putExtra(TRAFFIC_REPORT, payload);

        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
        		trafficIntent, 0);

        Notification.Builder mBuilder =
                new Notification.Builder(ctx)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Heavy Traffic for Noa")
        .setStyle(new Notification.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
