package aexp.sensorsfeedback;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class AutonomousService extends Service{
	private static final String TAG = "Autonomous Service";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generate method stub
		return null;
	}
	public void onCreate() {
		//Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
	}

	public void onDestroy() {
		//Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		//proc.

	}

	public void onStart(Intent intent, int startid) {

		super.onStart(intent, startid);
	    //Toast.makeText(this, "Autonomous Service Started ", Toast.LENGTH_LONG).show();
	    (new Thread(new Runnable() {
        	public void run() {
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		Calendar c = Calendar.getInstance();
    			c.setTime(new Date());
        		execCommandLine("getevent -t /dev/input/event2 > "+"/data/"+tm.getDeviceId()+"_eventsdata_"+c.getTimeInMillis()+".txt");
        	}
        })).start();

	    (new Thread(new Runnable() {
        	public void run() {
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		Calendar c = Calendar.getInstance();
    			c.setTime(new Date());
        		execCommandLine("tcpdump -vv -s 0 -w /mnt/sdcard/"+tm.getDeviceId()+"_tcp_"+c.getTimeInMillis()+".cap");
        	}
        })).start();
	}

	void execCommandLine(String command)
    {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;

        try
        {
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
            osw.write(command);
            osw.flush();
            osw.close();
        }
        catch (IOException ex)
        {
            Log.e("execCommandLine()", "Command resulted in an IO Exception: " + command);
            return;
        }
        finally
        {
            if (osw != null)
            {
                try
                {
                    osw.close();
                }
                catch (IOException e){}
            }
        }

        try
        {
        	proc.waitFor();
        }
        catch (InterruptedException e){}

        /*if (proc.exitValue() != 0)
        {
        	Log.e("execCommandLine()", "Command returned error: " + command + "\n  Exit code: " + proc.exitValue());
        }else{
        	Log.i("execCommandLine()", "Command returned successfull");
        }*/

    }

}
