package com.radar.niyo.data;

public class Place {
	
	public static String HOME_LAT = "home_lat";
	public static String HOME_LON = "home_lon";
	public static String WORK_LAT = "work_lat";
	public static String WORK_LON = "work_lon";
	public static String FERBER_LAT = "ferber_lat";
	public static String FERBER_LON = "ferber_lon";
	
	private String mName;
	private String mLat;
	private String mLon;
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		mName = name;
	}
	public String getLat() {
		return mLat;
	}
	public void setLat(String lat) {
		mLat = lat;
	}
	public String getLon() {
		return mLon;
	}
	public void setLon(String lon) {
		mLon = lon;
	}

}
