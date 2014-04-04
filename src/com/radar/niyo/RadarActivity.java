package com.radar.niyo;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.radar.niyo.data.FriendsTableColumns;
import com.radar.niyo.data.NiyoRadar;

import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RadarActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, OnMarkerClickListener, OnMapClickListener, OnInfoWindowClickListener{
	
	private static final String LOG_TAG = RadarActivity.class.getSimpleName();
	
//	private static final int AUTHENTICATE = 0;
//	private static final int REGISTER = 1;
	private static final int REFRESH = 2;
	private static final int FRIENDS = 3;
	private static final int SETTINGS = 4;
	
	private static final String DEBUG_PREFERENCE_KEY = "debug_pref";

	public static final String USER_EMAIL = "user_email";
	private boolean mToggleIndeterminate = false;
//	private AccountManager mAccountManager;
	private LocationClient mLocationClient;
	private LinkedHashMap<String, Marker> mMarkers;
	private LinkedHashMap<String, List<PolylineOptions>> mRoutes;
	private HashMap<String, Marker> mEmailToMarkers;
	private Marker mFocusedMarker;
	private String mNoaRouteKey = "noa";
	private String mWorkRouteKey = "work";
	LatLng homeLoc = new LatLng(Double.valueOf("32.188537"), Double.valueOf("34.896555"));
	LatLng workLoc = new LatLng(Double.valueOf("32.130606"), Double.valueOf("34.893335"));
	LatLng noaLoc = new LatLng(Double.valueOf("32.167393"), Double.valueOf("34.87512"));
	
	AtomicInteger msgId = new AtomicInteger();
	RadarBroadcastReceiver mRadarRec;
	String mRegid;
	
	private String[] mRadayNavOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
	
	LoaderManager.LoaderCallbacks<Cursor> mLoader;
	int mLoaderId;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ClientLog.d(LOG_TAG, "onCreate started");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.radar_layout);
		mRadarRec = new RadarBroadcastReceiver(this);
		final GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setTrafficEnabled(true);
		map.setMyLocationEnabled(true);
//		map.setOnInfoWindowClickListener(this);
		map.setOnMarkerClickListener(this);
		map.setOnMapClickListener(this);
		map.setOnInfoWindowClickListener(this);
		findViewById(R.id.arrowsContainer).setVisibility(View.GONE);
		mRoutes = new LinkedHashMap<String, List<PolylineOptions>>();
//		gcm = GoogleCloudMessaging.getInstance(this);
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
		
		View leftArrow = findViewById(R.id.leftArrow);
		leftArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ClientLog.d(LOG_TAG, "clicked left on "+mFocusedMarker.getId());
				Marker nextMarker = getNextMarker(mFocusedMarker);
				ClientLog.d(LOG_TAG, "animating to "+nextMarker.getId());
				nextMarker.setVisible(true);
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(nextMarker.getPosition(), 15);
				map.animateCamera(update);
				nextMarker.showInfoWindow();
				mFocusedMarker = nextMarker;
			}
		});
		
		View rightArrow = findViewById(R.id.rightArrow);
		rightArrow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ClientLog.d(LOG_TAG, "clicked right on "+mFocusedMarker.getId());
				Marker prevMarker = getPreviousMarker(mFocusedMarker);
				prevMarker.setVisible(true);
				ClientLog.d(LOG_TAG, "animating to "+prevMarker.getId());
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(prevMarker.getPosition(), 15);
				map.animateCamera(update);
				prevMarker.showInfoWindow();
				mFocusedMarker = prevMarker;
				
			}
		});
		
		initLoader();
		
		initDrawer();
		
		initTraffic();
		
		if (!TextUtils.isEmpty(getIntent().getStringExtra(GcmBroadcastReceiver.TRAFFIC_REPORT))) {
			map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
				
				@Override
				public void onMapLoaded() {
					showTraffic(map, mNoaRouteKey, noaLoc);
					
				}
			});
			
		}
		
	}
	
	private void initTraffic() {
		
		
		String workUrl = getDirectionsUrl(homeLoc, workLoc);
		String noaUrl = getDirectionsUrl(homeLoc, noaLoc);
		
		ServiceCaller workCaller = new ServiceCaller() {
			
			@Override
			public void success(Object data) {
				ParserTask parserTask = new ParserTask(mWorkRouteKey);
				
				// Invokes the thread for parsing the JSON data
				parserTask.execute((String)data);
				
			}
			
			@Override
			public void failure(Object data, String description) {
				// TODO Auto-generated method stub
				
			}
		};
		
		
		ServiceCaller noaCaller = new ServiceCaller() {
			
			@Override
			public void success(Object data) {
				ParserTask parserTask = new ParserTask(mNoaRouteKey);
				 
	            // Invokes the thread for parsing the JSON data
	            parserTask.execute((String)data);
				
			}
			
			@Override
			public void failure(Object data, String description) {
				// TODO Auto-generated method stub
				
			}
		};
		
		GenericHttpRequestTask workDownloadTask = new GenericHttpRequestTask(workCaller);
		ClientLog.d(LOG_TAG, "getting directions with url: "+workUrl);
		workDownloadTask.execute(workUrl);
		
		GenericHttpRequestTask noaDownloadTask = new GenericHttpRequestTask(noaCaller);
		ClientLog.d(LOG_TAG, "getting directions with url: "+noaUrl);
		noaDownloadTask.execute(noaUrl);
		
	}
	
	private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
		
		private String mRouteKey;
		
		public ParserTask(String routeKey) {
			mRouteKey = routeKey; 
		}
		 
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
 
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
 
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
 
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
 
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
 
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
 
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
 
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
 
                    points.add(position);
                }
 
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.parseColor("#8C999999"));
                
                List<PolylineOptions> subRoutes = mRoutes.get(mRouteKey);
                
                if (subRoutes == null) {
                	subRoutes = new ArrayList<PolylineOptions>();
                	mRoutes.put(mRouteKey, subRoutes);
                }
                subRoutes.add(lineOptions);
                
                
                
                if (getTitle().toString().toLowerCase().indexOf("traffic") > -1) {
                	
                	final GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
                    map.addPolyline(lineOptions);
                }
                
                
            }
 
            // Drawing polyline in the Google Map for the i-th route
            
            
            
            
        }
    }
	
	private String getDirectionsUrl(LatLng origin,LatLng dest){
		 
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
 
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
 
        // Sensor enabled
        String sensor = "sensor=false";
        
        String alternatives = "alternatives=true";
 
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+alternatives;
 
        // Output format
        String output = "json";
 
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
 
        return url;
    }
	
	public void showTraffic(GoogleMap map, String routeKey, LatLng loc) {
		
		if (mMarkers != null) {
			for (Marker marker : mMarkers.values()) {
				marker.setVisible(false);
			}
		}
		
		setTitle(mRadayNavOptions[1]);
		
		
//		for (String routeKey : mRoutes.keySet()) {
			if (mRoutes.get(mNoaRouteKey) != null) {
				for (PolylineOptions polylineOptions : mRoutes.get(routeKey)) {
					map.addPolyline(polylineOptions);
					polylineOptions.visible(true);
				}
				
			}
//		}
		
		LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
		
		
		
		boundsBuilder.include(homeLoc);
		boundsBuilder.include(loc);
		
		CameraUpdate update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200);
		map.animateCamera(update);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
	        selectItem(position);
	    }
		
		/** Swaps fragments in the main content view */
		private void selectItem(int position) {
			
			final int home = 0;
			final int noaTraffic = 1;
			final int workTraffic = 2;
			final GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			
			switch(position) {
				case home:
				{
					for (Marker marker : mMarkers.values()) {
						marker.setVisible(true);
					}
					Resources appR = getResources();
					CharSequence txt = appR.getText(appR.getIdentifier("app_name", "string", getPackageName()));
					setTitle(txt);
					
					for (String routeKey : mRoutes.keySet()) {
						if (mRoutes.get(routeKey) != null) {
							for (PolylineOptions polylineOptions : mRoutes.get(routeKey)) {
								polylineOptions.visible(false);
							}
							
						}
					}
					
					break;
				}
				case noaTraffic:
				{
					showTraffic(map, mNoaRouteKey, noaLoc);
					break;
				}
				case workTraffic:
				{
					showTraffic(map, mWorkRouteKey, workLoc);
					break;
				}
			}
			
		    // Highlight the selected item, update the title, and close the drawer
		    mDrawerList.setItemChecked(position, true);
		    
		    mDrawerLayout.closeDrawer(mDrawerList);
		}

		public void setTitle(CharSequence title) {
//		    mTitle = title;
		    getActionBar().setTitle(title);
		}

	}
	
	
	
	private void initDrawer() {
		
//		mRadayNavOptions = getResources().getStringArray(R.array.radar_nav_options);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setHomeButtonEnabled(true);
	    
		mRadayNavOptions = new String[]{"Home", "Noa Traffic", "Work Traffic"};
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mRadayNavOptions));
        
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_navigation_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };
        
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
	}

	private void initLoader() {
		
		final RadarActivity context = this;
		
		mLoader = new LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				
				Uri baseUri = NiyoRadar.FRIENDS_URI;
				
				// Now create and return a CursorLoader that will take care of
	            // creating a Cursor for the data being displayed.
	            String select = "((" + FriendsTableColumns.NAME + " NOTNULL) AND ("
	                    + FriendsTableColumns.NAME + " != '' ))";
	            Loader<Cursor> result = new CursorLoader(context, baseUri,
	            		NiyoRadar.FREINDS_SUMMARY_PROJECTION, select, null,
	                    FriendsTableColumns.NAME + " COLLATE LOCALIZED ASC");
	            
	            mLoaderId = result.getId();
	            return result;
			}

			@Override
			public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
				
				if (cursor.getCount() <= 0) {
					showNoFriends();
				}
				
				else {
					cursor.moveToFirst();
					
					hideAllMarkers();
					
					while(!cursor.isAfterLast()) {
						
						String email = cursor.getString(cursor.getColumnIndex(FriendsTableColumns.FRIEND_EMAIL));
						if (mEmailToMarkers != null) {
							Marker marker = mEmailToMarkers.get(email);
							if (marker != null) {
								marker.setVisible(true);
							}
						}
						launchLocationRequest(email);
						cursor.moveToNext();
					}
				}
			}

			@Override
			public void onLoaderReset(Loader<Cursor> arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		
	}

	public void updateDebugText(String text) {
		
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		
//		boolean storedApp = SettingsManager.getBoolean(this, USE_GOOGLE_MAPS, false);
//		String storedApp = SettingsManager.getString(this, USE_GOOGLE_MAPS);
		Boolean isDebug = sharedPref.getBoolean(DEBUG_PREFERENCE_KEY, false);
		
		if (isDebug) {
			TextView debugView = (TextView)findViewById(R.id.debugView);
			String currentText = debugView.getText().toString();
			
			String newText = currentText + "\n" + text;
			debugView.setText(newText);
		}
		
	}
	
	@Override
    protected void onStart() {
        super.onStart();
//        ClientLog.d(LOG_TAG, "Connect the client.");
        
    }
	
	@Override
	protected void onResume(){
		super.onResume();
		TextView debugView = (TextView)findViewById(R.id.debugView);
		debugView.setText("");
		registerReceiver(mRadarRec, new IntentFilter("com.niyo.updateFriend"));
		requestLocationsUpdate();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mRadarRec);
		setProgressBarIndeterminateVisibility(false);
		mToggleIndeterminate = false;
        invalidateOptionsMenu();
        getLoaderManager().destroyLoader(mLoaderId);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
	{
		ClientLog.d(LOG_TAG, "onCreateOptionsMenu started");
		
		if (!mToggleIndeterminate){
			MenuItem refreshMI = menu.add(0, REFRESH, 0, "Refresh");
			refreshMI.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			refreshMI.setIcon(R.drawable.ic_menu_refresh);
		}
		
		MenuItem friendsMI = menu.add(0, FRIENDS, 0, "Friends");
		friendsMI.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		friendsMI.setIcon(R.drawable.ic_menu_allfriends);
		friendsMI.setIntent(FriendsActivity.getCreationIntent(this));
		
		MenuItem settingsMenuItem1 = menu.add(0, SETTINGS, 0, "Settings");
		settingsMenuItem1.setIcon(R.drawable.ic_menu_settings_holo_light);
		settingsMenuItem1.setIntent(SettingsActivity.getCreationIntent(this));
		
		
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) 
	{
    	if (menuItem.getItemId() == REFRESH) {
    		showProgressBar();
    		requestLocationsUpdate();
    		return true;
    	}
    	
    	else if (menuItem.getItemId() == FRIENDS) {
    		Intent intent = FriendsActivity.getCreationIntent(this);
    		startActivity(intent);
    		return true;
    	}
    	
    	else if (menuItem.getItemId() == SETTINGS) {
    		Intent intent = SettingsActivity.getCreationIntent(this);
    		startActivity(intent);
    		return true;
    	}
    	else{
    		return false;
    	}
    	
	}
	
	private void requestLocationsUpdate() {
		
		getLoaderManager().initLoader(0, null, mLoader);
		
	}
	
	protected void hideAllMarkers() {
		
		if (mEmailToMarkers != null) {
			String email = SettingsManager.getString(this, USER_EMAIL);
			for (String markerEmail : mEmailToMarkers.keySet()) {
				
				if (!markerEmail.equals(email)) {
					Marker marker = mEmailToMarkers.get(markerEmail);
					marker.setVisible(false);
				}
				
				
			}
		}
		
		
	}

	private void showNoFriends() {
		Toast.makeText(this, "You have no friends", Toast.LENGTH_SHORT).show();
		hideProgressBar();
	}
	
	private void launchLocationRequest(final String askee) {
		
		String email = SettingsManager.getString(this, USER_EMAIL);
		UUID trxId = UUID.randomUUID(); 
		
		String url = NetworkUtilities.BASE_URL+"/askForPosition?user_asking="+email+"&user_answering="+askee+"&trx_id="+trxId;
		ClientLog.d(LOG_TAG, "asking for position to niyo server with "+url);
		final RadarActivity context = this;
		ServiceCaller caller = new ServiceCaller() {
			
			@Override
			public void success(Object data) {
				
				context.updateDebugText("server is looking for "+askee);
				ClientLog.d(LOG_TAG, "server is looking for "+askee);
			}
			
			@Override
			public void failure(Object data, String description) {
				
				Toast.makeText(context, description, Toast.LENGTH_LONG).show();
				hideProgressBar();
				
			}
		};
		GenericHttpRequestTask httpTask = new GenericHttpRequestTask(caller);
		httpTask.execute(url);
	}
	
	private void showProgressBar() {
		mToggleIndeterminate = true;
        setProgressBarIndeterminateVisibility(mToggleIndeterminate);
        invalidateOptionsMenu();
	}
	
	private void hideProgressBar() {
		mToggleIndeterminate = false;
        setProgressBarIndeterminateVisibility(mToggleIndeterminate);
        invalidateOptionsMenu();
	}

	
	public static Intent getCreationIntent(Activity acitivity){
		
		Intent intent = new Intent(acitivity, RadarActivity.class);
		return intent;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		ClientLog.e(LOG_TAG, "connection failed with "+arg0);
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		
		
		Location location = mLocationClient.getLastLocation();
		
		if (location != null){
			
			String email = SettingsManager.getString(this, USER_EMAIL);
			String imageUrl = SettingsManager.getString(this, GoogleAuthTask.PROFILE_IMAGE_URL);
			
			updateFriend(email, location.getLatitude(), location.getLongitude(), "", imageUrl, false, -1);
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11);
			
			GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
			map.animateCamera(update);
		}
		
		
		
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	public class FriendImageFetcher extends AsyncTask<String, Void, Void>
	{
		private final String LOG_TAG = FriendImageFetcher.class.getSimpleName();
		private ImageView mImageView;
		private Bitmap mBitMap;
		private String mEmail;
		private Double mLat;
		private Double mLon;
		private String mMsg;
		private Boolean mShouldAnimate;
		
		public FriendImageFetcher(ImageView imageView, String email, Double lat, Double lon,
				String msg, Boolean shouldAnimate) {
			
			mImageView = imageView;
			mEmail = email;
			mLat = lat;
			mLon = lon;
			mMsg = msg;
			mShouldAnimate = shouldAnimate;
		}

		@Override
		protected Void doInBackground(String... params) {
			
			URL url;
			try {
				url = new URL(params[0]);
				ClientLog.d(LOG_TAG, "do in backgroudn started with url "+params[0]);
			
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();   
				conn.setDoInput(true);   
				conn.connect();     
				InputStream is = conn.getInputStream();
				mBitMap = BitmapFactory.decodeStream(is); 
			
			} catch (Exception e) {
				e.printStackTrace();
				
				ClientLog.e(LOG_TAG, "Error in fetching "+params[0]);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
	         mImageView.setImageBitmap(mBitMap);
	         setNiyoMarker(mImageView, mEmail, mLat, mLon, mMsg, mShouldAnimate);
		}
		
	}
	
	protected void setNiyoMarker(ImageView image, String email, Double lat, Double lon, String msg, Boolean shouldAnimate) {
		
		MapFragment mapFragment = ((MapFragment)getFragmentManager().findFragmentById(R.id.map));
		if (mapFragment == null) return;
		GoogleMap map = mapFragment.getMap();
		
		
		MarkerOptions options = new MarkerOptions();
		LatLng position = new LatLng(lat, lon);
		options.position(position);
		options.title(email);
		options.snippet(msg);
//		LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
		
		
		if (mMarkers == null) {
			mMarkers = new LinkedHashMap<String, Marker>();
		}
		
		if (mEmailToMarkers == null) {
			mEmailToMarkers = new HashMap<String, Marker>();
		}
		
		
		
		
//		for (Marker marker : mMarkers.values()) {
//			ClientLog.d(LOG_TAG, marker.getTitle()+" position: "+marker.getPosition());
//			boundsBuilder.include(marker.getPosition());
//		}
		
//		Location location = mLocationClient.getLastLocation();
//		LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
//		boundsBuilder.include(myPosition);
		
		
		
		image.setDrawingCacheEnabled(true);
		image.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		image.layout(0, 0, 63, 77); 
	
		image.buildDrawingCache(true);
		
		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image.getDrawingCache());
		options.icon(icon);
		Marker marker = map.addMarker(options);
		ClientLog.d(LOG_TAG, "added marker id: "+marker.getId());
//		if (shouldAnimate) {
			mMarkers.put(marker.getId(), marker);
			mEmailToMarkers.put(email, marker);
//		}
		
//		LatLngBounds bounds = boundsBuilder.build();
//		CameraUpdate update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200);
		hideProgressBar();
		
//		if (shouldAnimate) {
//			map.animateCamera(update);
//		}
		
	}

	public void updateFriend(String email, Double lat, Double lon, String msg, String imageUrl, Boolean shouldAnimate, Integer ringMode) {
		
		ClientLog.d(LOG_TAG, "updating friend");
		String useremail = SettingsManager.getString(this, USER_EMAIL);
		
		if (!email.equals(useremail)) {
			updateDebugText("\n\n"+email+" has answered!");
		}
		
		
		if (mEmailToMarkers != null && mEmailToMarkers.get(email) != null) {
			Marker currMarker = mEmailToMarkers.get(email);
			currMarker.setPosition(new LatLng(lat, lon));
		}
		else {
			NiyoMarker image = new NiyoMarker(this);
			
			if (ringMode == AudioManager.RINGER_MODE_NORMAL || ringMode < 0) {
				image.initMarker(R.drawable.friend_placard);
			}
			else {
				image.initMarker(R.drawable.friend_placard_mute);
			}
			
			
			FriendImageFetcher task = new FriendImageFetcher(image, email, lat, lon, msg, shouldAnimate);
			task.execute(imageUrl);
		}
		
		
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		
		findViewById(R.id.arrowsContainer).setVisibility(View.VISIBLE);
		mFocusedMarker = marker;
		return false;
		
	}

	protected Marker getPreviousMarker(Marker marker) {
		
		ClientLog.d(LOG_TAG, "getPreviousMarker started with id "+marker.getId());
		Marker result = null;
		List<Marker> markers = getMarkersAsList();
		for (int i = 0; i < markers.size(); i++) {
			
			ClientLog.d(LOG_TAG, "traverse to marker: "+markers.get(i).getId());
			if (markers.get(i).getId().equals(marker.getId())) {
				if (i-1 >= 0) {
					result = markers.get(i-1);
				}
				else {
					result = markers.get(markers.size()-1);
				}
			}
		}
		
		if (result == null) {
			result = markers.get(0);
		}
		return result;
	}

	protected Marker getNextMarker(Marker marker) {
		
		ClientLog.d(LOG_TAG, "getNextMarker started with id "+marker.getId());
		Marker result = null;
		List<Marker> markers = getMarkersAsList();
		for (int i = 0; i < mMarkers.size(); i++) {
			
			ClientLog.d(LOG_TAG, "traverse to marker: "+markers.get(i).getId());
			if (markers.get(i).getId().equals(marker.getId())) {
				if (i+1 == markers.size()) {
					result = markers.get(0);
				}
				else {
					result = markers.get(i+1);
				}
			}
		}
		
		if (result == null) {
			result = markers.get(0);
		}
		return result;
	}

	private List<Marker> getMarkersAsList() {
		List<Marker> result = new ArrayList<Marker>();
		
		for (Marker marker : mMarkers.values()) {
			result.add(marker);
		}
		return result;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		findViewById(R.id.arrowsContainer).setVisibility(View.GONE);
		
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		ClientLog.d(LOG_TAG, "got requestCode "+requestCode);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
//		intent.setData(Uri.parse("https://plus.google.com/hangouts//CONVERSATION/[26-character ID]?hl=en_US&hscid=[19-digit ID]&hpe=[14-character value]&hpn=[Google+ Name of Recipient]&hnc=0&hs=41"));
		intent.setData(Uri.parse("https://plus.google.com/hangouts/CONVERSATION/103120499192262417425"));
//		intent.setType("vnd.android.cursor.item/vnd.googleplus.profile.comm");
		
//		intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//		intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		
		
		
		startActivity(intent);
		
	}
}
