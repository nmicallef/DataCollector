package aexp.sensorsfeedback;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.net.ftp.FTPClient;




import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FilesUploader extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	(new Thread(new Runnable() {
        	public void run() {
        		try{
					Thread.sleep(120000);
				}catch(Exception e){}
        		beat.run();
    	        stopSelf();
        	}
        })).start();
    }
    private static final int BUFFER = 2048;


    public Runnable beat = new Runnable() {

        public void run() {
        	try  {
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        		if (tm.getDeviceId() != null){

        			ArrayList fileslist = new ArrayList();
        			ArrayList fileslist2 = new ArrayList();
        			String filedir=getBaseContext().getFilesDir().getPath();
        			File f=new File(getBaseContext().getFilesDir(),"");
        			if(f.isDirectory()){
        				String files[]=  f.list();
        				for(int i=0;i<files.length;i++){
        					if (files[i].contains(tm.getDeviceId())){
        						if (!files[i].contains("zip")){
        							Log.d("abc_log",filedir+files[i]);
        							fileslist.add(new File(filedir+"/"+files[i]));
        						}
        						if (files[i].contains("light") || files[i].contains("batterylevel") || files[i].contains("magneticfield") || files[i].contains("wifi") || files[i].contains("noise")){
        							fileslist2.add(new File(filedir+"/"+files[i]));
            					}
        					}
        				}
        			}
        			Date dt = new Date();
        			String title = "Email Notification - User: "+tm.getDeviceId()+", "+dt.toString();
        			String content="";
        			for (int i = 0 ; i < fileslist.size(); i++){
        				File tempfile = (File)fileslist.get(i);
        				Date lastModDate = new Date(tempfile.lastModified());
        				content= content+ tempfile.getPath()+":"+lastModDate.toString()+", size:"+tempfile.length()+"\n";
        			}


        			Calendar c = Calendar.getInstance();
        			c.setTime(new Date());


  				  String fileName = tm.getDeviceId()+"_zip_"+c.getTimeInMillis()+".zip";

  				  try{
  					
  					DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
  					DecimalFormat twoDForm = new DecimalFormat("##");  
  					
  					BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"/"+"items_on_screen.txt"));	
  					
  					
  					File fr=new File(filedir);
  					if(fr.isDirectory()){
  						String files[]=  fr.list();
  						for(int i=0;i<files.length;i++){
  							if (files[i].contains("wifi_capture")){
  								
  								try{
  									File tempf = new File(filedir+"/"+files[i].toString());
  									if (tempf.exists()){
  										BufferedReader br = new BufferedReader(new FileReader(tempf));
  										String line;
  					            	    HashMap datecount= new HashMap();
  					            	    
  					            	    while ((line = br.readLine()) != null) {
  											String [] tempbuff = line.split(",");
  											SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
  								        	Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
  								        	if (!datecount.containsKey(dateFormat.format(date))){
  								        		datecount.put(dateFormat.format(date), 0);
  								        	}
  								        	
  								        	int currcount = (Integer)datecount.get(dateFormat.format(date));
  								        	currcount++;
  								        	datecount.put(dateFormat.format(date), currcount);
  										}
  					            	    
  					            	    List<String> sortedKeys=new ArrayList(datecount.keySet());
  					            		Collections.sort(sortedKeys);
  					            	
  					            		for (String day : sortedKeys) {
  					            			int total = (Integer)datecount.get(day);
  					            			double percentage = (Double.valueOf(total)/1440)*100;
  					            			
  					            			String status ="";
  											if (percentage >= 80){
  												status = "complete";
  											}else{
  												status = "incomplete";
  											}
  											
  											outdat.write(day+","+twoDForm.format(percentage)+"% ("+total +" out of 1440),"+status);
  											outdat.newLine();
  											//DataCompletionItem item = new DataCompletionItem(day,twoDForm.format(percentage)+"% ("+total +" out of 1440)",status);
  											//items2.put(day, item);
  					            		}
  										
  									}
  								}catch(Exception e){}
  								
  								break;
  							}
  						}
  					}
  					  
  					outdat.close();

  					  /*if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
  						File fz=new File("/mnt/sdcard","");
  	        			if(fz.isDirectory()){
  	        				filedir="/mnt/sdcard";
  	        			}
  					  }*/
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

  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileName);
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
  					  File fzip =new File(filedir+"/"+fileName);
  					  Log.d("abc_log","Zipped all files in: "+fileName);
  					  fzip.setReadable(true, false);
  					  fzip.setWritable(true, false);
  					  fzip.setExecutable(true, false);
  				  }catch(Exception e){
  					  e.printStackTrace();
  				  }

  				  String fileName2 = tm.getDeviceId()+"_attachment_zip_"+c.getTimeInMillis()+".zip";

  				  try{


  					  /*if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
  						File fz=new File("/mnt/sdcard","");
  	        			if(fz.isDirectory()){
  	        				filedir="/mnt/sdcard";
  	        			}
  					  }*/
  					  File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
					  if(fz.isDirectory()){
	        				filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
	        				  fz=new File(filedir,"");
	      					  if(!fz.isDirectory()){
	      						  fz.mkdir();
	      						  /*fz.setReadable(true, false);
	        					  fz.setWritable(true, false);
	        					  fz.setExecutable(true, false);*/
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

  					  zipFile.addFiles(fileslist2, parameters);

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
  					if (mWifi.isConnected()) {
  				  	  try {
	                    GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
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

  				  if (mWifi.isConnected()) {
  					/*AndroidAuthSession session = buildSession();
  					DropboxAPI<AndroidAuthSession> mApi = new DropboxAPI<AndroidAuthSession>(session);

  					mApi.getSession().startAuthentication(context);*/

  					  /*DbxAccountManager mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);
  					  Activity activity = (Activity) getApplicationContext();
  					  mDbxAcctMgr.startLink(activity, 0);*/


  				  }

  				  /*if (mWifi.isConnected()) {

  					  // upload ftp
  					  String user="nmicallef";
  					  String password= "42b9565b";
  					  String ftpServer="ftp.fileserve.com";


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
  		  							  		Log.d("abc_log", "upload succeeded");
  		  							  	}else{
  		  							  		Log.d("abc_log", "upload failed");
  		  							  	}
  		        					}
  		        				}
  							  }
  		        		  }else{
  							  Log.d("abc_log", "Not connected");
  						  }
  					  }catch (Exception e){
  						  Log.d("abc_log", "Error!!!");
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
  				  }*/
  				  for(int i=0; i < fileslist.size(); i++) {
  					 if (!fileslist.get(i).toString().contains("celltower")  && !fileslist.get(i).toString().contains("light") && !fileslist.get(i).toString().contains("magneticfield") && !fileslist.get(i).toString().contains("wifi") && !fileslist.get(i).toString().contains("noise")&& !fileslist.get(i).toString().contains("batterylevel")){
  						  File dfile=new File(fileslist.get(i).toString());
  						  if(dfile.exists()){
  							  boolean ans = dfile.delete();
  							  System.out.println(fileslist.get(i).toString()+", "+ans);
  						  }else{
  							  System.out.println(fileslist.get(i).toString()+", file not found");
  						  }
  					  }
  				  }

  			  	}
        	 } catch(Exception e) {
        	    e.printStackTrace();
        	 }
        }



        // If you'd like to change the access type to the full Dropbox instead of
        // an app folder, change this value.
        /*final private AccessType ACCESS_TYPE = AccessType.DROPBOX;

        private AndroidAuthSession buildSession() {
            AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
            AndroidAuthSession session;

            String[] stored = getKeys();
            if (stored != null) {
                AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
                session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
            } else {
                session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
            }
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);

            return session;
        }*/







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

        public void replaceWrongBytesInZip(File zip) throws IOException {
            byte find[] = new byte[] { 0, 0x08, 0x08, 0x08, 0 };
            int index;
            while( (index = indexOfBytesInFile(zip,find)) != -1) {
                replaceWrongZipByte(zip, index + 2);
            }
        }

        private int indexOfBytesInFile(File file,byte find[]) throws IOException {
            byte fileContent[] = new byte[(int) file.length()];
            FileInputStream fin = new FileInputStream(file);
            fin.read(fileContent);
            fin.close();
            return KMPMatch.indexOf(fileContent, find);
        }

        /**
         * Replace wrong byte http://sourceforge.net/tracker/?func=detail&aid=3477810&group_id=14481&atid=114481
         * @param zip file
         * @throws IOException
         */
        private void replaceWrongZipByte(File zip, int wrongByteIndex) throws IOException {
            RandomAccessFile  r = new RandomAccessFile(zip, "rw");
            int flag = Integer.parseInt("00001000", 2);
            r.seek(wrongByteIndex);
            int realFlags = r.read();
            if( (realFlags & flag) > 0) { // in latest versions this bug is fixed, so we're checking is bug exists.
                r.seek(wrongByteIndex);
                flag = (~flag & 0xff);
                // removing only wrong bit, other bits remains the same.
                r.write(realFlags & flag);
            }
            r.close();
        }


    };
}
