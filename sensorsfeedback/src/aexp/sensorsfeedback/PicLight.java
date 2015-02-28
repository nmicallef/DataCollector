package aexp.sensorsfeedback;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PicLight extends Service {

	 //Camera variables
	AudioManager mgr ;
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    private Camera mCamera2;
    //the camera parameters
    private Parameters parameters;


	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	/*(new Thread(new Runnable() {
        	public void run() {
    	        beat.run();
    	        //stopSelf();
        	}
        })).start();*/

    	this.beat.run();
        //this.stopSelf();
    }

    public Runnable beat = new Runnable() {

        public void run() {
           // Do something

        	Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Log.d("No of cameras",Camera.getNumberOfCameras()+"");



           CameraInfo camInfo = new CameraInfo();
           Camera.getCameraInfo(0, camInfo);
           mCamera = Camera.open(0);



           mgr = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
           mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);

           SurfaceView sv = new SurfaceView(getApplicationContext());

                try {
                    mCamera.setPreviewDisplay(sv.getHolder());
                    parameters = mCamera.getParameters();
                    /*if (camInfo.facing==(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                    	parameters.set("camera_id",2);
                    }*/
                     //set camera parameters
                     mCamera.setParameters(parameters);
                     mCamera.startPreview();
                     mCamera.takePicture(null, null, mCall);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                //Get a surface
                sHolder = sv.getHolder();
                //tells Android that this surface will have its data constantly replaced
                sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                /*if (camInfo.facing==(Camera.CameraInfo.CAMERA_FACING_FRONT)) {
                    mCamera = Camera.open(camNo);
                }*/
                Log.d("Camera 1","Camera 1 - Done");

                /*
                if (Camera.getNumberOfCameras() > 1){


                    //SurfaceView sv = new SurfaceView(getApplicationContext());

                    try {

                    	camInfo = new CameraInfo();
                        Camera.getCameraInfo(CameraInfo.CAMERA_FACING_FRONT, camInfo);
                        mCamera2 = Camera.open(CameraInfo.CAMERA_FACING_FRONT);

                        mCamera2.setPreviewDisplay(sv.getHolder());
                        parameters = mCamera2.getParameters();
                         //set camera parameters
                         mCamera2.setParameters(parameters);
                         mCamera2.startPreview();
                         mCamera2.takePicture(null, null, mCall);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                    //Get a surface
                    sHolder = sv.getHolder();
                    //tells Android that this surface will have its data constantly replaced
                    sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                }*/


            /*if (mCamera == null) {
               // no front-facing camera, use the first back-facing camera instead.
               // you may instead wish to inform the user of an error here...
                 mCamera = Camera.open();
            }*/

        	/*PackageManager pm = getPackageManager();
        	boolean frontCam, rearCam;

        	//It would be safer to use the constant PackageManager.FEATURE_CAMERA_FRONT
        	//but since it is not defined for Android 2.2, I substituted the literal value
        	frontCam = pm.hasSystemFeature("android.hardware.camera.front");

        	rearCam = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);

        	if (rearCam == true){
        		mCamera = Camera.open();

        	 	mgr = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
             	mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);

             	SurfaceView sv = new SurfaceView(getApplicationContext());

            	try {
                       mCamera.setPreviewDisplay(sv.getHolder());
                       parameters = mCamera.getParameters();

                        //set camera parameters
                      mCamera.setParameters(parameters);
                      mCamera.startPreview();
                      mCamera.takePicture(null, null, mCall);

                 } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                 }
            	//Get a surface
            	sHolder = sv.getHolder();
            	//tells Android that this surface will have its data constantly replaced
            	sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        	//}*/

        	/*if (frontCam == true){
        		Camera.Parameters parameters = mCamera.getParameters();
        		parameters.set("camera-id", 2);
        		mCamera.setParameters(parameters);
        		mCamera = Camera.open();

        		mgr = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
             	mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);

             	SurfaceView sv = new SurfaceView(getApplicationContext());

            	try {
                       mCamera.setPreviewDisplay(sv.getHolder());
                       parameters = mCamera.getParameters();

                        //set camera parameters
                      mCamera.setParameters(parameters);
                      mCamera.startPreview();
                      mCamera.takePicture(null, null, mCall);

                 } catch (IOException e) {
                       // TODO Auto-generated catch block
                       e.printStackTrace();
                 }
            	//Get a surface
            	sHolder = sv.getHolder();
            	//tells Android that this surface will have its data constantly replaced
            	sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        	}*/
         }

        Camera.PictureCallback mCall = new Camera.PictureCallback()
        {

           public void onPictureTaken(byte[] data, Camera camera)
           {
                 //decode the data obtained by the camera into a Bitmap

                 FileOutputStream outStream = null;
                 try{
                	  Log.d("PictureCallback","Started processing image");

                	  Camera.Parameters parameters = camera.getParameters();

                      int width = parameters.getPreviewSize().width;
                      int height = parameters.getPreviewSize().height;

                      Bitmap myPic = BitmapFactory.decodeByteArray( data, 0, data.length );
                      double counter =0;
                      double total=0;
                      for (int x=0; x < myPic.getWidth(); x++){
                    	  for (int y=0; y < myPic.getHeight(); y++){
                    		  counter++;

                    		  int pixel = myPic.getPixel(x,y);

                    		  int r = Color.red(pixel);
                    		  int b = Color.blue(pixel);
                    		  int g = Color.green(pixel);

                    		  double lum = (0.2126 * r) + (0.7152 *g) + (0.0722 * b);
                    		  total = total + lum;
                    	  }
                      }

                      Log.d("PictureCallback","Mean luminance = "+(total/counter));

                      Random r = new Random();
                      FileOutputStream stream = new FileOutputStream("/mnt/sdcard/Image"+r.nextInt(1000)+".png");
                      myPic.compress( Bitmap.CompressFormat.PNG, 100, stream );
                      stream.flush();
                      stream.close();


                      Log.d("PictureCallback","Done writing");
                 } catch (FileNotFoundException e){
                      Log.d("CAMERA", e.getMessage());
                 } catch (IOException e){
                      Log.d("CAMERA", e.getMessage());
                 }
                 mgr = (AudioManager)getSystemService(getApplicationContext().AUDIO_SERVICE);
                 mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                 mCamera.lock();

           }
        };


    };

}
