package com.radar.niyo;

import com.google.android.gms.auth.GoogleAuthUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	
	private static final String LOG_TAG = LoginActivity.class.getSimpleName();
	private AccountManager mAccountManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		
		
		String imageUrl = SettingsManager.getString(this, GoogleAuthTask.PROFILE_IMAGE_URL);
		if (!TextUtils.isEmpty(imageUrl)) {
			
			Intent intent = RadarActivity.getCreationIntent(this);
			startActivity(intent);
			finish();
		}
		
		setContentView(R.layout.login_layout);
		
		findViewById(R.id.signInBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		final LoginActivity context = this;
		final String[] namesArray = getAccountNames();
		final int[] selectedIndex = new int[1];
		builder.setTitle("Choose an account");
		builder.setSingleChoiceItems(namesArray, 0, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					selectedIndex[0] = which;
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ClientLog.e(LOG_TAG, "Error! "+e);
				}
				
			}
		});
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				final String userEmail = namesArray[selectedIndex[0]];
				SettingsManager.setString(context, RadarActivity.USER_EMAIL, userEmail);
				
				GoogleAuthTask task = new GoogleAuthTask(context, new ServiceCaller() {
					
					@Override
					public void success(Object data) {
						registerWithServer(SettingsManager.getString(context, GoogleAuthTask.PROFILE_ID), userEmail);
						
					}
					
					@Override
					public void failure(Object data, String description) {
						// TODO Auto-generated method stub
						
					}
				}, dialog, userEmail);
				task.execute();
				
			}
		});
		
		builder.setNegativeButton(R.string.cancel_button_label, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               dialog.cancel();
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
		
	}
	
	private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
	
	private void registerWithServer(String userId, String userEmail) {
		
		String reg_id = SettingsManager.getString(this, NiyoApplication.PROPERTY_REG_ID);
		
		if (TextUtils.isEmpty(reg_id) || TextUtils.isEmpty(userEmail)) {
			
			ClientLog.w(LOG_TAG, "unable to register to server. no gcm reg id or user email");
			Toast.makeText(this, "Can't register to server because couldn't retrieve gcm id", Toast.LENGTH_LONG).show();
		}
		else {
			String url = NetworkUtilities.BASE_URL+"/register?reg_id="+reg_id+"&user_id="+userId+"&user_email="+userEmail;
			ClientLog.d(LOG_TAG, "registering to niyo server with "+url);
			final LoginActivity context = this;
			ServiceCaller caller = new ServiceCaller() {
				
				@Override
				public void success(Object data) {
					ClientLog.d(LOG_TAG, "registration succeeded");
					Toast.makeText(context, "Registration to NIYO server succeeded", Toast.LENGTH_LONG).show();
					Intent intent = RadarActivity.getCreationIntent(context);
					startActivity(intent);
					finish();
				}
				
				@Override
				public void failure(Object data, String description) {
					Toast.makeText(context, "Registration to NIYO server FAILED", Toast.LENGTH_LONG).show();
					
				}
			};
			GenericHttpRequestTask httpTask = new GenericHttpRequestTask(caller);
			httpTask.execute(url);
		}
	}

}
