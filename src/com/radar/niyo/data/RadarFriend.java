package com.radar.niyo.data;

public class RadarFriend {
	
	private String _name;
	private String _email;
	
	public RadarFriend(String name, String email){
		setName(name);
		setEmail(email);
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public String getEmail() {
		return _email;
	}

	public void setEmail(String email) {
		_email = email;
	}

}
