package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AccelerometerAnalysis  extends Service {
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
        	try{
        		// prepare groundtruth data
        		BufferedReader r = new BufferedReader(new InputStreamReader(getBaseContext().getAssets().open("groundtruth_WISDM.csv")));
        		String sline;
        		int counter = 0;
        		ArrayList walkingal = new ArrayList();
        		ArrayList joggingal = new ArrayList();
        		ArrayList upstairsal = new ArrayList();
        		ArrayList downstairsal = new ArrayList();
        		ArrayList standingal = new ArrayList();
        		ArrayList sittingal = new ArrayList();
        		while ((sline = r.readLine()) != null) {
        			if (sline.contains("Walking")){
        				walkingal.add(sline);
        			}
        			if (sline.contains("Jogging")){
        				joggingal.add(sline);
        			}
        			if (sline.contains("Upstairs")){
        				upstairsal.add(sline);
        			}
        			if (sline.contains("Downstairs")){
        				downstairsal.add(sline);
        			}
        			if (sline.contains("Sitting")){
        				sittingal.add(sline);
        			}
        			if (sline.contains("Standing")){
        				standingal.add(sline);
        			}
        			//total.append(line);
        			counter++;
        		}
        		Log.d("abc_log","no of groundtruth items:"+counter+" ,walking:"+walkingal.size()+" ,jogging: "+joggingal.size()+" ,upstairs:"+upstairsal.size()+" ,downstairs"+downstairsal.size()+" ,sitting:"+sittingal.size()+" ,standing:"+standingal.size());

        		HashMap values =  new HashMap();
        		String dir = getBaseContext().getFilesDir().getPath();
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		//ArrayList fileslist = new ArrayList();
        		File f=new File(dir);
        		if(f.isDirectory()){
        			String files[]=  f.list();
        			for(int i=0;i<files.length;i++){
        				if (files[i].contains(tm.getDeviceId()) && (files[i].contains("Acceleration") || files[i].contains("Accelerometer")) && (!files[i].contains("Linear")) ){
        					if (!files[i].contains("zip")){
        						File tempf = new File(dir+files[i]);
        						long lastTime = tempf.lastModified();
        				        Date nowDate = new Date();
        				        long nowTime = nowDate.getTime();
        				        //if (nowTime - lastTime < 60*60*1000){

        				        	Log.d("abc_log",files[i]);
        				        	BufferedReader br = new BufferedReader(new FileReader(tempf));
        				            String line;

        				            while ((line = br.readLine()) != null) {
        				            	//line = SimpleCrypto.decrypt(SimpleCrypto.PASSWORD, line);
        				                String [] tempbuff = line.split(",");
        				                if (tempbuff.length > 5){
        				                	values.put(tempbuff[0].toString(), tempbuff[3].toString()+","+tempbuff[4].toString()+","+tempbuff[5].toString());
        				                }
        				            }
        				        	//fileslist.add(files[i]);
        				        //}
        					}
        				}
        			}
        		}
        		Log.d("abc_log","size of collected accelerometer data:"+values.size());

        		Iterator it = values.entrySet().iterator();
                ArrayList tempal = new ArrayList();
                ArrayList transformedal = new ArrayList();
        		long treshold = 10*1000;
                long firstTime =-1;
                int id=0;
        		while (it.hasNext()){
                	HashMap.Entry pairs = (HashMap.Entry)it.next();
                	if (firstTime != -1){
                		long currtime = Long.decode(pairs.getKey().toString());
                		if (currtime - firstTime <= treshold){
                			tempal.add(pairs.getKey().toString()+","+pairs.getValue().toString());
                		}else{
                			firstTime = currtime;
                			// do all the processing here
                			String result = processTenSecondsData(tempal,id);
                			if (result != null){
                				transformedal.add(result);
                				id++;
                			}
                			tempal = new ArrayList();
                		}
                	}else{
                		firstTime = Long.decode(pairs.getKey().toString());
                		tempal.add(pairs.getKey().toString()+","+pairs.getValue().toString());
                	}
                }


        		for (int i = 0; i < transformedal.size(); i++){
        			Log.d("abc_log",transformedal.get(i).toString());
        		}
        		Log.d("abc_log","size of transformed data: "+transformedal.size());

        		// use weka !!!

        	}catch(Exception e){
        		Log.d("abc_log","Exception in accelerometer analysis: "+e.toString());
        	}
        }

        private String processTenSecondsData(ArrayList al, int id){
        	//Log.d("abc_log","Entered: processTenSecondsData:"+al.size());
        	String result ="";
        	try{

        	double xavg=0, yavg=0,zavg=0;
        	double xpeak=0, ypeak=0,zpeak=0;
        	double xabsdev=0, yabsdev=0,zabsdev=0;
        	double xstandev=0, ystandev=0,zstandev=0;
        	double resultant=0;
        	double minx = 1000;
        	double maxx = -1000;
        	double miny = 1000;
        	double maxy = -1000;
        	double minz = 1000;
        	double maxz = -1000;
        	for (int i = 0; i < al.size(); i++){
        		String [] tempbuff = al.get(i).toString().split(",");
        		double tempx = Double.parseDouble(tempbuff[1]);
        		double tempy = Double.parseDouble(tempbuff[2]);
        		double tempz = Double.parseDouble(tempbuff[3]);

        		xavg = xavg + tempx;
        		yavg = yavg + tempy;
        		zavg = zavg + tempz;
        		if (tempx < minx){ minx = tempx; }
        		if (tempx > maxx){ maxx = tempx; }
        		if (tempy < miny){ miny = tempy; }
        		if (tempy > maxy){ maxy = tempy; }
        		if (tempz < minz){ minz = tempz; }
        		if (tempz > maxz){ maxz = tempz; }
        	}
        	resultant = Math.sqrt((xavg*xavg)+(yavg*yavg)+(zavg*zavg));
        	xavg = xavg / (double) al.size(); yavg = yavg / (double) al.size(); zavg = zavg / (double) al.size();

        	double binsizex = (maxx-minx)/11;
        	double binsizey = (maxy-miny)/11;
        	double binsizez = (maxy-minz)/11;


        	if ((minx == 1000) || (maxx == -1000) || (miny == 1000) || (maxy == -1000) || (minz == 1000) || (maxz == -1000)){
        		result = null;
        	}else{



        		double binx0=0, binx1=0, binx2=0, binx3=0, binx4=0, binx5=0, binx6=0, binx7=0, binx8=0, binx9=0;
        		double biny0=0, biny1=0, biny2=0, biny3=0, biny4=0, biny5=0, biny6=0, biny7=0, biny8=0, biny9=0;
        		double binz0=0, binz1=0, binz2=0, binz3=0, binz4=0, binz5=0, binz6=0, binz7=0, binz8=0, binz9=0;

        		HashMap pxvalues = new HashMap();
        		HashMap pyvalues = new HashMap();
        		HashMap pzvalues = new HashMap();
        		int amplitudex = 90;
        		int amplitudey = 90;
        		int amplitudez = 90;

        		for (int i = 0; i < al.size(); i++){
            		String [] tempbuff = al.get(i).toString().split(",");
            		double tempx = Double.parseDouble(tempbuff[1]);
            		double tempy = Double.parseDouble(tempbuff[2]);
            		double tempz = Double.parseDouble(tempbuff[3]);

            		xstandev = xstandev+((tempx - xavg)*(tempx - xavg));
            		ystandev = ystandev+((tempy - yavg)*(tempy - yavg));
            		zstandev = zstandev+((tempz - zavg)*(tempz - zavg));

            		xabsdev = xabsdev + (tempx - xavg);
            		yabsdev = yabsdev + (tempy - yavg);
            		zabsdev = zabsdev + (tempz - zavg);

            		if (tempx > ( (maxx * (double)amplitudex)/100 )){
            			pxvalues.put(Double.parseDouble(tempbuff[0]), tempx);
            		}

            		if (tempy > ( (maxy * (double)amplitudey)/100 )){
            			pyvalues.put(Double.parseDouble(tempbuff[0]), tempy);
            		}

            		if (tempz > ( (maxz * (double)amplitudez)/100 )){
            			pzvalues.put(Double.parseDouble(tempbuff[0]), tempz);
            		}

            		if ((tempx < maxx) && (tempx > (maxx-(binsizex*1)))) { binx0++; }
            		if ((tempx < (maxx-(binsizex*1))) && (tempx > (maxx-(binsizex*2)))) { binx1++; }
            		if ((tempx < (maxx-(binsizex*2))) && (tempx > (maxx-(binsizex*3)))) { binx2++; }
            		if ((tempx < (maxx-(binsizex*3))) && (tempx > (maxx-(binsizex*4)))) { binx3++; }
            		if ((tempx < (maxx-(binsizex*4))) && (tempx > (maxx-(binsizex*5)))) { binx4++; }
            		if ((tempx < (maxx-(binsizex*5))) && (tempx > (maxx-(binsizex*6)))) { binx5++; }
            		if ((tempx < (maxx-(binsizex*6))) && (tempx > (maxx-(binsizex*7)))) { binx6++; }
            		if ((tempx < (maxx-(binsizex*7))) && (tempx > (maxx-(binsizex*8)))) { binx7++; }
            		if ((tempx < (maxx-(binsizex*8))) && (tempx > (maxx-(binsizex*9)))) { binx8++; }
            		if ((tempx < (maxx-(binsizex*9))) && (tempx > minx)) { binx9++; }

            		if ((tempy < maxy) && (tempy > (maxy-(binsizey*1)))) { biny0++; }
            		if ((tempy < (maxy-(binsizey*1))) && (tempy > (maxy-(binsizey*2)))) { biny1++; }
            		if ((tempy < (maxy-(binsizey*2))) && (tempy > (maxy-(binsizey*3)))) { biny2++; }
            		if ((tempy < (maxy-(binsizey*3))) && (tempy > (maxy-(binsizey*4)))) { biny3++; }
            		if ((tempy < (maxy-(binsizey*4))) && (tempy > (maxy-(binsizey*5)))) { biny4++; }
            		if ((tempy < (maxy-(binsizey*5))) && (tempy > (maxy-(binsizey*6)))) { biny5++; }
            		if ((tempy < (maxy-(binsizey*6))) && (tempy > (maxy-(binsizey*7)))) { biny6++; }
            		if ((tempy < (maxy-(binsizey*7))) && (tempy > (maxy-(binsizey*8)))) { biny7++; }
            		if ((tempy < (maxy-(binsizey*8))) && (tempy > (maxy-(binsizey*9)))) { biny8++; }
            		if ((tempy < (maxy-(binsizey*9))) && (tempy > miny)) { biny9++; }

            		if ((tempz < maxz) && (tempz > (maxz-(binsizez*1)))) { binz0++; }
            		if ((tempz < (maxz-(binsizez*1))) && (tempz > (maxz-(binsizez*2)))) { binz1++; }
            		if ((tempz < (maxz-(binsizez*2))) && (tempz > (maxz-(binsizez*3)))) { binz2++; }
            		if ((tempz < (maxz-(binsizez*3))) && (tempz > (maxz-(binsizez*4)))) { binz3++; }
            		if ((tempz < (maxz-(binsizez*4))) && (tempz > (maxz-(binsizez*5)))) { binz4++; }
            		if ((tempz < (maxz-(binsizez*5))) && (tempz > (maxz-(binsizez*6)))) { binz5++; }
            		if ((tempz < (maxz-(binsizez*6))) && (tempz > (maxz-(binsizez*7)))) { binz6++; }
            		if ((tempz < (maxz-(binsizez*7))) && (tempz > (maxz-(binsizez*8)))) { binz7++; }
            		if ((tempz < (maxz-(binsizez*8))) && (tempz > (maxz-(binsizez*9)))) { binz8++; }
            		if ((tempz < (maxz-(binsizez*9))) && (tempz > minz)) { binz9++; }

            	}

        		while ((pxvalues.size() < 3)&& (amplitudex > 0) ){
        			pxvalues.clear();
        			amplitudex = amplitudex - 5;
        			for (int i = 0; i < al.size(); i++){
                		String [] tempbuff = al.get(i).toString().split(",");
                		double tempx = Double.parseDouble(tempbuff[1]);
                		if (tempx > ( (maxx * (double)amplitudex)/100 )){
                			pxvalues.put(tempbuff[0], tempx);
                		}
        			}
        			//Log.d("abc_log","size of pxvalues: "+pxvalues.size()+", amplitudex:"+amplitudex);
        		}

        		Iterator it = pxvalues.entrySet().iterator();
                double prevtimex = -1;
                while (it.hasNext()){
                	HashMap.Entry pairs = (HashMap.Entry)it.next();
                	double currtime = Double.parseDouble(pairs.getKey().toString());
                	if (prevtimex != -1){
                		xpeak = currtime -prevtimex;
                	}
                	prevtimex = currtime;
        		}
        		xpeak = xpeak / (double) pxvalues.size();

        		while ((pyvalues.size() < 3) && (amplitudey > 0)){
        			pyvalues.clear();
        			amplitudey = amplitudey - 5;
        			for (int i = 0; i < al.size(); i++){
                		String [] tempbuff = al.get(i).toString().split(",");
                		double tempy = Double.parseDouble(tempbuff[2]);
                		if (tempy > ( (maxy * (double)amplitudey)/100 )){
                			pyvalues.put(tempbuff[0], tempy);
                		}
        			}
        			//Log.d("abc_log","size of pyvalues: "+pyvalues.size()+", amplitudey:"+amplitudey);
        		}

        		it = pyvalues.entrySet().iterator();
                double prevtimey = -1;
                while (it.hasNext()){
                	HashMap.Entry pairs = (HashMap.Entry)it.next();
                	double currtime = Double.parseDouble(pairs.getKey().toString());
                	if (prevtimey != -1){
                		ypeak = currtime -prevtimey;
                	}
                	prevtimey = currtime;
        		}
        		ypeak = ypeak / (double) pyvalues.size();

        		while ((pzvalues.size() < 3) && (amplitudez > 0) ){
        			pzvalues.clear();
        			amplitudez = amplitudez - 5;
        			for (int i = 0; i < al.size(); i++){
                		String [] tempbuff = al.get(i).toString().split(",");
                		double tempz = Double.parseDouble(tempbuff[3]);
                		if (tempz > ( (maxz * (double)amplitudez)/100 )){
                			pzvalues.put(tempbuff[0], tempz);
                		}
        			}
        			//Log.d("abc_log","size of pzvalues: "+pzvalues.size()+", amplitudez:"+amplitudez);
        		}

        		it = pzvalues.entrySet().iterator();
                double prevtimez = -1;
                while (it.hasNext()){
                	HashMap.Entry pairs = (HashMap.Entry)it.next();
                	double currtime = Double.parseDouble(pairs.getKey().toString());
                	if (prevtimez != -1){
                		zpeak = currtime -prevtimez;
                	}
                	prevtimez = currtime;
        		}
        		zpeak = zpeak / (double) pzvalues.size();


        		xabsdev = xabsdev / (double)al.size();
        		yabsdev = yabsdev / (double)al.size();
        		zabsdev = zabsdev / (double)al.size();

        		xstandev = Math.sqrt(xstandev / (double) al.size());
        		ystandev = Math.sqrt(ystandev / (double) al.size());
        		zstandev = Math.sqrt(zstandev / (double) al.size());

        		binx0 = binx0 /(double)al.size();binx1 = binx1 /(double)al.size();binx2 = binx2 /(double)al.size();binx3 = binx3 /(double)al.size();
        		binx4 = binx4 /(double)al.size();binx5 = binx5 /(double)al.size();binx6 = binx6 /(double)al.size();binx7 = binx7 /(double)al.size();
        		binx8 = binx8 /(double)al.size();binx9 = binx9 /(double)al.size();

        		biny0 = biny0 /(double)al.size();biny1 = biny1 /(double)al.size();biny2 = biny2 /(double)al.size();biny3 = biny3 /(double)al.size();
        		biny4 = biny4 /(double)al.size();biny5 = biny5 /(double)al.size();biny6 = biny6 /(double)al.size();biny7 = biny7 /(double)al.size();
        		biny8 = biny8 /(double)al.size();biny9 = biny9 /(double)al.size();

        		binz0 = binz0 /(double)al.size();binz1 = binz1 /(double)al.size();binz2 = binz2 /(double)al.size();binz3 = binz3 /(double)al.size();
        		binz4 = binz4 /(double)al.size();binz5 = binz5 /(double)al.size();binz6 = binz6 /(double)al.size();binz7 = binz7 /(double)al.size();
        		binz8 = binz8 /(double)al.size();binz9 = binz9 /(double)al.size();

        		//result=	"minx: "+minx+", maxx:"+maxx+" ,binx:"+binsizex+" ,miny: "+miny+", maxy:"+maxy+" ,biny:"+binsizey+" ,minz: "+minz+", maxz:"+maxz+" ,binz:"+binsizez;
        		result =id+","+binx0+","+binx1+","+binx2+","+binx3+","+binx4+","+binx5+","+binx6+","+binx7+","+binx8+","+binx9+","+biny0+","+biny1+","+biny2+","+biny3+","+biny4+","+biny5+","+biny6+","+biny7+","+biny8+","+biny9+","+binz0+","+binz1+","+binz2+","+binz3+","+binz4+","+binz5+","+binz6+","+binz7+","+binz8+","+binz9+","+xavg+","+yavg+","+zavg+","+xpeak+","+ypeak+","+zpeak+","+xabsdev+","+yabsdev+","+zabsdev+","+xstandev+","+ystandev+","+zstandev+","+resultant;
        	}
        	}catch(Exception e){
        		result = null;
        		Log.d("abc_log","Exception in processTenSecondsData: "+e.toString());
        		e.printStackTrace();
        	}

        	return result;
        }


    };

}
