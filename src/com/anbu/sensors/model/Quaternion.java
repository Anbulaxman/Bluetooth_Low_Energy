package com.anbu.sensors.model;

public class Quaternion {

		public float[] quaternionData = new float[4];
	    
	    public Quaternion(float quaternionW, float quaternionX, float quaternionY, float quaternionZ)
	    {
	    	this.quaternionData[0] = quaternionW;
	    	this.quaternionData[1] = quaternionX;
	    	this.quaternionData[2] = quaternionY;
	    	this.quaternionData[3] = quaternionZ;

	    }
	    
	    public String getQuaternionData()
	    {
	    	String str="";
	    	str = "w: "+ quaternionData[0] +" x: "+ quaternionData[1]+" y:"+quaternionData[2]+" z:"+quaternionData[3];
	    	return str;
	    }
	    
}
