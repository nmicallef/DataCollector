package aexp.sensorsfeedback;


import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Config;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Toast;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Date;



public class SamplingService extends Service implements SensorEventListener,LocationListener {
	static final String LOG_TAG = "SAMPLINGSERVICE";
	static final boolean KEEPAWAKE_HACK = false;
	static final boolean MINIMAL_ENERGY = false;
	static final long MINIMAL_ENERGY_LOG_PERIOD = 15000L;

	static String currentApp = "";
	static String phoneid ="";

	static Thread currentTouch = null;
	static Process proc=null;

	  private LocationManager locationManager;
	  private String provider;


	/*public void onCreate(){
		super.onCreate();
		
		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onStartCommand" );

        SharedPreferences appPrefs = getSharedPreferences(
                                        Sensors.PREF_FILE,
                                        MODE_PRIVATE );
        rate = appPrefs.getInt(
                    Sensors.PREF_SAMPLING_SPEED,
                    SensorManager.SENSOR_DELAY_UI );
        if( Sensors.DEBUG )
        	Log.d( LOG_TAG, "rate: "+rate );

		screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
		IntentFilter screenOffFilter = new IntentFilter();
		screenOffFilter.addAction( Intent.ACTION_SCREEN_OFF );
		screenOffFilter.addAction( Intent.ACTION_SCREEN_ON );
		//if( KEEPAWAKE_HACK )
		registerReceiver( screenOffBroadcastReceiver, screenOffFilter );
		sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );


		startSampling();
		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onStartCommand ends" );
	}*/

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand( intent, flags, startId );

		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onStartCommand" );

        SharedPreferences appPrefs = getSharedPreferences(
                                        Sensors.PREF_FILE,
                                        MODE_PRIVATE );
        rate = appPrefs.getInt(
                    Sensors.PREF_SAMPLING_SPEED,
                    SensorManager.SENSOR_DELAY_UI );
        if( Sensors.DEBUG )
        	Log.d( LOG_TAG, "rate: "+rate );

		screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
		IntentFilter screenOffFilter = new IntentFilter();
		screenOffFilter.addAction( Intent.ACTION_SCREEN_OFF );
		screenOffFilter.addAction( Intent.ACTION_SCREEN_ON );
		//if( KEEPAWAKE_HACK )
		registerReceiver( screenOffBroadcastReceiver, screenOffFilter );
		sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );

		/*locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Criteria criteria = new Criteria();
	    provider = locationManager.getBestProvider(criteria, false);
	    Location location = locationManager.getLastKnownLocation(provider);*/

		startSampling();
		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onStartCommand ends" );

		return START_NOT_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onDestroy" );
		stopSampling();
		//if( KEEPAWAKE_HACK )
		unregisterReceiver( screenOffBroadcastReceiver );
	}

	public IBinder onBind(Intent intent) {
		return null;	// cannot bind
	}

// SensorEventListener
    public void onAccuracyChanged (Sensor sensor, int accuracy) {
    }

    private String getCurrentTopActivity() {

        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        //return ar.topActivity.getClassName().toString();
        return ar.topActivity.getClassName().toString()+";"+ar.topActivity.getPackageName();
    }

    private String getApplicationNameFromPackage(String pack) {
    	PackageManager pm = getApplicationContext().getPackageManager();
    	ApplicationInfo ai;
    	try {
    		ai = pm.getApplicationInfo( pack, 0);
    	} catch (NameNotFoundException e) {
    		ai = null;
    	}
    	String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    	return applicationName;
    }
    public void onSensorChanged(SensorEvent sensorEvent) {
		++logCounter;
    	if( !MINIMAL_ENERGY ) {
    		String[] currentActivity = getCurrentTopActivity().split(";");
    		String foregroundapp = currentActivity[0].toString();
    		//String appname = getApplicationNameFromPackage(currentActivity[1]);
    		//String foregroundapp =  getCurrentTopActivity();
    		//Log.d( LOG_TAG, "currlog: "+foregroundapp+","+currentActivity[1]+","+appname);
    		//Log.d( LOG_TAG, "currloga: "+currentActivity.length+","+currentActivity[0].toString()+","+currentActivity[1].toString());

    		try{
    		PrintWriter tCaptureFile = (PrintWriter)captureFiles.get(sensorEvent.sensor.getName());

    		if( tCaptureFile != null ) {
    			Calendar c = Calendar.getInstance();
    			Date d = new Date();
    			c.setTime(d);
    			if (usageCaptureFile!= null){

    				if (!foregroundapp.equals(currentApp)){
    					if (currentApp.length() > 2){
    						long rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
    						long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
    						String temp = c.getTimeInMillis()+",end,"+d.toString()+","+currentApp+","+rxBytes+","+txBytes;
    						usageCaptureFile.println(temp);
    					}
    					//Log.d( LOG_TAG, "currlog: "+foregroundapp+","+getApplicationNameFromPackage(currentActivity[1].toString()));
        				currentApp = foregroundapp;
        				mStartRX = TrafficStats.getTotalRxBytes();
        				mStartTX = TrafficStats.getTotalTxBytes();
        				Date d2 = new Date();
            			c.setTime(d2);
            			//String temp = c.getTimeInMillis()+",start,"+d.toString()+","+foregroundapp+","+appname+","+currentActivity[1];
            			String temp = c.getTimeInMillis()+",start,"+d.toString()+","+foregroundapp+","+currentActivity[1].toString()+","+getApplicationNameFromPackage(currentActivity[1].toString());

        				usageCaptureFile.println(temp);
        				//usageCaptureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
        				//locationManager.requestLocationUpdates(provider, 40, 1, this);
        			}
    			}
    			String tmp ="";

    			/*if ((sensorEvent.sensor.getName().contains("Acceleration") || sensorEvent.sensor.getName().contains("Accelerometer")) && (!sensorEvent.sensor.getName().contains("Linear")) ){
    				tmp = phoneid+",?,"+Long.toString(sensorEvent.timestamp);
    			}else{
    				tmp = Long.toString( sensorEvent.timestamp)+","+ c.getTimeInMillis()+","+sensorEvent.sensor.getName();
    			}*/
    			tmp = Long.toString( sensorEvent.timestamp)+","+ c.getTimeInMillis()+","+sensorEvent.sensor.getName();
    			for( int i = 0 ; i < sensorEvent.values.length ; ++i ) {
    				tmp = tmp +","+Float.toString( sensorEvent.values[i] );
    			}
    			tCaptureFile.println(tmp);


    			//tCaptureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, tmp));
    		}

    		}catch(Exception e){

    		}
    	} else {
    		++logCounter;
    		if( ( logCounter % MINIMAL_ENERGY_LOG_PERIOD ) == 0L )
    			Log.d( LOG_TAG, "logCounter: "+logCounter+" at "+new Date().toString());
    	}
    }



	private void stopSampling() {
		if( !samplingStarted )
			return;
		/*if( generateUserActivityThread != null ) {
			generateUserActivityThread.stopThread();
			generateUserActivityThread = null;
		}*/
		Calendar c = Calendar.getInstance();
		Date d = new Date();
		c.setTime(d);
		long rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
		long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
		String temp = c.getTimeInMillis()+",end,"+d.toString()+","+currentApp+","+rxBytes+","+txBytes;
		usageCaptureFile.println(temp);

        if( sensorManager != null ) {
        	if( Config.DEBUG )
        		Log.d( LOG_TAG, "unregisterListener/SamplingService" );
            //sensorManager.unregisterListener( this );
        	List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ORIENTATION);
        	for( int i = 0 ; i < sensors.size() ; ++i ){
    		    sensorManager.unregisterListener(this,sensors.get(i));
        	}
		}

        if( locationManager != null ) {
            locationManager.removeUpdates(this);
		}

        Iterator it = captureFiles.entrySet().iterator();

        while (it.hasNext()){
        	HashMap.Entry pairs = (HashMap.Entry)it.next();
        	PrintWriter tCaptureFile = (PrintWriter)pairs.getValue();
        	if (tCaptureFile != null){
        		tCaptureFile.flush();
        		tCaptureFile.close();
    			tCaptureFile = null;
        	}
        }
        captureFiles = null;
        if( usageCaptureFile != null ) {
        	usageCaptureFile.flush();
            usageCaptureFile.close();
			usageCaptureFile = null;
        }
        /*if( gpsCaptureFile != null ) {
        	gpsCaptureFile.flush();
            gpsCaptureFile.close();
			gpsCaptureFile = null;
        }*/

		samplingStarted = false;
		Date samplingStoppedTimeStamp = new Date();
		long secondsEllapsed =
			( samplingStoppedTimeStamp.getTime() -
			  samplingStartedTimeStamp.getTime() ) / 1000L;
		Log.d(LOG_TAG, "Sampling started: "+
				samplingStartedTimeStamp.toString()+
				"; Sampling stopped: "+
				samplingStoppedTimeStamp.toString()+
				" ("+secondsEllapsed+" seconds) "+
				"; samples collected: "+logCounter );
	}

	private void startSampling() {
		if( samplingStarted )
			return;

			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			captureFiles = new HashMap();

			mStartRX = TrafficStats.getTotalRxBytes();
			mStartTX = TrafficStats.getTotalTxBytes();

			//List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_ORIENTATION,Sensor.TYPE_MAGNETIC_FIELD);
			//List<Sensor> sensors = new ArrayList<Sensor>();
			//sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
			//sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION));
			//sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
			
			List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ORIENTATION);

			for( int i = 0 ; i < sensors.size() ; ++i ){
           	 String tempname = sensors.get(i).getName().replaceAll(" ","_");
           	 //File captureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_"+tempname+".csv" );
           	 File captureFileName = new File(getBaseContext().getFilesDir(), tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_"+tempname+".csv" );

           	 try {
                    captureFiles.put(sensors.get(i).getName(), new PrintWriter( new FileWriter( captureFileName, true )));

                } catch( IOException ex ) {
                    Log.e( LOG_TAG, ex.getMessage(), ex );
                }
           	 captureFileName.setReadable(true, false);
      	 	 captureFileName.setWritable(true, false);
      	 	 captureFileName.setExecutable(true, false);
           	if( Sensors.DEBUG ){
           		Log.d( LOG_TAG, "registerListener/SamplingService" );
           	}
           	sensorManager.registerListener(this,sensors.get( i ),SensorManager.SENSOR_DELAY_UI );
           }

			/*List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
            for( int i = 0 ; i < sensors.size() ; ++i ){
            	 String tempname = sensors.get(i).getName().replaceAll(" ","_");
            	 //File captureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_"+tempname+".csv" );
            	 File captureFileName = new File( "/mnt/sdcard", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_"+tempname+".csv" );
            	 try {
                     captureFiles.put(sensors.get(i).getName(), new PrintWriter( new FileWriter( captureFileName, true )));

                 } catch( IOException ex ) {
                     Log.e( LOG_TAG, ex.getMessage(), ex );
                 }

            	if( Sensors.DEBUG ){
            		Log.d( LOG_TAG, "registerListener/SamplingService" );
            	}
            	sensorManager.registerListener(this,sensors.get( i ),rate );
            }*/

            samplingStartedTimeStamp = new Date();

			if( Sensors.DEBUG )
				Log.d( LOG_TAG, "Capture file created" );

			//File gpsCaptureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_gps.csv" );
			//File usageCaptureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_usage.csv" );
            //File gpsCaptureFileName = new File( "/mnt/sdcard", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_gps.csv" );
			File usageCaptureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_usage.csv" );
            try {
                //gpsCaptureFile = new PrintWriter( new FileWriter( gpsCaptureFileName, true ) );
                usageCaptureFile = new PrintWriter( new FileWriter( usageCaptureFileName, true ) );
            } catch( IOException ex ) {
                Log.e( LOG_TAG, ex.getMessage(), ex );
            }
            usageCaptureFileName.setReadable(true, false);
       	 	usageCaptureFileName.setWritable(true, false);
       	 	usageCaptureFileName.setExecutable(true, false);
			samplingStarted = true;

	}


    //private String sensorName;
    private int rate = SensorManager.SENSOR_DELAY_UI;
    private SensorManager sensorManager;
    private HashMap captureFiles;
    //private PrintWriter gpsCaptureFile;
    private PrintWriter usageCaptureFile;
	private boolean samplingStarted = false;
	private ScreenOffBroadcastReceiver screenOffBroadcastReceiver = null;
	//private Sensor ourSensor;
	//private GenerateUserActivityThread generateUserActivityThread = null;
	private long logCounter = 0;
	private PowerManager.WakeLock sampingInProgressWakeLock;
	private Date samplingStartedTimeStamp;

	private Long mStartRX;
	private Long mStartTX;

	class ScreenOffBroadcastReceiver extends BroadcastReceiver {
		private static final String LOG_TAG = "ScreenOffBroadcastReceiver";

		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			    Log.d( LOG_TAG, "onReceive: screen off" );
				if( sensorManager != null && samplingStarted ) {
					stopSampling();
					currentApp = "";
				}

			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			    Log.d( LOG_TAG, "onReceive: screen on");
			    samplingStarted = false;
			    if( sensorManager != null){
					Log.d( LOG_TAG, "onReceive: start sampling");
					startSampling();
				}else{
					Log.d( LOG_TAG, "onReceive: do not start sampling");
					sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );
					startSampling();
				}
			}

			/*if( sensorManager != null && samplingStarted ) {
				if( generateUserActivityThread != null ) {
					generateUserActivityThread.stopThread();
					generateUserActivityThread = null;
				}
				generateUserActivityThread = new GenerateUserActivityThread();
				generateUserActivityThread.start();
			}*/
		}
	}

	public void onLocationChanged(Location location) {
		try{

			Calendar c = Calendar.getInstance();
			Date d = new Date();
			c.setTime(d);
			double lat = (double) (location.getLatitude());
			double lng = (double) (location.getLongitude());
			String temp = c.getTimeInMillis()+","+d.toString()+","+"GPS coordinates,"+lat+","+lng+","+currentApp;
			//gpsCaptureFile.println(temp);
			//gpsCaptureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
		}catch(Exception e){}
		//locationManager.removeUpdates(this);
	}

	public void onProviderDisabled(String provider) {	}

	public void onProviderEnabled(String provider) {	}

	public void onStatusChanged(String provider, int status, Bundle extras) {	}

	/*class GenerateUserActivityThread extends Thread {
		public void run() {
			if( Sensors.DEBUG )
				Log.d( LOG_TAG, "Waiting 2 sec for switching back the screen ..." );
			try {
				Thread.sleep( 2000L );
			} catch( InterruptedException ex ) {}
			if( Sensors.DEBUG )
				Log.d( LOG_TAG, "User activity generation thread started" );

			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			userActivityWakeLock =
				pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
						"GenerateUserActivity");
			userActivityWakeLock.acquire();
			if( Sensors.DEBUG )
				Log.d( LOG_TAG, "User activity generation thread exiting" );
		}

		public void stopThread() {
			if( Sensors.DEBUG )
				Log.d( LOG_TAG, "User activity wake lock released" );
			userActivityWakeLock.release();
			userActivityWakeLock = null;
		}

		PowerManager.WakeLock userActivityWakeLock;
	}*/
}

