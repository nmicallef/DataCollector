package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {


	public static final String PREF_FILE2 = "prefs2";
	private static final String SAMPLING_SERVICE_POSITION_KEY = "samplingServicePositon";
    @Override
    public void onReceive(final Context context, Intent intent) {
    	
    	HashMap sensorsList = new HashMap();
        String filedir=context.getApplicationContext().getFilesDir().getPath()+"/";
        int samplingServiceRate =1;
       
        try{
        	File tempfz = new File(filedir+"settings.txt");
		
        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;
    	    	while ((line = br.readLine()) != null) {
			
        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]);         				
    	    		}else {
        				sensorsList.put(tempbuff[0], tempbuff[1]);
        			}
        	}
        	br.close();
        		
        	Intent iBatteryLevelService = new Intent(context, BatteryLevel.class);
        	PendingIntent piBatteryLevelService = PendingIntent.getService(context, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
        	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(piBatteryLevelService);
        	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piBatteryLevelService);
        	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);

        	Intent iRunningApplicationsService = new Intent(context, RunningApplications.class);
        	PendingIntent piRunningApplicationsService = PendingIntent.getService(context, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
        	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(piRunningApplicationsService);
        	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRunningApplicationsService);
        	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);

        	Boolean state = false;
        	List<String> sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("magnetic")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iMagneticFieldService = new Intent(context, MagneticField.class);
    	    	PendingIntent piMagneticFieldService = PendingIntent.getService(context, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piMagneticFieldService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piMagneticFieldService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
    	    }
    	    
    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("wi-fi")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){	
    	    	Intent iWifiService = new Intent(context, Wifi.class);
    	    	PendingIntent piWifiService = PendingIntent.getService(context, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piWifiService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piWifiService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
    	    }
    	    
    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("light") || str.toLowerCase().contains("rgb")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iLightService = new Intent(context, Lightv3.class);
    	    	PendingIntent piLightService = PendingIntent.getService(context, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piLightService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piLightService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
    	    }
    	    
    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("accelerometer") || str.toLowerCase().contains("linear") || str.toLowerCase().contains("acceleration")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iAccelerometerService = new Intent(context, Accelerometer.class);
    	    	PendingIntent piAccelerometerService = PendingIntent.getService(context, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piAccelerometerService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piAccelerometerService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
    	    }
    	    
    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("rotation")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iRotationService = new Intent(context, Rotation.class);
    	    	PendingIntent piRotationService = PendingIntent.getService(context, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piRotationService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRotationService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);
    	    }
	    	/*(new Thread(new Runnable() {
        		public void run() {
        			Intent i = new Intent();
        			i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Accelerometer" );
        			context.startService( i );
        		}
        	})).start();

	    	(new Thread(new Runnable() {
        		public void run() {
        			Intent i = new Intent();
        			i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Rotation" );
        			context.startService( i );
        		}
        	})).start();*/

    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("microphone")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iNoiseService = new Intent(context, Sound.class);
    	    	PendingIntent piNoiseService = PendingIntent.getService(context, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piNoiseService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piNoiseService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
    	    }
    	    
    	    state = false;
        	sortedKeys=new ArrayList(sensorsList.keySet());
			Collections.sort(sortedKeys);
			for (String str : sortedKeys) {
				if(str.toLowerCase().contains("gps")){
    				if (sensorsList.get(str).toString().contains("true")){
    					state = true;
    				}
    	    	}
    		}
    	    if (state){
    	    	Intent iGPSService = new Intent(context, GPS.class);
    	    	PendingIntent piGPSService = PendingIntent.getService(context, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    	alarmManager.cancel(piGPSService);
    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piNoiseService);
    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGPSService);
    	    }
    	    
	    	/*Calendar fouramCalendar = Calendar.getInstance();
	    	fouramCalendar.set(Calendar.HOUR_OF_DAY, 4);
	    	fouramCalendar.set(Calendar.MINUTE, 0);
	    	fouramCalendar.set(Calendar.SECOND, 0);
	    	Intent iFilesUploaderService = new Intent(context, FilesUploader.class);
			PendingIntent piFilesUploaderService = PendingIntent.getService(context, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(piFilesUploaderService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fouramCalendar.getTimeInMillis() , 21600000, piFilesUploaderService);*/

	    	/*Calendar threeamCalendar = Calendar.getInstance();
	    	fouramCalendar.set(Calendar.HOUR_OF_DAY, 3);
	    	fouramCalendar.set(Calendar.MINUTE, 0);
	    	fouramCalendar.set(Calendar.SECOND, 0);
	    	Intent iDailyAnalysisService = new Intent(context, DailyAnalysis.class);
	    	PendingIntent piDailyAnalysisService = PendingIntent.getService(context, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piDailyAnalysisService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,threeamCalendar.getTimeInMillis() , 21600000, piDailyAnalysisService);
	    
	    	Calendar tenamCalendar = Calendar.getInstance();
	    	tenamCalendar.set(Calendar.HOUR_OF_DAY, 10);
	    	tenamCalendar.set(Calendar.MINUTE, 30);
	    	tenamCalendar.set(Calendar.SECOND, 0);
	    	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	Intent intent2 = new Intent(context, ReminderService.class);
	    	PendingIntent pendingIntent2 = PendingIntent.getService(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager.cancel(pendingIntent2);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,tenamCalendar.getTimeInMillis() , 21600000, pendingIntent2);*/
	    
	    
	    	(new Thread(new Runnable() {
        		public void run() {
        			Intent i = new Intent();
        			i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SamplingService" );
        			context.startService( i );
        		}
        	})).start();


    		Intent iHeartBeatService = new Intent(context, HeartBeat.class);
			PendingIntent piHeartBeatService = PendingIntent.getService(context, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piHeartBeatService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);


			SharedPreferences appPrefs = context.getSharedPreferences(PREF_FILE2,0 );
	    	SharedPreferences.Editor ed = appPrefs.edit();
	    	ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, true );
	    	ed.commit();
	    
        	}	
        }catch(Exception e){}    	
    	
    }
}
