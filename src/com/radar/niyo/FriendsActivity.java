package com.radar.niyo;

import com.radar.niyo.contacts.ContactsListActivity;
import com.radar.niyo.data.ChooseContactActivity;
import com.radar.niyo.data.FriendsTableColumns;
import com.radar.niyo.data.InsertNewFriendTask;
import com.radar.niyo.data.NiyoRadar;
import com.radar.niyo.data.RadarFriend;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SearchView.OnQueryTextListener;

public class FriendsActivity extends Activity {
	
	private static final String LOG_TAG = FriendsActivity.class.getSimpleName();
	private static final int DONE = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_layout);
		setTitle("Friends");
		
//		test();
		
		Intent intent = getIntent();
		if (intent.getData() == null) {
            intent.setData(NiyoRadar.FRIENDS_URI);
        }
		
		FragmentManager fm = getFragmentManager();
		
		// Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment list = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
	}
	
	public static class CursorLoaderListFragment extends ListFragment 
	implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

		SimpleCursorAdapter mAdapter;
		
		String mCurFilter;
		
		@Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Give some text to display if there is no data.  In a real
            // application this would come from a resource.
            setEmptyText("No friends");

            // We have a menu item to show in action bar.
            setHasOptionsMenu(true);

            // Create an empty adapter we will use to display the loaded data.
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_2, null,
                    new String[] { FriendsTableColumns.NAME, FriendsTableColumns.FRIEND_EMAIL },
                    new int[] { android.R.id.text1, android.R.id.text2 }, 0);
            setListAdapter(mAdapter);

            // Start out with a progress indicator.
            setListShown(false);

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            getLoaderManager().initLoader(0, null, this);
        }
		
		@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
            MenuItem item = menu.add("Search");
            item.setIcon(R.drawable.content_new);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            
            Intent intent = ContactsListActivity.getCreationIntent(getActivity());
            item.setIntent(intent);
            
            MenuItem doneItem = menu.add(0, DONE, 0, "Done");
            doneItem.setIcon(R.drawable.navigation_accept);
            doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            doneItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					getActivity().finish();
					return true;
				}
			});
        }
		
//		@Override
//		public boolean onOptionsItemSelected(MenuItem menuItem) {
//			
//			if (menuItem.getItemId() == DONE) {
//				getActivity().finish();
//				return true;
//			}
//			return false;
//		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			
			Uri baseUri = NiyoRadar.FRIENDS_URI;
			
			// Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            String select = "((" + FriendsTableColumns.NAME + " NOTNULL) AND ("
                    + FriendsTableColumns.NAME + " != '' ))";
            return new CursorLoader(getActivity(), baseUri,
            		NiyoRadar.FREINDS_SUMMARY_PROJECTION, select, null,
                    FriendsTableColumns.NAME + " COLLATE LOCALIZED ASC");
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			
			// Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
			
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
			
		}
		
		@Override
		public void onResume() {
			super.onResume();
			getLoaderManager().restartLoader(0, null, this);
		}

		public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
            // Don't do anything if the filter hasn't actually changed.
            // Prevents restarting the loader when restoring state.
            if (mCurFilter == null && newFilter == null) {
                return true;
            }
            if (mCurFilter != null && mCurFilter.equals(newFilter)) {
                return true;
            }
            mCurFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }

		@Override
		public boolean onQueryTextSubmit(String arg0) {
			// TODO Auto-generated method stub
			return true;
		}
		
		@Override public void onListItemClick(ListView l, View v, int position, long id) {
			// Insert desired behavior here.
			
			
//			TextView emailView = (TextView)v.findViewById(android.R.id.text2);
//			String email = emailView.getText().toString();
			
			Uri uri = ContentUris.withAppendedId(getActivity().getIntent().getData(), id);
			Log.i(LOG_TAG, "Item clicked: " + uri);
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
			
		}
		
	}
	
	
	
	private void test() {
		
		InsertNewFriendTask task = new InsertNewFriendTask(this, new ServiceCaller() {
			
			@Override
			public void success(Object data) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void failure(Object data, String description) {
				// TODO Auto-generated method stub
				
			}
		});
		
		RadarFriend friend = new RadarFriend("Ori Harel", "ori.harel@gmail.com");
		task.execute(friend);
		
	}

	public static Intent getCreationIntent(Activity context){
		
		Intent result = new Intent(context, FriendsActivity.class);
		return result;
	}

}
