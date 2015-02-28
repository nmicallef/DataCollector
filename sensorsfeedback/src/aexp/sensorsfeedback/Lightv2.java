package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Lightv2 extends Service {
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

        	PrintWriter captureFile;
            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
       	 	File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_light_capture.csv" );
            //File captureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+".csv" );
            try {
           	 	captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
           	 	int counter = 0;
           	 	while (counter <= 15)
           	 	{
           	 		Date d = new Date();
           	 		Calendar c = Calendar.getInstance();
           	 		c.setTime(d);
           	 		String temp = c.getTimeInMillis()+","+d.toString()+",";

           	 		Scanner st = new Scanner(new File("/sys/devices/virtual/lightsensor/switch_cmd/lightsensor_file_state"));
           	 		int lux = st.nextInt();
           	 		st.close();

           	 		temp = temp+lux;
           	 		//captureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
           	 		captureFile.println(temp);
           	 		Thread.sleep(1000);
           	 		counter++;
           	 	}


        	 	captureFile.close();

        	 	captureFileName.setReadable(true, false);
     	 	captureFileName.setWritable(true, false);
     	 	captureFileName.setExecutable(true, false);
         } catch( IOException ex ) {
             Log.e( "lightv2_writing", ex.getMessage(), ex );
         }catch( Exception e ) {

         }
     }
 };

}
