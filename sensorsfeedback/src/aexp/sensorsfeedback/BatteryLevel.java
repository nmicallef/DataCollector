package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BatteryLevel extends Service {
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
        			File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_batterylevel_capture.csv" );

           	 		captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
           	 		Date d = new Date();
           	 		Calendar c = Calendar.getInstance();
        	 		c.setTime(d);
           	 		String temp = c.getTimeInMillis()+","+d.toString()+",";
           	 		Intent batteryIntent = registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
           	 		int rawlevel = batteryIntent.getIntExtra("level", -1);
           	 		double scale = batteryIntent.getIntExtra("scale", -1);
           	 		double level = -1;
           	 		if (rawlevel >= 0 && scale > 0) {
           	 			level = rawlevel / scale;
           	 		}
           	 		temp = temp+level+","+rawlevel+","+scale;
           	 		//captureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
           	 		captureFile.println(temp);
           	 		captureFile.close();

           	 		captureFileName.setReadable(true, false);
           	 		captureFileName.setWritable(true, false);
        	 		captureFileName.setExecutable(true, false);
        		}
            } catch( IOException ex ) {
                Log.e( "batterylevel_writing", ex.getMessage(), ex );
            }catch( Exception e ) {

            }
        	stopSelf();
        }
    };

}
