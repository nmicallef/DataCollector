package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class GPS extends Service {
	
	private LocationManager locationManager;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    
	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	try{
    		
    		
    		String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(!provider.contains("gps")){
                final Intent intent = new Intent();
                intent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                intent.setData(Uri.parse("3"));
                sendBroadcast(intent);
            }
    		
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            
    		LocationListener locationListener = new LocationListener() {
    			public void onLocationChanged(Location location) {
    				
    				//
    				System.out.println("on location changed");
    				PrintWriter captureFile;
    				final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    			   	File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_gps_capture.csv" );
    			    try {
    			    	captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
    			       	Date d = new Date();
    			    	Calendar c = Calendar.getInstance();
    			    	c.setTime(d);
    			    	double lat = (double) (location.getLatitude());
    					double lng = (double) (location.getLongitude());
    					String temp = Long.toString(location.getTime())+","+ c.getTimeInMillis()+","+d.toString()+","+lat+","+lng;
    		   	 		System.out.println(temp);
    					captureFile.println(temp);
    			       	captureFile.close();
    			       	captureFileName.setReadable(true, false);
    			 		captureFileName.setWritable(true, false);
    			 		captureFileName.setExecutable(true, false);
    			    } catch( Exception e ) {
    			        e.printStackTrace();
    			    }
    			}

    			public void onProviderDisabled(String arg0) {
    				// TODO Auto-generated method stub
    				Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    			}

    			public void onProviderEnabled(String provider) {
    				// TODO Auto-generated method stub
    				
    			}

    			public void onStatusChanged(String provider, int status,
    					Bundle extras) {
    				// TODO Auto-generated method stub
    					
    			}    			
    		};
    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,locationListener);
    		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		
    		if (location == null){
    			System.out.println("location is null");
    		}else{
	   	 	
    		                
    			PrintWriter captureFile;
    			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	   	 	File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_gps_capture.csv" );
    	   	 	try {
    	   	 		captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
    	   	 		Date d = new Date();
    	   	 		Calendar c = Calendar.getInstance();
    	   	 		c.setTime(d);
    	   	 		double lat = (double) (location.getLatitude());
    	   	 		double lng = (double) (location.getLongitude());
    	   	 		String temp = Long.toString(location.getTime())+","+ c.getTimeInMillis()+","+d.toString()+","+lat+","+lng;
    	   	 		System.out.println(temp);
    	   	 		captureFile.println(temp);
    	   	 		captureFile.close();
    	   	 		captureFileName.setReadable(true, false);
    	   	 		captureFileName.setWritable(true, false);
    	   	 		captureFileName.setExecutable(true, false);
    	   	 	} catch( Exception e ) {
    	   	 		e.printStackTrace();
    	   	 	}
    		}
    		
    	}catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    

    
    
	/*public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		PrintWriter captureFile;
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
   	 	File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_gps_capture.csv" );
        try {
       	 	captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
       	 	Date d = new Date();
    	 	Calendar c = Calendar.getInstance();
    	 	c.setTime(d);
    	 	double lat = (double) (location.getLatitude());
			double lng = (double) (location.getLongitude());
			String temp = Long.toString(location.getTime())+","+ c.getTimeInMillis()+","+d.toString()+","+lat+","+lng;
    	 	captureFile.println(temp);
       	 	captureFile.close();
       	 	captureFileName.setReadable(true, false);
 			captureFileName.setWritable(true, false);
 			captureFileName.setExecutable(true, false);
        } catch( Exception e ) {
        	e.printStackTrace();
        }
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}*/

	

}
