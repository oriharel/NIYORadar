package com.radar.niyo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class WazeParserServiceCaller extends ServiceCaller {
	
	private final static String LOG_TAG = WazeParserServiceCaller.class.getSimpleName();
	private Context mContext;
	private String mGcmType;
	public static final int NOTIFICATION_ID = 1;
	public static final String TRAFFIC_REPORT = "traffic_report";
	
	public WazeParserServiceCaller(Context context, String gcmType) {
		super();
		mContext = context;
		mGcmType = gcmType;
	}

	@Override
	public void success(Object data) {
		
		ClientLog.d(LOG_TAG, "got answer from waze with length is "+((String)data).length());
		
		try {
			String dataStr = (String)data;
			String dataStrNoNan = dataStr.replace("NaN", "0");
			JSONObject jsonData = new JSONObject(dataStrNoNan);
			JSONArray alternatives = jsonData.getJSONArray("alternatives");
			int shortestTimeInSec = -1;
			String shortestRouteName = "";
			
			for(int i=0;i<alternatives.length();i++){
				
				JSONObject alternative = (JSONObject)alternatives.get(i);
				JSONObject response = (JSONObject)alternative.get("response");
				String routeName = (String)response.get("routeName");
				
				ClientLog.d(LOG_TAG, "analyizing route name "+routeName);
				
				JSONArray results = response.getJSONArray("results");
				ClientLog.d(LOG_TAG, "got "+results.length()+" results");
				int routeTimeInt = 0;
				
				for(int j=0;j<results.length();j++){
					JSONObject result = (JSONObject)results.get(j);
					int crossTimeInt = result.getInt("crossTime");
//					ClientLog.d(LOG_TAG, "crossTimeInt: "+crossTimeInt);
					routeTimeInt += crossTimeInt;
				}
				
//				int routeTimeInt = Integer.valueOf(routeTimeClean);
				
				if (shortestTimeInSec < 0 || routeTimeInt < shortestTimeInSec) {
					shortestTimeInSec = routeTimeInt;
					shortestRouteName = routeName;
				}
				
			}
			String title = "";
			int shortestTimeMin = shortestTimeInSec/60;
			
			if (mGcmType.equals("morningTrafficToWork")) {
				title = "Traffic Report to work";
			}
			else if (mGcmType.equals("eveningTrafficHome")) {
				title = "Traffic report to home";
			}
			else {
				title = mGcmType;
			}
			
			sendNotification(title, shortestTimeMin+" mins on "+shortestRouteName, "dummy", mContext);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void failure(Object data, String description) {
		// TODO Auto-generated method stub

	}
	
	// Put the GCM message into a notification and post it.
    public static void sendNotification(String title, String msg, String payload, Context context) {
    	NotificationManager notificationManager = (NotificationManager)
    			context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent trafficIntent = new Intent(context, RadarActivity.class);
        trafficIntent.putExtra(TRAFFIC_REPORT, payload);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
        		trafficIntent, 0);

        Notification.Builder mBuilder =
                new Notification.Builder(context)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(title)
        .setStyle(new Notification.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
