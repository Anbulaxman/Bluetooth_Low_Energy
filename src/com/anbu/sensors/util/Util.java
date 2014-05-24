package com.anbu.sensors.util;

import android.util.Log;

public class Util {

	public static float q16FloatFromBytes(byte lowByte,  byte midLowByte, byte midHighByte, byte highByte)   {
	    
	    // get the raw packed int value
	    int siVal = Int32FromBytes(lowByte, midLowByte, midHighByte, highByte);
	    
	    // extract & return the Q16 value
	    return q16Value(siVal);
	}
	
	public static float q30FloatFromBytes(byte lowByte,  byte midLowByte, byte midHighByte, byte highByte)   {
	    
	    // get the raw packed int value
	    int siVal = Int32FromBytes(lowByte, midLowByte, midHighByte, highByte);
	    
	    // extract & return the Q16 value
	    return q30Value(siVal);
	}

	public static int Int32FromBytes(byte lowByte, byte lowByte2, byte highByte, byte highByte2) {
		return ((highByte2 & 0xFF) << 24 | (highByte & 0xFF) << 16 | (lowByte2 & 0xFF) << 8 | (lowByte & 0xFF));
	}
	
	public static short Int16FromBytes(byte lowByte, byte highByte) {
	    return (short) ((lowByte&0xff) | ((highByte&0xff) << 8));
	}
	
	public static float q16Value(int rawVal) {
	    // these are fixed point Q16 numbers so we divide by 2^16 (65,536)
	    return (float)rawVal / (float)65536;
	}
	
	// get a fixed point Q30 value from a raw integer
	public static float q30Value(int rawVal) {
	    // these are fixed point Q30 numbers so we divide by 2^30 (1073741824)
	    return (float)rawVal / (float)1073741824;
	}
}
