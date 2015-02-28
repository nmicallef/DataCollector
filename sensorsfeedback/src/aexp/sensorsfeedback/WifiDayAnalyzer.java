package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiDayAnalyzer extends Service {

	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	new Thread(new Runnable() {
        	public void run() {
    	        beat.run();
    	        stopSelf();
        	}
        }).start();
    }


    public Runnable beat = new Runnable() {

        public void run() {

        	HashMap listsyesterday = new HashMap();
        	for (int i = 0; i < 24; i++){
        		if (i < 10){
        			listsyesterday.put("0"+String.valueOf(i), new ArrayList());
        		}else{
        			listsyesterday.put(String.valueOf(i), new ArrayList());
        		}
        	}

        	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        	String dir = getBaseContext().getFilesDir().getPath();
        	Calendar c = Calendar.getInstance();
			c.setTime(new Date());
    		c.add(Calendar.DATE, -3);
        	File f=new File(dir);
    		if(f.isDirectory()){
    			String files[]=  f.list();
    			for(int i=0;i<files.length;i++){
    				if (files[i].contains(tm.getDeviceId()) && files[i].contains("wifi")){
    					Log.d("abc_log",files[i]);

    					try{
    						File tempf = new File(dir+files[i]);
			        		BufferedReader br = new BufferedReader(new FileReader(tempf));
			            	String line;

			            	while ((line = br.readLine()) != null) {
			            		//line = SimpleCrypto.decrypt(SimpleCrypto.PASSWORD, line);
			            		//Log.d("abc_log",line);
			            		String [] tempbuff = line.split(",");
			            		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			            		Date date = format.parse(tempbuff[0]);
			            		//Log.d("abc_log",date.toString());

			            		//Log.d("abc_log",date.getDate()+","+date.getMonth()+","+date.getYear()+","+c.get(Calendar.DATE)+","+c.get(Calendar.MONTH)+","+c.get(Calendar.YEAR));
			            		if ((date.getDate() == c.get(Calendar.DATE)) && (date.getMonth() == c.get(Calendar.MONTH)) ){
			            			//Log.d("abc_log",date.toString());
			            			String names = "";
			            			for (int z =1; z < tempbuff.length; z++){
			            				if ((z & 1) != 0){
			            					names = names + tempbuff[z]+",";
			            				}
			            			}
			            			//Log.d("abc_log",names);
			            			String key = "";
			            			if (date.getHours() < 10){
			            				key ="0"+String.valueOf(date.getHours());
			            			}else{
			            				key =String.valueOf(date.getHours());
			            			}
			            			ArrayList al = (ArrayList)listsyesterday.get(key);
			            			al.add(names);
			            			listsyesterday.remove(key);
			            			listsyesterday.put(key, al);
			            		}

			            	}
    					}catch(Exception e){
    						e.printStackTrace();
    					}
    				}
    			}
    		}

    		//write lists yesterday
    		PrintWriter captureFile;
    		File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_wifi_timetable_capture.csv" );
            try {
            	captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
            	for (int i = 0; i < 24; i++){
            		String temp = c.DATE+"/"+c.MONTH+";";
        			ArrayList al = new ArrayList();
        			if (i < 10){
        				al = filterArrayList((ArrayList)listsyesterday.get("0"+String.valueOf(i)));
        				temp = temp +"0"+String.valueOf(i)+";";
        			}else{
        				al = filterArrayList((ArrayList)listsyesterday.get(String.valueOf(i)));
        				temp = temp +String.valueOf(i)+";";
        			}
            		for (int j = 0; j < al.size(); j++){
            			temp = temp + al.get(j).toString()+";";
            		}
            		//captureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
            		captureFile.println(temp);
        		}
            	captureFile.close();
            } catch( IOException ex ) {
                Log.e( "abc_log", ex.getMessage(), ex );
            }catch( Exception e ) {
                Log.e( "abc_log", e.getMessage(), e );
            }


    		HashMap filteredMap = new HashMap();
    		for (int i = 0; i < 24; i++){
    			Log.d("abc_log",String.valueOf(i)+":");
    			if (i < 10){
        			ArrayList al = filterArrayList((ArrayList)listsyesterday.get("0"+String.valueOf(i)));
        			filteredMap.put("0"+String.valueOf(i), al);
        			for (int j = 0; j < al.size(); j++){
        				Log.d("abc_log",al.get(j).toString());
        			}
        		}else{
        			ArrayList al = filterArrayList((ArrayList)listsyesterday.get(String.valueOf(i)));
        			filteredMap.put(String.valueOf(i), al);
        			for (int j = 0; j < al.size(); j++){
        				Log.d("abc_log",al.get(j).toString());
        			}
        		}
    		}

    		HashMap mostusedn = getMostUsedNetworks(filteredMap);

    		HashMap mostusedo = new HashMap();
    		String fullfilename =tm.getDeviceId()+"_wifi_recurrentlist.csv";
    		try{
    			File tempf = new File(fullfilename);
    		 	BufferedReader br = new BufferedReader(new FileReader(tempf));
    		    String line;

    		    while ((line = br.readLine()) != null) {
    		    	//line = SimpleCrypto.decrypt(SimpleCrypto.PASSWORD, line);
    		    	String [] tempbuff = line.split(";");
    		        mostusedo.put(tempbuff[0], tempbuff[1]+";"+tempbuff[2]+";"+tempbuff[3]);
    		    }
    		}catch(Exception e){
    			e.printStackTrace();
    		}


    		if (mostusedo.size() > 0){
    			Iterator it = mostusedo.entrySet().iterator();
				while (it.hasNext()){
               		HashMap.Entry pairs = (HashMap.Entry)it.next();
               		String current = pairs.getKey().toString();

                	Iterator itz = mostusedn.entrySet().iterator();
    				while (itz.hasNext()){
    					HashMap.Entry pairz = (HashMap.Entry)itz.next();
                    	String [] tempbuff1 = pairz.getKey().toString().split(",");
    					int count = 0;
    					for (int z =0; z < tempbuff1.length; z++){
    						if (current.contains(tempbuff1[z])){
    							count++;
    						}
    					}
    					if (count > ((tempbuff1.length * 70) / 100)){
    						String [] tempbuff2 = pairs.getValue().toString().split(";");
    						mostusedo.put(current, String.valueOf(Integer.getInteger(tempbuff2[0])+((Integer)pairz.getValue()))+";"+String.valueOf(Integer.getInteger(tempbuff2[1])+1)+";"+tempbuff2[2]);
    					}else{
    						mostusedo.put(pairz.getKey().toString(), String.valueOf(((Integer)pairz.getValue()))+";1;?");
    					}
    				}
				}
    		}else{
    			Iterator itz = mostusedn.entrySet().iterator();
				while (itz.hasNext()){
					HashMap.Entry pairz = (HashMap.Entry)itz.next();
					mostusedo.put(pairz.getKey().toString(), String.valueOf(((Integer)pairz.getValue()))+";1;?");
				}
    		}

    		//process questions

    		String qfilename =tm.getDeviceId()+"_questions.txt";
    		int currentid=1;
    		try{
    			File tempf = new File(qfilename);
    		 	BufferedReader br = new BufferedReader(new FileReader(tempf));
    		    String line;
    		    String [] tempbuff = new String[5];
    		    while ((line = br.readLine()) != null) {
    		    	//line = SimpleCrypto.decrypt(SimpleCrypto.PASSWORD, line);
    		    	tempbuff = line.split(";");
    		    }
    		    if (tempbuff[1].length() > 0){
    		    	currentid = Integer.parseInt(tempbuff[1]) +1;
    		    }

    		}catch(Exception e){
    			e.printStackTrace();
    		}

    		Iterator itv = mostusedo.entrySet().iterator();
    		while (itv.hasNext()){
               	HashMap.Entry pairs = (HashMap.Entry)itv.next();
               	String[] temp = pairs.getValue().toString().split(";");
               	if (temp[3] == "?"){
               		int a = Integer.parseInt(temp[1]);
               		int b = Integer.parseInt(temp[2]);
               		if (a >= 15 ){
               			// get time range

               			// formulate question

               			// edit mostusedo

               			// save question to file

               			// increment id

               		}else if (b >= 5){

               		}
               	}

               	//capture2File.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
    		}

			// write usage to file
			PrintWriter capture2File;
			if (fullfilename.length() > 1){
				File capture2FileName = new File( fullfilename );
            	try {
            		capture2File = new PrintWriter( new FileWriter( capture2FileName, false ) );
            		Iterator ita = mostusedo.entrySet().iterator();
            		while (ita.hasNext()){
                       	HashMap.Entry pairs = (HashMap.Entry)ita.next();
                       	String temp = pairs.getKey().toString()+";"+pairs.getValue().toString();
                       	//capture2File.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
            			capture2File.println(temp);
        			}

            		capture2File.close();
            	} catch( IOException ex ) {
                	Log.e( "abc_log", ex.getMessage(), ex );
            	}catch( Exception e ) {
            		Log.e( "abc_log", e.getMessage(), e );
            	}
			}

        }

        private String getTimeFrameForQuestion(HashMap hm, String list){
        	return "";
        }

        private ArrayList filterArrayList(ArrayList al){

        	ArrayList filteredal = new ArrayList();
        	HashMap allbasket = new HashMap();
        	for (int i =0; i < al.size(); i++){
        		if (i == 0){
        			filteredal.add(al.get(i).toString());
        			String [] tempbuff = al.get(i).toString().split(",");
        			for (int j =0; j < tempbuff.length; j++){
        				allbasket.put(tempbuff[j],"");
        			}
        		}else{
        			String [] tempbuff = al.get(i).toString().split(",");
        			int notfound =0;
        			for (int j =0; j < tempbuff.length; j++){
        				if (!allbasket.containsKey(tempbuff[j])){
        					notfound++;
        				}
        			}
        			if (notfound > ((tempbuff.length * 70) / 100)){
        				for (int j =0; j < tempbuff.length; j++){
            				if (!allbasket.containsKey(tempbuff[j])){
            					allbasket.put(tempbuff[j],"");
            				}
            			}
        				filteredal.add(al.get(i).toString());
        			}
        		}
        	}

			//Log.d("abc_log",al.size()+","+filteredal.size());

        	return filteredal;
        }



        private HashMap getMostUsedNetworks(HashMap hm){
        	HashMap result = new HashMap();

        	for (int i = 0; i < 24; i++){
    			ArrayList al = new ArrayList();
        		if (i < 10){
        			al = (ArrayList)hm.get("0"+String.valueOf(i));
        		}else{
        			al = (ArrayList)hm.get(String.valueOf(i));
        		}
        		for (int j = 0; j < al.size(); j++){
        			if (result.size() < 1){
        				result.put(al.get(j).toString(), 1);
       				}else{
       					Iterator it = result.entrySet().iterator();
       					while (it.hasNext()){
       	                	HashMap.Entry pairs = (HashMap.Entry)it.next();
        	                String [] tempbuff1 = al.get(j).toString().split(",");
        	                String current = pairs.getKey().toString();
        	                int count = 0;
        	                for (int z =0; z < tempbuff1.length; z++){
        	                	if (current.contains(tempbuff1[z])){
        	                		count++;
        	                	}
        	                }
        	                if (count > ((tempbuff1.length * 70) / 100)){
        	                	result.put(current, ((Integer)pairs.getValue())+1);
        	                }else{
        	                	result.put(al.get(j).toString(), 1);
        	                }

        				}
        			}
        		}
        	}
			Iterator it = result.entrySet().iterator();
			while (it.hasNext()){
               	HashMap.Entry pairs = (HashMap.Entry)it.next();
               	Log.d("abc_log",pairs.getKey()+","+pairs.getValue());
			}

        	return result;
        }

    };

}
