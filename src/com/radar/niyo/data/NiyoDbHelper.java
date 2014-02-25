package com.radar.niyo.data;

import com.radar.niyo.ClientLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class NiyoDbHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = NiyoDbHelper.class.getSimpleName();
	public static final String FRIENDS_TABLE = "friends";
	
	private static final String TABLE_FRIENDS_CREATE =
			"create table " + FRIENDS_TABLE + " ("
					+ FriendsTableColumns._ID + " integer primary key autoincrement, "
					+ FriendsTableColumns.NAME + " TEXT, "
					+ FriendsTableColumns.GIVEN_NAME + " TEXT, "
					+ FriendsTableColumns.FAMILY_NAME + " TEXT, "
					+ FriendsTableColumns.LINK + " TEXT, "
					+ FriendsTableColumns.PICTURE_URL + " TEXT, "
					+ FriendsTableColumns.GENDER + " TEXT, "
					+ FriendsTableColumns.BIRTHDAY + " DATE, "
					+ FriendsTableColumns.FRIEND_EMAIL + " TEXT, "
					+ FriendsTableColumns.FRIEND_ID + " TEXT);";
	
	public NiyoDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ClientLog.d(LOG_TAG, "onCreate started");
		db.execSQL(TABLE_FRIENDS_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
