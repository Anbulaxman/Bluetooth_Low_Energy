package com.anbu.sensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.anbu.sensor.R;
import com.anbu.sensors.service.BleService;
import com.anbu.sensors.util.Constant;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class RotationVectorDemo extends Activity implements OnClickListener{
    private GLSurfaceView mGLSurfaceView;
    private MyRenderer mRenderer;
    
    private TextView textStatus;
	private Button btnStartSensor;
	private FrameLayout layout;
    
    private final static String TAG = "Anbu";

	public static final String EXTRAS_DEVICE_NAME = "DeviceName";
	public static final String EXTRAS_DEVICE_ADDRESS = "DeviceAddress";
	
    private String deviceName;
    private String deviceAddress;
    private BleService bleService;
    private boolean isConnected = false;
    
    private float[] mRotationMatrix = new float[16];
    private float[] resultingAngles = new float[3];
    private static final boolean TRY_TRANSPOSED_VERSION = false;
    final static float rad2deg = (float) (180.0f / Math.PI);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo3d_activity);
        
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        
        btnStartSensor = (Button) findViewById(R.id.btnStartSensor);
        textStatus = (TextView) findViewById(R.id.textStatus);
        layout = (FrameLayout) findViewById(R.id.layout);
        btnStartSensor.setOnClickListener(this);
        
        // Activity
        mRenderer = new MyRenderer();
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mRenderer);
        layout.addView(mGLSurfaceView);
        
        getActionBar().setTitle(deviceName);
	     getActionBar().setDisplayHomeAsUpEnabled(true);
	     
	     final Intent gattServiceIntent = new Intent(this, BleService.class);
	     bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
        
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    @Override
	protected void onStop() {
		super.onStop();

	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        bleService = null;
    }
    
    boolean flag = true;
	@Override
	public void onClick(View v) {

		if(v==btnStartSensor)
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
		}
	};

    class MyRenderer implements GLSurfaceView.Renderer{
        private Cube mCube;
        private Sensor mRotationVectorSensor;
        

        public MyRenderer() {

            mCube = new Cube();
            // initialize the rotation matrix to identity
            mRotationMatrix[ 0] = 1;
            mRotationMatrix[ 4] = 1;
            mRotationMatrix[ 8] = 1;
            mRotationMatrix[12] = 1;
        }

        public void onDrawFrame(GL10 gl) {
            // clear screen
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

            // set-up modelview matrix
            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslatef(0, 0, -3.0f);
//            float[] result = new float[16];
//            SensorManager.remapCoordinateSystem(mRotationMatrix,
//              SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
//              result);
//            gl.glMultMatrixf(result, 0);
            gl.glMultMatrixf(mRotationMatrix, 0);

            // draw our object
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

            mCube.draw(gl);
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // set view-port
            gl.glViewport(0, 0, width, height);
            // set projection matrix
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // dither is enabled by default, we don't need it
            gl.glDisable(GL10.GL_DITHER);
            // clear screen in white
            gl.glClearColor(1,1,1,1);
        }

        class Cube {
            // initialize our cube
            private FloatBuffer mVertexBuffer;
            private FloatBuffer mColorBuffer;
            private ByteBuffer  mIndexBuffer;

            public Cube() {
                final float vertices[] = {
                        -1, -1, -1,		 1, -1, -1,
                         1,  1, -1,	    -1,  1, -1,
                        -1, -1,  1,      1, -1,  1,
                         1,  1,  1,     -1,  1,  1,
                };

                final float colors[] = {
                        0,  0,  0,  1,  1,  0,  0,  1,
                        1,  1,  0,  1,  0,  1,  0,  1,
                        0,  0,  1,  1,  1,  0,  1,  1,
                        1,  1,  1,  1,  0,  1,  1,  1,
                };

                final byte indices[] = {
                        0, 4, 5,    0, 5, 1,
                        1, 5, 6,    1, 6, 2,
                        2, 6, 7,    2, 7, 3,
                        3, 7, 4,    3, 4, 0,
                        4, 7, 6,    4, 6, 5,
                        3, 0, 1,    3, 1, 2
                };

                ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
                vbb.order(ByteOrder.nativeOrder());
                mVertexBuffer = vbb.asFloatBuffer();
                mVertexBuffer.put(vertices);
                mVertexBuffer.position(0);

                ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
                cbb.order(ByteOrder.nativeOrder());
                mColorBuffer = cbb.asFloatBuffer();
                mColorBuffer.put(colors);
                mColorBuffer.position(0);

                mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
                mIndexBuffer.put(indices);
                mIndexBuffer.position(0);
            }

            public void draw(GL10 gl) {
                gl.glEnable(GL10.GL_CULL_FACE);
                gl.glFrontFace(GL10.GL_CW);
                gl.glShadeModel(GL10.GL_SMOOTH);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
            }            
        }
    }
    
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
            	switch(bundle.getInt("CMD"))
  			  	{
  			    
  			  		case Constant.PDI_CMD_STREAMRECORD:
  			  			float[] gyroscope = bundle.getFloatArray("GYROSCOPE");
  			  			float[] accelerometer = bundle.getFloatArray("ACCELEROMETER");
  			  			//float[] quaternion = bundle.getFloatArray("QUATERNION");
  			  			float[] magnetometer = bundle.getFloatArray("MAGNETOMETER");
  			  			if(accelerometer!=null && magnetometer!=null)
  			  			SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometer,
  			  				magnetometer);
//  			  			mRotationMatrix = transpose(mRotationMatrix);
//  			  		resultingAngles[0] = (float) (Math.asin(mRotationMatrix[4]));
//  			  	   final float cosC = (float) Math.cos(resultingAngles[0]);
//  			  	   resultingAngles[0] = resultingAngles[0] * rad2deg;
//  			  	   resultingAngles[2] = (float) (Math.acos(mRotationMatrix[0] / cosC))
//  			  	     * rad2deg;
//  			  	   resultingAngles[1] = (float) (Math.acos(mRotationMatrix[5] / cosC))
//  			  	     * rad2deg;
   			  			break;
  			  		default:
  			  	}
            	
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
    
    private float[] transpose(float[] source) {
    	  final float[] result = source.clone();
    	  if (TRY_TRANSPOSED_VERSION) {
    	   result[1] = source[4];
    	   result[2] = source[8];
    	   result[4] = source[1];
    	   result[6] = source[9];
    	   result[8] = source[2];
    	   result[9] = source[6];
    	  }
    	  // the other values in the matrix are not relevant for rotations
    	  return result;
   }

}
