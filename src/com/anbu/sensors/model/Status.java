package com.anbu.sensors.model;

public class Status {
	
	    byte model;
	    byte chargerState=0x00;
	    float volts;
	    
	    public Status(byte model, byte chargerState, float volts)
	    {
	    	this.model = model;
	    	this.chargerState = chargerState;
	    	this.volts = volts;
	    }
	    
	    public String getStatus()
	    {
	    	String str="";
	    	str = "Battery: "+ volts+ "volts. "+getChargerState();
	    	return str;
	    }
	    
	    public String getChargerState()
	    {
	    	if(chargerState==0)
	    		return "Not charging";
	    	else
	    		return "Charging";
	    }
	    
	    public String getBatteryVoltage()
	    {
	    	return ""+volts;
	    }
}
