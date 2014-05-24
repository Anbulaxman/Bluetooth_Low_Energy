package com.anbu.sensors.model;

public class SensorData {
	
		float volts;
		byte bleState;
		public DateTime dateTime;
		public Gyroscope gyroscope;
		public Accelerometer accelerometer;
		public Magnetometer magnetometer;
		public Quaternion quaternion;
	    
	    public SensorData()
	    {

	    }
	    
	    public void setBatteryVolts(float volts)
	    {
	    	this.volts = volts;
	    }
	    public String getBatteryVolts()
	    {
	    	String str="";
	    	str = "Battery: "+ volts+ "volts. ";
	    	return str;
	    }
	    
	    public void setBleState(byte bleState)
	    {
	    	this.bleState = bleState;
	    }
	    public String getBleState()
	    {
	    	String str="";
	    	str = "BleState: "+ bleState;
	    	return str;
	    }

}
