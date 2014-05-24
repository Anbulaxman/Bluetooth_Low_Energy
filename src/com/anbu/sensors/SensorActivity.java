package com.anbu.sensors;

import com.anbu.sensor.R;
import com.anbu.sensors.service.BleService;
import com.anbu.sensors.util.Constant;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SensorActivity extends Activity implements OnClickListener {
	
	private final static String TAG = "Anbu";

	public static final String EXTRAS_DEVICE_NAME = "DeviceName";
	public static final String EXTRAS_DEVICE_ADDRESS = "DeviceAddress";
	
	private TextView textStatus, textData;
	private Button btnToggleLED, btnStartSensor, btnVersion, btnStatus;
	
	private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    private boolean isConnected = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensor_activity);

		final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        textData = (TextView) findViewById(R.id.textData);
        textStatus = (TextView) findViewById(R.id.textStatus);
		btnToggleLED = (Button) findViewById(R.id.btnToggleLED);
		btnStartSensor = (Button) findViewById(R.id.btnStartSensor);
		btnVersion = (Button) findViewById(R.id.btnVersion);
		btnStatus = (Button) findViewById(R.id.btnStatus);

		btnToggleLED.setOnClickListener(this);
		btnStartSensor.setOnClickListener(this);
		btnVersion.setOnClickListener(this);
		btnStatus.setOnClickListener(this);
		
		 getActionBar().setTitle(deviceName);
	     getActionBar().setDisplayHomeAsUpEnabled(true);
	     
	     final Intent gattServiceIntent = new Intent(this, BleService.class);
	     bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

	}

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

	@Override
	protected void onStop() {
		super.onStop();

	}
	
	@Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bleService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {   	
        getMenuInflater().inflate(R.menu.demo, menu);        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_demo:
            	finish();
            	final Intent intent = new Intent(this, RotationVectorDemo.class);
                intent.putExtra(SensorActivity.EXTRAS_DEVICE_NAME, deviceName);
                intent.putExtra(SensorActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
                startActivity(intent);
                break;
        }
        return true;
    }
    
	boolean flag = true;
	@Override
	public void onClick(View v) {

		if(v==btnToggleLED)
		{
			bleService.writeCMDfortoggleLED();
		}else if(v==btnStartSensor)
		{
			if(flag)
			{
				bleService.writeCMDforStartStreaming();
				btnStartSensor.setText("Stop Sensor");
				flag=false;
			}
			else 
			{
				bleService.writeCMDforStopStreaming();
				btnStartSensor.setText("Start Sensor");
				flag = true;
			}
		}else if(v==btnVersion)
		{
			bleService.writeCMDforFirmwireVersion();
		}else if(v==btnStatus)
		{
			bleService.writeCMDforStatus();
		}
	};
	
	 private final ServiceConnection serviceConnection = new ServiceConnection() {

	        @Override
	        public void onServiceConnected(ComponentName componentName, IBinder service) {
	            bleService = ((BleService.LocalBinder) service).getService();
	            if (!bleService.initialize()) {
	                Log.e(TAG, "Unable to initialize Bluetooth");
	                finish();
	            }
	            // Automatically connects to the device upon successful start-up initialization.
	            bleService.connect(deviceAddress);
	        }

	        @Override
	        public void onServiceDisconnected(ComponentName componentName) {
	            bleService = null;
	        }
	  };
	  
	  private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            final String action = intent.getAction();
	            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
	                isConnected = true;
	                textStatus.setText("Connected");
	                Log.i(TAG, "Device connected");
	            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
	                isConnected = false;
	                textStatus.setText("Disconnected");
	                Log.i(TAG, "Device disconnected");
	            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

	            } else if (BleService.ACTION_DATA_AVAILABLE.equals(action)) {
	            	Bundle bundle = intent.getExtras();
	            	String str="";
	            	switch(bundle.getInt("CMD"))
	  			  	{
	  			    
	  			  		case Constant.BLE_CMD_VERSION:
	  			  			String version = bundle.getString("VERSION","");
	  			  			String date = bundle.getString("VERSIONDATE","");
	  			  			str = "Firmwire Version  "+version+ "  Date: "+date;
	  			  			break;
	  			  		case Constant.BLE_CMD_STATUS:
	  			  			String batteryVoltage = bundle.getString("BATTERY","");
	  			  			String chargingState = bundle.getString("CHARGINGSTATE","");
	  			  			str = "Battery  "+batteryVoltage+ ".  "+chargingState;
	  			  			break;
	  			  		case Constant.PDI_CMD_STREAMRECORD:
	  			  			float[] gyroscope = bundle.getFloatArray("GYROSCOPE");
	  			  			float[] accelerometer = bundle.getFloatArray("ACCELEROMETER");
	  			  			float[] quaternion = bundle.getFloatArray("QUATERNION");
	  			  			float[] magnetometer = bundle.getFloatArray("MAGNETOMETER");
	  			  			if(gyroscope!=null)
	  			  				str = "Gyroscope "+ "x: "+ gyroscope[0]+" y: "+gyroscope[1]+" z: "+gyroscope[2]+"\n";
	  			  			if(accelerometer!=null)
	  			  				str = str + "Accelerometer "+ "x: "+ accelerometer[0]+" y: "+accelerometer[1]+" z: "+accelerometer[2]+"\n";
	  			  			if(quaternion!=null)
	  			  				str = str + "Quaternion "+ "w: "+ quaternion[0]+" x: "+ quaternion[1]+" y: "+quaternion[2]+" z: "+quaternion[3]+"\n";
	  			  			if(magnetometer!=null)
	  			  				str = str + "Magnetometer "+ "x: "+ magnetometer[0]+" y: "+magnetometer[1]+" z: "+magnetometer[2]+"\n";

	  			  			break;
	  			  		default:
	  			  	}
	            	
	            	textData.setText(str);
	            }
	        }
	    };
	    
	    private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BleService.ACTION_DATA_AVAILABLE);
	        return intentFilter;
	    }
}