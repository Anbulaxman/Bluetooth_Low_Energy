package com.anbu.sensors.model;

public class Gyroscope {

		public float[] gyroscopeData = new float[3];
	    
	    public Gyroscope(float gyroscopeX, float gyroscopeY, float gyroscopeZ)
	    {
	    	this.gyroscopeData[0] = gyroscopeX;
	    	this.gyroscopeData[1] = gyroscopeY;
	    	this.gyroscopeData[2] = gyroscopeZ;

	    }
	    
	    public String getGyroscopeData()
	    {
	    	String str="";
	    	str = "x: "+ gyroscopeData[0]+" y:"+gyroscopeData[1]+" z:"+gyroscopeData[2];
	    	return str;
	    }
	    
}
