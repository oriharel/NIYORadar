package com.radar.niyo.data;

import com.radar.niyo.ClientLog;
import com.radar.niyo.ServiceCaller;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class InsertNewFriendTask extends
		AsyncTask<RadarFriend, Void, Boolean> {
	
	private static final String LOG_TAG = InsertNewFriendTask.class.getSimpleName();
	private Context _context;
	private ServiceCaller _caller;
	
	
	public InsertNewFriendTask(Context context, ServiceCaller caller) {
		
		_context = context;
		_caller = caller;
	}

	@Override
	protected Boolean doInBackground(RadarFriend... params) {
		
		RadarFriend friend = params[0];
		ContentValues values = new ContentValues();
		
		values.put(FriendsTableColumns.NAME, friend.getName());
		values.put(FriendsTableColumns.FRIEND_EMAIL, friend.getEmail());
		Uri result = _context.getContentResolver().insert(NiyoRadar.FRIENDS_URI, values);
		
		ClientLog.d(LOG_TAG, "added a friend name "+friend.getName()+" result was "+result.toString());
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
    	 ClientLog.d(LOG_TAG, "friends db insertion succeeded");
    	 
    	 _caller.success(result);
	}

}
