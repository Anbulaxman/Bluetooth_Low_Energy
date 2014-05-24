package com.anbu.sensors.model;

public class Magnetometer {
		public float[] magnetometerData = new float[3];
	    
	    public Magnetometer(float magnetometerX, float magnetometerY, float magnetometerZ)
	    {
	    	this.magnetometerData[0] = magnetometerX;
	    	this.magnetometerData[1] = magnetometerY;
	    	this.magnetometerData[2] = magnetometerZ;

	    }
	    
	    public String getMagnetometerData()
	    {
	    	String str="";
	    	str = "x: "+ magnetometerData[0]+" y:"+magnetometerData[1]+" z:"+magnetometerData[2];
	    	return str;
	    }
	    
}
