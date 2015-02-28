package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class HeartBeat extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	(new Thread(new Runnable() {
			public void run() {
				beat.run();
				stopSelf();
	        }
		})).start();
    }

    public static final String PREF_FILE2 = "prefs2";
	private static final String SAMPLING_SERVICE_POSITION_KEY = "samplingServicePositon";

    public Runnable beat = new Runnable() {
    	
    	public String tail( File file ) {
		    RandomAccessFile fileHandler = null;
		    try {
		        fileHandler = new RandomAccessFile( file, "r" );
		        long fileLength = fileHandler.length() - 1;
		        StringBuilder sb = new StringBuilder();

		        for(long filePointer = fileLength; filePointer != -1; filePointer--){
		            fileHandler.seek( filePointer );
		            int readByte = fileHandler.readByte();

		            if( readByte == 0xA ) {
		                if( filePointer == fileLength ) {
		                    continue;
		                }
		                break;

		            } else if( readByte == 0xD ) {
		                if( filePointer == fileLength - 1 ) {
		                    continue;
		                }
		                break;
		            }

		            sb.append( ( char ) readByte );
		        }

		        String lastLine = sb.reverse().toString();
		        return lastLine;
		    } catch( java.io.FileNotFoundException e ) {
		        e.printStackTrace();
		        return null;
		    } catch( java.io.IOException e ) {
		        e.printStackTrace();
		        return null;
		    } finally {
		        if (fileHandler != null )
		            try {
		                fileHandler.close();
		            } catch (IOException e) {
		                /* ignore */
		            }
		    }
		}

    	
    	
    	private void checkThatEverythingIsSwitchedOn(int samplingServiceRate){
    		
    		String dir=getBaseContext().getFilesDir().getPath()+"/";
 			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
 		 	String userid=tm.getDeviceId();
			
    		// check accelerometer
 			try{
    			File tempf = new File(dir+userid+"_accelerometer_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 600000 ){
							Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
	 		    	    	PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piAccelerometerService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
    		// check magneticfield
 			try{
    			File tempf = new File(dir+userid+"_magneticfield_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 600000 ){
							Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
	 		    	    	PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piMagneticFieldService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
 			// check noise
 			try{
    			File tempf = new File(dir+userid+"_noise_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 600000 ){
							Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
	 		    	    	PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piNoiseService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
  
    		// wifi
    		try{
    			File tempf = new File(dir+userid+"_wifi_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 600000 ){
							Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
	 		    	    	PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piWifiService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
						}
    				}
    			}
    		}catch(Exception e){}
    		
    		
    		// light
    		try{
    			File tempf = new File(dir+userid+"_light_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 600000 ){
							Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
					    	PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
					    	AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					    	alarmManager.cancel(piLightService);
					    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
						}
    				}
    			}
    		}catch(Exception e){}

    	}
    	

        public void run() {
      
        	Intent intent = new Intent(getApplicationContext(), Sensors.class);
 		    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 		    boolean found = false;
 		    ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 				  if (service.service.getClassName().toLowerCase().contains("aexp.sensorsfeedback")) {
 			          found = true;
 			      }
 			}
 			String message="";
 			if (found){
 			   message="Sensors are running!!!";
 			   
 			    boolean f = false;
 	 		    manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 				  if (service.service.getClassName().toLowerCase().contains("aexp.sensorsfeedback.samplingservice")) {
 	 			          f = true;
 	 			      }
 	 			}
 			   
 	 			if (f){
 	 				message="Sensors are running!!! aa";
 	 			}else{
 	 				message="Sensors are running!!! bb";
 	 				(new Thread(new Runnable() {
 	 		        	public void run() {
 	 		        		Intent i = new Intent();
 	 		        		i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SamplingService" );
 	 		        		getApplicationContext().startService( i );
 	 		        	}
 	 		        })).start();
 	 			}
 			    
 			}else{
 			   message="Sensors are switched off!!!";

 			    (new Thread(new Runnable() {
 		        	public void run() {
 		        		Intent i = new Intent();
 		        		i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SamplingService" );
 		        		getApplicationContext().startService( i );
 		        	}
 		        })).start();
 			    
 		    	Intent iHeartBeatService = new Intent(getApplicationContext(), HeartBeat.class);
 				PendingIntent piHeartBeatService = PendingIntent.getService(getApplicationContext(), 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
 				AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 			    alarmManager.cancel(piHeartBeatService);
 			    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);

 			    HashMap sensorsList = new HashMap();
 		        String filedir=getBaseContext().getFilesDir().getPath()+"/";
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
 		         			    
 		    	    	Boolean state = false;
 		    	    	List<String> sortedKeys=new ArrayList(sensorsList.keySet());
 		    	    	Collections.sort(sortedKeys);
 		    	    	for (String str : sortedKeys) {
 		    	    		if(str.toLowerCase().contains("wifi")){
 		    	    			if (sensorsList.get(str).toString().contains("true")){
 		    	    				state = true;
 		    	    			}
 		    	    		}
 		    	    	}
 		    	    	if (state){
 		    	    		Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
 		    	    		PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    		alarmManager.cancel(piWifiService);
 		    	    		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piWifiService);
 		    	    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
 		    	    	}
 		    	    	
 		    	    	Intent iBatteryLevelService = new Intent(getApplicationContext(), BatteryLevel.class);
 		    	    	PendingIntent piBatteryLevelService = PendingIntent.getService(getApplicationContext(), 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piBatteryLevelService);
 		    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piBatteryLevelService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);

 		    	    	Intent iRunningApplicationsService = new Intent(getApplicationContext(), RunningApplications.class);
 		    	    	PendingIntent piRunningApplicationsService = PendingIntent.getService(getApplicationContext(), 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piRunningApplicationsService);
 		    	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRunningApplicationsService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);
 			   
 		    	    	state = false;
 		    	    	sortedKeys=new ArrayList(sensorsList.keySet());
 		    	    	Collections.sort(sortedKeys);
 		    	    	for (String str : sortedKeys) {
 		    	    		if(str.toLowerCase().contains("magnetic")){
 		    	    			if (sensorsList.get(str).toString().contains("true")){
 		    	    				state = true;
 		    	    			}
 		    	    		}
 		    	    	}
 		    	    	if (state){
 		    	    		Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
 		    	    		PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    		alarmManager.cancel(piMagneticFieldService);
 		    	    		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piMagneticFieldService);
 		    	    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
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
 		    	    		Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
 		    	    		PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    		alarmManager.cancel(piLightService);
 		    	    		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piLightService);
 		    	    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
 		    	    	}
 		    	    	
 		    	    	state = false;
 		    	    	sortedKeys=new ArrayList(sensorsList.keySet());
 		    	    	Collections.sort(sortedKeys);
 		    	    	for (String str : sortedKeys) {
 		    	    		if(str.toLowerCase().contains("accelerometer") || str.toLowerCase().contains("acceleration") || str.toLowerCase().contains("linear") ){
 		    	    			if (sensorsList.get(str).toString().contains("true")){
 		    	    				state = true;
 		    	    			}
 		    	    		}
 		    	    	}
 		    	    	
 		    	    	if (state){
 		    	    		Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
 		    	    		PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
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
 		    	    		Intent iRotationService = new Intent(getApplicationContext(), Rotation.class);
 		    	    		PendingIntent piRotationService = PendingIntent.getService(getApplicationContext(), 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    		alarmManager.cancel(piRotationService);
 		    	    		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRotationService);
 		    	    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);
 		    	    	}
 		    	    	
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
 		    	    		Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
 		    	    		PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
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
 		    	    		Intent iGPSService = new Intent(getApplicationContext(), GPS.class);
 		    	    		PendingIntent piGPSService = PendingIntent.getService(getApplicationContext(), 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    		alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    		alarmManager.cancel(piGPSService);
 		    	    		//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piNoiseService);
 		    	    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGPSService);
 		    	    	}
 		    	    	
			    		/*Calendar fouramCalendar = Calendar.getInstance();
			    		fouramCalendar.set(Calendar.HOUR_OF_DAY, 4);
			    		fouramCalendar.set(Calendar.MINUTE, 0);
			    		fouramCalendar.set(Calendar.SECOND, 0);
			    		Intent iFilesUploaderService = new Intent(getApplicationContext(), FilesUploader.class);
			    		PendingIntent piFilesUploaderService = PendingIntent.getService(getApplicationContext(), 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
			    		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			    		alarmManager.cancel(piFilesUploaderService);
			    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fouramCalendar.getTimeInMillis() , 21600000, piFilesUploaderService);*/

			    		/*Calendar threeamCalendar = Calendar.getInstance();
			    		fouramCalendar.set(Calendar.HOUR_OF_DAY, 3);
			    		fouramCalendar.set(Calendar.MINUTE, 0);
			    		fouramCalendar.set(Calendar.SECOND, 0);
			    		Intent iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
			    		PendingIntent piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
						alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			    		alarmManager.cancel(piDailyAnalysisService);
			    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,threeamCalendar.getTimeInMillis() ,  21600000, piDailyAnalysisService);

			    		Calendar tenamCalendar = Calendar.getInstance();
			    		tenamCalendar.set(Calendar.HOUR_OF_DAY, 10);
			    		tenamCalendar.set(Calendar.MINUTE, 30);
			    		tenamCalendar.set(Calendar.SECOND, 0);
			    		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			    		Intent intent2 = new Intent(getApplicationContext(), ReminderService.class);
			    		PendingIntent pendingIntent2 = PendingIntent.getService(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
			    		alarmManager.cancel(pendingIntent2);
			    		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,tenamCalendar.getTimeInMillis() , 21600000, pendingIntent2);*/

 						SharedPreferences appPrefs = getApplicationContext().getSharedPreferences(PREF_FILE2,0 );
 			    		SharedPreferences.Editor ed = appPrefs.edit();
 			    		ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, true );
 			    		ed.commit();
 			    
 		        	}
 		        }catch(Exception e){}

 		    }
 		        
 			checkThatEverythingIsSwitchedOn(1);
		        
 			//Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
 			stopSelf();
        }
    };
}
