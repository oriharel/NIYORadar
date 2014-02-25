package com.radar.niyo.friends;

import com.radar.niyo.FriendsActivity;
import com.radar.niyo.R;
import com.radar.niyo.ServiceCaller;
import com.radar.niyo.data.DeleteFriendTask;
import com.radar.niyo.data.FriendsTableColumns;
import com.radar.niyo.data.NiyoRadar;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class FriendDetailsActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{
	
	private Uri mUri;
	private static final int DELETE = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.friend_details_layout);
        
        final Intent intent = getIntent();
        
        final String action = intent.getAction();

        // For an edit action:
        if (Intent.ACTION_EDIT.equals(action)) {
        	mUri = intent.getData();
        }
        
        getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuItem friendsMI = menu.add(0, DELETE, 0, "Delete");
		friendsMI.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		friendsMI.setIcon(android.R.drawable.ic_menu_delete);
		friendsMI.setIntent(FriendsActivity.getCreationIntent(this));
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) 
	{
    	if (menuItem.getItemId() == DELETE){
    		
    		final FriendDetailsActivity context = this;
    		
    		DeleteFriendTask task = new DeleteFriendTask(this, new ServiceCaller() {
				
				@Override
				public void success(Object data) {
					
					context.finish();
					
				}
				
				@Override
				public void failure(Object data, String description) {
					// TODO Auto-generated method stub
					
				}
			});
    		
    		task.execute(getIntent().getData());
    	}
    	
    	
    	
    	return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		
//		String select = "((" + FriendsTableColumns.NAME + " NOTNULL) AND ("
//                + FriendsTableColumns.NAME + " != '' ))";
        return new CursorLoader(this, mUri,
        		NiyoRadar.FREINDS_SUMMARY_PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor data) {
		
		data.moveToFirst();
		
		int colNameIndex = data.getColumnIndex(FriendsTableColumns.NAME);
        String name = data.getString(colNameIndex);
        
        int colEmailIndex = data.getColumnIndex(FriendsTableColumns.FRIEND_EMAIL);
        String email = data.getString(colEmailIndex);
        
        TextView nameView = (TextView)findViewById(R.id.detailName);
        TextView emailView = (TextView)findViewById(R.id.detailEmail);
        
        nameView.setText(name);
        emailView.setText(email);
        
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

}
