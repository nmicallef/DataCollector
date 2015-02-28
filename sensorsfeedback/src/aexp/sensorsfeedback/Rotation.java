package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Rotation extends Service implements SensorEventListener{
	long end;

	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	try{
    		end = System.currentTimeMillis()+10000;
    		sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );
    		sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),200000 );
    	}catch (Exception e){}
    }

    private SensorManager sensorManager;

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		PrintWriter captureFile;
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
   	 	try {
        	if (tm.getDeviceId() != null){
         		File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_rotation_capture.csv" );

         		captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
         		Date d = new Date();
         		Calendar c = Calendar.getInstance();
         		c.setTime(d);
         		String temp = Long.toString( event.timestamp)+","+ c.getTimeInMillis()+","+d.toString()+","+event.sensor.getName();
         		for( int i = 0 ; i < event.values.length ; ++i ) {
         			temp = temp +","+Float.toString( event.values[i] );
         		}
         		captureFile.println(temp);
         		captureFile.close();
         		captureFileName.setReadable(true, false);
         		captureFileName.setWritable(true, false);
         		captureFileName.setExecutable(true, false);

         		if (System.currentTimeMillis() > end){
             		sensorManager.unregisterListener( this );
             		stopSelf();
             	}
         	}
        } catch( IOException ex ) {
            Log.e( "rotation_writing", ex.getMessage(), ex );
        }catch( Exception e ) {

        }

   	 	
	}
}
