/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anbu.sensors.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;

import com.anbu.sensors.parser.SensorParserManager;
import com.anbu.sensors.util.Constant;
import com.anbu.sensors.util.Constant.SensoPlexLEDState;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BleService extends Service {
	private final static String TAG = "Anbu";

	private BluetoothManager bluetoothManager;
	private BluetoothAdapter adapter;
	private String deviceAddress;
	private BluetoothGatt gatt;
	private int connectionState = STATE_DISCONNECTED;

	SensorParserManager mSensorParserManager;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	private final static String INTENT_PREFIX = BleService.class.getPackage()
			.getName();
	public final static String ACTION_GATT_CONNECTED = INTENT_PREFIX
			+ ".ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = INTENT_PREFIX
			+ ".ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = INTENT_PREFIX
			+ ".ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = INTENT_PREFIX
			+ ".ACTION_DATA_AVAILABLE";
	public final static String EXTRA_SERVICE_UUID = INTENT_PREFIX
			+ ".EXTRA_SERVICE_UUID";
	public final static String EXTRA_CHARACTERISTIC_UUID = INTENT_PREFIX
			+ ".EXTRA_CHARACTERISTIC_UUI";
	public final static String EXTRA_DATA = INTENT_PREFIX + ".EXTRA_DATA";
	public final static String EXTRA_TEXT = INTENT_PREFIX + ".EXTRA_TEXT";

	// Implements callback methods for GATT events that the app cares about.
	// For example, connection change and services discovered.
	private final BluetoothGattExecutor executor = new BluetoothGattExecutor() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			super.onConnectionStateChange(gatt, status, newState);

			String intentAction;
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				intentAction = ACTION_GATT_CONNECTED;
				connectionState = STATE_CONNECTED;
				broadcastUpdate(intentAction);
				Log.i(TAG, "Connected to GATT server.");
				// Attempts to discover services after successful connection.
				mSensorParserManager = SensorParserManager
						.getSingleParserMonitor(BleService.this);
				mSensorParserManager.start();

				Log.i(TAG, "Attempting to start service discovery:"
						+ BleService.this.gatt.discoverServices());
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				intentAction = ACTION_GATT_DISCONNECTED;
				connectionState = STATE_DISCONNECTED;
				Log.i(TAG, "Disconnected from GATT server.");
				if (mSensorParserManager != null)
					mSensorParserManager.stop();
				broadcastUpdate(intentAction);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);

			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
				notifyAllSensor(gatt);
			} else {
				Log.w(TAG, "onServicesDiscovered received: " + status);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			//Log.d(TAG, "Data available");
			mSensorParserManager.addToParserQueue(characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "writing complete");

		}
	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	

	public class LocalBinder extends Binder {
		public BleService getService() {
			return BleService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// After using a given device, you should make sure that
		// BluetoothGatt.close() is called
		// such that resources are cleaned up properly. In this particular
		// example, close() is
		// invoked when the UI is disconnected from the Service.
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	/**
	 * Initializes a reference to the local Bluetooth adapter.
	 *
	 * @return Return true if the initialization is successful.
	 */
	public boolean initialize() {
		// For API level 18 and above, get a reference to BluetoothAdapter
		// through
		// BluetoothManager.
		if (bluetoothManager == null) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (bluetoothManager == null) {
				Log.e(TAG, "Unable to initialize BluetoothManager.");
				return false;
			}
		}

		adapter = bluetoothManager.getAdapter();
		if (adapter == null) {
			Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
			return false;
		}

		return true;
	}

	/**
	 * Connects to the GATT server hosted on the Bluetooth LE device.
	 *
	 * @param address
	 *            The device address of the destination device.
	 *
	 * @return Return true if the connection is initiated successfully. The
	 *         connection result is reported asynchronously through the
	 *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 *         callback.
	 */
	public boolean connect(final String address) {
		if (adapter == null || address == null) {
			Log.w(TAG,
					"BluetoothAdapter not initialized or unspecified address.");
			return false;
		}

		// Previously connected device. Try to reconnect.
		if (deviceAddress != null && address.equals(deviceAddress)
				&& gatt != null) {
			Log.d(TAG,
					"Trying to use an existing BluetoothGatt for connection.");
			if (gatt.connect()) {
				connectionState = STATE_CONNECTING;
				return true;
			} else {
				return false;
			}
		}

		final BluetoothDevice device = adapter.getRemoteDevice(address);
		if (device == null) {
			Log.w(TAG, "Device not found.  Unable to connect.");
			return false;
		}
		// We want to directly connect to the device, so we are setting the
		// autoConnect
		// parameter to false.
		gatt = device.connectGatt(this, false, executor);
		Log.d(TAG, "Trying to create a new connection.");
		deviceAddress = address;
		connectionState = STATE_CONNECTING;
		return true;
	}

	/**
	 * Disconnects an existing connection or cancel a pending connection. The
	 * disconnection result is reported asynchronously through the
	 * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
	 * callback.
	 */
	public void disconnect() {
		if (adapter == null || gatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		gatt.disconnect();
	}

	/**
	 * After using a given BLE device, the app must call this method to ensure
	 * resources are released properly.
	 */
	public void close() {
		if (gatt == null) {
			return;
		}
		gatt.close();
		gatt = null;
	}

	public void readCharacteristic(BluetoothGatt gatt) {
		BluetoothGattCharacteristic characteristic;
		if (adapter == null || gatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		characteristic = gatt.getService(Constant.SENSOR_SERVICE)
				.getCharacteristic(Constant.SENSOR_DATA_CHAR);
		gatt.readCharacteristic(characteristic);
	}

	private void notifyAllSensor(BluetoothGatt gatt) {

		if (adapter == null || gatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}

		List<BluetoothGattCharacteristic> list = gatt.getService(
				Constant.SENSOR_SERVICE).getCharacteristics();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getProperties() == BluetoothGattCharacteristic.PROPERTY_NOTIFY) {
				//Log.i("Anbu", "UUID: " + list.get(i).getUuid());
				gatt.setCharacteristicNotification(list.get(i), true);
			}
		}

	}

	public void writeCMDforFirmwireVersion() {
		Log.i(TAG, "onWrite");
		if (gatt != null) {
			BluetoothGattCharacteristic characteristic = gatt.getService(
					Constant.SENSOR_SERVICE).getCharacteristic(
					Constant.SENSOR_CONFIG_CHAR);
			characteristic.setValue(new byte[] { Constant.BLE_CMD_VERSION,
					(byte) 0x00 });
			gatt.writeCharacteristic(characteristic);
		}
	}

	public void writeCMDforStatus() {
		Log.i(TAG, "onWrite");
		if (gatt != null) {
			BluetoothGattCharacteristic characteristic = gatt.getService(
					Constant.SENSOR_SERVICE).getCharacteristic(
					Constant.SENSOR_CONFIG_CHAR);
			characteristic.setValue(new byte[] { Constant.BLE_CMD_STATUS,
					(byte) 0x00 });
			gatt.writeCharacteristic(characteristic);
		}
	}

	static SensoPlexLEDState ledState = Constant.SensoPlexLEDState.LEDGreen;

	public void writeCMDfortoggleLED() {
		Log.i(TAG, "onWrite");
		if (gatt != null) {
			Byte lightColor = Constant.BLE_CMD_LEDSYSCONTROL;
			if (ledState == Constant.SensoPlexLEDState.LEDSystemControl) {
				lightColor = Constant.BLE_CMD_LEDSYSCONTROL;
				ledState = Constant.SensoPlexLEDState.LEDGreen;
			} else if (ledState == Constant.SensoPlexLEDState.LEDGreen) {
				lightColor = Constant.BLE_CMD_LEDGREEN;
				ledState = Constant.SensoPlexLEDState.LEDRed;
			} else if (ledState == Constant.SensoPlexLEDState.LEDRed) {
				lightColor = Constant.BLE_CMD_LEDRED;
				ledState = Constant.SensoPlexLEDState.LEDSystemControl;
			}
			BluetoothGattCharacteristic characteristic = gatt.getService(
					Constant.SENSOR_SERVICE).getCharacteristic(
					Constant.SENSOR_CONFIG_CHAR);
			characteristic.setValue(new byte[] { Constant.BLE_CMD_SETLED,
					lightColor });
			gatt.writeCharacteristic(characteristic);
		}
	}

	public void writeCMDforStartStreaming() {
		Log.i(TAG, "onWrite");
		if (gatt != null) {
			BluetoothGattCharacteristic characteristic = gatt.getService(
					Constant.SENSOR_SERVICE).getCharacteristic(
					Constant.SENSOR_CONFIG_CHAR);
			characteristic.setValue(new byte[] { Constant.BLE_CMD_STREAMENABLE,
					0x01 });
			gatt.writeCharacteristic(characteristic);
		}
	}

	public void writeCMDforStopStreaming() {
		Log.i(TAG, "onWrite");
		if (gatt != null) {
			BluetoothGattCharacteristic characteristic = gatt.getService(
					Constant.SENSOR_SERVICE).getCharacteristic(
					Constant.SENSOR_CONFIG_CHAR);
			characteristic.setValue(new byte[] { Constant.BLE_CMD_STREAMENABLE,
					0x00 });
			gatt.writeCharacteristic(characteristic);
		}
	}
	
}
