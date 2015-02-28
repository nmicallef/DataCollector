package aexp.sensorsfeedback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

public class Noisev2  extends Service {
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
	        	 MediaRecorder mRecorder = new MediaRecorder();
	             mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	             mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	             mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	             mRecorder.setOutputFile("/dev/null");
	             try
	             {
	                 mRecorder.prepare();
	             }catch (java.io.IOException ioe) {
	                 Log.e("noise2", "IOException: " + Log.getStackTraceString(ioe));

	             }catch (java.lang.SecurityException e) {
	                 Log.e("noise2", "SecurityException: " + Log.getStackTraceString(e));
	             }
	             try
	             {
	                 mRecorder.start();
	                 NoiseAnalysisThread analysisThread = new NoiseAnalysisThread(mRecorder,getBaseContext());
	     			 analysisThread.start();
	     			 try{
		 				Thread.sleep(15000);
		 			 }catch(Exception e){}
		             if (analysisThread != null) {
		 				analysisThread.stopDetection();
		 				analysisThread = null;
		 			 }
		             if (mRecorder != null) {
		                 mRecorder.stop();
		                 mRecorder.release();
		                 mRecorder = null;
		             }
	             }catch (java.lang.SecurityException e) {
	                 Log.e("noise2", "SecurityException: " + Log.getStackTraceString(e));
	             }
	        }
	   };

}
