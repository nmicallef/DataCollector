package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.hardware.SensorManager;

public class SensorSettings extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        
        
        /*SharedPreferences appPrefs = getSharedPreferences(
                                        Sensors.PREF_FILE,
                                        MODE_PRIVATE );
        boolean captureState =
            appPrefs.getBoolean( Sensors.PREF_CAPTURE_STATE, false );
        if( captureState ) {
            CheckBox cb = (CheckBox)findViewById( R.id.settings_capture_cb );
            cb.setChecked( true );
        }
        int speedState = appPrefs.getInt( Sensors.PREF_SAMPLING_SPEED,
        		SensorManager.SENSOR_DELAY_NORMAL );
        Spinner spinner = (Spinner)findViewById(
                            R.id.settings_samplingspeed_spinner );
        spinnerData = new SpinnerData[4];
        spinnerData[0] = new SpinnerData( "Normal",
                            SensorManager.SENSOR_DELAY_NORMAL );
        spinnerData[1] = new SpinnerData( "UI",
                            SensorManager.SENSOR_DELAY_UI );
        spinnerData[2] = new SpinnerData( "Game",
                            SensorManager.SENSOR_DELAY_GAME );
        spinnerData[3] = new SpinnerData( "Fastest",
                            SensorManager.SENSOR_DELAY_FASTEST );
        ArrayAdapter<SpinnerData> adapter =
            new ArrayAdapter<SpinnerData>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerData );
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter( adapter );
        for( int i = 0 ; i < spinnerData.length ; ++i )
            if( speedState == spinnerData[i].getValue() ) {
                spinner.setSelection( i );
                break;
            }*/
        
        try{
			String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 
        	File tempfz = new File(filedir+"settings.txt");
		
        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;
    	    
        		while ((line = br.readLine()) != null) {
			
        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRunning")){
        				samplingServiceRunning = Boolean.parseBoolean(tempbuff[1]); 
        			}
        			if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]); 
        				System.out.println(samplingServiceRate);
        			}
        		}
        		br.close();
        	}
        	
        	//int resID = getResources().getIdentifier("editText2", "id", "aexp.sensorsfeedback");
        	//EditText edittext = (EditText) findViewById(resID);
        	
        	EditText edittext = (EditText) findViewById (R.id.editText2);
    		edittext.setText(""+samplingServiceRate);
    		System.out.println("samplingServiceRate:"+samplingServiceRate);
        	edittext.addTextChangedListener(new TextWatcher() {
        		 
        		   public void afterTextChanged(Editable s) {
        		   }
        		 
        		   public void beforeTextChanged(CharSequence s, int start, 
        		     int count, int after) {
        		   }
        		 
        		   public void onTextChanged(CharSequence s, int start, 
        		     int before, int count) {
        			   try{
        				   samplingServiceRate = Integer.parseInt(s.toString());
        			   }catch(Exception e){}
        		   }
            });
        }catch(Exception e){e.printStackTrace();}
        
    }

    protected void onPause() {
        super.onPause();
        try{
	    	 String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));	
	    	 outdat.write("samplingServiceRunning,"+samplingServiceRunning);
	    	 outdat.newLine();
	    	 outdat.write("samplingServiceRate,"+samplingServiceRate);
	    	 outdat.newLine();
	    	 outdat.close();
	     }catch(Exception e){}
    }


    private boolean samplingServiceRunning = false;
	private int samplingServiceRate = 1;
}
