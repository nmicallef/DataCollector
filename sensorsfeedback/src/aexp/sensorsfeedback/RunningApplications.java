package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class RunningApplications  extends Service {
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

    public Runnable beat = new Runnable() {

        public void run() {
        	try {
        		PrintWriter captureFile;
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		if (tm.getDeviceId() != null){
        			File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_runningapplications_capture.csv" );

        			captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
           	 		Date d = new Date();
           	 		Calendar c = Calendar.getInstance();
           	 		c.setTime(d);
           	 		String filecapturename="";

           	 		BufferedReader in = null;

           	 		try {
           	 			Process process = null;
           	 			process = Runtime.getRuntime().exec("top -n 1 -d 1");

           	 			in = new BufferedReader(new InputStreamReader(process.getInputStream()));

           	 			String line ="";
           	 			String content = "";

           	 			while((line = in.readLine()) != null) {
           	 				content += line + "\n";
           	 			}
           	 			PrintWriter captureFile2;
           	 			filecapturename =  tm.getDeviceId()+"_"+c.getTimeInMillis()+"_cpumem_capture.csv";
           	 			File captureFileName2 = new File( getBaseContext().getFilesDir(), filecapturename);
           	 			captureFile2 = new PrintWriter( new FileWriter( captureFileName2, true ) );
           	 			captureFile2.println(content);
           	 			captureFile2.close();
           	 			captureFileName2.setReadable(true, false);
           	 			captureFileName2.setWritable(true, false);
           	 			captureFileName2.setExecutable(true, false);

           	 		} catch (IOException e) {
           	 			// TODO Auto-generated catch block
           	 			e.printStackTrace();
           	 		}
           	 		finally {
           	 			if(in != null) {
           	 				try {
           	 					in.close();
           	 				} catch (IOException e) {
           	 					// TODO Auto-generated catch block
           	 					e.printStackTrace();
           	 				}
           	 			}
           	 		}


           	 		String temp = c.getTimeInMillis()+","+d.toString()+","+filecapturename+",";

           	 		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

           	 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
           	 			temp = temp + service.service.getClassName()+";"+service.pid+";"+TrafficStats.getUidRxBytes(service.uid)+";"+TrafficStats.getUidTxBytes(service.uid)+",";
           	 		}

           	 		captureFile.println(temp);
           	 		captureFile.close();
           	 		captureFileName.setReadable(true, false);
           	 		captureFileName.setWritable(true, false);
           	 		captureFileName.setExecutable(true, false);
        		}
            } catch( IOException ex ) {
                Log.e( "runningapplications_writing", ex.getMessage(), ex );
            }catch( Exception e ) { }
        }
    };

}
