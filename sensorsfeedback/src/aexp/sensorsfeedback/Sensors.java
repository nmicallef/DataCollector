package aexp.sensorsfeedback;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;



public class Sensors extends Activity implements OnClickListener
{
    public static final String PREF_CAPTURE_STATE = "captureState";
    public static final String PREF_SAMPLING_SPEED = "samplingSpeed";
    public static final boolean DEBUG = true;
    public static final String PREF_FILE = "prefs";
    public static final String PREF_FILE2 = "prefs2";
    static final int MENU_SETTINGS = 1;
    static final String LOG_TAG = "SENSORS";
    static PopupWindow popUp;
    static LinearLayout[] popuplayout;
    static boolean click = true;
    static int currpage =1;
    static HashMap sensorsList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        SensorManager sensorManager =
                (SensorManager)getSystemService( SENSOR_SERVICE  );
        ArrayList<SensorItem> items = new ArrayList<SensorItem>();
        List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );

        sensorsList = new HashMap();
        
        /*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
		samplingServiceRunning = appPrefs.getBoolean( SAMPLING_SERVICE_POSITION_KEY, false );*/
        
        String filedir=getBaseContext().getFilesDir().getPath()+"/";
        
        try{
        	File tempfz = new File(filedir+"settings.txt");
		
        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;
    	    
        		while ((line = br.readLine()) != null) {
			
        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRunning")){
        				samplingServiceRunning = Boolean.parseBoolean(tempbuff[1]); 
        			}else if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]); 
        			}else{
        				sensorsList.put(tempbuff[0], tempbuff[1]);
        			}
        		}
        		br.close();
        	}else{
        		BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));	
				outdat.write("samplingServiceRunning,false");
				outdat.newLine();
				outdat.write("samplingServiceRate,1");
				outdat.newLine();
				for( int i = 0 ; i < sensors.size() ; ++i ){
		        	SensorItem item = new SensorItem( sensors.get( i ).getName() );
		        	if (item.getSensorName().toLowerCase().contains("rgb") || item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
		        		sensorsList.put(item.getSensorName(), "true");
		        		outdat.write(item.getSensorName()+",true");
						outdat.newLine();
		        	}
		        }
				sensorsList.put("Wi-Fi sensor", ",true");
				outdat.write("Wi-Fi sensor"+",true");
				outdat.newLine();
				sensorsList.put("Microphone sensor", ",true");
				outdat.write("Microphone sensor"+",true");
				outdat.newLine();
				sensorsList.put("GPS sensor", ",true");
				outdat.write("GPS sensor"+",true");
				outdat.newLine();
				outdat.close();
        	}	
        }catch(Exception e){}
		
		

		if (isServiceRunning()){
			samplingServiceRunning = true;
		}else{
			samplingServiceRunning = false;
		}
		popUp = new PopupWindow(this);
		popUp.setBackgroundDrawable(new BitmapDrawable());
		
			
		HashMap<String,DataCompletionItem> items2 = new HashMap<String,DataCompletionItem>();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
		Date lastdate = new Date();
		int totalvalid=0;
		try{
			
			
			File tempf = new File(filedir+"items_on_screen.txt");
		
			if (tempf.exists()){
				BufferedReader br = new BufferedReader(new FileReader(tempf));
				String line;
        	    
        	    while ((line = br.readLine()) != null) {
				
        	    	String [] tempbuff = line.split(",");
        	    	if (tempbuff.length >= 3){
        	    		DataCompletionItem item = new DataCompletionItem(tempbuff[0],tempbuff[1],tempbuff[2]);
        	    		items2.put(tempbuff[0], item);
        	    		lastdate = dateFormat.parse(tempbuff[0]);
        	    		if (tempbuff[2].equals("complete")){
        	    			totalvalid++;
        	    		}
        	    	}
        	    	
        	    }				
			}
		}catch(Exception e){}
		
		List<DataCompletionItem> sortedItems2=new ArrayList(items2.values());
        
        ListView lv2 = (ListView) findViewById (R.id.list2);

        Comparator<DataCompletionItem> myComparator = new Comparator<DataCompletionItem>() {
            public int compare(DataCompletionItem obj1,DataCompletionItem obj2) {
                return obj1.getDate().compareTo(obj2.getDate());
            }
        };

        Collections.sort(sortedItems2, myComparator);
        listAdapter3 = new DataCompletionAdapter( lv2.getContext(), sortedItems2);
        
        lv2.setAdapter(  listAdapter3 );
        
		
        int resdateID = getResources().getIdentifier("textView4", "id", "aexp.sensorsfeedback");
        TextView completiondate = (TextView) findViewById(resdateID);
        
        lastdate.setDate(lastdate.getDate()+(21-totalvalid));
        
        completiondate.setText(dateFormat.format(lastdate));
        
        
		/*
		HashMap<String,QuestionsFileItem> items2 = new HashMap<String,QuestionsFileItem>();
		String filedir=getBaseContext().getFilesDir().getPath()+"/processed/";
		File f=new File(filedir);
		if(f.isDirectory()){
			String files[]=  f.list();
			for(int i=0;i<files.length;i++){
				if (files[i].contains("questions")){
					String status="Empty";
					try{
						File tempf = new File(filedir+files[i].toString());
						if (tempf.exists()){
							BufferedReader br = new BufferedReader(new FileReader(tempf));
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
								status="Complete";
							}else{
								if (full > 0){
									status="Partially Complete";
								}
							}
						}
					}catch(Exception e){}

					try{
						String[] tempbuff = files[i].split("_");
						QuestionsFileItem item = new QuestionsFileItem(tempbuff[2],status,"Day");
						Date d = dateFormat.parse(tempbuff[2]);
						items2.put(String.valueOf(d.getTime()), item);
					}catch(Exception e){}
				}
			}
		}*/

		/*List<String> sortedKeys=new ArrayList(items2.keySet());
		Collections.sort(sortedKeys);
		int counter = 1;
		for (String str: sortedKeys){
			QuestionsFileItem temp = items2.get(str);
			String scount = String.valueOf(counter);
			if (scount.length() == 1){
				scount = "0"+ scount;
			}
			temp.setDay("Day "+scount);
			items2.put(str,temp);
			counter++;
		}
		for (int z = counter; z <= 7; z++){
			QuestionsFileItem temp = new QuestionsFileItem("N/A","", "Day 0"+z);
			items2.put("Day 0"+z, temp);
		}

		List<QuestionsFileItem> sortedItems2=new ArrayList(items2.values());

        
        ListView lv2 = (ListView) findViewById (R.id.list2);

        Comparator<QuestionsFileItem> myComparator = new Comparator<QuestionsFileItem>() {
            public int compare(QuestionsFileItem obj1,QuestionsFileItem obj2) {
                return obj1.getDay().compareTo(obj2.getDay());
            }
        };

        Collections.sort(sortedItems2, myComparator);
        listAdapter2 = new QuestionsFileAdapter( lv2.getContext(), sortedItems2);
        
        lv2.setAdapter(  listAdapter2 );
        lv2.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
            	onListItemClick(parent,view,position,id);
            	return;
            }
        });*/
        
        
		// add new logic for UI
		
       for( int i = 0 ; i < sensors.size() ; ++i ){
        	SensorItem item = new SensorItem( sensors.get( i ).getName());
        	if (item.getSensorName().toLowerCase().contains("rgb") || item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
        		if (sensorsList.containsKey(item.getSensorName())){
        			String state = sensorsList.get(item.getSensorName()).toString();
        			item.setCheckboxState(Boolean.parseBoolean(state));
        		}else{
        			item.setCheckboxState(false);
        		}
        		items.add( item );
        		if( samplingServiceRunning ) {
        			if (item.getCheckboxState()){
        				item.setSampling( true );
        			}
        		}
        	}
        }
       
   	    SensorItem item = new SensorItem( "Wi-Fi sensor");
   	    if (sensorsList.containsKey(item.getSensorName())){
			String state = sensorsList.get(item.getSensorName()).toString();
			item.setCheckboxState(Boolean.parseBoolean(state));
		}else{
			item.setCheckboxState(false);
		}
   	    if( samplingServiceRunning ) {
			if (item.getCheckboxState()){
				item.setSampling( true );
			}
		}
   	    items.add(item);
   	    
   	    item = new SensorItem("Microphone sensor");
	    if (sensorsList.containsKey(item.getSensorName())){
			String state = sensorsList.get(item.getSensorName()).toString();
			item.setCheckboxState(Boolean.parseBoolean(state));
		}else{
			item.setCheckboxState(false);
		}
	    if( samplingServiceRunning ) {
			if (item.getCheckboxState()){
				item.setSampling( true );
			}
		}
	    items.add(item);
	    
	    item = new SensorItem("GPS sensor");
	    if (sensorsList.containsKey(item.getSensorName())){
			String state = sensorsList.get(item.getSensorName()).toString();
			item.setCheckboxState(Boolean.parseBoolean(state));
		}else{
			item.setCheckboxState(false);
		}
	    if( samplingServiceRunning ) {
			if (item.getCheckboxState()){
				item.setSampling( true );
			}
		}
	    items.add(item);
		
        ListView lv= (ListView) findViewById (R.id.list);
        listAdapter = new SensorListAdapter( lv.getContext(), items);
        lv.setAdapter(  listAdapter );


        int resID = getResources().getIdentifier("button1", "id", "aexp.sensorsfeedback");


		Button buttonStart = (Button) findViewById(resID);
		resID = getResources().getIdentifier("button2", "id", "aexp.sensorsfeedback");
		Button buttonStop = (Button) findViewById(resID);

		/*resID = getResources().getIdentifier("buttonftp", "id", "aexp.sensorsfeedback");
		Button buttonFtp = (Button) findViewById(resID);*/

		buttonStart.setOnClickListener(this);
	    buttonStop.setOnClickListener(this);
	    
	    
    }

    protected void onListItemClick(AdapterView<?> parent, View v, int position, long id) {

    	final QuestionsFileItem curritem = (QuestionsFileItem)listAdapter2.getItem( position );
    	final String chosendate = curritem.getDate();


    	if (!chosendate.equals("N/A")){

    		if (click) {

    			currpage = 1;

    			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    			String dir=getBaseContext().getFilesDir().getPath()+"/processed/";
        		String filename = tm.getDeviceId()+"_set_"+chosendate+"_questions.csv";
        		
        		
        	
    			HashMap questionsl = new HashMap();
    			HashMap answersl = new HashMap();
    			HashMap questionsn = new HashMap();
    			HashMap answersn = new HashMap();

    			int totalpages=1;

        		HashMap questionpages = new HashMap();
        		HashMap answerspages = new HashMap();
        		HashMap temphmq = new HashMap();
        		HashMap temphma = new HashMap();

        		try{
        			File tempf = new File(dir+filename);

        			BufferedReader br = new BufferedReader(new FileReader(tempf));
    			    int index =0;
    			    int count = 0;
        			String line;
        			while ((line = br.readLine()) != null) {

        				System.out.println(line);
        				String[] tempbuff =  line.split(";");
        				if (tempbuff.length > 0){
        					if (tempbuff[0].toString().contains("Q")){
        						if ((count % 2) == 0){
            						if (count !=0){
            							index = 0;
            							questionpages.put(totalpages-1, temphmq);
                    					answerspages.put(totalpages-1, temphma);
                    					temphmq = new HashMap();
                    					temphma = new HashMap();
                    					totalpages++;
            						}
            					}

        						if (tempbuff[2].length()> 1){
        							if (tempbuff[1].toString().contains("QL")){
        								questionsl.put(tempbuff[0].toString().replace("Q", ""),tempbuff[2].toString()+";"+tempbuff[3].toString());
        								temphmq.put(index, "QL;"+tempbuff[2].toString()+";"+tempbuff[3].toString());
        				        	}
        							if (tempbuff[1].toString().contains("QN")){
        								questionsn.put(tempbuff[0].toString().replace("Q", ""),tempbuff[2].toString()+";"+tempbuff[3].toString());
        								temphmq.put(index, "QN;"+tempbuff[2].toString()+";"+tempbuff[3].toString());
        							}
        						}
        					}
        					if (tempbuff[0].toString().contains("A")){
        						if (tempbuff.length > 2){
        							if (tempbuff[1].toString().contains("AL")){
        								answersl.put(tempbuff[0].toString().replace("A", ""),tempbuff[2].toString());
        								temphma.put(index, "AL;"+tempbuff[2].toString());

        							}
        							if (tempbuff[1].toString().contains("AN")){
        								answersn.put(tempbuff[0].toString().replace("A", ""),tempbuff[2].toString());
        								temphma.put(index, "AN;"+tempbuff[2].toString());
        							}
        						}else{
        							if (tempbuff[1].toString().contains("AL")){
        								answersl.put(tempbuff[0].toString().replace("A", ""),"");
        								temphma.put(index, "AL;"+"");
        						    }
        							if (tempbuff[1].toString().contains("AN")){
        								answersn.put(tempbuff[0].toString().replace("A", ""),"");
        								temphma.put(index, "AN;"+"");
        							}
        						}
        						count++;
            					index++;
        					}

        				}
        			}
        			if (temphmq.size() > 0){
            			questionpages.put(totalpages-1, temphmq);
            			answerspages.put(totalpages-1, temphma);
            			System.out.println(questionpages.size()+","+answerspages.size()+","+temphmq.size()+","+temphma.size());
            		}
        		}catch(Exception e){e.printStackTrace();}

        		//Double ceiling = Math.ceil(questions.size()/3);
        		//final int maxpages  = ceiling.intValue()+1;
        		// determine number of pages based on retrieved numbers
        		/*int totalpages = 0;
        		int tp = questionsl.size() / 3;
    			double r  = questionsl.size() % 3;
        		if (r > 0){
        			tp = tp +1;
        		}
        		totalpages  = totalpages + tp;

        		tp = questionsn.size() / 3;
    			r  = questionsn.size() % 3;
        		if (r > 0){
        			tp = tp +1;
        		}
        		totalpages  = totalpages + tp;
        		HashMap questionpages = new HashMap();
        		HashMap answerspages = new HashMap();
        		int pageno=0;
        		HashMap temphmq = new HashMap();
        		HashMap temphma = new HashMap();
        		int count =0;
        		for (int i = 0; i < questionsl.size(); i++){
        			if ((i % 3) == 0){
        				if (i != 0){
        					questionpages.put(pageno, temphmq);
        					answerspages.put(pageno, temphma);
        					pageno++;
        					count = 0;
        					temphmq = new HashMap();
        					temphma = new HashMap();
        				}
        			}
        			if (questionsl.containsKey(String.valueOf(i))){
        				temphmq.put(count, "QL;"+questionsl.get(String.valueOf(i)).toString());
        			}
        			if (answersl.containsKey(String.valueOf(i))){
        				temphma.put(count, "AL;"+answersl.get(String.valueOf(i)).toString());
        			}
        			count++;
        		}
        		if (temphmq.size() > 0){
        			questionpages.put(pageno, temphmq);
        			answerspages.put(pageno, temphma);
        			pageno++;
        		}
        		temphmq = new HashMap();
        		temphma = new HashMap();
        		count =0;
        		for (int i = 0; i < questionsn.size(); i++){
        			if ((i % 3) == 0){
        				if (i != 0){
        					questionpages.put(pageno, temphmq);
        					answerspages.put(pageno, temphma);
        					pageno++;
        					count = 0;
        					temphmq = new HashMap();
        					temphma = new HashMap();
        				}
        			}
        			if (questionsn.containsKey(String.valueOf(i+questionsl.size()))){
        				temphmq.put(count, "QN;"+questionsn.get(String.valueOf(i+questionsl.size())).toString());
        			}
        			if (answersn.containsKey(String.valueOf(i+questionsl.size()))){
        				temphma.put(count, "AN;"+answersn.get(String.valueOf(i+questionsl.size())).toString());
        			}
        			count++;
        		}
        		if (temphmq.size() > 0){
        			questionpages.put(pageno, temphmq);
        			answerspages.put(pageno, temphma);
        			pageno++;
        		}*/



        		final int tpages = totalpages;

    			final Display display = getWindowManager().getDefaultDisplay();
				
    			
    			popuplayout = new LinearLayout[totalpages];
    			boolean flag = false;
    			
    			for (int p=0; p < totalpages; p++){
    				popuplayout[p] = new LinearLayout(getBaseContext());
    				
    				TextView tv1 = new TextView(this);
    				tv1.setTextColor(Color.BLACK);
    				tv1.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
    				tv1.setTypeface(null, Typeface.BOLD);
    				tv1.setText("Questions for "+chosendate.split("-")[2]+" "+chosendate.split("-")[1]);
    				popuplayout[p].addView(tv1, 0);


    				TextView tv2a = new TextView(this);
    				tv2a.setTextColor(Color.BLACK);
    				tv2a.setTextSize(TypedValue.COMPLEX_UNIT_SP,4);
    				tv2a.setTag("ignore");
    				popuplayout[p].addView(tv2a);

    				// add logic for each page
    				LinearLayout mainLayout = new LinearLayout(getBaseContext());
    				LayoutParams mainparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    			    mainparams.width = LayoutParams.FILL_PARENT;
    			    mainparams.height= LayoutParams.WRAP_CONTENT;
    			    mainparams.gravity = Gravity.BOTTOM;
    			    mainLayout.setLayoutParams(mainparams);
    			    mainLayout.setOrientation(LinearLayout.VERTICAL);
    			    mainLayout.setId(33);
    			    
    			    
    			    //mainLayout.addView(prevButton);
    				if ((questionpages.containsKey(p)) && (answerspages.containsKey(p))){
    					HashMap currentpageq  = (HashMap)questionpages.get(p);
    					HashMap currentpagea  = (HashMap)answerspages.get(p);
    					for (int s = 0; s < currentpageq.size(); s++){
    						if (currentpageq.containsKey(s)){
    							//String buff = currentpageq.get(s).toString();
    							String [] tempbuff = currentpageq.get(s).toString().split(";");
    							TextView tvtemp = new TextView(this);
    							tvtemp.setTextColor(Color.BLACK);
    							tvtemp.setTextSize(TypedValue.COMPLEX_UNIT_SP,11);
    							//tvtemp.setTypeface(null, Typeface.BOLD);
    							String displaytext ="";

    							int hourb = Integer.parseInt(tempbuff[2].split("-")[1].split(":")[0]);
    							int minuteb = Integer.parseInt(tempbuff[2].split("-")[1].split(":")[1]);
    							minuteb = minuteb +1;
    							if (minuteb == 60){
    								minuteb =0;
    								hourb++;
    							}
    							String hourbt = String.valueOf(hourb);
    							if (hourb < 10){
    								hourbt = "0"+hourbt;
    							}
    							String minutebt = String.valueOf(minuteb);
    							if (minuteb < 10){
    								minutebt = "0"+minutebt;
    							}
    							if (tempbuff[0].equals("QL")){
    								displaytext = "Where was your phone from about "+tempbuff[2].split("-")[0]+" to "+hourbt+":"+minutebt+"?";
    							}else{
    								displaytext = "What kind of environment (in terms of noise, light and proximity to other electronic devices) was your phone in from about "+tempbuff[2].split("-")[0]+" to "+hourbt+":"+minutebt+"?";
    							}
    							tvtemp.setText(displaytext);
    							tvtemp.setTag(currentpageq.get(s).toString());
    							mainLayout.addView(tvtemp);

    							//buff = currentpagea.get(s).toString();
    							tempbuff = currentpagea.get(s).toString().split(";");
    							EditText edtemp = new EditText(this);
    							edtemp.setTextColor(Color.BLACK);
    							edtemp.setTextSize(TypedValue.COMPLEX_UNIT_SP,11);
    							if (tempbuff.length > 1){
    								edtemp.setText(tempbuff[1]);
    							}
    							
    						    
    							edtemp.setTag(currentpagea.get(s).toString());
    							edtemp.setInputType(InputType.TYPE_CLASS_TEXT);
    							LayoutParams edparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    		    			    edparams.height = 40;
    		    			    edtemp.setLayoutParams(edparams);
    		    			    
    							/*edtemp.setOnFocusChangeListener(new OnFocusChangeListener() {

    						        public void onFocusChange(View v, boolean hasFocus) {
    						            if (hasFocus == true){
    						            	 final InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    								           
    						                	inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    						                	inputMgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    						                popUp.setFocusable(true);

    						            }
    						        }
    						    });*/
    		    			 	mainLayout.addView(edtemp);

    							TextView tv3 = new TextView(this);
    		    				tv3.setTextColor(Color.BLACK);
    		    				tv3.setTextSize(TypedValue.COMPLEX_UNIT_SP,4);
    		    				tv3.setTag("ignore");
    		    				mainLayout.addView(tv3);

    						}


    					}

    				}


    				popuplayout[p].addView(mainLayout);


    				TextView tv2 = new TextView(this);
    				tv2.setTextColor(Color.BLACK);
    				tv2.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
    				tv2.setTypeface(null, Typeface.BOLD);
    				tv2.setGravity(Gravity.RIGHT);
    				LayoutParams tv2lp = new LayoutParams();
    				tv2lp.width = LayoutParams.FILL_PARENT;
    				tv2lp.height= LayoutParams.WRAP_CONTENT;
    				tv2.setLayoutParams(tv2lp);
    				tv2.setText("Page "+(p+1)+" of "+totalpages+"  ");
    				popuplayout[p].addView(tv2);
    				
    				TextView tv4 = new TextView(this);
    				tv4.setTextColor(Color.BLACK);
    				tv4.setTextSize(TypedValue.COMPLEX_UNIT_SP,4);
    				tv4.setTag("ignore");
    				popuplayout[p].addView(tv4);

    				// add prev and next buttons
    				Button prevButton = new Button(this);
    			    prevButton.setText("Previous");
    			    prevButton.setTextSize(10);
     			    LayoutParams pbuttonparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    			    pbuttonparams.width = 85;
    			    pbuttonparams.height = 25;
    			    pbuttonparams.gravity = Gravity.BOTTOM;
    			    prevButton.setLayoutParams(pbuttonparams);

    			    prevButton.setOnClickListener(new OnClickListener() {

    		               public void onClick(View v)
    		               {
    		            	   if (currpage-1 > 0){
    		            		   popUp.dismiss();
    		            		   currpage = currpage - 1;
    		            		   click = false;
    		            		   popUp.setContentView(popuplayout[currpage-1]);
    		    				   popUp.showAtLocation(v, Gravity.TOP, 0, 30);
    		    	    		   popUp.update(0, 30, display.getWidth()-40, popuplayout[currpage-1].getLayoutParams().height);
    		    	    		   popUp.setFocusable(true);
    		            	   }
    		               }
    			    });

    			    if (p == 0){
    			    	prevButton.setEnabled(false);
    			    }


    				Button nextButton = new Button(this);
    			    nextButton.setText("Next");
    			    nextButton.setTextSize(10);
    			    LayoutParams nbuttonparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    			    nbuttonparams.width = 85;
    			    nbuttonparams.height = 25;
    			    nbuttonparams.gravity = Gravity.BOTTOM;
    			    nextButton.setLayoutParams(nbuttonparams);
    			    nextButton.setOnClickListener(new OnClickListener() {

    		               public void onClick(View v)
    		               {
    		            	   if (currpage+1 <= tpages){
    		            		   popUp.dismiss();
    		            		   currpage = currpage + 1;
    		            		   click = false;
    		            		   popUp.setContentView(popuplayout[currpage-1]);
    		    	    		   popUp.showAtLocation(v, Gravity.TOP, 0, 30);
    		    	    		   popUp.update(0, 30, display.getWidth()-40, popuplayout[currpage-1].getLayoutParams().height);
    		    	    		   popUp.setFocusable(true);
    		            	   }
    		               }
    			    });

    			    if (p == (tpages-1)){
    			      nextButton.setEnabled(false);
    			    }

    			    Button closeButton = new Button(this);
    			    closeButton.setText("Close");
    			    closeButton.setTextSize(10);
    			    LayoutParams cbuttonparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    			    cbuttonparams.width = 85;
    			    cbuttonparams.height = 25;
    			    cbuttonparams.gravity = Gravity.BOTTOM;
    			    closeButton.setLayoutParams(cbuttonparams);
    			    closeButton.setOnClickListener(new OnClickListener() {

    		               public void onClick(View v)
    		               {

    		            	   	if (!click){
    		            	   		
    		            	   		/*InputMethodManager inpmanager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
    		            	   		IBinder binder = v.getWindowToken(); 
    		            	   		if (binder != null) { 
    		            	   			inpmanager.hideSoftInputFromWindow(binder, InputMethodManager.HIDE_IMPLICIT_ONLY); 
    		            	   		}*/
    		            	   		
    		            	   		popUp.dismiss();
    		            	   		click = true;
    		            	   		
    		            	   		int full=0;
    	    		            	int count =0;
    		            	   		String dir=getBaseContext().getFilesDir().getPath()+"/processed/";
    		                		String filename = tm.getDeviceId()+"_set_"+chosendate+"_questions.csv";

    		                		try{
    		                			BufferedWriter outq = new BufferedWriter(new FileWriter(dir+filename));


    		    		            	File tempf = new File(dir+tm.getDeviceId()+"_labeled_locations.dat");
    		    	    	    		HashMap labeledlocations = new HashMap();
    		    	    				if (tempf.exists()){
    		    	    					BufferedReader br = new BufferedReader(new FileReader(tempf));
    		    	    					String line;

    		    	    		            while ((line = br.readLine()) != null) {
    		    	    		            	String [] tempbuff = line.split(",");
    		    	    		            	if (tempbuff.length > 0){
    		    	    		            		if(!labeledlocations.containsKey(tempbuff[0])){
    		    	    		            			labeledlocations.put(tempbuff[0], tempbuff[1]);
    		    	    		            		}
    		    	    		            	}
    		    	    		            }

    		    	    				}


    		                		    String prev="-1";
    		                		    String temploc ="-1";
    		                			for (int z =0; z < popuplayout.length; z++){

    		                				LinearLayout ll = popuplayout[z];
    		                				for (int k =0; k < ll.getChildCount(); k++){
    		                					View vi = ll.getChildAt(k);
    		                					if (vi instanceof LinearLayout){
    		                						if (vi.getId() == 33){
    		                							LinearLayout ml  = (LinearLayout)vi;
    		                							for (int c =0; c < ml.getChildCount(); c++){
    		                								View vi2 = ml.getChildAt(c);
    		    		                					if (vi2 instanceof TextView){
    		    		                						TextView tvx = (TextView)vi2;
    		    		                						if (!tvx.getTag().toString().equals("ignore")){
    		    		                							if (vi2 instanceof EditText){
    		    		                								System.out.println("A"+count+";"+tvx.getTag().toString().split(";")[0]+";"+tvx.getText());
    		    		                								outq.write("A"+count+";"+tvx.getTag().toString().split(";")[0]+";"+tvx.getText());
    		    		                								if (tvx.getText().length() > 0){
    		    		                									full++;
    		    		                								}
    		    		                								outq.newLine();
    		    		                								count++;
    		    		                								if (temploc != "-1"){
    		    		                									if (tvx.getText().length() > 0){
    		    		                										labeledlocations.put(temploc, tvx.getText());
    		    		                									}
    		    		                									temploc="-1";
    		    		                								}
    		    		                							}else{
    		    		                								System.out.println("Q"+count+";"+tvx.getTag().toString());
    		    		                								outq.write("Q"+count+";"+tvx.getTag().toString());
    		    		                								outq.newLine();
    		    		                								String[] qbuff = tvx.getTag().toString().split(";");
    		    		                								if (qbuff[0].contains("QL")){
    		    		                									if (qbuff[1].contains("Location") && (!qbuff[1].contains("Unclassified"))){
    		    		                										if (!labeledlocations.containsKey(qbuff[1])){
    		    		                											temploc =qbuff[1];
    		    		                										}
    		    		                									}
    		    		                								}
    		    		                							}
    		    		                						}
    		    		                					}
    		                							}
    		                						}
    		                					}
    		                				}

    		                			}

    		                			outq.close();

    		                			try{
    		        		    			File gfile =new File(dir+filename);
    		        						gfile.setReadable(true, false);
    		        						gfile.setWritable(true, false);
    		        						gfile.setExecutable(true, false);
    		        					}catch(Exception e){
    		        						e.printStackTrace();
    		        					}

    		                			outq = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_labeled_locations.dat"));
    		                			List<String> sortedKeys=new ArrayList(labeledlocations.keySet());
    		                			Collections.sort(sortedKeys);
    		                			for (String str : sortedKeys) {
    		                				String str2 = labeledlocations.get(str).toString();
    		                				outq.write(str+","+str2);
    		                				outq.newLine();
    		                			}
    		        					outq.close();

    		        					try{
    		        		    			File gfile =new File(dir+tm.getDeviceId()+"_labeled_locations.dat");
    		        						gfile.setReadable(true, false);
    		        						gfile.setWritable(true, false);
    		        						gfile.setExecutable(true, false);
    		        					}catch(Exception e){
    		        						e.printStackTrace();
    		        					}

    		                		}catch(Exception e){e.printStackTrace();}
    		                		//popuplayout[1].getChildCount();

    		                		//Toast.makeText(getApplicationContext(),String.valueOf(popuplayout.length), Toast.LENGTH_SHORT).show();
    		                		if (count == full){
     	    		            	    //change status of listview
    		                			curritem.setStatus("Complete");
    		                			listAdapter2.notifyDataSetChanged();
        	    		            }else{
        	    		            	if (full > 0){
        	    		            		curritem.setStatus("Partially complete");
        	    		            		listAdapter2.notifyDataSetChanged();
        	    		            	}
        	    		            }
    		            	   	}

    		               }


    			    });

    			    LinearLayout buttonsLayout = new LinearLayout(getBaseContext());
    			    LayoutParams buttonsparams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    			    buttonsparams.width = LayoutParams.WRAP_CONTENT;
    			    buttonsparams.height= LayoutParams.WRAP_CONTENT;
    			    buttonsparams.gravity = Gravity.BOTTOM;
    			   
    			    buttonsLayout.setLayoutParams(buttonsparams);
    			    buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
    			    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    				buttonsLayout.addView(prevButton);
    				buttonsLayout.addView(nextButton);
    				buttonsLayout.addView(closeButton);
    				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    				lp.setMargins(5, 5, 5, 5);
    				popuplayout[p].setLayoutParams(lp);
    				popuplayout[p].setPadding(8, 8, 8, 8);
    				popuplayout[p].addView(buttonsLayout);
    				popuplayout[p].setOrientation(LinearLayout.VERTICAL);
    				popuplayout[p].setBackgroundResource(android.R.color.darker_gray);
    				popuplayout[p].setVisibility(0);

    				//popuplayout[p].
    				
    				
    				if ((p+1) == currpage){
    					popUp.setContentView(popuplayout[p]);
    	    		}

    			}
    			popUp.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    			popUp.showAtLocation(v, Gravity.TOP, 0, 30);
    			popUp.update(0, 30, display.getWidth()-40, popuplayout[currpage-1].getLayoutParams().height); 
    			popUp.setFocusable(true);
    			click = false;
    			popUp.dismiss();
     		   	click = false;
     		    popUp.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
     		    popUp.setContentView(popuplayout[currpage-1]);
				popUp.showAtLocation(v, Gravity.TOP, 0, 30);
	    		popUp.update(0, 30, display.getWidth()-40, popuplayout[currpage-1].getLayoutParams().height);
	    		popUp.setFocusable(true);
	

    		}
    	}
    	/*

    	if (click) {
    		currpage = 1;

    	   	final Display display = getWindowManager().getDefaultDisplay();
            popuplayout = new LinearLayout(this);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            popuplayout.setOrientation(LinearLayout.VERTICAL);

    	   	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

    	   	final String chosendate = ((QuestionsFileItem)listAdapter.getItem( position )).getDate();
    	   	String dir=getBaseContext().getFilesDir().getPath()+"/processed/";
    		String filename = tm.getDeviceId()+"_set_"+chosendate+"_questions.csv";


			HashMap questions = new HashMap();
			HashMap answers = new HashMap();


    		try{
    			File tempf = new File(dir+filename);

    			BufferedReader br = new BufferedReader(new FileReader(tempf));

    			String line;
        	    int count =1;
    			while ((line = br.readLine()) != null) {
    				String[] tempbuff =  line.split(";");
    				System.out.println(line);
    				if (tempbuff.length > 0){
    					if (tempbuff[0].toString().contains("Q")){
    						//TextView tv = new TextView(this);
    						if (tempbuff[1].length()> 1){
    							//tv.setText(tempbuff[1].toString());
    							//popuplayout.addView(tv, count);
    							questions.put(tempbuff[0].toString().replace("Q", ""),tempbuff[1].toString());
    						}
    					}
    					if (tempbuff[0].toString().contains("A")){
    						//EditText ed = new EditText(this);
    						// Setting the type of input that you want
    						//ed.setInputType(InputType.TYPE_CLASS_TEXT);
    						if (tempbuff[1].length()> 1){
    							//ed.setText(tempbuff[1].toString());
    							answers.put(tempbuff[0].toString().replace("A", ""),tempbuff[1].toString());
    						}else{
    							answers.put(tempbuff[0].toString().replace("A", ""),"");
    						}
    						//popuplayout.addView(ed, count);
    					}
    					count++;
    				}
    			}
    		}catch(Exception e){}

    		Double ceiling = Math.ceil(questions.size()/3);
    		final int maxpages  = ceiling.intValue()+1;

			TextView tv1 = new TextView(this);
			tv1.setText("Answer Questions for "+chosendate+": Page "+currpage+" of "+maxpages);
			popuplayout.addView(tv1, 0);


			Button nextButton = new Button(this);
		    nextButton.setText("Next");
		    nextButton.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		    nextButton.setOnClickListener(new OnClickListener() {

	               public void onClick(View v)
	               {
	            	   if (currpage+1 <= maxpages){
	            		   popUp.dismiss();
	                       currpage++;
	            		   popuplayout = new LinearLayout(getBaseContext());
	                       LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	                       popuplayout.setOrientation(LinearLayout.VERTICAL);
	                       TextView tv1 = new TextView(getBaseContext());
	           			   tv1.setText("Answer Questions for "+chosendate+": Page "+currpage+" of "+maxpages);
	           			   popuplayout.addView(tv1, 0);
	           			   popUp.setContentView(popuplayout);
	            		   popUp.showAtLocation(v, Gravity.TOP, 0, 50);
	                       popUp.update(0, 50, display.getWidth()-40, display.getHeight()-100);

	            	   }
	               }
		    });

		    popuplayout.addView(nextButton);

            popuplayout.setOnClickListener(new OnClickListener() {

               public void onClick(View v)
               {
            	   if (!click){
                       popUp.dismiss();
                       click = true;
                   }

               }
            });

        	popUp.setContentView(popuplayout);
    		popUp.showAtLocation(v, Gravity.TOP, 0, 50);
            popUp.update(0, 50, display.getWidth()-40, display.getHeight()-100);
            click = false;
        } */

    	/*Sensor sensor = ((SensorItem)listAdapter.getItem( position )).getSensor();
        String sensorName = sensor.getName();
        Intent i = new Intent();
        i.setClassName( "aexp.sensorsfeedback","aexp.sensors.SensorMonitor" );
        i.putExtra( "sensorname",sensorName );
        startActivity( i );*/
    }


	protected void onPause(){
	     super.onPause();
	     click = true;
	     /*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
	     SharedPreferences.Editor ed = appPrefs.edit();
	     ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, samplingServiceRunning );
	     ed.commit();*/
	     try{
	    	 String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));	
	    	 outdat.write("samplingServiceRunning,"+samplingServiceRunning);
	    	 outdat.newLine();
	    	 outdat.write("samplingServiceRate,"+samplingServiceRate);
	    	 outdat.newLine();
	    	 for (int j = 0; j < listAdapter.getCount(); j ++){
		 		SensorItem item = (SensorItem)listAdapter.getItem( j );
		 		outdat.write(item.getSensorName()+","+item.getCheckboxState());
	 		    outdat.newLine(); 
	    	 }
	    	 outdat.close();
	     }catch(Exception e){}
	     
	     UpdateWidget(this);
	}

	/*protected void onStart(){
	     super.onStart();

	 	loadQuestionsInUI();
	}*/


	protected void onResume() {
		super.onResume();
		click = true;
		/*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
		samplingServiceRunning = appPrefs.getBoolean( SAMPLING_SERVICE_POSITION_KEY, false );*/
		
		try{
			
			SensorManager sensorManager =
	                (SensorManager)getSystemService( SENSOR_SERVICE  );
	        ArrayList<SensorItem> items = new ArrayList<SensorItem>();
	        List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );
			
			
			String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 
        	File tempfz = new File(filedir+"settings.txt");
		
        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;
    	    
        		while ((line = br.readLine()) != null) {
			
        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRunning")){
        				samplingServiceRunning = Boolean.parseBoolean(tempbuff[1]); 
        			}else if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]); 
        				System.out.println("onresume:"+samplingServiceRate);
        			}/*else{
        				sensorsList.put(tempbuff[0], tempbuff[1]);
        			}*/
        			
        		}
        		br.close();
        	}else{
        		BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));	
				outdat.write("samplingServiceRunning,false");
				outdat.newLine();
				outdat.write("samplingServiceRate,1");
				outdat.newLine();
				
				for( int i = 0 ; i < sensors.size() ; ++i ){
		        	SensorItem item = new SensorItem( sensors.get( i ).getName() );
		        	if (item.getSensorName().toLowerCase().contains("rgb") || item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("orientation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
		        		sensorsList.put(item.getSensorName(), "true");
		        		outdat.write(item.getSensorName()+",true");
						outdat.newLine();
		        	}
		        }
				sensorsList.put("Wi-Fi sensor", ",true");
				outdat.write("Wi-Fi sensor"+",true");
				outdat.newLine();
				sensorsList.put("Microphone sensor", ",true");
				outdat.write("Microphone sensor"+",true");
				outdat.newLine();
				sensorsList.put("GPS sensor", ",true");
				outdat.write("GPS sensor"+",true");
				outdat.newLine();
				outdat.close();
        	}
        	
        	
        	if (isServiceRunning()){
    			samplingServiceRunning = true;
    		}else{
    			samplingServiceRunning = false;
    		}

    		/*for( int i = 0 ; i < sensors.size() ; ++i ){
            	SensorItem item = new SensorItem( sensors.get( i ) );
            	if (item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("orientation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
            		if (sensorsList.containsKey(item.getSensorName())){
            			String state = sensorsList.get(item.getSensorName()).toString();
            			item.setCheckboxState(Boolean.parseBoolean(state));
            		}else{
            			item.setCheckboxState(false);
            		}
            		items.add( item );
            		if( samplingServiceRunning ) {
                		item.setSampling( true );
            		}
            	}
            }
    		
    		ListView lv= (ListView) findViewById (R.id.list);
            listAdapter = new SensorListAdapter( lv.getContext(), items);
            lv.setAdapter(  listAdapter );*/
        	
        }catch(Exception e){}
		
		UpdateWidget(this);
	}


	/*private void loadQuestionsInUI(){
		HashMap questions = new HashMap();
		LayoutInflater inflater = getLayoutInflater();
		View dialoglayout = inflater.inflate(R.layout.dialog_layout, (ViewGroup) getCurrentFocus());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialoglayout);
		builder.show();
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String dir = "/mnt/sdcard/";
    	File f=new File(dir);
		if(f.isDirectory()){
			String files[]=  f.list();
			for(int i=0;i<files.length;i++){
				if (files[i].contains(tm.getDeviceId()) && files[i].contains("questions")){
					Log.d("abc_log",files[i]);
					try{
						File tempf = new File(dir+files[i]);
		        		BufferedReader br = new BufferedReader(new FileReader(tempf));
		            	String line;

		            	while ((line = br.readLine()) != null) {
		            		//line = SimpleCrypto.decrypt(SimpleCrypto.PASSWORD, line);
		            		Log.d("abc_log",line);
		            		String [] tempbuff = line.split(",");
		            		if (tempbuff[3] == "?"){
		            			questions.put(tempbuff[1], tempbuff[2]);
		            		}
		            	}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}*/

	protected void onDestroy() {
		super.onDestroy();

		/*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
	    SharedPreferences.Editor ed = appPrefs.edit();
	    ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, samplingServiceRunning  );
	    ed.commit();*/
		
		try{
	    	 String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));	
	    	 outdat.write("samplingServiceRunning,"+samplingServiceRunning);
	    	 outdat.newLine();
	    	 outdat.write("samplingServiceRate,"+samplingServiceRate);
	    	 outdat.newLine();
	    	 for (int j = 0; j < listAdapter.getCount(); j ++){
	 			SensorItem item = (SensorItem)listAdapter.getItem( j );
	 			outdat.write(item.getSensorName()+","+item.getCheckboxState());
 		    	outdat.newLine(); 
	 		 }
	    	 outdat.close();
	     }catch(Exception e){}
		
	    UpdateWidget(this);
		if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onDestroy" );
	}

	protected void onLongListItemClick( View v, int pos, long id) {
		/*if( Sensors.DEBUG )
			Log.d( LOG_TAG, "onLongListItemClick pos: "+pos+"; id: " + id );
		if( samplingServiceRunning){
			stopSamplingService();
		}else{
			startSamplingService( pos );
		}
		*/
		/*
		// If sampling is running on another sensor
		if( samplingServiceRunning[pos] && ( pos != samplingServicePosition ) )
			startSamplingService( pos );
		else
// If sampling is running on the same sensor
		if( samplingServiceRunning[pos] && ( pos == samplingServicePosition ) )
			stopSamplingService(pos);
		else
// If no sampling is running then just start the sampling on the sensor
		if( !samplingServiceRunning[pos] )
			startSamplingService( pos );*/
	}


    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SETTINGS, 1, R.string.menu_settings );
        return result;
    }

    public boolean onOptionsItemSelected(MenuItem item) {


    	int id = item.getItemId();
        switch( id ) {
            case MENU_SETTINGS:
                Intent i = new Intent();
                i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SensorSettings" );
                startActivity( i );
                break;

        }
        return true;
    }



	private void startSamplingService( ) {
		//stopSamplingService(position);
	   
		
		(new Thread(new Runnable() {
        	public void run() {
        		Intent i = new Intent();
        		i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SamplingService" );
        		startService( i );
        	}
        })).start();
		samplingServiceRunning = true;
		for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			sensorsList.put(item, String.valueOf(item.getCheckboxState()));
			if (item.getCheckboxState()){
				item.setSampling( true );
			}
		}
		
		listAdapter.notifyDataSetChanged();
		UpdateWidget(this);

		System.out.println("sampling rate is "+samplingServiceRate+" minutes");
		Intent iHeartBeatService = new Intent(this, HeartBeat.class);
		PendingIntent piHeartBeatService = PendingIntent.getService(this, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piHeartBeatService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);

	    Intent iBatteryLevelService = new Intent(this, BatteryLevel.class);
		PendingIntent piBatteryLevelService = PendingIntent.getService(this, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piBatteryLevelService);
	    //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piBatteryLevelService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);
		
	    
		//AlarmManager alarmManager;
	    Boolean state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("wi-fi")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iWifiService = new Intent(this, Wifi.class);
	    	PendingIntent piWifiService = PendingIntent.getService(this, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piWifiService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piWifiService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
	    }

	    Intent iRunningApplicationsService = new Intent(this, RunningApplications.class);
		PendingIntent piRunningApplicationsService = PendingIntent.getService(this, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piRunningApplicationsService);
	    //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRunningApplicationsService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);
	     
	    
	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("magnetic")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iMagneticFieldService = new Intent(this, MagneticField.class);
	    	PendingIntent piMagneticFieldService = PendingIntent.getService(this, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piMagneticFieldService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piMagneticFieldService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
	    }
	    
	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("rgb")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iLightService = new Intent(this, Lightv3.class);
	    	PendingIntent piLightService = PendingIntent.getService(this, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piLightService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piLightService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
	    }

	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("acceleration") || item.getSensorName().toLowerCase().contains("accelerometer") || item.getSensorName().toLowerCase().contains("linear")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iAccelerometerService = new Intent(this,Accelerometer.class);
	    	PendingIntent piAccelerometerService = PendingIntent.getService(this, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piAccelerometerService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piAccelerometerService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
	    }
	    
	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("rotation")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iRotationService = new Intent(this,Rotation.class);
	    	PendingIntent piRotationService = PendingIntent.getService(this, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piRotationService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piRotationService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);
	    }
	    
	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("gps")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iGPSService = new Intent(this, GPS.class);
	    	PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piGPSService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piNoiseService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGPSService);
	    }
	    
	    
	    /*(new Thread(new Runnable() {
        	public void run() {
        		Intent i = new Intent();
        		i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Accelerometer" );
        		startService( i );
        	}
        })).start();

	    (new Thread(new Runnable() {
        	public void run() {
        		Intent i = new Intent();
        		i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Rotation" );
        		startService( i );
        	}
        })).start();*/
	    
	    state = false;
	    for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			if (item.getSensorName().toLowerCase().contains("microphone")){
				if (item.getCheckboxState()){
					state = true;
				}
	    	}
		}
	    if (state){
	    	Intent iNoiseService = new Intent(this, Sound.class);
	    	PendingIntent piNoiseService = PendingIntent.getService(this, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piNoiseService);
	    	//alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piNoiseService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
	    }
	    
	    /*Calendar fouramCalendar = Calendar.getInstance();
	    //set the time to midnight tonight
	    fouramCalendar.set(Calendar.HOUR_OF_DAY, 4);
	    fouramCalendar.set(Calendar.MINUTE, 0);
	    fouramCalendar.set(Calendar.SECOND, 0);
	    Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
	    //intent.putExtra("MyClass", obj);
		PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piFilesUploaderService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fouramCalendar.getTimeInMillis() , 21600000, piFilesUploaderService);*/

	    /*Intent iDailyAnalysisService = new Intent(this,DailyAnalysis.class);
		PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piDailyAnalysisService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 21600000, piDailyAnalysisService);*/


	    /*Calendar threeamCalendar = Calendar.getInstance();
	    fouramCalendar.set(Calendar.HOUR_OF_DAY, 3);
	    fouramCalendar.set(Calendar.MINUTE, 0);
	    fouramCalendar.set(Calendar.SECOND, 0);
	    Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
	    PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piDailyAnalysisService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,threeamCalendar.getTimeInMillis() , 21600000, piDailyAnalysisService);

	    Calendar tenamCalendar = Calendar.getInstance();
	    tenamCalendar.set(Calendar.HOUR_OF_DAY, 10);
	    tenamCalendar.set(Calendar.MINUTE, 30);
	    tenamCalendar.set(Calendar.SECOND, 0);
	    alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
	    Intent intent = new Intent(this, ReminderService.class);
	    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager.cancel(pendingIntent);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,tenamCalendar.getTimeInMillis() , 21600000, pendingIntent);*/
	    
	    /*Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
		PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piDailyAnalysisService);
	    
	    Intent iReminderService = new Intent(this, ReminderService.class);
		PendingIntent piReminderService = PendingIntent.getService(this, 0, iReminderService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piReminderService);*/

	}

	private void stopSamplingService() {
		if( samplingServiceRunning ) {
        	Intent i = new Intent();
        	i.setClassName( "aexp.sensorsfeedback","aexp.sensors.SamplingService" );
        	stopService( i );
        	/*i = new Intent();
        	i.setClassName( "aexp.sensorsfeedback","aexp.sensors.AutonomousService" );
        	stopService( i );*/
    		/*SensorItem item = (SensorItem)listAdapter.getItem( samplingServicePosition );
    		item.setSampling( false );*/


        	for (int j = 0; j < listAdapter.getCount(); j ++){
    			SensorItem item = (SensorItem)listAdapter.getItem( j );
    			item.setSampling( false );
    		}
        	samplingServiceRunning = false;
			listAdapter.notifyDataSetChanged();
			UpdateWidget(this);
			Intent iHeartBeatService = new Intent(this, HeartBeat.class);
			PendingIntent piHeartBeatService = PendingIntent.getService(this, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piHeartBeatService);

		    /*Intent iWifiDayAnalyzerService = new Intent(this, WifiDayAnalyzer.class);
			PendingIntent piWifiDayAnalyzerService = PendingIntent.getService(this, 0, iWifiDayAnalyzerService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piWifiDayAnalyzerService);*/

		    /*Intent iAccelerometerAnalysisService = new Intent(this, AccelerometerAnalysis.class);
			PendingIntent piAccelerometerAnalysisService = PendingIntent.getService(this, 0, iAccelerometerAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piAccelerometerAnalysisService);*/

		    Intent iWifiService = new Intent(this, Wifi.class);
			PendingIntent piWifiService = PendingIntent.getService(this, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piWifiService);

		    Intent iNoiseService = new Intent(this, Sound.class);
			PendingIntent piNoiseService = PendingIntent.getService(this, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piNoiseService);
		    
		    Intent iGPSService = new Intent(this, GPS.class);
			PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piGPSService);

		    Intent iBatteryLevelService = new Intent(this, BatteryLevel.class);
			PendingIntent piBatteryLevelService = PendingIntent.getService(this, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piBatteryLevelService);

		    Intent iRunningApplicationsService = new Intent(this, RunningApplications.class);
			PendingIntent piRunningApplicationsService = PendingIntent.getService(this, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piRunningApplicationsService);

		    Intent iMagneticFieldService = new Intent(this, MagneticField.class);
			PendingIntent piMagneticFieldService = PendingIntent.getService(this, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piMagneticFieldService);

		    Intent iLightService = new Intent(this, Lightv3.class);
			PendingIntent piLightService = PendingIntent.getService(this, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piLightService);

		    Intent iAccelerometerService = new Intent(this, Accelerometer.class);
			PendingIntent piAccelerometerService = PendingIntent.getService(this, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piAccelerometerService);

		    Intent iRotationService = new Intent(this, Rotation.class);
			PendingIntent piRotationService = PendingIntent.getService(this, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piRotationService);

		    /*i = new Intent();
        	i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Accelerometer" );
        	stopService( i );

        	i = new Intent();
        	i.setClassName( "aexp.sensorsfeedback","aexp.sensors.Rotation" );
        	stopService( i );*/

		    /*Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
			PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piFilesUploaderService);*/

		    /*Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
			PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piDailyAnalysisService);
		    
		    Intent iReminderService = new Intent(this, ReminderService.class);
			PendingIntent piReminderService = PendingIntent.getService(this, 0, iReminderService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piReminderService);*/

		}
	}
	//private static PendingIntent servicet = null;
	public static void UpdateWidget(final Context Ct)
	{

		/*final AlarmManager m = (AlarmManager) Ct.getSystemService(Context.ALARM_SERVICE);

	    final Calendar TIME = Calendar.getInstance();
	    TIME.set(Calendar.MINUTE, 0);
	    TIME.set(Calendar.SECOND, 0);
	    TIME.set(Calendar.MILLISECOND, 0);


	    final Intent i = new Intent(Ct, MyService.class);

        if (servicet == null)
        {
            servicet = PendingIntent.getService(Ct, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        m.setRepeating(AlarmManager.RTC, TIME.getTime().getTime(), 3000 * 60, servicet); */

	        ComponentName Me =new ComponentName(Ct, SensorsWidgetProvider.class);

	        RemoteViews remoteViews=new RemoteViews(Ct.getPackageName(), R.layout.widget1);

	        AppWidgetManager ApMgr=AppWidgetManager.getInstance(Ct);


	        Intent intent = new Intent(Ct, Sensors.class);
		    PendingIntent pendingIntent = PendingIntent.getActivity(Ct, 0, intent, 0);
		    boolean found = false;
		    ActivityManager manager = (ActivityManager) Ct.getSystemService(ACTIVITY_SERVICE);
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

	public boolean isServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	if (service.service.getClassName().toLowerCase().contains("aexp.sensorsfeedback")) {
	            return true;
	        }
	    }
	    return false;
	}

    private SensorListAdapter listAdapter;
	private QuestionsFileAdapter listAdapter2;

	private DataCompletionAdapter listAdapter3;
	private boolean samplingServiceRunning = false;
	private int samplingServiceRate = 1;
	private int samplingServicePosition = 0;
	private static final String SAMPLING_SERVICE_POSITION_KEY = "samplingServicePositon";

	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		Button b = (Button)arg0;
        String buttonText = b.getText().toString();


        try{
        	String dir = getBaseContext().getFilesDir().getPath();
        	while(dir.length() > 1){

        		File tempdir = new File(dir);
        		tempdir.setExecutable(true,false);
        		tempdir.setReadable(true, false);
        		tempdir.setWritable(true, false);

        		if (dir.lastIndexOf("/") == 0){
        			dir = "/";
        		}else{
        			dir = dir.substring(0, dir.lastIndexOf("/"));
        		}
        	}
        }catch(Exception e){}



    	if (buttonText.equals("Start Recording")){
    		if( !samplingServiceRunning){
    			startSamplingService();
    		}
    	}else if (buttonText.equals("Stop Recording")){
    		if( samplingServiceRunning){
    			stopSamplingService();
    		}
        }/*else if (buttonText.equals("Ftp Upload")){
        	int resID = getResources().getIdentifier("editText", "id", "aexp.sensorsfeedback");

        	EditText edittext = (EditText) findViewById(resID);
        	if (edittext.getText().length() > 0){
        		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

				if (mWifi.isConnected()) {

					  // upload ftp
					  String user="user1";
					  String password= "9V47466K";
					  String ftpServer="ftp.fileserve.com";
					  String filedir=getBaseContext().getFilesDir().getPath();
					  if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
	  					File fz=new File("/mnt/sdcard","");
	  	        		if(fz.isDirectory()){
	  	        			filedir="/mnt/sdcard";
	  	        		}
	  				  }

					  FTPClient con = new FTPClient();
					  try
					  {
						  con.connect(ftpServer);
						  //con.setFileType(FTP.BINARY_FILE_TYPE);
						  if (con.login (user, password))
						  {
							  Log.d("abc_log","Connected to "+ftpServer);
							  con.enterLocalPassiveMode();

							  File fz=new File(getBaseContext().getFilesDir(),"");
							  if(fz.isDirectory()){
		        				String files[]=  fz.list();
		        				for(int z=0;z<files.length;z++){
		        					if (files[z].contains(tm.getDeviceId()) && files[z].contains(".zip")){
		        						Log.d("abc_log",filedir+files[z]);

		        						File source=new File(filedir+"/"+files[z]);

		        						FileInputStream fileInputStream = new FileInputStream(source);
		  							  	boolean result = con.storeFile("/"+files[z], fileInputStream);
		  							  	fileInputStream.close();
		  							  	if (result) {
		  							  		Toast.makeText(getApplicationContext(),"Upload Succeeded", Toast.LENGTH_LONG).show();
		  							  	}else{
		  							  		Toast.makeText(getApplicationContext(),"Upload Failed", Toast.LENGTH_LONG).show();
		  							  	}
		        					}
		        				}
							  }
		        		  }else{
		        			  Toast.makeText(getApplicationContext(),"Ftp Not Connected", Toast.LENGTH_LONG).show();
						  }
					  }catch (Exception e){
						  Toast.makeText(getApplicationContext(),"Ftp Error!!", Toast.LENGTH_LONG).show();
						  e.printStackTrace();
					  }
					  try
					  {
						  con.logout();
						  con.disconnect();
					  }catch (Exception e)
					  {
						  e.printStackTrace();
					  }
				  }

        	}else{
        		Toast.makeText(getApplicationContext(),"Host is not available", Toast.LENGTH_LONG).show();
        	}

        }*/

	}


	public class MyService extends Service
	{
	    @Override
	    public void onCreate()
	    {
	        super.onCreate();
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId)
	    {

	        buildUpdate();

	        return super.onStartCommand(intent, flags, startId);
	    }

	    private void buildUpdate()
	    {

	    	ComponentName thisWidget = new ComponentName(this, SensorsWidgetProvider.class);
	        AppWidgetManager manager = AppWidgetManager.getInstance(this);

	        boolean found = false;
		      ActivityManager manager2 = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
			  for (RunningServiceInfo service : manager2.getRunningServices(Integer.MAX_VALUE)) {
				    if (service.service.getClassName().toLowerCase().contains("aexp.sensorsfeedback")) {
			            found = true;
			        }
			  }
			  String message="";
			  if (found){
				  message="Sensors are running!!!";
			  }else{
				  message="Sensors are switched off!!!";

				  (new Thread(new Runnable() {
			        	public void run() {
			        		Intent i = new Intent();
			        		i.setClassName( "aexp.sensorsfeedback","aexp.sensorsfeedback.SamplingService" );
			        		getBaseContext().startService( i );
			        	}
			        })).start();
					//samplingServiceRunning = true;
					/*(new Thread(new Runnable() {
			        	public void run() {
			        		Intent i = new Intent();
			    	    	i.setClassName( "aexp.sensorsfeedback","aexp.sensors.AutonomousService" );
			    	    	getBaseContext().startService( i );
			        	}
			        })).start();*/

					SharedPreferences appPrefs = getBaseContext().getSharedPreferences(PREF_FILE2,0 );
				    SharedPreferences.Editor ed = appPrefs.edit();
				    ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, true );
				    ed.commit();
			  }

			  Intent intent = new Intent(getBaseContext(), Sensors.class);
		      PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);

			  RemoteViews views = new RemoteViews(getBaseContext().getPackageName(), R.layout.widget1);
			  int resID = getBaseContext().getResources().getIdentifier("button", "id", "aexp.sensorsfeedback");
			  views.setOnClickPendingIntent(resID, pendingIntent);
		      // To update a label
			  resID = getBaseContext().getResources().getIdentifier("widget1label", "id", "aexp.sensorsfeedback");
			  views.setTextViewText(resID, message);

			  manager.updateAppWidget(thisWidget, views);
	    }

	    @Override
	    public IBinder onBind(Intent intent)
	    {
	        return null;
	    }
	}

}
