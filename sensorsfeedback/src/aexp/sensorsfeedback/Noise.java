package aexp.sensorsfeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class Noise extends Service {
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private static final int MY_MSG = 1;
    private int channelConfiguration;;

    private int frequency;

    short[] tempBuffer;

		@Override
	    public IBinder onBind(Intent arg0) {
	        // TODO Auto-generated method stub
	        return null;
	    }

	    @Override
	    public void onStart(Intent i, int startId) {
	    	(new Thread(new Runnable() {
	        	public void run() {
	        		setFrequency(8000);
	    	        setChannelConfiguration(AudioFormat.CHANNEL_CONFIGURATION_MONO);
	    	        beat.run();
	    	        stopSelf();
	        	}
	        })).start();
	    }

	    /**
	     * @return the audioEncoding
	     */
	    public int getAudioEncoding()
	    {
	        return audioEncoding;
	    }

	    /**
	     * @return the channelConfiguration
	     */
	    public int getChannelConfiguration()
	    {
	        return channelConfiguration;
	    }

	    /**
	     * @return the frequency
	     */
	    public int getFrequency()
	    {
	        return frequency;
	    }

	    /**
	     * Calculate SPL P = square root ( 2*Z*I ) - > Pressure Z = Acoustic
	     * Impedance = 406.2 for air at 30 degree celsius I = Intensity = 2*Z*pi
	     * square*frequency square*Amplitude square
	     *
	     * @param bsize
	     *            - the size of FFT required.
	     */
	    public double measure(int bsize)
	    {
	        int i = 0;
	        double frequency = 0;
	        double amplitude = 0;
	        double max = 0.0;
	        int max_index = 0;
	        double w = 0.0;

	        double Z = 406.2;
	        double I = 0.0;
	        double P = 0.0;
	        double P0 = 2 * 0.00001; // is constant
	        double Istar = 0.0; // SPL

	        Complex[] x = new Complex[bsize];

	        for (i = 0; i < bsize; i++)
	        {
	            x[i] = new Complex(tempBuffer[i], 0);
	        }

	        Complex[] xf = new Complex[bsize];
	        xf = FFT.fft(x);

	        for (i = 1; i < bsize / 2; i++)
	        {
	            w = xf[i].abs();
	            if (w > max)
	            {
	                max_index = i;
	                max = w;
	            }
	        }
	        // Frequency and Amp of fundamental frequency
	        frequency = max_index * bsize;
	        amplitude = max * 2 / bsize;

	        I = Z * 2 * 2 * Math.PI * Math.PI * frequency * frequency * amplitude
	                * amplitude;
	        P = Math.sqrt(Z * I);
	        if (P != 0)
	            Istar = round(20 * Math.log10(P / P0) / 10, 3); // divide by 10 to
	                                                            // correct the
	                                                            // calculation

	        /*Message msg = handle.obtainMessage(MY_MSG, "\n\nFrequency = " + frequency + " Hz\n"
	                + Istar + " db SPL");
	        handle.sendMessage(msg);*/
	        return Istar;

	    }

	    /**
	     * Utility Function to round a double value
	     *
	     * @param d
	     *            - The decimal value
	     * @param decimalPlace
	     *            - how many places required
	     * @return double - the rounded value
	     */
	    public double round(double d, int decimalPlace)
	    {
	        // see the Javadoc about why we use a String in the constructor
	        // http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
	        BigDecimal bd = new BigDecimal(Double.toString(d));
	        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	        return bd.doubleValue();
	    }



	    public Runnable beat = new Runnable() {

	        public void run() {
	            // Do something
	        	AudioRecord recordInstance = null;

	            // We're important...
	            android.os.Process
	                    .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

	            short bufferSize = 2048;// 4096;

	            recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, getFrequency(), getChannelConfiguration(), getAudioEncoding(), bufferSize);


	            tempBuffer = new short[bufferSize];
	            recordInstance.startRecording();
	            // Continue till STOP button is pressed.
	            int counter = 0;

	            try{

	            	PrintWriter captureFile;
                	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
           	 		File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_noise_capture.csv" );

           	 		captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );

           	 		while (counter <= 15)
           	 		{
	            		double splValue = 0.0;

	                	for (int i = 0; i < tempBuffer.length; i++)
	                	{
	                    	tempBuffer[i] = 0;
	                	}

	                	recordInstance.read(tempBuffer, 0, bufferSize);
	                	splValue = measure(bufferSize); // calculate SPL

	                	Date d = new Date();
	               	 	Calendar c = Calendar.getInstance();
	               	 	c.setTime(d);
	 	                captureFile.println(c.getTimeInMillis()+","+d.toString()+","+splValue);
	 	                //captureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD,d.toString()+","+splValue));

	                	Thread.sleep(1000);
	                	counter++;
	            	}

           	 		captureFile.close();

           	 		captureFileName.setReadable(true, false);
           	 		captureFileName.setWritable(true, false);
           	 		captureFileName.setExecutable(true, false);
            	}catch(Exception w){}
	            // Close resources...
	            recordInstance.stop();
	        }
	    };

	    /**
	     * @param channelConfiguration
	     *            the channelConfiguration to set
	     */
	    public void setChannelConfiguration(int channelConfiguration)
	    {
	        this.channelConfiguration = channelConfiguration;
	    }

	    /**
	     * @param frequency
	     *            the frequency to set
	     */
	    public void setFrequency(int frequency)
	    {
	        this.frequency = frequency;
	    }


}
