package com.radar.niyo.data;

import com.radar.niyo.ClientLog;
import com.radar.niyo.ServiceCaller;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class DeleteFriendTask extends AsyncTask<Uri, Void, Boolean> {

	private Context mContext;
	private ServiceCaller _caller;
	private static final String LOG_TAG = DeleteFriendTask.class.getSimpleName();
	
	public DeleteFriendTask(Context context, ServiceCaller caller) {
		mContext = context;
		_caller = caller;
	}
	
	@Override
	protected Boolean doInBackground(Uri... params) {
		
		Uri uri = params[0];
		mContext.getContentResolver().delete(uri, null, null);
		
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
    	 ClientLog.e(LOG_TAG, "friend deleted");
    	 
    	 _caller.success(result);
	}

}
