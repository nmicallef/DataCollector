package aexp.sensorsfeedback;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;

public class SensorsWidgetProvider extends AppWidgetProvider {

	  public void onUpdate(final Context Ct, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

	    /*final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

	    final Calendar TIME = Calendar.getInstance();
	    TIME.set(Calendar.MINUTE, 0);
	    TIME.set(Calendar.SECOND, 0);
	    TIME.set(Calendar.MILLISECOND, 0);


	    final Intent i = new Intent(context, MyService.class);

        if (service == null)
        {
            service = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), 3000 * 60, service);  */

		  ComponentName Me =new ComponentName(Ct, SensorsWidgetProvider.class);

	        RemoteViews remoteViews=new RemoteViews(Ct.getPackageName(), R.layout.widget1);

	        AppWidgetManager ApMgr=AppWidgetManager.getInstance(Ct);


	        Intent intent = new Intent(Ct, Sensors.class);
		    PendingIntent pendingIntent = PendingIntent.getActivity(Ct, 0, intent, 0);
		    boolean found = false;
		    ActivityManager manager = (ActivityManager) Ct.getSystemService(Ct.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				  if (service.service.getClassName().toLowerCase().contains("aexp.sensorsfeedback")) {
			          found = true;
			      }
			}
			String message="";
			if (found){
			   message="Sensors are running!!!";
			}else{
			   message="Sensors are switched off!!!";


			}

			int resID = Ct.getResources().getIdentifier("button", "id", "aexp.sensorsfeedback");
			remoteViews.setOnClickPendingIntent(resID, pendingIntent);
		      // To update a label
			resID = Ct.getResources().getIdentifier("widget1label", "id", "aexp.sensorsfeedback");
			remoteViews.setTextViewText(resID, message);

	        ApMgr.updateAppWidget( Me, remoteViews);

	  }


	}

