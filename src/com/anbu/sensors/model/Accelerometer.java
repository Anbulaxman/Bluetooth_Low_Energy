package com.anbu.sensors.model;

public class Accelerometer {
	
		public float[] accelerometerData = new float[3];
	    
	    public Accelerometer(float accelerometerX, float accelerometerY, float accelerometerZ)
	    {
	    	this.accelerometerData[0] = accelerometerX;
	    	this.accelerometerData[1] = accelerometerY;
	    	this.accelerometerData[2] = accelerometerZ;

	    }
	    
	    public String getAccelerometerData()
	    {
	    	String str="";
	    	str = "x: "+ accelerometerData[0]+" y:"+accelerometerData[1]+" z:"+accelerometerData[2];
	    	return str;
	    }
	    
}
