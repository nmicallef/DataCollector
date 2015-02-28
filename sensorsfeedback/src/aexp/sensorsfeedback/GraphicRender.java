package aexp.sensorsfeedback;

//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//import javax.imageio.ImageIO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

public class GraphicRender{

	public static final float WAVEFORM_DEFAULT_TIMESTEP = 0.1F;
	private int xMarker=-1;
	private int yMarker=-1;

	public GraphicRender(){
	}

	/**
	 * Render a waveform of a wave file
	 *
	 * @param wave	Wave object
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderWaveform(Wave wave, String filename) {
		renderWaveform(wave,WAVEFORM_DEFAULT_TIMESTEP,filename);
	}

	/**
	 * Render a waveform of a wave file
	 *
	 * @param wave	Wave object
	 * @param timeStep	time interval in second, as known as 1/fps
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderWaveform(Wave wave, float timeStep, String filename) {

		// for signed signals, the middle is 0 (-1 ~ 1)
		double middleLine=0;

		// usually 8bit is unsigned
		if (wave.getWaveHeader().getBitsPerSample()==8){
			// for unsigned signals, the middle is 0.5 (0~1)
			middleLine=0.5;
		}

		double[] nAmplitudes = wave.getNormalizedAmplitudes();
		int width = (int) (nAmplitudes.length / wave.getWaveHeader().getSampleRate() / timeStep);
		int height = 500;
		int middle = height / 2;
		int magnifier = 1000;

		int numSamples = nAmplitudes.length;

		if (width>0){
			int numSamplePerTimeFrame = numSamples / width;

			int[] scaledPosAmplitudes = new int[width];
			int[] scaledNegAmplitudes = new int[width];

			// width scaling
			for (int i = 0; i < width; i++) {
				double sumPosAmplitude = 0;
				double sumNegAmplitude = 0;
				int startSample=i * numSamplePerTimeFrame;
				for (int j = 0; j < numSamplePerTimeFrame; j++) {
					double a = nAmplitudes[startSample + j];
					if (a > middleLine) {
						sumPosAmplitude += (a-middleLine);
					} else {
						sumNegAmplitude += (a-middleLine);
					}
				}

				int scaledPosAmplitude = (int) (sumPosAmplitude
						/ numSamplePerTimeFrame * magnifier + middle);
				int scaledNegAmplitude = (int) (sumNegAmplitude
						/ numSamplePerTimeFrame * magnifier + middle);

				scaledPosAmplitudes[i] = scaledPosAmplitude;
				scaledNegAmplitudes[i] = scaledNegAmplitude;
			}

			// render wave form image
			//BufferedImage bufferedImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
			Bitmap bufferedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

			// set default white background
			Canvas graphics = new Canvas(bufferedImage);
			Paint paint =  new Paint();
			paint.setColor(Color.WHITE);
	        paint.setStrokeWidth(3);
			graphics.drawRect(0, 0, bufferedImage.getWidth(),bufferedImage.getHeight(),paint);

			//Graphics2D graphics = bufferedImage.createGraphics();
			//graphics.setPaint(new Color(255, 255, 255));
			//graphics.fillRect(0, 0, bufferedImage.getWidth(),bufferedImage.getHeight());
			// end set default white background

			for (int i = 0; i < width; i++) {
				for (int j = scaledNegAmplitudes[i]; j < scaledPosAmplitudes[i]; j++) {
					int y = height - j;	// j from -ve to +ve, i.e. draw from top to bottom
					if (y < 0) {
						y = 0;
					} else if (y >= height) {
						y = height - 1;
					}
					bufferedImage.setPixel(i, y, 0);
					//bufferedImage.setRGB(i, y, 0);
				}
			}
			// end render wave form image

			// export image
				int dotPos = filename.lastIndexOf(".");
				String extension=filename.substring(dotPos + 1);

				try {
				       FileOutputStream out = new FileOutputStream(filename);
				       bufferedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
				} catch (Exception e) {
				       e.printStackTrace();
				}

				//ImageIO.write(bufferedImage, extension,	new File(filename));
			// end export image
		}
		else{
			System.err.println("renderWaveform error: Empty Wave");
		}
	}

	/**
	 * Render a spectrogram of a wave file
	 *
	 * @param spectrogram	spectrogram object
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderSpectrogram(Spectrogram spectrogram,String filename){
		renderSpectrogramData(spectrogram.getNormalizedSpectrogramData(),filename);
	}

	/**
	 *
	 * Render a spectrogram data array
	 *
	 * @param spectrogramData	spectrogramData[time][frequency]=intensity, which time is the x-axis, frequency is the y-axis, intensity is the color darkness
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderSpectrogramData(double[][] spectrogramData, String filename) {

		if (spectrogramData!=null){
			int width=spectrogramData.length;
			int height=spectrogramData[0].length;

			Bitmap bufferedImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			//BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int i=0; i<width; i++){
				if (i==xMarker){
					for (int j=0; j<height; j++){
						//bufferedImage.setRGB(i, j, 0xFF00);	// green
						bufferedImage.setPixel(i, j, 0xFF00);	// green
					}
				}
				else{
					for (int j=0; j<height; j++){
						int value;
						if (j==yMarker){
							value=0xFF0000;	// red
						}
						else{
							value=255-(int)(spectrogramData[i][j]*255);
						}
						//bufferedImage.setRGB(i, height-1-j, value<<16|value<<8|value);
						bufferedImage.setPixel(i, height-1-j, value<<16|value<<8|value);
					}
				}
			}

				int dotPos = filename.lastIndexOf(".");
				String extension=filename.substring(dotPos + 1);
				try {
				       FileOutputStream out = new FileOutputStream(filename);
				       bufferedImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
				} catch (Exception e) {
				       e.printStackTrace();
				}
				//ImageIO.write(bufferedImage, extension, new File(filename));

		}
		else{
			System.err.println("renderSpectrogramData error: Empty Wave");
		}
	}

	/**
	 * Set the vertical marker
	 *
	 * @param x	x-offset pixel of the marker
	 */
	public void setVerticalMarker(int x){
		this.xMarker=x;
	}

	/**
	 * Set the horizontal marker
	 *
	 * @param y	y-offset pixel of the marker
	 */
	public void setHorizontalMarker(int y){
		this.yMarker=y;
	}

	/**
	 * Reset the markers
	 */
	public void resetMarkers(){
		xMarker=-1;
		yMarker=-1;
	}
}