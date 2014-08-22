package com.radar.niyo.data;

public class AutoEvent {
	
	private String mTitle;
	private String mLat;
	private String mLon;
	private Long mStartTime;
	private String mLocation;
	private Integer mEventId;
	
	public String getTitle() {
		return mTitle;
	}
	public void setTitle(String title) {
		mTitle = title;
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
	public Long getStartTime() {
		return mStartTime;
	}
	public void setStartTime(Long startTime) {
		mStartTime = startTime;
	}
	public String getLocation() {
		return mLocation;
	}
	public void setLocation(String location) {
		mLocation = location;
	}
	public Integer getEventId() {
		return mEventId;
	}
	public void setEventId(Integer eventId) {
		mEventId = eventId;
	}

}
