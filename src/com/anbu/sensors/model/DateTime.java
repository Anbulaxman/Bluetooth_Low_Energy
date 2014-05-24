package com.anbu.sensors.model;

public class DateTime {
	
		byte hour, min, sec, day, month, year;
	    
	    public DateTime(byte hour, byte min, byte sec, byte day, byte month, byte year)
	    {
	    	this.hour = hour;
	    	this.min = min;
	    	this.sec = sec;
	    	
	    	this.day = day;
	    	this.month = month;
	    	this.year = year;
	    }
	    
	    public String getDateTime()
	    {
	    	String str="";
	    	str = "Date: "+ day+"/"+month+"/"+year + "  Time: "+ hour+":"+min+":"+sec;
	    	return str;
	    }
}
