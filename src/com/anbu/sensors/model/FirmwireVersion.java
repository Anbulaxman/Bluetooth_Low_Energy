package com.anbu.sensors.model;

public class FirmwireVersion {
	
	    byte version;
	    byte revision;
	    byte subrevision;
	    byte month;
	    byte day;
	    byte year;
	    byte model; 
	    
	    public FirmwireVersion(byte version, byte revision, byte subrevision,
                byte month, byte day, byte year)
	    {
	    	this.version = version;
	    	this.revision = revision;
	    	this.subrevision = subrevision;
	    	this.month = month;
	    	this.day = day;
	    	this.year = year;
	    }
	    
	    public String getFirmwireVersion()
	    {
	    	String str="";
	    	str = version + "."+revision+"."+subrevision;
	    	return str;
	    }
	    
	    public String getVersionDate()
	    {
	    	String str="";
	    	str = day+"/"+month+"/"+year;
	    	return str;
	    }
}
