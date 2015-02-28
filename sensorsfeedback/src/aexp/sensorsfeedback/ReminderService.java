package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class ReminderService extends IntentService {
    private static final int NOTIF_ID = 1;

    public ReminderService(){
        super("ReminderService");
    }

    @Override
      protected void onHandleIntent(Intent intent) {
    	System.out.println("Notification!!!");
    	Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
		System.out.println("Today's date is "+dateFormat.format(cal.getTime()));

		cal.add(Calendar.DATE, -1);
		System.out.println("Yesterday's date was "+dateFormat.format(cal.getTime())); 
	
		String dir = getBaseContext().getFilesDir().getPath()+"/";
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		
		int status=0;
		try{
			File thefile =new File(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_questions.csv");
			if (thefile.exists()  ){	
				BufferedReader br = new BufferedReader(new FileReader(thefile));
				String line;
    	    	int count =0;
    	    	int full =0;
				while ((line = br.readLine()) != null) {
					String [] tembuff = line.split(";");
					if (tembuff[0].contains("A")){
						count++;
						if (tembuff.length > 2){
							if (tembuff[2].length() > 1){
								full++;
							}
						}
					}
				}
				if (count == full){
					status=2;
				}else{
					if (full > 0){
						status=3;
					}
				}
			}else{
				status = -1;
			}
		}catch(Exception e){}
		
		if (status == 0){
			NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
			Notification updateComplete = new Notification();
			updateComplete.icon = android.R.drawable.sym_def_app_icon;
			updateComplete.tickerText = "Answer questions for the previous day";
			updateComplete.when = System.currentTimeMillis();
			updateComplete.flags = Notification.FLAG_ONLY_ALERT_ONCE|Notification.FLAG_AUTO_CANCEL;
			//updateComplete.flags =Notification.DEFAULT_ALL|Notification.FLAG_ONLY_ALERT_ONCE;
					
			Intent notificationIntent = new Intent(getBaseContext(),Sensors.class);
			PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,notificationIntent, 0);
			updateComplete.setLatestEventInfo(getBaseContext(), "SensorsFeedback Reminder","Provide Feedback", contentIntent);
			//updateComplete.vibrate = new long[]{100, 500};
			
			notificationManager.notify(0, updateComplete);
			
			
		}
    }

}
