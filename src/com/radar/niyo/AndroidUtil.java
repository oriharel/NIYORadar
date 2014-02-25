package com.radar.niyo;

public class AndroidUtil 
{
	public static final String MAIL_TO = "support@pageonce.com";
	
	public static String getArrayAsString(Object[] array)
	{
		String result = "";
		if (array != null) {
			for (Object object : array) {
				result += object.toString() + ", ";
			}
		}
		return result;
	}

}
