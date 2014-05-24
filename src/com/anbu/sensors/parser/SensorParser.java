package com.anbu.sensors.parser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.content.Context;

import com.anbu.sensors.model.Accelerometer;
import com.anbu.sensors.model.DateTime;
import com.anbu.sensors.model.FirmwireVersion;
import com.anbu.sensors.model.Gyroscope;
import com.anbu.sensors.model.Magnetometer;
import com.anbu.sensors.model.Quaternion;
import com.anbu.sensors.model.SensorData;
import com.anbu.sensors.model.Status;
import com.anbu.sensors.service.BleService;
import com.anbu.sensors.util.Constant;
import com.anbu.sensors.util.Util;

public class SensorParser {
	
	private final static String TAG = "Anbu";
	
	public enum RXSTATES {RXSTATE_IDLE, RXSTATE_CMD, RXSTATE_STF,RXSTATE_DATA};

	/*---- buffer defines ----*/
	private static int RXPKTSIZE = 256;
	int checkSumErrorCount=0;

	/*---- static variable declarations ----*/
	static RXSTATES RxState = RXSTATES.RXSTATE_IDLE;
	static int RxIndex;
	static byte[] RxPacket = new byte[RXPKTSIZE];
	
	byte chksum;    
    
    Context mContext;
    
    public SensorParser(Context context)
    {
    	this.mContext = context;
    }
	
	public void parsePacketData(byte packetData[])
	{
		for(int i=0; i<packetData.length;i++)
		{
			processPacketByte(packetData[i]);
		}
	}
	
	private void processPacketByte(byte c)
	{
	    switch( RxState ) {
	        case RXSTATE_IDLE: {
	            if( c == Constant.PDI_START_OF_PACKET ) {
	                RxState = RXSTATES.RXSTATE_CMD;
	            }
	            break;
	        }
	        case RXSTATE_CMD: {
	            RxPacket[0] = c;
	            chksum = c;
	            RxIndex = 1;
	            RxState = RXSTATES.RXSTATE_DATA;
	            break;
	        }
	        case RXSTATE_STF: {
	            RxPacket[RxIndex] = c;
	            chksum += c;
	            RxIndex++;
	            RxState = RXSTATES.RXSTATE_DATA;
	            break;
	        }
	        case RXSTATE_DATA: {
	            switch( c ) {
	                case Constant.PDI_BYTE_STUFFING: {
	                    RxState = RXSTATES.RXSTATE_STF;
	                    chksum += Constant.PDI_BYTE_STUFFING;
	                    break;
	                }
	                case Constant.PDI_END_OF_PACKET: {
	                    /*!!!! END OF PACKET !!!!*/
	                    if( chksum == 0 ) {
	                       processPDIPacket(RxPacket);
	                    } else {
	                        /*---- checksum error! ----*/
	                        
	                        // *** This happens when partial packets get
	                        //  interspersed with our actual packet data if we are
	                        //  trying to send data from the SP-10c Module faster
	                        //  than the android frameworks allow
	                        checkSumErrorCount = checkSumErrorCount + 1;
	                        Log.i(TAG,"Checksum error for packet!  "+chksum+".   checksum errors"+checkSumErrorCount);
	                    }
	                    RxState = RXSTATES.RXSTATE_IDLE;
	                    RxIndex = 0;
	                    return;
	                }
	                default: {
	                    RxPacket[RxIndex] = c;
	                    chksum += c;
	                    RxIndex++;
	                    break;
	                }
	            }
	            break;
	        }
	            
	    }
	    
	    /*---- check for buffer overrun ----*/
	    if( RxIndex >= RXPKTSIZE ) {
	        RxState = RxState.RXSTATE_IDLE;
	        RxIndex = 0;
	    }

	}
	
	private void processPDIPacket(byte[] bytes) {
	    try {
	    	byte cmd = bytes[0];
	        switch (cmd ) {
	            case Constant.PDI_CMD_VERSION: {
	            	Log.i(TAG ,"Received Version.");
	               	processFirmwareVersionPacket(bytes);
	                break;
	            }
	            case Constant.PDI_CMD_TEMPERATURE: {
	                Log.i(TAG ,"Received Temperature.");
	                break;
	            }
	            case Constant.PDI_CMD_PRESSURE: {
	                Log.i(TAG ,"Received Pressure.");
	                break;
	            }
	            case Constant.PDI_CMD_RTC: {
	                //[self processSystemTimePacket:bytes length:length];
	                break;
	            }
	            case Constant.PDI_CMD_CONFIG: {
	                Log.i(TAG ,"Received Config.");
	                break;
	            }
	            case Constant.PDI_CMD_LOGGETCONFIG: {
	                Log.i(TAG ,"Received LOG GET Config.");
	                break;
	            }
	            case Constant.PDI_CMD_LOGCONFIG: {
	                Log.i(TAG ,"Received Log Config.");
	                break;
	            }
	            case Constant.PDI_CMD_LOGRECORD: {
	                Log.i(TAG ,"Received Log Record.");
	                break;
	            }
	            case Constant.PDI_CMD_LOGSTATUS: {
	                Log.i(TAG ,"Received Log Status.");
	                
	                break;
	            }
	            case Constant.PDI_CMD_STATUS: {
	            	Log.i(TAG ,"Received Status.");
	            	processStatusPacket(bytes);
	                break;
	            }
	            case Constant.PDI_CMD_STREAMRECORD: {
	                // call our method to parse a data stream packet
	            	Log.i(TAG ,"Received Stream.");
	            	processDataStreamPacket(bytes);
	                break;
	            }
	            default: {
	                Log.i(TAG ,"Received Unknown Packet:"+ cmd +"  of length: "+ bytes.length+".");
	                break;
	            }
	        }
	    }
	    catch (Exception ex) {
	    	Log.i(TAG ,"Error trying to process PDI Packet."+ex.toString());
	    }
	}

	private void processFirmwareVersionPacket(byte[] bytes) {
		
		FirmwireVersion firmwireVersion = new FirmwireVersion(bytes[1],bytes[2],bytes[3],bytes[4],bytes[5],bytes[6]);
		
		Bundle bundle = new Bundle();
		bundle.putInt("CMD", Constant.BLE_CMD_VERSION);
		bundle.putString("VERSION", firmwireVersion.getFirmwireVersion());
		bundle.putString("VERSIONDATE", firmwireVersion.getVersionDate());
		broadcastUpdate(BleService.ACTION_DATA_AVAILABLE,bundle);
	}
	
	private void processStatusPacket(byte[] bytes) {
//		for(byte b: bytes)
//			   Log.i("Anbu", "" + String.format("%02x", b&0xff));
		
		short packedVolts = Util.Int16FromBytes(bytes[3], bytes[4]);//(short) ((short)bytes[3] | (short)((short)bytes[4] << 8));
		float volts = (float)packedVolts / 100.f;
		Status status = new Status(bytes[1],bytes[2],volts);
		
		Bundle bundle = new Bundle();
		bundle.putInt("CMD", Constant.BLE_CMD_STATUS);
		bundle.putString("BATTERY", status.getBatteryVoltage());
		bundle.putString("CHARGINGSTATE", status.getChargerState());
		broadcastUpdate(BleService.ACTION_DATA_AVAILABLE,bundle);
	}
	
	private void processDataStreamPacket(byte[] bytes) {
		SensorData sensorData = new SensorData();
		int curByteIndex = 1;
		   
		short options = Util.Int16FromBytes(bytes[curByteIndex++], bytes[curByteIndex++]);//(short) ((short)bytes[1] | (short)((short)bytes[2] << 8));
		
		 if ( (options & Constant.LOGDATA_TIMEDATE)!=0 ) {
		        // 6 Bytes of date/time data
		        Byte sec = bytes[curByteIndex++];
		        Byte min = bytes[curByteIndex++];
		        Byte hour = bytes[curByteIndex++];
		        Byte day = bytes[curByteIndex++];
		        Byte month = bytes[curByteIndex++];
		        Byte year = bytes[curByteIndex++];
		        
		        sensorData.dateTime = new DateTime(hour,min, sec, day, month, year);
				Log.i(TAG ,""+sensorData.dateTime.getDateTime());
		 }
		 if ( (options & Constant.LOGDATA_TIMESTAMP)!=0 ) {
		        
		        curByteIndex+=4;
		        
		 }
		 if ((options & Constant.LOGDATA_BATTERYVOLTS)!=0 ) {
			 	// 1 Byte   
				Byte packedVoltsValue = bytes[curByteIndex++];
				float volts = (float)packedVoltsValue / 10.f;
				sensorData.setBatteryVolts(volts);
				Log.i(TAG ,""+sensorData.getBatteryVolts());
		 }
		 if ( (options & Constant.LOGDATA_BLESTATE)!=0 ) {
		        // 1 Byte
		        Byte bleState = bytes[curByteIndex++];
		        sensorData.setBleState(bleState);
		        Log.i(TAG ,""+sensorData.getBleState());
		 }
		 if ( (options & Constant.LOGDATA_GYROS)!=0 ) {
		        
		        // 12 Bytes for the gyro reading
			 	Byte lowByte = bytes[curByteIndex++];
	            Byte midLowByte = bytes[curByteIndex++];
	            Byte midHighByte = bytes[curByteIndex++];
	            Byte highByte = bytes[curByteIndex++];
	            float gyroscopeX = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float gyroscopeY = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	           
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float gyroscopeZ = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);

	            sensorData.gyroscope = new Gyroscope(gyroscopeX, gyroscopeY, gyroscopeZ);
	            Log.i(TAG ,"Gyroscope "+sensorData.gyroscope.getGyroscopeData());
		 }
		 if ((options & Constant.LOGDATA_ACCELS)!=0 ) {
			 	// 12 Bytes for the accel reading
			 	Byte lowByte = bytes[curByteIndex++];
	            Byte midLowByte = bytes[curByteIndex++];
	            Byte midHighByte = bytes[curByteIndex++];
	            Byte highByte = bytes[curByteIndex++];
	            float accelerometerX = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float accelerometerY = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	           
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float accelerometerZ = Util.q16FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);

	            
	            sensorData.accelerometer = new Accelerometer(accelerometerX, accelerometerY, accelerometerZ);
	            Log.i(TAG ,"Accelerometer "+sensorData.accelerometer.getAccelerometerData());
		 }

		 if ( (options & Constant.LOGDATA_QUATERNION)!=0 ) {
			 	// 16 Bytes for the accel reading
			 	Byte lowByte = bytes[curByteIndex++];
	            Byte midLowByte = bytes[curByteIndex++];
	            Byte midHighByte = bytes[curByteIndex++];
	            Byte highByte = bytes[curByteIndex++];
	            float quaternionW = Util.q30FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float quaternionX = Util.q30FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	           
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float quaternionY = Util.q30FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            midLowByte = bytes[curByteIndex++];
	            midHighByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            float quaternionZ = Util.q30FloatFromBytes(lowByte, midLowByte, midHighByte, highByte);

	            
	            sensorData.quaternion = new Quaternion(quaternionW, quaternionX, quaternionY, quaternionZ);
	            Log.i(TAG ,"Quaternion "+sensorData.quaternion.getQuaternionData());
		 }
		 if ((options & Constant.LOGDATA_COMPASS)!=0 ) {
			 	// 6 Bytes for the magnetometer reading
			 	Byte lowByte = bytes[curByteIndex++];
	            Byte highByte = bytes[curByteIndex++];
	            short magnetometerX = Util.Int16FromBytes(lowByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            short magnetometerY = Util.Int16FromBytes(lowByte, highByte);
	            
	            lowByte = bytes[curByteIndex++];
	            highByte = bytes[curByteIndex++];
	            short magnetometerZ = Util.Int16FromBytes(lowByte, highByte);
	            
	            sensorData.magnetometer = new Magnetometer(magnetometerX, magnetometerY, magnetometerZ);
	            Log.i(TAG ,"Magnetometer "+sensorData.magnetometer.getMagnetometerData());
		 }

		Bundle bundle = new Bundle();
		bundle.putInt("CMD", Constant.PDI_CMD_STREAMRECORD);
		bundle.putFloatArray("GYROSCOPE", sensorData.gyroscope.gyroscopeData);
		bundle.putFloatArray("ACCELEROMETER", sensorData.accelerometer.accelerometerData);
		bundle.putFloatArray("QUATERNION", sensorData.quaternion.quaternionData);
		bundle.putFloatArray("MAGNETOMETER", sensorData.magnetometer.magnetometerData);
		broadcastUpdate(BleService.ACTION_DATA_AVAILABLE,bundle);
	}

	private void broadcastUpdate(String action,
			Bundle bundle) {
		final Intent intent = new Intent(action);
		intent.putExtras(bundle);
		mContext.sendBroadcast(intent);
  }
	
}
