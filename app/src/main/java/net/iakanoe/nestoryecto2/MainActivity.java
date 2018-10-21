package net.iakanoe.nestoryecto2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

public class MainActivity extends WearableActivity{
	SensorManager sensorManager;
	final float[] accelerometerReading = {0f, 0f, 0f};
	final float[] magnetometerReading = {0f, 0f, 0f};
	Orientation actualOrientation;
	int motor1 = 0;
	int motor2 = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		// Enables Always-on
		setAmbientEnabled();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		
		Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magne = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		if(accel != null)
			sensorManager.registerListener(sensorEventListener, accel, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
		
		if(magne != null)
			sensorManager.registerListener(sensorEventListener, magne, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		sensorManager.unregisterListener(sensorEventListener);
	}
	
	SensorEventListener sensorEventListener = new SensorEventListener(){
		@Override public void onSensorChanged(SensorEvent event){
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
			}
			
			final float[] rotationMatrix = new float[9];
			final float[] orientationData = new float[3];
			SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
			SensorManager.getOrientation(rotationMatrix, orientationData);
			actualOrientation = new Orientation(orientationData);
			computeMotorValues();
			showOnScreen();
			sendViaBT();
		}
		
		@Override public void onAccuracyChanged(Sensor sensor, int accuracy){}
	};
	
	void computeMotorValues(){
		double x = (double)actualOrientation.getRoll();
		double y = (double)actualOrientation.getPitch();
		double phi = Math.atan2(y, x) + (Math.PI / 4);
		double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		motor1 = (int)Math.round(r * Math.cos(phi));
		motor2 = (int)Math.round(r * Math.sin(phi));
	}
	
	void showOnScreen(){
		float x = actualOrientation.getRoll();
		float y = actualOrientation.getPitch();
		//poner imagen de flecha y mover a (x;y) en pantalla.
		//tambien se puede rotar a phi
	}
	
	void sendViaBT(){
		//enviar datos {motor1, motor2} por bluetooth al arduino
	}
	
	private class Orientation {
		private float yaw;
		private float pitch;
		private float roll;
		
		Orientation(float[] data){
			if(data.length != 3){
				Log.e(
					"ArrayNotLongEnough",
					"The data array for an orientation object must have three values.",
					new ArrayIndexOutOfBoundsException("The data array for an orientation object must have three values.")
				);
				return;
			}
			
			this.yaw = data[0];
			this.pitch = data[1];
			this.roll = data[2];
		}
		
		public float getYaw(){
			return yaw;
		}
		
		public float getPitch(){
			return pitch;
		}
		
		public float getRoll(){
			return roll;
		}
	}
}