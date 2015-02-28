package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;


import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DailyAnalysis extends Service {
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
    	
    	HashMap apg = new HashMap();
    	HashMap ap= new HashMap();
    	HashMap rapg = new HashMap();

        public void run() {
        	try{
        		
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		// add a check that it won't run if it's not between 4 to 5 am
        		if (tm.getDeviceId() != null){
        			//step 1 - extract previous days data from file (using FilesSanityCheck)
        			for (int dy = 1; dy >= 1; dy--){
        				System.out.println("in daily analysis");
        				//get yesterdays date
        				Calendar cal = Calendar.getInstance();
        				DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        				System.out.println("Today's date is "+dateFormat.format(cal.getTime()));

        			
        				cal.add(Calendar.DATE, -dy);
        				System.out.println("Yesterday's date was "+dateFormat.format(cal.getTime())); 
        		
        				String dir = getBaseContext().getFilesDir().getPath()+"/";
        				File thefile =new File(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_highlevelenv10m.arff");
        				if (!thefile.exists()  ){	
        			
        					System.out.println("before copying: "+dir);
        					boolean flag1 = copyFiles(tm.getDeviceId(), dir,"_wifi_capture.csv",dateFormat.format(cal.getTime()),1);
        					boolean flag2 = copyFiles(tm.getDeviceId(), dir,"_light_capture.csv",dateFormat.format(cal.getTime()),2);
        					boolean flag3 = copyFiles(tm.getDeviceId(), dir,"_noise_capture.csv",dateFormat.format(cal.getTime()),1);
        					boolean flag4 = copyFiles(tm.getDeviceId(), dir,"_magneticfield_capture.csv",dateFormat.format(cal.getTime()),2);		
        					//step 2 - define main/uncommon locations based on the previous locations ()
        			
        					if (flag1 || flag2 || flag3 || flag4 ){
        					
        					HashMap relatedsets= new HashMap();
        					HashMap classified = new HashMap();
        					//load old data from locations file
        					try{
        						InputStream is = getAssets().open(tm.getDeviceId()+"_locations_definitions1.dat");
        						//InputStream is = getAssets().open(tm.getDeviceId()+"_locations_definitions_set7touse.dat");
        						BufferedReader br = new BufferedReader(new InputStreamReader(is));
        						String line;
        						while ((line = br.readLine()) != null) {
        							String[] tempbuff = line.split(";");
        							if (tempbuff.length >= 2){
        								if (!relatedsets.containsKey(tempbuff[0])){
        									HashMap tmp = new HashMap();
        									tmp.put(tempbuff[1], "");
        									relatedsets.put(tempbuff[0], tmp);
        								}else{
        									HashMap tmp = (HashMap)relatedsets.get(tempbuff[0]);
        									if (!tmp.containsKey(tempbuff[1])){
        										tmp.put(tempbuff[1], "");
        									}
        									relatedsets.put(tempbuff[0], tmp);
        								}
        								if (!classified.containsKey(tempbuff[1])){
        									classified.put(tempbuff[1], "");
        								}
        							}
        						}
        					}catch(Exception xe){}
        			
        					//load yesterdays wifi lists
        					ArrayList yesterdayswifis = processWifiDatav2(dir+"processed/",dateFormat.format(cal.getTime()));
        			
        					for (int i =0; i < yesterdayswifis.size(); i ++){
        						String item1 = yesterdayswifis.get(i).toString();
        						if (!classified.containsKey(item1)){
        							List<String> availableKeys=new ArrayList(relatedsets.keySet());
        							Collections.sort(availableKeys);
        							boolean flag = false; 
        							for (String str1 : availableKeys) {
        								HashMap list = (HashMap)relatedsets.get(str1);
        								List<String> akeys=new ArrayList(list.keySet());
        								for (String item2 : akeys) {
        									String [] tempbuff = item1.split(",");
        									int count =0;
        									for (int z =0;  z< tempbuff.length; z++){
        										if (tempbuff[z].length() > 1){
        											if (item2.contains(tempbuff[z])){
        												count++;
        											}
        										}
        									}
        									if (count >= (tempbuff.length * 0.6) ){
        										HashMap tmp = (HashMap)relatedsets.get(str1);
        										if (!tmp.containsKey(item1)){
        											tmp.put(item1,"");
        										}
        										relatedsets.put(str1, tmp);
        										classified.put(item1, "");	
        										flag = true;
        										break;
        									}
        								}
        								if (flag == true){
        									break;
        								}	
        							}
        							if (flag == false){
        								HashMap tmp = new HashMap();
        								tmp.put(item1, "");
        								relatedsets.put("NewLocation"+(relatedsets.size()+1), tmp);
        								classified.put(item1, "");
        								System.out.println("newlocation:"+item1);
        							}
        						}
        					}
        			
        					HashMap locationsmapping = new HashMap();
        					// store data
        					try{
        						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+"processed/"+tm.getDeviceId()+"_locations_definitions.dat"));	
        			
        						List<String> sortedKeys=new ArrayList(relatedsets.keySet());
        						Collections.sort(sortedKeys);
        						for (String str : sortedKeys) {
        							HashMap list = (HashMap)relatedsets.get(str);
        							List<String> keys=new ArrayList(list.keySet());
        							for (String str2 : keys) {
        								locationsmapping.put(str2, str);
        								System.out.println(str+","+str2);
        								outdat.write(str+";"+str2);
        								outdat.newLine();
        							}
        						}
        						outdat.close();
        						try{
        							File gfile =new File(dir+"processed/"+tm.getDeviceId()+"_locations_definitions.dat");
        							gfile.setReadable(true, false);
        							gfile.setWritable(true, false);
        							gfile.setExecutable(true, false);
        						}catch(Exception e){
        							e.printStackTrace();
        						} 
        					}catch(Exception xe){}
        			
        					//Step 3 - process data as defined in envprocess data 
        					apg = new HashMap();
        					ap= new HashMap();
        					rapg = new HashMap();
    	
        					HashMap hm4 = processNoiseData(dir+"processed/",dateFormat.format(cal.getTime()));
        					HashMap hm1 = processWifiDatav3(dir+"processed/",dateFormat.format(cal.getTime()));
        					HashMap hm2 = processLightData(dir+"processed/",dateFormat.format(cal.getTime()));
        					HashMap hm3 = processMagneticFieldData(dir+"processed/",dateFormat.format(cal.getTime()));
        		
        					HashMap locationshm = labelExtractedLocationsAndTransitions(hm1,locationsmapping);
    				
        					String locattribute="";
        					try{
        						InputStream is = getAssets().open(tm.getDeviceId()+"_profile_optimized.arff");
        						BufferedReader br = new BufferedReader(new InputStreamReader(is));
        						String line;
        						while ((line = br.readLine()) != null) {
        							if (line.contains("@attribute location")){
        								locattribute = line;
        								break;
        							}
        						}
        						br.close();
        					}catch(Exception e){} 
    				
    				
        					HashMap locmaps = prepareFilesToProcessByWeka(dir+"processed/",tm.getDeviceId(),dateFormat.format(cal.getTime()),locationshm,hm2,hm3,hm4,locattribute);
    				
        					HashMap questions = new HashMap();
        					try{
    					
    					
        						File tempf = new File(dir+"processed/"+tm.getDeviceId()+"_labeled_locations.dat");
        						HashMap labeledlocations = new HashMap();
        						if (tempf.exists()){
        							BufferedReader br = new BufferedReader(new FileReader(tempf));
        							String line;
    		            	
        							while ((line = br.readLine()) != null) {
        								String [] tempbuff = line.split(",");
        								if (tempbuff.length > 0){
        									if(!labeledlocations.containsKey(tempbuff[0])){
        										labeledlocations.put(tempbuff[0], "");
        									}
        								}
        							}
    	    			
        						}
    					
        						List<String> sortedLocMaps=new ArrayList(locmaps.keySet());
        						Collections.sort(sortedLocMaps);
        						for (String str : sortedLocMaps) {
        							if (!labeledlocations.containsKey(str)){
        								String currline = locmaps.get(str).toString();
        								if (currline.contains(";")){
        									// more than 1 occurances
        									String tempfl = "QL;"+str;
        									String [] tempinst = currline.split(";");
        									for (int j = 0; j < tempinst.length; j ++){
        										String[] tempbuff = tempinst[j].toString().split(",");
        										tempfl = tempfl+";"+tempbuff[0].split("--")[0]+"-"+tempbuff[tempbuff.length-1].split("--")[0].split(":")[0]+":"+tempbuff[tempbuff.length-1].split("--")[1];
        									}	
        									questions.put(questions.size(), tempfl);
        								}else{
        									//1 occurance
        									String[] tempbuff = currline.split(",");
        									questions.put(questions.size(), "QL;"+str+";"+tempbuff[0].split("--")[0]+"-"+tempbuff[tempbuff.length-1].split("--")[0].split(":")[0]+":"+tempbuff[tempbuff.length-1].split("--")[1]);
        								}
        							}
        						}
        					}catch(Exception xe){}
    				
        					//step 4 - use weka to find instances when the data did not match.
        					try{
        						//InputStream is = getAssets().open(tm.getDeviceId()+"_set6set7_to_use.arff");
        						//InputStream is = getAssets().open(tm.getDeviceId()+"_set7_to_use.arff");
        						InputStream is = getAssets().open(tm.getDeviceId()+"_profile_optimized.arff");
        						BufferedReader br = new BufferedReader(new InputStreamReader(is));
        						Instances train = new Instances(br);
        						BufferedReader reader = new BufferedReader(new FileReader(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_highlevelenv10m.arff"));
        						Instances test = new Instances(reader);
        						reader.close();
        						BufferedReader reader2 = new BufferedReader(new FileReader(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_highlevelenv10m.arff"));
        						Instances testfull = new Instances(reader2);
        						reader2.close();
        						br.close();
        						train.setClassIndex(train.numAttributes() - 1);
        						// setting class attribute
        						test.setClassIndex(test.numAttributes() - 1);
        						test.deleteAttributeAt(3);
        						test.deleteAttributeAt(2);
        						test.deleteAttributeAt(1);
        						test.deleteAttributeAt(0);
    					 
        						J48 j48 = new J48();
        						j48.setUnpruned(false);
        						j48.setConfidenceFactor(new Float("0.1"));
        						ArrayList correctlyPredicted = new ArrayList();
        						ArrayList incorrectlyPredicted = new ArrayList();
        						HashMap incorrectlocations = new HashMap();
        						String prevloc="-1";
        						String prevtime="-1";
        						j48.buildClassifier(train);
        						// dump results in file
    					
        						System.out.println(j48.toString());
        						BufferedWriter outd = new BufferedWriter(new FileWriter(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_j48tree.txt"));
        						outd.write(j48.toString());outd.newLine();
					 
        						outd.close();
        						try{
        							File gfile =new File(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_j48tree.txt");
        							gfile.setReadable(true, false);
        							gfile.setWritable(true, false);
        							gfile.setExecutable(true, false);
        						}catch(Exception e){
        							e.printStackTrace();
        						}
    					 
        						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_wekaresults.txt"));	
    	        			
        						for (int i = 0; i < test.numInstances(); i++) {
        							double pred = j48.classifyInstance(test.instance(i));
        							String st1 = test.classAttribute().value((int) test.instance(i).classValue());
        							if (test.classAttribute().value((int) test.instance(i).classValue()).equalsIgnoreCase(test.classAttribute().value((int) pred).toString())){
        								correctlyPredicted.add(test.instance(i).toString());
        							}else{
        								incorrectlyPredicted.add(test.instance(i).toString());
        								int hour = new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue();
        								String minutes = testfull.instance(i).stringValue(testfull.attribute("minutes"));
        								if (!incorrectlocations.containsKey(st1)){
        									incorrectlocations.put(st1, (new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes")));
        								}else{
        									String te = incorrectlocations.get(st1).toString();
										
        									if (prevloc.equalsIgnoreCase(st1)){
        										if (minutes.equals("00--09")){
        											if (prevtime.contains("50--59") && prevtime.split(":")[0].contains(String.valueOf(hour-1))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}	
        										if (minutes.equals("10--19")){
        											if (prevtime.contains("00--09") && prevtime.split(":")[0].contains(String.valueOf(hour))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}
        										if (minutes.equals("20--29")){
        											if (prevtime.contains("10--19") && prevtime.split(":")[0].contains(String.valueOf(hour))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}
        										if (minutes.equals("30--39")){
        											if (prevtime.contains("20--29") && prevtime.split(":")[0].contains(String.valueOf(hour))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}
        										if (minutes.equals("40--49")){
        											if (prevtime.contains("30--39") && prevtime.split(":")[0].contains(String.valueOf(hour))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}
        										if (minutes.equals("50--59")){
        											if (prevtime.contains("40--49") && prevtime.split(":")[0].contains(String.valueOf(hour))){
        												te = te+","+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        											}else{
        												te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));	
        											}
        										}
        									}else{
        										te = te+";"+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        									}
										
        									incorrectlocations.put(st1, te);
        								}
        								prevtime=String.valueOf(hour)+":"+minutes;
        								prevloc = st1;
        							}
        							String line = "ID: " + i +", actual: " + test.classAttribute().value((int) test.instance(i).classValue())+", predicted: " + test.classAttribute().value((int) pred)+", "+(new Double(testfull.instance(i).value(testfull.attribute("hour"))).intValue())+":"+testfull.instance(i).stringValue(testfull.attribute("minutes"));
        							outdat.write(line);outdat.newLine();
        							System.out.println(line);
        						}
    					 
        						outdat.close();
        						try{
        							File gfile =new File(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_wekaresults.txt");
        							gfile.setReadable(true, false);
        							gfile.setWritable(true, false);
        							gfile.setExecutable(true, false);
        						}catch(Exception e){
        							e.printStackTrace();
        						} 

        						System.out.println("weka finished "+correctlyPredicted.size()+","+incorrectlyPredicted.size());
    				    
        				 
        						//Step 5 - collate the unmatched instances and prepare questions. 
        						List<String> sortedErrorMaps=new ArrayList(incorrectlocations.keySet());
        						Collections.sort(sortedErrorMaps);
        						for (String str : sortedErrorMaps) {
        							String currline = incorrectlocations.get(str).toString();
        							if (currline.contains(";")){
        								// more than 1 occurances
        								String tempfl = "QN;"+str;
        								String [] tempinst = currline.split(";");
        								for (int j = 0; j < tempinst.length; j ++){
        									String[] tempbuff = tempinst[j].toString().split(",");
        									tempfl = tempfl+";"+tempbuff[0].split("--")[0]+"-"+tempbuff[tempbuff.length-1].split("--")[0].split(":")[0]+":"+tempbuff[tempbuff.length-1].split("--")[1];
        								}	
        								questions.put(questions.size(), tempfl);
        							}else{
        								//1 occurance
        								String[] tempbuff = currline.split(",");
        								questions.put(questions.size(), "QN;"+str+";"+tempbuff[0].split("--")[0]+"-"+tempbuff[tempbuff.length-1].split("--")[0].split(":")[0]+":"+tempbuff[tempbuff.length-1].split("--")[1]);
        							}
        						}   
        						int counter =0;
        						HashMap questionsimportance = new HashMap();
        						//calculate length of temporal slot
        						for (int z=0; z < questions.size(); z++) {
        							String questionstype = questions.get(z).toString().split(";")[0];
        							String tempbuff[] =  questions.get(z).toString().split(";");
        							SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        							if (tempbuff.length > 2){
        								if (questionstype.contains("QN")){
        									for (int x=2; x < tempbuff.length; x++) {
        										String[] tp = tempbuff[x].split("-");
        										Date date1 = (Date) format.parse(tp[1].toString());
        										Date date2 = (Date) format.parse(tp[0].toString());
        										long duration = (date1.getTime() - date2.getTime())/60000;
        										System.out.println(tempbuff[0]+";"+tempbuff[1]+";"+tempbuff[x]+";"+duration);
        										questionsimportance.put(tempbuff[0]+";"+tempbuff[1]+";"+tempbuff[x], duration);
        									}
     							
        								}else{
        									long duration =0;
        									String te = new String();
        									for (int x=2; x < tempbuff.length; x++) {
        										te=te+tempbuff[x].toString()+";";
        										String[] tp = tempbuff[x].split("-");
        										Date date1 = (Date) format.parse(tp[1].toString());
        										Date date2 = (Date) format.parse(tp[0].toString());
        										duration = duration+ (date1.getTime() - date2.getTime())/60000;
        									}
        									if (te.length()>1){
        										te=te.substring(0, te.length()-1);
        									}
        									System.out.println(tempbuff[0]+";"+tempbuff[1]+";"+te+";"+duration);
        									questionsimportance.put(tempbuff[0]+";"+tempbuff[1]+";"+te, duration);
        								}
        							}
        						}
     					
     					
        						List sortedValues=entriesSortedByValues (questionsimportance);
        						BufferedWriter outq = new BufferedWriter(new FileWriter(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_questions.csv"));
        						//for (String str : sortedKeys) {
        						for (int i=0;i<sortedValues.size();i++) {
        							Entry<String,Integer> e = (Entry<String,Integer>) sortedValues.get(i);
        							System.out.println("sorted "+i+","+e.getKey()+";"+e.getValue());
        							outq.write("Q"+i+";"+e.getKey());
        							outq.newLine();
        							outq.write("A"+i+";"+e.getKey().split(";")[0].toString().replace("Q", "A")+";");
        							outq.newLine();
        						}
            				
        						outq.close();	
     					/*for (int z=0; z < questions.size(); z++) {
    						String questionstype = questions.get(z).toString().split(";")[0];
    						if (questionstype.contains("QN")){
    							String tempbuff[] =  questions.get(z).toString().split(";");
    							if (tempbuff.length > 2){
    								for (int x=2; x < tempbuff.length; x++) {
    									System.out.println(counter+","+tempbuff[0]+";"+tempbuff[1]+";"+tempbuff[x]);
    	    							outq.write("Q"+counter+";"+tempbuff[0]+";"+tempbuff[1]+";"+tempbuff[x]);
    	    							outq.newLine();
    	    							outq.write("A"+counter+";"+questionstype.replace("Q", "A")+";");
    	    							outq.newLine();
    	    							counter++;
    								}
    							}
    						}else{
    							System.out.println(z+","+questions.get(z).toString());
    							outq.write("Q"+counter+";"+questions.get(z).toString());
    							outq.newLine();
    							outq.write("A"+counter+";"+questionstype.replace("Q", "A")+";");
    							outq.newLine();
    							counter++;
    						}
    					}*/
    					
        						try{
        							File gfile =new File(dir+"processed/"+tm.getDeviceId()+"_set_"+dateFormat.format(cal.getTime())+"_questions.csv");
        							gfile.setReadable(true, false);
        							gfile.setWritable(true, false);
        							gfile.setExecutable(true, false);
        						}catch(Exception e){
        							e.printStackTrace();
        						}
        					}catch(Exception e){
        						e.printStackTrace();
        					}
    				 
    				         		
        					//Step 6 - zip all processing files and send it via email
        					ZipProcessedFilesAndEmailThem(tm.getDeviceId(),dateFormat.format(cal.getTime()));	
        				}else{
        					System.out.println("yesterdays file exists");
        				}
        				}
        			}
        		}
        	}catch(Exception e){
        		Log.d("abc_log","Exception in Daily analysis: "+e.toString());
        		e.printStackTrace();
        	}
        }
        
        private <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

        		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

        		Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() {
        			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
        				return e2.getValue().compareTo(e1.getValue());
        			}
        		});

        		return sortedEntries;
        }	
        
        public HashMap prepareFilesToProcessByWeka(String dir, String userid, String yesdate, HashMap hm1, HashMap hm2, HashMap hm3, HashMap hm4, String locattribute){
        	List<String> sKeys=new ArrayList(hm4.keySet());
			Collections.sort(sKeys);
			HashMap printinglines = new HashMap();
			//write everything
			HashMap distinctlocations = new HashMap();
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(dir+userid+"_set_"+yesdate+"_highlevelenv10m.arff"));
				BufferedWriter outnew = new BufferedWriter(new FileWriter(dir+userid+"_set_"+yesdate+"_newintervals.csv"));
				
				out.write("@relation "+userid+"_set_"+yesdate+"_highlevelenv10m-weka.filters.unsupervised.attribute.Remove-R1-2");out.newLine();
				out.newLine();
				out.write("@attribute dayofweek numeric");out.newLine();
				out.write("@attribute weekend {weekday,weekend}");out.newLine();
				out.write("@attribute hour numeric");out.newLine();
				out.write("@attribute minutes {00--09,10--19,20--29,30--39,40--49,50--59}");out.newLine();
				out.write("@attribute nmean numeric");out.newLine();
				out.write("@attribute nmode numeric");out.newLine();
				out.write("@attribute nmedian numeric");out.newLine();
				out.write("@attribute nstddev numeric");out.newLine();
				out.write("@attribute nmin numeric");out.newLine();
				out.write("@attribute nmax numeric");out.newLine();
				out.write("@attribute nrange numeric");out.newLine();
				out.write("@attribute lmean numeric");out.newLine();
				out.write("@attribute lmode numeric");out.newLine();
				out.write("@attribute lmedian numeric");out.newLine();
				out.write("@attribute lstddev numeric");out.newLine();
				out.write("@attribute lmin numeric");out.newLine();
				out.write("@attribute lmax numeric");out.newLine();
				out.write("@attribute lrange numeric");out.newLine();
				out.write("@attribute mf1mean numeric");out.newLine();
				out.write("@attribute mf1mode numeric");out.newLine();
				out.write("@attribute mf1median numeric");out.newLine();
				out.write("@attribute mf1stddev numeric");out.newLine();
				out.write("@attribute mf1min numeric");out.newLine();
				out.write("@attribute mf1max numeric");out.newLine();
				out.write("@attribute mf1range numeric");out.newLine();
				out.write("@attribute mf2mean numeric");out.newLine();
				out.write("@attribute mf2mode numeric");out.newLine();
				out.write("@attribute mf2median numeric");out.newLine();
				out.write("@attribute mf2stddev numeric");out.newLine();
				out.write("@attribute mf2min numeric");out.newLine();
				out.write("@attribute mf2max numeric");out.newLine();
				out.write("@attribute mf2range numeric");out.newLine();
				out.write("@attribute mf3mean numeric");out.newLine();
				out.write("@attribute mf3mode numeric");out.newLine();
				out.write("@attribute mf3median numeric");out.newLine();
				out.write("@attribute mf3stddev numeric");out.newLine();
				out.write("@attribute mf3min numeric");out.newLine();
				out.write("@attribute mf3max numeric");out.newLine();
				out.write("@attribute mf3range numeric");out.newLine();
				
				//out.write("dayofweek,weekend,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,location");
				//out.newLine();
				//BufferedWriter out2 = new BufferedWriter(new FileWriter(dir+userid+"_set_"+yesdate+"_highlevelenv1hour.csv"));
			    int linescounter = 0;
				for (String str : sKeys) {
					String[] strsplit = str.split(",");
					String month = strsplit[0].split("/")[0];
					String day= strsplit[0].split("/")[1];
					String weekend = "";
					int dayoftheweek = Integer.parseInt(strsplit[1]);
					if (dayoftheweek == Calendar.SATURDAY) { 
						weekend="weekend";
					} else if (dayoftheweek == Calendar.SUNDAY) { 
						weekend="weekend";
					} else {
						weekend="weekday";
					}
				
				
					HashMap list4 = (HashMap)hm4.get(str);
					HashMap list1 = (HashMap)hm1.get(str);
					HashMap list2 = (HashMap)hm2.get(str);
					HashMap list3 = (HashMap)hm3.get(str);
				    String prevstr1="-1";
					System.out.println("**********************************Day: "+str);
					for (int x = 0; x < 24; x++){
						int count = 0; 
						int count2 = 0; 
						int count3 = 0; 
						double nmean =0.0;
						double nmode =0.0;
						double nmedian =0.0;
						double nstddev =0.0;
						double nmin =0.0;
						double nmax =0.0;
						double nrange =0.0;
						double lmean =0.0;
						double lmode =0.0;
						double lmedian =0.0;
						double lstddev =0.0;
						double lmin =0.0;
						double lmax =0.0;
						double lrange =0.0;
						double m1mean =0.0;
						double m1mode =0.0;
						double m1median =0.0;
						double m1stddev =0.0;
						double m1min =0.0;
						double m1max =0.0;
						double m1range =0.0;
						double m2mean =0.0;
						double m2mode =0.0;
						double m2median =0.0;
						double m2stddev =0.0;
						double m2min =0.0;
						double m2max =0.0;
						double m2range =0.0;
						double m3mean =0.0;
						double m3mode =0.0;
						double m3median =0.0;
						double m3stddev =0.0;
						double m3min =0.0;
						double m3max =0.0;
						double m3range =0.0;
					
						String all_loc="";
						if (x < 10){
							try{
								String st4 = "";
								String st1 = "";
								String st2 = "";
								String st3 = "";
								HashMap minutesmap4 = null;
								HashMap minutesmap1 = null;
								HashMap minutesmap2 = null;
								HashMap minutesmap3 = null;
								try{	
									minutesmap4 = (HashMap)list4.get("0"+String.valueOf(x));
								}catch(Exception ez){}
								try{
									minutesmap1 = (HashMap)list1.get("0"+String.valueOf(x));
								}catch(Exception ez){}
								try{	
									minutesmap2 = (HashMap)list2.get("0"+String.valueOf(x));
								}catch(Exception ez){}
								try{
									minutesmap3 = (HashMap)list3.get("0"+String.valueOf(x));
								}catch(Exception ez){}
								List<String> sortedMinutes=new ArrayList(minutesmap4.keySet());
								Collections.sort(sortedMinutes);
								for (String smin : sortedMinutes) {
								
									if (minutesmap4 != null){
										try{	
											st4 = (String) minutesmap4.get(smin);
											if (st4.length() > 0){
												String [] tempbuff = st4.split(",");
												nmean = nmean + Double.parseDouble(tempbuff[0]);
												nmode = nmode + Double.parseDouble(tempbuff[1]);
												nmedian = nmedian + Double.parseDouble(tempbuff[2]);
												nstddev = nstddev + Double.parseDouble(tempbuff[3]);
												nmin = nmin + Double.parseDouble(tempbuff[4]);
												nmax = nmax + Double.parseDouble(tempbuff[5]);
												nrange = nrange + Double.parseDouble(tempbuff[6]);
												count ++;
											}
										}catch(Exception ez){}
									}
									if (minutesmap1 != null){
										try{	
											String alz = (String) minutesmap1.get(smin);
											st1 = "";
											if (alz.length() > 2){
												st1 = alz.split(",")[0];
											}
										}catch(Exception ez){}	
									}
									if (minutesmap2 != null){
										try{	
											st2 = (String) minutesmap2.get(smin);
											if (st2.length() > 0){
												String [] tempbuff = st2.split(",");
												lmean = lmean + Double.parseDouble(tempbuff[0]);
												lmode = lmode + Double.parseDouble(tempbuff[1]);
												lmedian = lmedian + Double.parseDouble(tempbuff[2]);
												lstddev = lstddev + Double.parseDouble(tempbuff[3]);
												lmin = lmin + Double.parseDouble(tempbuff[4]);
												lmax = lmax + Double.parseDouble(tempbuff[5]);
												lrange = lrange + Double.parseDouble(tempbuff[6]);
												count3 ++;
											}
										}catch(Exception ez){}
									}
									if (minutesmap3 != null){
										try{	
											st3 = (String) minutesmap3.get(smin);
											if (st3.length() > 0){
												String [] tempbuff = st3.split(",");
												m1mean = m1mean + Double.parseDouble(tempbuff[0]);
												m1mode = m1mode + Double.parseDouble(tempbuff[1]);
												m1median = m1median + Double.parseDouble(tempbuff[2]);
												m1stddev = m1stddev + Double.parseDouble(tempbuff[3]);
												m1min = m1min + Double.parseDouble(tempbuff[4]);
												m1max = m1max + Double.parseDouble(tempbuff[5]);
												m1range = m1range + Double.parseDouble(tempbuff[6]);
												m2mean = m2mean + Double.parseDouble(tempbuff[7]);
												m2mode = m2mode + Double.parseDouble(tempbuff[8]);
												m2median = m2median + Double.parseDouble(tempbuff[9]);
												m2stddev = m2stddev + Double.parseDouble(tempbuff[10]);
												m2min = m2min + Double.parseDouble(tempbuff[11]);
												m2max = m2max + Double.parseDouble(tempbuff[12]);
												m2range = m2range + Double.parseDouble(tempbuff[13]);
												m3mean = m3mean + Double.parseDouble(tempbuff[14]);
												m3mode = m3mode + Double.parseDouble(tempbuff[15]);
												m3median = m3median + Double.parseDouble(tempbuff[16]);
												m3stddev = m3stddev + Double.parseDouble(tempbuff[17]);
												m3min = m3min + Double.parseDouble(tempbuff[18]);
												m3max = m3max + Double.parseDouble(tempbuff[19]);
												m3range = m3range + Double.parseDouble(tempbuff[20]);
												count2 ++;
											}
										}catch(Exception ez){}
									}
									if (st1.length() > 0){
										all_loc = all_loc+st1+",";
									}
									if (st4.length()> 0){
										if (st2.length() < 2){
											st2 = "0.0,0.0,0.0,0.0,0.0,0.0,0.0";
										}
										if (!st1.contains("NewLocation")){
											System.out.println(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											printinglines.put(linescounter, dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											//System.out.println(st4+","+st2+","+st3+","+st1);
											//printinglines.put(linescounter, st4+","+st2+","+st3+","+st1);
										
										}else{
											outnew.write(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											//outnew.write(st4+","+st2+","+st3+","+st1);
											outnew.newLine();
										}
										if (!distinctlocations.containsKey(st1)){
											distinctlocations.put(st1, x+":"+smin);
										}else{
											String te = distinctlocations.get(st1).toString();
											if (!prevstr1.equalsIgnoreCase(st1)){
												te = te+";"+x+":"+smin;
											}else{
												te = te+","+x+":"+smin;
											}
											distinctlocations.put(st1, te);
										}
										prevstr1= st1;
										linescounter++;
										//out.write(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
										//out.newLine();
									}
								}
								/*if (nmean > 0){ nmean = nmean/count; }
								if (nmode > 0){ nmode = nmode/count; }
								if (nmedian > 0){ nmedian = nmedian/count; }
								if (nstddev > 0){ nstddev = nstddev/count; }
								if (nmin > 0){ nmin = nmin/count; }
								if (nmax > 0){ nmax = nmax/count; }
								if (nrange > 0){ nrange = nrange/count; }
							
								if (lmean > 0){ lmean = lmean/count3; }
								if (lmode > 0){ lmode = lmode/count3; }
								if (lmedian > 0){ lmedian = lmedian/count3; }
								if (lstddev > 0){ lstddev = lstddev/count3; }
								if (lmin > 0){ lmin = lmin/count3; }
								if (lmax > 0){ lmax = lmax/count3; }
								if (lrange > 0){ lrange = lrange/count3; }
							
								if (m1mean > 0){ m1mean = m1mean/count2; }
								if (m1mode > 0){ m1mode = m1mode/count2; }
								if (m1median > 0){ m1median = m1median/count2; }
								if (m1stddev > 0){ m1stddev = m1stddev/count2; }
								if (m1min > 0){ m1min = m1min/count2; }
								if (m1max > 0){ m1max = m1max/count2; }
								if (m1range > 0){ m1range = m1range/count2; }
							
								if (m2mean > 0){ m2mean = m2mean/count2; }
								if (m2mode > 0){ m2mode = m2mode/count2; }
								if (m2median > 0){ m2median = m2median/count2; }
								if (m2stddev > 0){ m2stddev = m2stddev/count2; }
								if (m2min > 0){ m2min = m2min/count2; }
								if (m2max > 0){ m2max = m2max/count2; }
								if (m2range > 0){ m2range = m2range/count2; }
							
								if (m3mean > 0){ m3mean = m3mean/count2; }
								if (m3mode > 0){ m3mode = m3mode/count2; }
								if (m3median > 0){ m3median = m3median/count2; }
								if (m3stddev > 0){ m3stddev = m3stddev/count2; }
								if (m3min > 0){ m3min = m3min/count2; }
								if (m3max > 0){ m3max = m3max/count2; }
								if (m3range > 0){ m3range = m3range/count2; }
							
							
								out2.write(dayoftheweek+","+weekend+","+x+","+nmean+","+nmode+","+nmedian+","+nstddev+","+nmin+","+nmax+","+nrange+","+
									lmean+","+lmode+","+lmedian+","+lstddev+","+lmin+","+lmax+","+lrange+","+
									m1mean+","+m1mode+","+m1median+","+m1stddev+","+m1min+","+m1max+","+m1range+","+
									m2mean+","+m2mode+","+m2median+","+m2stddev+","+m2min+","+m2max+","+m2range+","+
									m3mean+","+m3mode+","+m3median+","+m3stddev+","+m3min+","+m3max+","+m3range+","+mostFrequentLocation(all_loc));
								out2.newLine();*/
							}catch(Exception zz){zz.printStackTrace();}
						}else{
							try{
								String st4 = "";
								String st1 = "";
								String st2 = "";
								String st3 = "";
								HashMap minutesmap4 = null;
								HashMap minutesmap1 = null;
								HashMap minutesmap2 = null;
								HashMap minutesmap3 = null;
								try{	
									minutesmap4 = (HashMap)list4.get(String.valueOf(x));
								}catch(Exception ez){}
								try{
									minutesmap1 = (HashMap)list1.get(String.valueOf(x));
								}catch(Exception ez){}
								try{	
									minutesmap2 = (HashMap)list2.get(String.valueOf(x));
								}catch(Exception ez){}
								try{
									minutesmap3 = (HashMap)list3.get(String.valueOf(x));
								}catch(Exception ez){}
								List<String> sortedMinutes=new ArrayList(minutesmap4.keySet());
								Collections.sort(sortedMinutes);
								for (String smin : sortedMinutes) {
									if (minutesmap4 != null){
										try{
											st4 = (String) minutesmap4.get(smin);
											if (st4.length() > 0){
												String [] tempbuff = st4.split(",");
												nmean = nmean + Double.parseDouble(tempbuff[0]);
												nmode = nmode + Double.parseDouble(tempbuff[1]);
												nmedian = nmedian + Double.parseDouble(tempbuff[2]);
												nstddev = nstddev + Double.parseDouble(tempbuff[3]);
												nmin = nmin + Double.parseDouble(tempbuff[4]);
												nmax = nmax + Double.parseDouble(tempbuff[5]);
												nrange = nrange + Double.parseDouble(tempbuff[6]);
												count ++;
											}
										}catch(Exception ez){}
									}
									if (minutesmap1 != null){
										try{	
											String alz = (String) minutesmap1.get(smin);
											st1 = "";
											if (alz.length() > 2){
												st1 = alz.split(",")[0];
											}
										}catch(Exception ez){}	
									}
									if (minutesmap2 != null){
										try{	
											st2 = (String) minutesmap2.get(smin);
											if (st2.length() > 0){
												String [] tempbuff = st2.split(",");
												lmean = lmean + Double.parseDouble(tempbuff[0]);
												lmode = lmode + Double.parseDouble(tempbuff[1]);
												lmedian = lmedian + Double.parseDouble(tempbuff[2]);
												lstddev = lstddev + Double.parseDouble(tempbuff[3]);
												lmin = lmin + Double.parseDouble(tempbuff[4]);
												lmax = lmax + Double.parseDouble(tempbuff[5]);
												lrange = lrange + Double.parseDouble(tempbuff[6]);
												count3 ++;
											}
										}catch(Exception ez){}
									}
									if (minutesmap3 != null){
										try{	
											st3 = (String) minutesmap3.get(smin);
											if (st3.length() > 0){
												String [] tempbuff = st3.split(",");
												m1mean = m1mean + Double.parseDouble(tempbuff[0]);
												m1mode = m1mode + Double.parseDouble(tempbuff[1]);
												m1median = m1median + Double.parseDouble(tempbuff[2]);
												m1stddev = m1stddev + Double.parseDouble(tempbuff[3]);
												m1min = m1min + Double.parseDouble(tempbuff[4]);
												m1max = m1max + Double.parseDouble(tempbuff[5]);
												m1range = m1range + Double.parseDouble(tempbuff[6]);
												m2mean = m2mean + Double.parseDouble(tempbuff[7]);
												m2mode = m2mode + Double.parseDouble(tempbuff[8]);
												m2median = m2median + Double.parseDouble(tempbuff[9]);
												m2stddev = m2stddev + Double.parseDouble(tempbuff[10]);
												m2min = m2min + Double.parseDouble(tempbuff[11]);
												m2max = m2max + Double.parseDouble(tempbuff[12]);
												m2range = m2range + Double.parseDouble(tempbuff[13]);
												m3mean = m3mean + Double.parseDouble(tempbuff[14]);
												m3mode = m3mode + Double.parseDouble(tempbuff[15]);
												m3median = m3median + Double.parseDouble(tempbuff[16]);
												m3stddev = m3stddev + Double.parseDouble(tempbuff[17]);
												m3min = m3min + Double.parseDouble(tempbuff[18]);
												m3max = m3max + Double.parseDouble(tempbuff[19]);
												m3range = m3range + Double.parseDouble(tempbuff[20]);
												count2 ++;
											}
										}catch(Exception ez){}
									}
									if (st1.length() > 0){
										all_loc = all_loc+st1+",";
									}
									if (st4.length()> 0){
										if (st2.length() < 2){
											st2 = "0.0,0.0,0.0,0.0,0.0,0.0,0.0";
										}
										if (!st1.contains("NewLocation")){
											System.out.println(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											printinglines.put(linescounter, dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											//System.out.println(st4+","+st2+","+st3+","+st1);
											//printinglines.put(linescounter, st4+","+st2+","+st3+","+st1);
										}else{
											outnew.write(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
											//outnew.write(st4+","+st2+","+st3+","+st1);
											outnew.newLine();
										}
										if (!distinctlocations.containsKey(st1)){
											distinctlocations.put(st1, x+":"+smin);
										}else{
											String te = distinctlocations.get(st1).toString();
											if (!prevstr1.equalsIgnoreCase(st1)){
												te = te+";"+x+":"+smin;
											}else{
												te = te+","+x+":"+smin;
											}
											distinctlocations.put(st1, te);
										}
										prevstr1= st1;
										linescounter++;
										//out.write(dayoftheweek+","+weekend+","+x+","+smin+","+st4+","+st2+","+st3+","+st1);
										//out.newLine();
									}
								}
								/*if (nmean > 0){ nmean = nmean/count; }
								if (nmode > 0){ nmode = nmode/count; }
								if (nmedian > 0){ nmedian = nmedian/count; }
								if (nstddev > 0){ nstddev = nstddev/count; }
								if (nmin > 0){ nmin = nmin/count; }
								if (nmax > 0){ nmax = nmax/count; }
								if (nrange > 0){ nrange = nrange/count; }
							
								if (lmean > 0){ lmean = lmean/count3; }
								if (lmode > 0){ lmode = lmode/count3; }
								if (lmedian > 0){ lmedian = lmedian/count3; }
								if (lstddev > 0){ lstddev = lstddev/count3; }
								if (lmin > 0){ lmin = lmin/count3; }
								if (lmax > 0){ lmax = lmax/count3; }
								if (lrange > 0){ lrange = lrange/count3; }
							
								if (m1mean > 0){ m1mean = m1mean/count2; }
								if (m1mode > 0){ m1mode = m1mode/count2; }
								if (m1median > 0){ m1median = m1median/count2; }
								if (m1stddev > 0){ m1stddev = m1stddev/count2; }
								if (m1min > 0){ m1min = m1min/count2; }
								if (m1max > 0){ m1max = m1max/count2; }
								if (m1range > 0){ m1range = m1range/count2; }
							
								if (m2mean > 0){ m2mean = m2mean/count2; }
								if (m2mode > 0){ m2mode = m2mode/count2; }
								if (m2median > 0){ m2median = m2median/count2; }
								if (m2stddev > 0){ m2stddev = m2stddev/count2; }
								if (m2min > 0){ m2min = m2min/count2; }
								if (m2max > 0){ m2max = m2max/count2; }
								if (m2range > 0){ m2range = m2range/count2; }
							
								if (m3mean > 0){ m3mean = m3mean/count2; }
								if (m3mode > 0){ m3mode = m3mode/count2; }
								if (m3median > 0){ m3median = m3median/count2; }
								if (m3stddev > 0){ m3stddev = m3stddev/count2; }
								if (m3min > 0){ m3min = m3min/count2; }
								if (m3max > 0){ m3max = m3max/count2; }
								if (m3range > 0){ m3range = m3range/count2; }
							
							
								out2.write(dayoftheweek+","+weekend+","+x+","+nmean+","+nmode+","+nmedian+","+nstddev+","+nmin+","+nmax+","+nrange+","+
									lmean+","+lmode+","+lmedian+","+lstddev+","+lmin+","+lmax+","+lrange+","+
									m1mean+","+m1mode+","+m1median+","+m1stddev+","+m1min+","+m1max+","+m1range+","+
									m2mean+","+m2mode+","+m2median+","+m2stddev+","+m2min+","+m2max+","+m2range+","+
									m3mean+","+m3mode+","+m3median+","+m3stddev+","+m3min+","+m3max+","+m3range+","+mostFrequentLocation(all_loc));
								out2.newLine();*/
							}catch(Exception zz){zz.printStackTrace();}
						}
					}
				}
				// add locations
				List<String> sortedLocations=new ArrayList(distinctlocations.keySet());
				Collections.sort(sortedLocations);
				String loclist="";
				for (String str : sortedLocations) {
					if (!str.contains("NewLocation")){
						loclist = loclist+str+",";
					}
				}
				loclist = loclist.substring(0,loclist.length()-1);
				if (locattribute.length() > 1){
					out.write(locattribute);out.newLine();
				}else{
					out.write("@attribute location {"+loclist+"}");out.newLine();
				}
				out.newLine();			
				out.write("@data");out.newLine();
				
				
				//print everything
				List<Integer> sortedLines=new ArrayList(printinglines.keySet());
				Collections.sort(sortedLines);

				for (int i=0; i < sortedLines.size(); i++){
					int x = sortedLines.get(i);
					out.write(printinglines.get(x).toString());
					out.newLine();
				}
				
				out.close();
				outnew.close();
				
				try{
	    			File gfile =new File(dir+userid+"_set_"+yesdate+"_highlevelenv10m.arff");
					gfile.setReadable(true, false);
					gfile.setWritable(true, false);
					gfile.setExecutable(true, false);
				}catch(Exception e){
					e.printStackTrace();
				}
				
				try{
	    			File gfile =new File(dir+userid+"_set_"+yesdate+"_newintervals.csv");
					gfile.setReadable(true, false);
					gfile.setWritable(true, false);
					gfile.setExecutable(true, false);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}catch(Exception ex){ex.printStackTrace();}
			
			return distinctlocations;
        }
        
        private Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
        {

            List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

            // Sorting the list based on values
            Collections.sort(list, new Comparator<Entry<String, Integer>>()
            {
                public int compare(Entry<String, Integer> o1,
                        Entry<String, Integer> o2)
                {
                    if (order)
                    {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                    else
                    {
                        return o2.getValue().compareTo(o1.getValue());

                    }
                }
            });

            // Maintaining insertion order with the help of LinkedList
            Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
            for (Entry<String, Integer> entry : list)
            {
                sortedMap.put(entry.getKey(), entry.getValue());
            }

            return sortedMap;
        }
        
        public String mostFrequentLocation(String loc){
    		String result ="";
    		if (loc.contains(",")){
    			String [] tempbuff = loc.split(",");
    			HashMap<String,Integer> frequencies = new HashMap();
    			for (int i =0; i < tempbuff.length; i++){
    				if (frequencies.containsKey(tempbuff[i])){
    					frequencies.put(tempbuff[i], frequencies.get(tempbuff[i]) + 1);
    				}else{
    					frequencies.put(tempbuff[i], 1);
    				}
    			}
    		
    			Map<String, Integer> sortedMapDesc = sortByComparator(frequencies, false);
    	    
    	    
    	    
    			Iterator itr1 = sortedMapDesc.entrySet().iterator();
    			if (itr1.hasNext()){
    				Map.Entry pairs = (Map.Entry)itr1.next();
    				result = pairs.getKey().toString();
    			}
    		}
    		
    		return result;
    	}
        
        public HashMap labelExtractedLocationsAndTransitions(HashMap wifi, HashMap mostcommon){
    		
    		List<String> sortedKeys=new ArrayList(wifi.keySet());
    		Collections.sort(sortedKeys);
    		
    		//write everything
    		try{
    			//BufferedWriter out = new BufferedWriter(new FileWriter(directory+userid+"_location_all.txt"));
    			String prevlocations ="";
    			
    			for (String str : sortedKeys) {
    				System.out.println("**********************************Day: "+str);
    				//out.write("**********************************Day: "+str);
    			    //out.newLine();
    			    
    				HashMap listsyesterday = (HashMap)wifi.get(str);
    				System.out.println("**********************************Day: "+str);
    				
    				
    				for (int x = 0; x < 24; x++){
    					System.out.println("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    					//out.write("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    					//out.newLine();
    				    if (x < 10){
    				    	try{
    				    		HashMap minutesmap = (HashMap)listsyesterday.get("0"+String.valueOf(x));
    				    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    							Collections.sort(sortedMinutes);
    							for (String smin : sortedMinutes) {
    								ArrayList al = (ArrayList) minutesmap.get(smin);
    								System.out.println("Minutes: "+smin+":");
    								String uniquenames ="";
    								HashMap nameonly = new HashMap();
    								
    						//		out.write("Minutes: "+smin+":");
    								if (al != null){
    									for (int j = 0; j < al.size(); j++){
    											System.out.println(al.get(j).toString());
    											if (ap.containsKey(al.get(j).toString())){
    												ArrayList wz = (ArrayList)ap.get(al.get(j).toString());
    												for (int t =0; t < wz.size(); t++){
    													AccessPoint tap = (AccessPoint) wz.get(t);
    													//fulldetails=fulldetails+"("+tap.getName()+","+tap.getId()+","+tap.getSignal()+"),";
    													if (tap.getName().length() > 1){
    					            						if (!nameonly.containsKey(tap.getName())){
    					            							nameonly.put(tap.getName(), "");
    					            						}
    					            					}
    												}
    											}
    									}

    								}
    								
    								if (nameonly.size() > 0){
    			            			List<String> sK=new ArrayList(nameonly.keySet());
    			    					Collections.sort(sK);
    			    					for (String st : sK) {
    			    						uniquenames=uniquenames+st+",";
    			    					}
    			    				}
    								//out.write(al.get(j).toString()+": "+fulldetails);
    								//out.newLine();
    								boolean flag=false;
    								int itemToCompareSize = uniquenames.split(",").length;
    								List<String> mostc=new ArrayList(mostcommon.keySet());
    		    					Collections.sort(mostc);
    		    					String currloc="";
    								for (String currentlist : mostc){
    									String [] tempbuff = currentlist.split(",");
    									int count =0;
    									for (int z =0;  z< tempbuff.length; z++){
    										if (tempbuff[z].length() > 1){
    											if (uniquenames.contains(tempbuff[z])){
    												count++;
    											}
    										}
    									}
    									
    									//System.out.println(count+","+tempbuff.length+",{"+currentlist+"},("+itemToCompare+")");
    									
    									if (count >= (tempbuff.length * 0.6) ){
    										flag = true;
    										currloc = mostcommon.get(currentlist).toString();
    									}else{
    										if (itemToCompareSize == count){
    											flag = true;
    											currloc = mostcommon.get(currentlist).toString();
    										}
    									}
    									
    								}
    								if (flag){
    									minutesmap.put(smin, currloc+","+uniquenames);
    									prevlocations=currloc;
    								}else{
    									/*String [] tempbuff = prevlocations.split("\t");
    									int count =0;
    									for (int z =0;  z< tempbuff.length; z++){
    										if (tempbuff[z].length() > 1){
    											if (uniquenames.contains(tempbuff[z])){
    												count++;
    											}
    										}
    									}
    									if (count == 0){
    										System.out.println("transition,"+uniquenames);
    										minutesmap.put(smin, "transition,"+uniquenames);
    									}else{
    										System.out.println("other,"+uniquenames);
    										minutesmap.put(smin, "other,"+uniquenames);
    									}*/
    									minutesmap.put(smin, "NewLocation,"+uniquenames);
    									
    									prevlocations=uniquenames;
    								}

    							}
    							listsyesterday.put("0"+String.valueOf(x), minutesmap);
    				    	}catch(Exception zz){}
    					}else{
    						try{
    							HashMap minutesmap = (HashMap)listsyesterday.get(String.valueOf(x));
    				    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    							Collections.sort(sortedMinutes);
    							for (String smin : sortedMinutes) {
    								ArrayList al = (ArrayList) minutesmap.get(smin);
    								System.out.println("Minutes: "+smin+":");
    								//out.write("Minutes: "+smin+":");
    								String uniquenames ="";
    								HashMap nameonly = new HashMap();
    								
    								if (al != null){
    									for (int j = 0; j < al.size(); j++){
    										System.out.println(al.get(j).toString());
    										if (ap.containsKey(al.get(j).toString())){
    											ArrayList wz = (ArrayList)ap.get(al.get(j).toString());
    											for (int t =0; t < wz.size(); t++){
    												AccessPoint tap = (AccessPoint) wz.get(t);
    												//fulldetails=fulldetails+"("+tap.getName()+","+tap.getId()+","+tap.getSignal()+"),";
    												if (tap.getName().length() > 1){
    				            						if (!nameonly.containsKey(tap.getName())){
    				            							nameonly.put(tap.getName(), "");
    				            						}
    				            					}
    											}
    										}
    									}
    								}
    								if (nameonly.size() > 0){
    			            			List<String> sK=new ArrayList(nameonly.keySet());
    			    					Collections.sort(sK);
    			    					for (String st : sK) {
    			    						uniquenames=uniquenames+st+",";
    			    					}
    			    				}
    								//out.write(al.get(j).toString()+": "+fulldetails);
    								//out.newLine();
    								
    								boolean flag=false;
    								int itemToCompareSize = uniquenames.split(",").length;
    								List<String> mostc=new ArrayList(mostcommon.keySet());
    		    					Collections.sort(mostc);
    		    					String currloc="";
    								for (String currentlist : mostc){
    									String [] tempbuff = currentlist.split(",");
    									int count =0;
    									for (int z =0;  z< tempbuff.length; z++){
    										if (tempbuff[z].length() > 1){
    											if (uniquenames.contains(tempbuff[z])){
    												count++;
    											}
    										}
    									}
    									
    									//System.out.println(count+","+tempbuff.length+",{"+currentlist+"},("+itemToCompare+")");
    									
    									if (count >= (tempbuff.length * 0.6) ){
    										flag = true;
    										currloc = mostcommon.get(currentlist).toString();
    									}else{
    										if (itemToCompareSize == count){
    											flag = true;
    											currloc = mostcommon.get(currentlist).toString();
    										}
    									}
    									
    								}
    								if (flag){
    									System.out.println(currloc+","+uniquenames);
    									minutesmap.put(smin, currloc+","+uniquenames);
    									prevlocations=currloc;
    								}else{
    									
    									/*String [] tempbuff = prevlocations.split("\t");
    									int count =0;
    									for (int z =0;  z< tempbuff.length; z++){
    										if (tempbuff[z].length() > 1){
    											if (uniquenames.contains(tempbuff[z])){
    												count++;
    											}
    										}
    									}
    									if (count == 0){
    										System.out.println("transition,"+uniquenames);
    										minutesmap.put(smin, "transition,"+uniquenames);
    									}else{
    										System.out.println("other,"+uniquenames);
    										minutesmap.put(smin, "other,"+uniquenames);
    									}
    									prevlocations=uniquenames;*/
    									minutesmap.put(smin, "NewLocation,"+uniquenames);
    									prevlocations=uniquenames;
    								}

    							}

    							listsyesterday.put(String.valueOf(x), minutesmap);
    						}catch(Exception zz){}
    					}
    				}
    				wifi.put(str, listsyesterday);
    			}
    			//out.close();
    		}catch(Exception ex){ex.printStackTrace();}

    		
    		return wifi;
    	}
        
        
        public HashMap processNoiseData(String directory, String yesdate){
    		HashMap noise = new HashMap();
    		try{
    			File tempf = new File(directory+"set_"+yesdate+"_noise_capture.csv");
    					    
    		    BufferedReader br = new BufferedReader(new FileReader(tempf));
    		    String line;
    		            	
    		    while ((line = br.readLine()) != null) {
    		    	String [] tempbuff = line.split(",");
    		        SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    		        Date date = format.parse(tempbuff[1]);
    		        Calendar c = Calendar.getInstance();
    		        c.setTime(date);
    		        String currdate="";
    		        int currminutes;
    		        int currmonth = date.getMonth()+1;
    		        if (currmonth < 10){
    		            if (date.getDate() < 10){
    		            	currdate ="0"+currmonth+"/"+"0"+date.getDate();
    	            	}else{
    	            		currdate ="0"+currmonth+"/"+date.getDate();
    	            	}
                	}else{
                		if (date.getDate() < 10){
                			currdate =currmonth+"/"+"0"+date.getDate();
                    	}else{
                    		currdate =currmonth+"/"+date.getDate();
                    	}
                	}
    		        currdate = currdate+","+c.get(Calendar.DAY_OF_WEEK);
    		        currminutes= date.getMinutes();
    		            		
    		        String currhours = "";
    		        if (date.getHours() < 10){
                		currhours ="0"+String.valueOf(date.getHours());
                	}else{
                		currhours =String.valueOf(date.getHours());
                	}
    		            		
                	ArrayList avgnoise = new ArrayList();
                	for (int z=2; z < tempbuff.length; z++){
                		try{
                			avgnoise.add(Double.parseDouble(tempbuff[z]));
                		}catch(Exception exx){}
                	}
    		            		
    		        if ((avgnoise.size() > 0)){
    		        	if (!noise.containsKey(currdate))
                        {
    		        		HashMap hoursmap = new HashMap();
                						
                            for (int x = 0; x < 24; x++){
                            	HashMap minutesmap = new HashMap();
                                minutesmap.put("00--09", new ArrayList());
                                minutesmap.put("10--19", new ArrayList());
                                minutesmap.put("20--29", new ArrayList());
                                minutesmap.put("30--39", new ArrayList());
                                minutesmap.put("40--49", new ArrayList());
                                minutesmap.put("50--59", new ArrayList());
                            
                                if (x< 10){
                                	hoursmap.put("0"+String.valueOf(x), minutesmap);
                				}else{
                					hoursmap.put(String.valueOf(x), minutesmap);
                				}
                			}
                            noise.put(currdate, hoursmap);
                        }
                        HashMap datemap = (HashMap)noise.get(currdate);
                        HashMap hoursmap = (HashMap)datemap.get(currhours);
                        String currmin = "";
                        if ((currminutes >= 0) && (currminutes<= 9)) {currmin="00--09";}
                        if ((currminutes >= 10) && (currminutes<= 19)) {currmin="10--19";}
                       	if ((currminutes >= 20) && (currminutes<= 29)) {currmin="20--29";}
                        if ((currminutes >= 30) && (currminutes<= 39)) {currmin="30--39";}
                        if ((currminutes >= 40) && (currminutes<= 49)) {currmin="40--49";}
                       	if ((currminutes >= 50) && (currminutes<= 59)) {currmin="50--59";}
                        ArrayList minutesal = (ArrayList)hoursmap.get(currmin);
                        minutesal.addAll(avgnoise);
                       	hoursmap.put(currmin, minutesal);
                        datemap.put(currhours, hoursmap);
                        noise.put(currdate, datemap);
    		        }
    	            			
    		    }
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    					
    		List<String> sortedKeys=new ArrayList(noise.keySet());
    		Collections.sort(sortedKeys);
    					
    		//write everything
    		try{
    			//BufferedWriter out = new BufferedWriter(new FileWriter(directory+userid+"_db_all.txt"));
    			for (String str : sortedKeys) {
    							
    				//out.write("**********************************Day: "+str);
    			    //out.newLine();
    						    
    				HashMap listsyesterday = (HashMap)noise.get(str);
    				//System.out.println("**********************************Day: "+str);
    							
    				for (int x = 0; x < 24; x++){
    					//System.out.println("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    					//out.write("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    					//out.newLine();
    				    if (x < 10){
    				    	try{
    				    		HashMap minutesmap = (HashMap)listsyesterday.get("0"+String.valueOf(x));
    				    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    							Collections.sort(sortedMinutes);
    							for (String smin : sortedMinutes) {
    								ArrayList al = (ArrayList) minutesmap.get(smin);
    								//System.out.print("Minutes: "+smin+":");
    								String average= computeNoiseStatistics(al);
    								//System.out.println(average);
    								minutesmap.put(smin, average);
    								//out.write("Minutes: "+smin+":");
    								//out.write(average);
    								//out.newLine();
    							}
    							listsyesterday.put("0"+String.valueOf(x), minutesmap);
    						}catch(Exception zz){}
    					}else{
    						try{
    							HashMap minutesmap = (HashMap)listsyesterday.get(String.valueOf(x));
    					   		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    							Collections.sort(sortedMinutes);
    							for (String smin : sortedMinutes) {
    								ArrayList al = (ArrayList) minutesmap.get(smin);
    								//System.out.print("Minutes: "+smin+":");
    								String average= computeNoiseStatistics(al);
    								//System.out.println(average);
    								minutesmap.put(smin, average);
    											
    								//out.write("Minutes: "+smin+":");
    								//out.write(average);
    								//out.newLine();
    							}
    							listsyesterday.put(String.valueOf(x), minutesmap);
    						}catch(Exception zz){}
    					}
    				}
    				noise.put(str, listsyesterday);
    			}
    			//out.close();
    		}catch(Exception ex){ex.printStackTrace();}
    		
    		return noise;
    	}
    	
    	
    	
    	private String computeNoiseStatistics(ArrayList al){
    		String result="";
    		
    		if (al!= null){
    			ArrayList<Double> data = new ArrayList();
    			for (int i =0; i < al.size(); i++){
        			double curr = (Double)al.get(i);
        			data.add(curr);
        		}
    			Statistics st = new Statistics(data);
    			if (st.median() > 0){
    				result = st.getMean()+","+st.getMode()+","+st.median()+","+st.getStdDev()+","+st.getMinimum()+","+st.getMaximum()+","+st.getRange();
    			}
    			//System.out.println(result);
        	}
        	return result;
        }
    	
    	
    	public HashMap processWifiDatav3(String directory, String yesdate){
    		HashMap wifi = new HashMap();
    					
    					try{
    						File tempf = new File(directory+"set_"+yesdate+"_wifi_capture.csv");
    					    
    		        		BufferedReader br = new BufferedReader(new FileReader(tempf));
    		            	String line;
    		            	
    		            	while ((line = br.readLine()) != null) {
    		            		String [] tempbuff = line.split(",");
    		            		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    		            		Date date = format.parse(tempbuff[1]);
    		            		String currdate="";
    		            		int currminutes;
    		            		int currmonth =  date.getMonth()+1;
    		            		if (currmonth < 10){
    		            			if (date.getDate() < 10){
    		            				currdate ="0"+currmonth+"/"+"0"+date.getDate();
    	            				}else{
    	            					currdate ="0"+currmonth+"/"+date.getDate();
    	            				}
                				}else{
                					if (date.getDate() < 10){
                						currdate =currmonth+"/"+"0"+date.getDate();
                    				}else{
                    					currdate =currmonth+"/"+date.getDate();
                    				}
                				}
    		            		Calendar c = Calendar.getInstance();
    		            		c.setTime(date);
    		            		currdate = currdate+","+c.get(Calendar.DAY_OF_WEEK);
    		            		currminutes= date.getMinutes();
    		            		
    		            		String currhours = "";
                				if (date.getHours() < 10){
                					currhours ="0"+String.valueOf(date.getHours());
                				}else{
                					currhours =String.valueOf(date.getHours());
                				}
    		            			
                				
                				
                				String apgname="";
    		            		if (tempbuff.length > 2){
    		            			
    		            			apgname = "APG"+apg.size();
    			            		boolean flag = false;
    			            		for (int z =2; z < tempbuff.length; z=z+ 4){
    		            				if (z+4 <= tempbuff.length ){
    		            					
    		            					int signal =0;
    		            					if (tempbuff[z+2].charAt(0) == '-'){
    		            						try{
    		            							signal = Integer.parseInt(tempbuff[z+2].substring(1));
    		            						}catch(Exception e){}
    		            					}
    		            					
    		            					AccessPoint tap = new AccessPoint(tempbuff[z].trim(), tempbuff[z+1].trim(),signal,apgname);
    		            					if (ap.containsKey(apgname)){
    		            						ArrayList al = (ArrayList)ap.get(apgname);
    		            						al.add(tap);
    		            						ap.put(apgname, al);
    		            						
    		            					}else{
    		            						ArrayList al = new ArrayList();
    		            						al.add(tap);
    		            						ap.put(apgname, al);
    		            						flag = true;
    		            					}
    		            				}
    		            			}
    			            		
    			            		String currmin = "";
    	                            if ((currminutes >= 0) && (currminutes<= 9)) {currmin="00--09";}
    	                            if ((currminutes >= 10) && (currminutes<= 19)) {currmin="10--19";}
    	                            if ((currminutes >= 20) && (currminutes<= 29)) {currmin="20--29";}
    	                            if ((currminutes >= 30) && (currminutes<= 39)) {currmin="30--39";}
    	                            if ((currminutes >= 40) && (currminutes<= 49)) {currmin="40--49";}
    	                            if ((currminutes >= 50) && (currminutes<= 59)) {currmin="50--59";}
    			            		
    			            		if (flag){
    			            			AccessPointGroup tapg = new AccessPointGroup(apgname, currdate,currhours,currmin,"","","","");
    			            			apg.put(apgname, tapg);
    			            		}
    		            		}else{
    		            			apgname = "none";
    		            		}
    		            		if (!wifi.containsKey(currdate))
                                {
                                	HashMap hoursmap = new HashMap();
                						
                                	for (int x = 0; x < 24; x++){
                                		HashMap minutesmap = new HashMap();
                                    	minutesmap.put("00--09", new ArrayList());
                                    	minutesmap.put("10--19", new ArrayList());
                                    	minutesmap.put("20--29", new ArrayList());
                                    	minutesmap.put("30--39", new ArrayList());
                                    	minutesmap.put("40--49", new ArrayList());
                                    	minutesmap.put("50--59", new ArrayList());
                            
                                		if (x< 10){
                			           		hoursmap.put("0"+String.valueOf(x), minutesmap);
                						}else{
                					        hoursmap.put(String.valueOf(x), minutesmap);
                						}
                					}
                                	wifi.put(currdate, hoursmap);
                                }
                                HashMap datemap = (HashMap)wifi.get(currdate);
                                HashMap hoursmap = (HashMap)datemap.get(currhours);
                                String currmin = "";
                                if ((currminutes >= 0) && (currminutes<= 9)) {currmin="00--09";}
                                if ((currminutes >= 10) && (currminutes<= 19)) {currmin="10--19";}
                                if ((currminutes >= 20) && (currminutes<= 29)) {currmin="20--29";}
                                if ((currminutes >= 30) && (currminutes<= 39)) {currmin="30--39";}
                                if ((currminutes >= 40) && (currminutes<= 49)) {currmin="40--49";}
                                if ((currminutes >= 50) && (currminutes<= 59)) {currmin="50--59";}
                                ArrayList minutesal = (ArrayList)hoursmap.get(currmin);
                                minutesal.add(apgname);
                                hoursmap.put(currmin, minutesal);
                                datemap.put(currhours, hoursmap);
                                wifi.put(currdate, datemap);
    		            	}
    	            			
    		            	
    					}catch(Exception e){
    						e.printStackTrace();
    					}
    					
    					List<String> sortedKeys=new ArrayList(wifi.keySet());
    					Collections.sort(sortedKeys);
    					
    					//write everything
    					try{
    						//BufferedWriter out = new BufferedWriter(new FileWriter(directory+userid+"_location_all.txt"));
    						for (String str : sortedKeys) {
    							
    							//out.write("**********************************Day: "+str);
    						    //out.newLine();
    						    
    							HashMap listsyesterday = (HashMap)wifi.get(str);
    							//System.out.println("**********************************Day: "+str);
    							
    							for (int x = 0; x < 24; x++){
    								//System.out.println("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.write("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.newLine();
    							    if (x < 10){
    							    	try{
    							    		HashMap minutesmap = (HashMap)listsyesterday.get("0"+String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    									//		System.out.println("Minutes: "+smin+":");
    									//		out.write("Minutes: "+smin+":");
    											if (al != null){
    												for (int j = 0; j < al.size(); j++){
    													String fulldetails ="";
    													if (ap.containsKey(al.get(j).toString())){
    														ArrayList wz = (ArrayList)ap.get(al.get(j).toString());
    														for (int t =0; t < wz.size(); t++){
    															AccessPoint tap = (AccessPoint) wz.get(t);
    															fulldetails=fulldetails+"("+tap.getName()+","+tap.getId()+","+tap.getSignal()+"),";
    														}
    													}
    										//			out.write(al.get(j).toString()+": "+fulldetails);
    										//			out.newLine();
    												}
    											}
    										}
    							    	}catch(Exception zz){}
    								}else{
    									try{
    										HashMap minutesmap = (HashMap)listsyesterday.get(String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    											//System.out.println("Minutes: "+smin+":");
    											//out.write("Minutes: "+smin+":");
    											if (al != null){
    												for (int j = 0; j < al.size(); j++){
    												//	System.out.println(al.get(j).toString());
    													String fulldetails ="";
    													if (ap.containsKey(al.get(j).toString())){
    														ArrayList wz = (ArrayList)ap.get(al.get(j).toString());
    														for (int t =0; t < wz.size(); t++){
    															AccessPoint tap = (AccessPoint) wz.get(t);
    															fulldetails=fulldetails+"("+tap.getName()+","+tap.getId()+","+tap.getSignal()+"),";
    														}
    													}
    											//		out.write(al.get(j).toString()+": "+fulldetails);
    											//		out.newLine();
    												}
    											}
    										}
    									}catch(Exception zz){}
    								}
    							}
    						}
    						//out.close();
    					}catch(Exception ex){ex.printStackTrace();}
    					
    		return wifi;
    	}
    	
    	public HashMap processLightData(String directory, String yesdate){
    		HashMap light = new HashMap();
    					try{
    						File tempf = new File(directory+"set_"+yesdate+"_light_capture.csv");
    		        		BufferedReader br = new BufferedReader(new FileReader(tempf));
    		            	String line;
    		            	
    		            	while ((line = br.readLine()) != null) {
    		            		String [] tempbuff = line.split(",");
    		            		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    		            		Date date = format.parse(tempbuff[2]);
    		            		String currdate="";
    		            		int currminutes;
    		            		int currmonth = date.getMonth()+1;
    		            		if (currmonth < 10){
    		            			if (date.getDate() < 10){
    		            				currdate ="0"+currmonth+"/"+"0"+date.getDate();
    	            				}else{
    	            					currdate ="0"+currmonth+"/"+date.getDate();
    	            				}
                				}else{
                					if (date.getDate() < 10){
                						currdate =currmonth+"/"+"0"+date.getDate();
                    				}else{
                    					currdate =currmonth+"/"+date.getDate();
                    				}
                				}
    		            		Calendar c = Calendar.getInstance();
    		            		c.setTime(date);
    		            		currdate = currdate+","+c.get(Calendar.DAY_OF_WEEK);
    		            		currminutes= date.getMinutes();
    		            		
    		            		String currhours = "";
                				if (date.getHours() < 10){
                					currhours ="0"+String.valueOf(date.getHours());
                				}else{
                					currhours =String.valueOf(date.getHours());
                				}
    		            		
    		            		double lvalue = -1.0;
    		            		if (tempbuff.length > 6){
    		            			try{
    		            				lvalue = Double.parseDouble(tempbuff[6]);
    		            			}catch(Exception exx){}
    		            		}else{
    		            			try{
    		            				lvalue = Double.parseDouble(tempbuff[3]);
    		            			}catch(Exception exx){}
    		            		}
    		            		if (lvalue != -1.0){
    		            			if (!light.containsKey(currdate))
                                	{
                                		HashMap hoursmap = new HashMap();
                						
                                		for (int x = 0; x < 24; x++){
                                			HashMap minutesmap = new HashMap();
                                    		minutesmap.put("00--09", new ArrayList());
                                    		minutesmap.put("10--19", new ArrayList());
                                    		minutesmap.put("20--29", new ArrayList());
                                    		minutesmap.put("30--39", new ArrayList());
                                    		minutesmap.put("40--49", new ArrayList());
                                    		minutesmap.put("50--59", new ArrayList());
                            
                                			if (x< 10){
                			            		hoursmap.put("0"+String.valueOf(x), minutesmap);
                							}else{
                						        hoursmap.put(String.valueOf(x), minutesmap);
                							}
                						}
                                		light.put(currdate, hoursmap);
                                	}
                                	HashMap datemap = (HashMap)light.get(currdate);
                                	HashMap hoursmap = (HashMap)datemap.get(currhours);
                                	String currmin = "";
                                	if ((currminutes >= 0) && (currminutes<= 9)) {currmin="00--09";}
                                	if ((currminutes >= 10) && (currminutes<= 19)) {currmin="10--19";}
                                	if ((currminutes >= 20) && (currminutes<= 29)) {currmin="20--29";}
                                	if ((currminutes >= 30) && (currminutes<= 39)) {currmin="30--39";}
                                	if ((currminutes >= 40) && (currminutes<= 49)) {currmin="40--49";}
                                	if ((currminutes >= 50) && (currminutes<= 59)) {currmin="50--59";}
                                	ArrayList minutesal = (ArrayList)hoursmap.get(currmin);
                                	minutesal.add(lvalue);
                                	hoursmap.put(currmin, minutesal);
                                	datemap.put(currhours, hoursmap);
                                	light.put(currdate, datemap);
    		            		}
    	            			
    		            	}
    					}catch(Exception e){
    						e.printStackTrace();
    					}
    					
    					List<String> sortedKeys=new ArrayList(light.keySet());
    					Collections.sort(sortedKeys);
    					
    					//write everything
    					try{
    						//BufferedWriter out = new BufferedWriter(new FileWriter(directory+userid+"_li_all.txt"));
    						for (String str : sortedKeys) {
    							
    							//out.write("**********************************Day: "+str);
    						    //out.newLine();
    						    
    							HashMap listsyesterday = (HashMap)light.get(str);
    							//System.out.println("**********************************Day: "+str);
    							
    							for (int x = 0; x < 24; x++){
    								//System.out.println("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.write("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.newLine();
    							    if (x < 10){
    							    	try{
    							    		HashMap minutesmap = (HashMap)listsyesterday.get("0"+String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    											//System.out.print("Minutes: "+smin+":");
    											String average= averageLightList(al);
    											//System.out.println(average);
    											minutesmap.put(smin, average);
    											//out.write("Minutes: "+smin+":");
    											//out.write(average);
    											//out.newLine();
    										}
    										listsyesterday.put("0"+String.valueOf(x), minutesmap);
    							    	}catch(Exception zz){}
    								}else{
    									try{
    										HashMap minutesmap = (HashMap)listsyesterday.get(String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    											//System.out.print("Minutes: "+smin+":");
    											String average= averageLightList(al);
    											//System.out.println(average);
    											minutesmap.put(smin, average);
    											
    											//out.write("Minutes: "+smin+":");
    											//out.write(average);
    											//out.newLine();
    										}
    										listsyesterday.put(String.valueOf(x), minutesmap);
    									}catch(Exception zz){}
    								}
    							}
    							light.put(str, listsyesterday);
    						}
    						//out.close();
    					}catch(Exception ex){ex.printStackTrace();}
    		
    		return light;
    	}
    	
    	private String averageLightList(ArrayList al){
    		String result="";
    		
    		if (al!= null){
    			ArrayList<Double> data = new ArrayList();
    			for (int i =0; i < al.size(); i++){
        			double curr = (Double)al.get(i);
        			data.add(curr);
        		}
    			Statistics st = new Statistics(data);
    			if (st.median() > 0){
    				result = st.getMean()+","+st.getMode()+","+st.median()+","+st.getStdDev()+","+st.getMinimum()+","+st.getMaximum()+","+st.getRange();
    			}
    			System.out.println(result);
        	}
    		return result;
        }
    	
    	public HashMap processMagneticFieldData(String directory, String yesdate){
    		HashMap magnetic = new HashMap();
    		
    					try{
    						File tempf = new File(directory+"set_"+yesdate+"_magneticfield_capture.csv");
    					    
    		        		BufferedReader br = new BufferedReader(new FileReader(tempf));
    		            	String line;
    		            	
    		            	while ((line = br.readLine()) != null) {
    		            		try{
    		            		String [] tempbuff = line.split(",");
    		            		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    		            		Date date = format.parse(tempbuff[2]);
    		            		String currdate="";
    		            		int currminutes;
    		            		int currmonth = date.getMonth()+1;
    		            		if (currmonth < 10){
    		            			if (date.getDate() < 10){
    		            				currdate ="0"+currmonth+"/"+"0"+date.getDate();
    	            				}else{
    	            					currdate ="0"+currmonth+"/"+date.getDate();
    	            				}
                				}else{
                					if (date.getDate() < 10){
                						currdate =currmonth+"/"+"0"+date.getDate();
                    				}else{
                    					currdate =currmonth+"/"+date.getDate();
                    				}
                				}
    		            		Calendar c = Calendar.getInstance();
    		            		c.setTime(date);
    		            		currdate = currdate+","+c.get(Calendar.DAY_OF_WEEK);
    		            		currminutes= date.getMinutes();
    		            		
    		            		String currhours = "";
                				if (date.getHours() < 10){
                					currhours ="0"+String.valueOf(date.getHours());
                				}else{
                					currhours =String.valueOf(date.getHours());
                				}
    		            		
    		            		double mvalue1 = -10000.0;
    		            		double mvalue2 = -10000.0;
    		            		double mvalue3 = -10000.0;
    		            		if (tempbuff.length > 5){
    		            			try{
    		            				mvalue1 = Double.parseDouble(tempbuff[3]);
    		            				mvalue2 = Double.parseDouble(tempbuff[4]);
    		            				mvalue3 = Double.parseDouble(tempbuff[5]);
    		            			}catch(Exception exx){}
    		            		}
    		            		System.out.println(mvalue1+","+mvalue2+","+mvalue3);
    		            		if ((mvalue1 != -10000.0) && (mvalue2 != -10000.0) && (mvalue3 != -10000.0)){
    		            			if (!magnetic.containsKey(currdate))
                                	{
                                		HashMap hoursmap = new HashMap();
                						
                                		for (int x = 0; x < 24; x++){
                                			HashMap minutesmap = new HashMap();
                                    		minutesmap.put("00--09", new ArrayList());
                                    		minutesmap.put("10--19", new ArrayList());
                                    		minutesmap.put("20--29", new ArrayList());
                                    		minutesmap.put("30--39", new ArrayList());
                                    		minutesmap.put("40--49", new ArrayList());
                                    		minutesmap.put("50--59", new ArrayList());
                            
                                			if (x< 10){
                			            		hoursmap.put("0"+String.valueOf(x), minutesmap);
                							}else{
                						        hoursmap.put(String.valueOf(x), minutesmap);
                							}
                						}
                                		magnetic.put(currdate, hoursmap);
                                	}
                                	HashMap datemap = (HashMap)magnetic.get(currdate);
                                	HashMap hoursmap = (HashMap)datemap.get(currhours);
                                	String currmin = "";
                                	if ((currminutes >= 0) && (currminutes<= 9)) {currmin="00--09";}
                                	if ((currminutes >= 10) && (currminutes<= 19)) {currmin="10--19";}
                                	if ((currminutes >= 20) && (currminutes<= 29)) {currmin="20--29";}
                                	if ((currminutes >= 30) && (currminutes<= 39)) {currmin="30--39";}
                                	if ((currminutes >= 40) && (currminutes<= 49)) {currmin="40--49";}
                                	if ((currminutes >= 50) && (currminutes<= 59)) {currmin="50--59";}
                                	ArrayList minutesal = (ArrayList)hoursmap.get(currmin);
                                	minutesal.add(mvalue1+","+mvalue2+","+mvalue3);
                                	hoursmap.put(currmin, minutesal);
                                	datemap.put(currhours, hoursmap);
                                	magnetic.put(currdate, datemap);
    		            		}
    		            		}catch(Exception es){es.printStackTrace();}
    		            	}
    					}catch(Exception e){
    						e.printStackTrace();
    					}
    					
    					List<String> sortedKeys=new ArrayList(magnetic.keySet());
    					Collections.sort(sortedKeys);
    					
    					//write everything
    					try{
    						//BufferedWriter out = new BufferedWriter(new FileWriter(directory+userid+"_mf_all.txt"));
    						for (String str : sortedKeys) {
    							
    							//out.write("**********************************Day: "+str);
    						    //out.newLine();
    						    
    							HashMap listsyesterday = (HashMap)magnetic.get(str);
    							//System.out.println("**********************************Day: "+str);
    							
    							for (int x = 0; x < 24; x++){
    								//System.out.println("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.write("^^^^^^^^^^^^^^^^^^Hour: "+String.valueOf(x)+":");
    								//out.newLine();
    							    if (x < 10){
    							    	try{
    							    		HashMap minutesmap = (HashMap)listsyesterday.get("0"+String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    									//		System.out.print("Minutes: "+smin+":");
    											String average= averageMagneticList(al);
    									//		System.out.println(average);
    											minutesmap.put(smin, average);
    									//		out.write("Minutes: "+smin+":");
    									//		out.write(average);
    									//		out.newLine();
    										}
    										listsyesterday.put("0"+String.valueOf(x), minutesmap);
    							    	}catch(Exception zz){}
    								}else{
    									try{
    										HashMap minutesmap = (HashMap)listsyesterday.get(String.valueOf(x));
    							    		List<String> sortedMinutes=new ArrayList(minutesmap.keySet());
    										Collections.sort(sortedMinutes);
    										for (String smin : sortedMinutes) {
    											ArrayList al = (ArrayList) minutesmap.get(smin);
    										//	System.out.print("Minutes: "+smin+":");
    											String average= averageMagneticList(al);
    										//	System.out.println(average);
    											minutesmap.put(smin, average);
    											
    										//	out.write("Minutes: "+smin+":");
    										//	out.write(average);
    										//	out.newLine();
    										}
    										listsyesterday.put(String.valueOf(x), minutesmap);
    									}catch(Exception zz){}
    								}
    							}
    							magnetic.put(str, listsyesterday);
    						}
    						//out.close();
    					}catch(Exception ex){ex.printStackTrace();}
    		
    		return magnetic;
    	}
    	
    	private String averageMagneticList(ArrayList al){
    		String result="";
    		//System.out.println(al.size());
    		if (al!= null){
    			ArrayList<Double> data1 = new ArrayList();
    			ArrayList<Double> data2= new ArrayList();
    			ArrayList<Double> data3 = new ArrayList();
    			for (int i =0; i < al.size(); i++){
    				String[] tempbuff = al.get(i).toString().split(","); 
        			double curr1 = Double.parseDouble(tempbuff[0]);
        			data1.add(curr1);
        			double curr2 = Double.parseDouble(tempbuff[1]);
        			data2.add(curr2);
        			double curr3 = Double.parseDouble(tempbuff[2]);
        			data3.add(curr3);
        		}
    			Statistics st1 = new Statistics(data1);
    			Statistics st2 = new Statistics(data2);
    			Statistics st3 = new Statistics(data3);
    			
    			double mean1=st1.getMean();double mode1=st1.getMode();double median1=st1.median();	double stddev1=st1.getStdDev();
    			double min1=st1.getMinimum(); double max1=st1.getMaximum(); double range1=st1.getRange();
    			
    			if (Double.isNaN(mean1)){ mean1 = 0.0; } if (Double.isNaN(mode1)){ mode1 = 0.0; } if (Double.isNaN(median1)){ median1 = 0.0; }
    			if (Double.isNaN(stddev1)){ stddev1 = 0.0; } if (Double.isNaN(min1)){ min1 = 0.0; } if (Double.isNaN(max1)){ max1 = 0.0; }
    			if (Double.isNaN(range1)){ range1 = 0.0; }
    			
    			double mean2=st2.getMean();double mode2=st2.getMode();double median2=st2.median();	double stddev2=st2.getStdDev();
    			double min2=st2.getMinimum(); double max2=st2.getMaximum(); double range2=st2.getRange();
    			
    			if (Double.isNaN(mean2)){ mean2 = 0.0; } if (Double.isNaN(mode2)){ mode2 = 0.0; } if (Double.isNaN(median2)){ median2 = 0.0; }
    			if (Double.isNaN(stddev2)){ stddev2 = 0.0; } if (Double.isNaN(min2)){ min2 = 0.0; } if (Double.isNaN(max2)){ max2 = 0.0; }
    			if (Double.isNaN(range2)){ range2 = 0.0; }
    			
    			double mean3=st3.getMean();double mode3=st3.getMode();double median3=st3.median();	double stddev3=st3.getStdDev();
    			double min3=st3.getMinimum(); double max3=st3.getMaximum(); double range3=st3.getRange();
    			
    			if (Double.isNaN(mean3)){ mean3 = 0.0; } if (Double.isNaN(mode3)){ mode3 = 0.0; } if (Double.isNaN(median3)){ median3 = 0.0; }
    			if (Double.isNaN(stddev3)){ stddev3 = 0.0; } if (Double.isNaN(min3)){ min3 = 0.0; } if (Double.isNaN(max3)){ max3 = 0.0; }
    			if (Double.isNaN(range3)){ range3 = 0.0; }
    			
    			result = result+ mean1+","+mode1+","+median1+","+stddev1+","+min1+","+max1+","+range1+",";
    			result = result+ mean2+","+mode2+","+median2+","+stddev2+","+min2+","+max2+","+range2+",";
    			result = result+ mean3+","+mode3+","+median3+","+stddev3+","+min3+","+max3+","+range3;
    			//System.out.println("res:"+result);
        	}
    		return result;
        }
        
        public ArrayList processWifiDatav2(String directory, String yesdate){
    		
        	ArrayList wifinames = new ArrayList();
        	
    		try{
    			File tempf = new File(directory+"set_"+yesdate+"_wifi_capture.csv");
    		    BufferedReader br = new BufferedReader(new FileReader(tempf));
    		    String line;
    		            	
    		    while ((line = br.readLine()) != null) {
    		    	String [] tempbuff = line.split(",");
    		        if (tempbuff.length > 2){
    		        	String nameid = "";
    			        HashMap nameonly = new HashMap(); 
    			        for (int z =2; z < tempbuff.length; z=z+ 4){
    		            	if (z+4 <= tempbuff.length ){
    		            		//tempbuff[z] = tempbuff[z].replace(" ","_");
    		            		//tempbuff[z] = tempbuff[z].replace("'","_");
    		            		if (tempbuff[z].trim().length() > 1){
    		            			nameid=nameid+tempbuff[z].trim()+"-"+tempbuff[z+1].trim()+",";
    		            			if (!nameonly.containsKey(tempbuff[z].trim())){
    		            				nameonly.put(tempbuff[z].trim(), "");
    		            			}
    		            		}
    		            	}
    		            }
    			        /*if (nameid.length() > 2){
    			        	wifilist.add(nameid);
    			        }*/
    			        if (nameonly.size() > 0){
    			        	List<String> sortedKeys=new ArrayList(nameonly.keySet());
    			    		Collections.sort(sortedKeys);
    			    		String uniquenames="";
    			    		for (String str : sortedKeys) {
    			    			uniquenames=uniquenames+str+",";
    			    		}
    			    		wifinames.add(uniquenames);
    			        }
    			            		
    		         }
    		     }
    		            	
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
    		return wifinames;
    	}
        
        public void ZipProcessedFilesAndEmailThem(String userid, String yesterday){
			ArrayList fileslist = new ArrayList();
			String filedir=getBaseContext().getFilesDir().getPath()+"/processed/";
			File f=new File(filedir);
			if(f.isDirectory()){
				String files[]=  f.list();
				for(int i=0;i<files.length;i++){
					if (!files[i].contains("zip")){
						Log.d("abc_log",filedir+files[i]);
						fileslist.add(new File(filedir+"/"+files[i]));
					}
				}
			}
			
			Date dt = new Date();
			String title = "Email Notification (Processing files) - User: "+userid+", "+dt.toString();
			String content="";
			for (int i = 0 ; i < fileslist.size(); i++){
				File tempfile = (File)fileslist.get(i);
				Date lastModDate = new Date(tempfile.lastModified());
				content= content+ tempfile.getPath()+":"+lastModDate.toString()+", size:"+tempfile.length()+"\n";
			}
			
			String fileName2 = userid+"_processingfiles_zip_"+yesterday+".zip";

				try{

					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
				if(fz.isDirectory()){
        			filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
        			fz=new File(filedir,"");
      				if(!fz.isDirectory()){
      					fz.mkdir();
      					fz.setReadable(true, false);
        				fz.setWritable(true, false);
        				fz.setExecutable(true, false);
      				}
				 }

					 ZipFile zipFile = new ZipFile(filedir+"/"+fileName2);
					 ZipParameters parameters = new ZipParameters();
					 parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

					 parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
					 parameters.setEncryptFiles(true);
					 parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

					 parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

					 // Set password
					 parameters.setPassword("abcd1234");

					 zipFile.addFiles(fileslist, parameters);

				  } catch (ZipException e) {
					  e.printStackTrace();
				  }

				  try{
					  File fzip =new File(filedir+"/"+fileName2);
					  Log.d("abc_log","Zipped all files in: "+fileName2);
					  fzip.setReadable(true, false);
					  fzip.setWritable(true, false);
					  fzip.setExecutable(true, false);
				  }catch(Exception e){
					  e.printStackTrace();
				  }

				  ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				  NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


				  if (CheckInternet()){
				  	  try {
                    GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley1234");
                    sender.addAttachment(filedir+"/"+fileName2, content);
                    sender.sendMail(title,
                            content,
                            "sensorsdatacollection@gmail.com",
                            "nicholas.micallef@gcu.ac.uk");
				  } catch (Exception e) {
					 Log.e("SendMail", e.getMessage(), e);
				  }
			}

        }
        
        public boolean CheckInternet()
        {
            ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            // Here if condition check for wifi and mobile network is available or not.
            // If anyone of them is available or connected then it will return true, otherwise false;

            if (wifi.isConnected()) {
                return true;
            } else if (mobile.isConnected()) {
                return true;
            }
            return false;
        }
        
        public boolean copyFiles(String userid,String dir, String filetype, String yesdate, int index){
    		boolean flag = false;
        	String filename = userid+filetype;
    		String outdir=dir+"processed/";
    		File fz=new File(outdir);
    		try{	
    			if(!fz.isDirectory()){
    				fz.mkdir();
    				fz.setReadable(true, false);
    				fz.setWritable(true, false);
    				fz.setExecutable(true, false);
    			}
        	}catch(Exception e){
        		e.printStackTrace();
        	}
    		
    		
    		String[] parsedate = yesdate.split("-");
    		try{
    			String prevmonth = parsedate[1].toString();
    			int prevday = Integer.parseInt(parsedate[2].toString());
    			File tempf = new File(dir+filename);
    			
    			BufferedReader br = new BufferedReader(new FileReader(tempf));
    			BufferedWriter outdat = new BufferedWriter(new FileWriter(outdir+"set_"+yesdate+filetype));	
    			
    			String line;
            	
            	while ((line = br.readLine()) != null) {
            		String [] tempbuff = line.split(",");
            		if (tempbuff.length >= index){
            			String tempbuff2[] = tempbuff[index].split(" ");
            			if (tempbuff2.length >= 2){
            				try{
            					int day = Integer.parseInt(tempbuff2[2]);
            					
            					if ((day == prevday) && (tempbuff2[1].equalsIgnoreCase(prevmonth))){
            						flag = true;
            						outdat.write(line);
            						outdat.newLine();
            					}
            				}catch(Exception et){}
            			}
            		}
            	}
            	outdat.close();
            	br.close();
    			
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
    		try{
    			File gfile =new File(outdir+"set_"+yesdate+filetype);
				gfile.setReadable(true, false);
				gfile.setWritable(true, false);
				gfile.setExecutable(true, false);
			}catch(Exception e){
				e.printStackTrace();
			}
    		return flag;
    	}
    };
}
