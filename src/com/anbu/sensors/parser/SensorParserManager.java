package com.anbu.sensors.parser;

import java.util.LinkedList;
import java.util.Queue;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

public class SensorParserManager {

	private final static String TAG = "Anbu";
	private static SensorParserManager _singletonInstance = null;
	Queue<BluetoothGattCharacteristic> _characteristicQueue;
	private boolean isStarted = false;
	SensorParser mSensorParser;
	static Context mContext;

	private SensorParserManager() {
	}

	public static SensorParserManager getSingleParserMonitor(Context context) {
		if (_singletonInstance == null) {
			_singletonInstance = new SensorParserManager();			
		}
		mContext=context;
		return _singletonInstance;
	}

	public void start() {
		isStarted=true;
		if (_characteristicQueue == null)
			_characteristicQueue = new LinkedList<BluetoothGattCharacteristic>();
		mSensorParser= new SensorParser(mContext);
		processCommandQueue();
	}

	public void stop() {
		isStarted=false;
		mSensorParser=null;
	}

	public void addToParserQueue(BluetoothGattCharacteristic characteristic) {
		
		_characteristicQueue.add(characteristic);
	}

	private void processCommandQueue() {
		if (_characteristicQueue != null) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (isStarted) {
							for(int i=0; i<_characteristicQueue.size();i++) {
								
								BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) _characteristicQueue.remove();
								mSensorParser.parsePacketData(characteristic.getValue());
							}
						}
					} catch (Exception ex) {
						Log.i(TAG,"Error: Unable to process!\n\t" + ex);
					} finally {
						_characteristicQueue.clear();
					}
				}

			}).start();
		}
	}
	
}
