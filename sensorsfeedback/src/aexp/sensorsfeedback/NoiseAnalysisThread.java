package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import com.musicg.fingerprint.FingerprintManager;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

import android.content.Context;
import android.media.MediaRecorder;
import android.telephony.TelephonyManager;

public class NoiseAnalysisThread extends Thread{
	private MediaRecorder mRecorder;
	private volatile Thread _thread;
	private Context currcxt;
	private static double mEMA = 0.0;
	static final private double EMA_FILTER = 0.6;

	public NoiseAnalysisThread(MediaRecorder recorder, Context context){
		this.mRecorder = recorder;
		this.currcxt = context;
	}

	public void start() {
		_thread = new Thread(this);
        _thread.start();
    }

	public void stopDetection(){
		_thread = null;
	}

	public void run() {
		try {
			byte[] buffer;
			final TelephonyManager tm = (TelephonyManager) currcxt.getSystemService(Context.TELEPHONY_SERVICE);

       	 	File captureFileName = new File( currcxt.getFilesDir(), tm.getDeviceId()+"_noise_capture.csv" );
			PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
			Thread thisThread = Thread.currentThread();
			while (_thread == thisThread) {
				Date d = new Date();
        	 	Calendar c = Calendar.getInstance();
        	 	c.setTime(d);

        	 	String temp = c.getTimeInMillis()+","+d.toString()+","+ soundDb(Math.exp(-7))+","+Double.toString((getAmplitudeEMA()));
        	 	captureFile.println(temp);

        	 	captureFileName.setReadable(true, false);
        	 	captureFileName.setWritable(true, false);
        	 	captureFileName.setExecutable(true, false);
			}

       	 	captureFile.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double soundDb(double ampl){
	   return  20 * Math.log10(getAmplitudeEMA() / ampl);
	}
	    public double getAmplitude() {
	        if (mRecorder != null)
	            return  (mRecorder.getMaxAmplitude());
	        else
	            return 0;

	    }
	    public double getAmplitudeEMA() {
	        double amp =  getAmplitude();
	        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
	        return mEMA;
	    }

}
