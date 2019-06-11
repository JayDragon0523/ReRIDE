package com.example.reride.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTime {
	
	public static final int FORMAT_SHORT = 0;
	public static final int FORMAT_LONG = 1;
	public static final int FORMAT_HIDE = 2;
	
	public static final String getTodayTimestamp(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String time = format.format( new Date() ) + " 00:00:00";
		format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return String.valueOf( date.getTime() );
	}
	
	public static final String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        return date;
	}
	
	public static final String getYestoryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yestoday = sdf.format(calendar.getTime());
        return yestoday;
	}
	
	public static final String getDateFormat( long date, int dataFormat ){
		String pattern = "";
		switch( dataFormat ){
			case FORMAT_LONG:
				pattern = "yyyy-MM-dd";
				break;
			case FORMAT_SHORT:
				pattern = "MM-dd";
				break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
	public static final String getDateFormat( long date, int dataFormat, int timeFormat ){
		String pattern = "";
		switch( dataFormat ){		
			case FORMAT_LONG:
				pattern = "yyyy-MM-dd";
				break;
			case FORMAT_SHORT:
				pattern = "MM-dd";
				break;
			case FORMAT_HIDE:
				pattern = "";
				break;
		}
		switch( timeFormat ){
			case FORMAT_LONG:
				pattern += (dataFormat!=FORMAT_HIDE?" ":"") + "HH:mm:ss";
				break;
			case FORMAT_SHORT:
				pattern += (dataFormat!=FORMAT_HIDE?" ":"") + "HH:mm";
				break;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
	
}
