package aexp.sensorsfeedback;


import java.io.File;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.telephony.TelephonyManager;

import com.musicg.fingerprint.FingerprintManager;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

public class SoundAnalysisThread extends Thread{
	private RecorderThread recorder;
	private WaveHeader waveHeader;
	private volatile Thread _thread;
	private Context currcxt;

	public SoundAnalysisThread(RecorderThread recorder, Context context){
		this.recorder = recorder;
		this.currcxt = context;
		AudioRecord audioRecord = recorder.getAudioRecord();

		int bitsPerSample = 0;
		if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT){
			bitsPerSample = 16;
		}
		else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
			bitsPerSample = 8;
		}

		int channel = 0;
		// whistle detection only supports mono channel
		if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_CONFIGURATION_MONO){
			channel = 1;
		}

		waveHeader = new WaveHeader();
		waveHeader.setChannels(AudioFormat.CHANNEL_CONFIGURATION_MONO);
		//waveHeader.setChannels(channel);
		waveHeader.setBitsPerSample(bitsPerSample);
		waveHeader.setSampleRate(audioRecord.getSampleRate());
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

			Thread thisThread = Thread.currentThread();
			while (_thread == thisThread) {
				// detect sound
				buffer = recorder.getFrameBytes();

				// audio analyst
				if (buffer != null) {
					// sound detected

					Date d = new Date();
	        	 	Calendar c = Calendar.getInstance();
	        	 	c.setTime(d);
	        	 	String filecapturename =  currcxt.getFilesDir()+"/"+tm.getDeviceId()+"_"+c.getTimeInMillis()+"_";

					System.out.println("*Analyzing:");
					Wave currentWave = new Wave(waveHeader,buffer);

					try{
						byte[] fingerprint=currentWave.getFingerprint();

						// dump the fingerprint
						FingerprintManager fingerprintManager=new FingerprintManager();
						fingerprintManager.saveFingerprintAsFile(fingerprint, filecapturename+"fingerprint.fingerprint");

						System.out.println("*file:"+filecapturename+"fingerprint.fingerprint");
						File captureFileName = new File( filecapturename+"fingerprint.fingerprint" );
						captureFileName.setReadable(true, false);
	           	 		captureFileName.setWritable(true, false);
	           	 		captureFileName.setExecutable(true, false);

						/*Spectrogram spectrogram = new Spectrogram(currentWave);

						// Graphic render
						GraphicRender render = new GraphicRender();
						render.renderWaveform(currentWave,filecapturename+"waveform.jpg");
						System.out.println("*file:"+filecapturename+"waveform.jpg");
						// render.setHorizontalMarker(1);
						// render.setVerticalMarker(1);
						render.renderSpectrogram(spectrogram, filecapturename+"spectogram.jpg");
						System.out.println("*file:"+filecapturename+"spectogram.jpg");*/
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
				else{
					// no sound detected
					System.out.println("no sound detected");
				}
				// end audio analyst
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
