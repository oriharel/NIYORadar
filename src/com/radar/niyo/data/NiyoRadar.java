package com.radar.niyo.data;

import android.net.Uri;

public class NiyoRadar {
	
	public static String AUTHORITY = "com.radar.provider";
	public static String SCHEME = "content://";
	public static final String FRIENDS = "/friends";
	public static final Uri FRIENDS_URI =  Uri.parse(SCHEME + AUTHORITY + FRIENDS);
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.radar.friend";
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.radar.friend";
	
	public static final String[] FREINDS_SUMMARY_PROJECTION = new String[] {
		FriendsTableColumns._ID,
		FriendsTableColumns.NAME,
		FriendsTableColumns.FRIEND_EMAIL
    };

}
