package de.idvos.fastonlineidentification;

import java.io.IOException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.opentok.android.BaseVideoCapturer;
//import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.BaseVideoCapturer;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.SubscriberKit;
import com.opentok.android.PublisherKit.PublisherListener;
import com.opentok.android.Session.SessionListener;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit.VideoListener;

import de.idvos.fastonlineidentification.sdk.CustomVideoCapturer;
import de.idvos.fastonlineidentification.sdk.CustomVideoRenderer;

public class TokBoxManager implements SessionListener {

	private static final String TAG = "TokBoxManager";
	
	private static final String API_KEY = "44880212";
	private static final String FLASH_MODE_TORCH = "torch";
	private static final String FLASH_MODE_OFF = "off";
	
	public static interface TokBoxCallback {
		
		public void onTokBoxConnected();
		public void onTokBoxError(Exception e);
		public void publishingStarted();
		public ViewGroup getSubscribtionFrame();
		
	}
	
	private static TokBoxManager mInstance = null;
	
	public static TokBoxManager getInstance(Context context, TokBoxCallback callback) {
		if (mInstance==null) {
			mInstance = new TokBoxManager(context, callback);
		}else {
            if(callback != null)
                mInstance.setmCallback(callback);
        }


		
		if (mInstance.mCallback!=callback) {
			throw new RuntimeException("Callback instance different.");
		}
		
		return mInstance;
	}
	
	private Session mSession = null;
	private Publisher mPublisher = null;
	private Subscriber mSubscriber = null;
	
	private Context mContext;
	private TokBoxCallback mCallback;
	
	private Class<?> mCameraClass;
	private Class<?> mParametersClass;
	private Object mCamera = null;
	private String mOriginalFlashMode = null;

	public TokBoxManager(Context context, TokBoxCallback callback) {
		mContext = context;
		mCallback = callback;
	}

    public void setmCallback(TokBoxCallback mCallback) {
        this.mCallback = mCallback;
    }

    public void connect(Context context, String sessionId, String token) {

		
		mSession = new Session(context, API_KEY, sessionId);
		mSession.setSessionListener(this);
		mSession.connect(token);
	}

    public void finishSession(){
        if (mSession!=null) {
            mSession.disconnect();
            mSession = null;
        }
    }
	public void swapCamera() {
        CustomVideoCapturer customVideoCapturer = (CustomVideoCapturer)mPublisher.getCapturer();

        try {
            if(customVideoCapturer.isFrontCamera()){
                customVideoCapturer.swapCamera(customVideoCapturer.getMainCameraIndex());
            }else {
                customVideoCapturer.swapCamera(customVideoCapturer.getFrontCameraIndex());
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Toast.makeText(mContext,R.string.connection_failed_try_again,Toast.LENGTH_LONG).show();
        }

//        if(mPublisher != null)
//		    mPublisher.swapCamera();
	}

    public boolean isFrontCam() {
        CustomVideoCapturer customVideoCapturer = (CustomVideoCapturer)mPublisher.getCapturer();
        return customVideoCapturer.isFrontCamera();
	}
	
	public boolean hasFlashLight() {
		if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			return false;
		}
		
		if (mCamera==null) {
			return false;
		}
		
		return true;
	}
	
	private void releaseCamera() {
		if (mPublisher==null) {
			return;
		}
		
		CustomVideoCapturer capturer = (CustomVideoCapturer)mPublisher.getCapturer();
//		BaseVideoCapturer capturer = mPublisher.getCapturer();

		if (capturer==null) {
			return;
		}
		
		capturer.stopCapture();
		capturer.destroy();
	}
	
	private void attachCamera() {
		if (mPublisher==null) {
			return;
		}

//        CustomVideoCapturer capturer = mPublisher.getCapturer();
        CustomVideoCapturer capturer = (CustomVideoCapturer)mPublisher.getCapturer();

		if (capturer==null) {
			return;
		}
		
		if (!capturer.isCaptureStarted()) {
			capturer.init();
			capturer.startCapture();
		}
	}
	
	public boolean isFlashLightOn() {
		if (mPublisher==null) {
			return false;
		}

        if(true)
            return ((CustomVideoCapturer)mPublisher.getCapturer()).isFlashOn();
		
		releaseCamera();
		
		boolean result = false;
		int cameraId = mPublisher.getCameraId();
		
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			mCameraClass = cl.loadClass("android.hardware.Camera");
			mParametersClass = cl.loadClass("android.hardware.Camera$Parameters");
			Method method = mCameraClass.getMethod("open", Integer.TYPE);
			mCamera = method.invoke(mCamera, cameraId);
			
			SurfaceView surface = new SurfaceView(mContext);
			SurfaceHolder holder = surface.getHolder();
			method = mCameraClass.getMethod("setPreviewDisplay", SurfaceHolder.class);
			method.invoke(mCamera, holder);
			
			Method m = mCameraClass.getMethod("getParameters");
			Object params = m.invoke(mCamera);
			m = mParametersClass.getMethod("getFlashMode");
			String flashMode = (String) m.invoke(params);
			
			m = mCameraClass.getMethod("release");
			m.invoke(mCamera);
			
			if (FLASH_MODE_TORCH.equals(flashMode)) {
				result = true;
			}
			else {
				mOriginalFlashMode = flashMode;
				result = false;
			}
		} catch (Throwable t) {
			Log.e(TAG, "aaa", t);
			result = false;
		}
		
		attachCamera();
		
		return result;
	}


    public void toggleFlashLight(){
        CustomVideoCapturer capturer = (CustomVideoCapturer)mPublisher.getCapturer();
        capturer.toggleFlashLight();
    }

    public void toggleFlashLight(boolean isOn){
        CustomVideoCapturer capturer = (CustomVideoCapturer)mPublisher.getCapturer();
        capturer.toggleFlashLight(isOn);
    }
	public void setFlashMode(boolean on) {
		if (mCamera==null) {
			return;
		}
		
		releaseCamera();
		
		int cameraId = mPublisher.getCameraId();


        ((CustomVideoCapturer)mPublisher.getCapturer()).setFlashOn(true);
		
//		Camera camera = Camera.open(cameraId);
//		try {
//			camera.setPreviewDisplay(new SurfaceView(mContext).getHolder());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Parameters params = camera.getParameters();
//		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
//		camera.setParameters(params);
		
		
//		try {
//			ClassLoader cl = ClassLoader.getSystemClassLoader();
//			mCameraClass = cl.loadClass("android.hardware.Camera");
//			mParametersClass = cl.loadClass("android.hardware.Camera$Parameters");
//			Method method = mCameraClass.getMethod("open", Integer.TYPE);
//			mCamera = method.invoke(mCamera, cameraId);
//			
//			SurfaceView surface = new SurfaceView(mContext);
//			SurfaceHolder holder = surface.getHolder();
//			method = mCameraClass.getMethod("setPreviewDisplay", SurfaceHolder.class);
//			method.invoke(mCamera, holder);
////			Camera c;
////			c.setPreviewDisplay(holder);
//			
//			Method m = mCameraClass.getMethod("getParameters");
//			Object params = m.invoke(mCamera);
//			m = mParametersClass.getMethod("setFlashMode", String.class);
//			m.invoke(params, on?FLASH_MODE_TORCH:mOriginalFlashMode==null?FLASH_MODE_OFF:mOriginalFlashMode);
//			
//			m = mCameraClass.getMethod("setParameters", mParametersClass);
//			m.invoke(mCamera, params);
//			
//			Log.i(TAG, "flash mode is set");
//			
//			m = mCameraClass.getMethod("release");
//			m.invoke(mCamera);
//		} catch (Throwable t) { 
//			Log.e(TAG, "aaa", t);
//		}
		
		attachCamera();
	}
	
	@Override
	public void onConnected(Session session) {
		mCallback.onTokBoxConnected();
	}

	@Override
	public void onDisconnected(Session session) {
		Log.i(TAG, "TokBox disconnected");
	}

	public void startTransmitting(ViewGroup frame) {
		mPublisher = new Publisher(mContext, "You");
        mPublisher.setCapturer(new CustomVideoCapturer(mContext));
//        mPublisher.setRenderer(new CustomVideoRenderer(mContext));
		mPublisher.setPublisherListener(mPublisherListener);
		mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		frame.removeAllViews();
		frame.addView(mPublisher.getView(), params);
		
		mSession.publish(mPublisher);
	}
	
	private PublisherListener mPublisherListener = new PublisherListener() {
		
		@Override
		public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
			Log.w(TAG, "Publish stream destroyed");
		}
		
		@Override
		public void onStreamCreated(PublisherKit publisher, Stream stream) {
			Log.i(TAG, "Stream created");
			mCallback.publishingStarted();
		}
		
		@Override
		public void onError(PublisherKit publisher, OpentokError error) {
			mCallback.onTokBoxError(new Exception(error.getMessage()));
		}
	};
	
	@Override
	public void onError(Session session, OpentokError error) {
		mCallback.onTokBoxError(new Exception(error.getMessage()));
	}

	@Override
	public void onStreamDropped(Session session, Stream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStreamReceived(Session session, Stream stream) {
		mSubscriber = new Subscriber(mContext, stream);
		mSubscriber.setVideoListener(mVideoListener);
		mSession.subscribe(mSubscriber);
	}
	
	private VideoListener mVideoListener = new VideoListener() {
		
		@Override
		public void onVideoEnabled(SubscriberKit subscriber, String reason) { }
		
		@Override
		public void onVideoDisabled(SubscriberKit subscriber, String reason) { }
		
		@Override
		public void onVideoDisableWarningLifted(SubscriberKit subscriber) { }
		
		@Override
		public void onVideoDisableWarning(SubscriberKit subscriber) { }
		
		@Override
		public void onVideoDataReceived(SubscriberKit subscriber) {


			mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
			ViewGroup frame = mCallback.getSubscribtionFrame();
			frame.removeAllViews();
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			frame.removeAllViews();
            View subscriberView = mSubscriber.getView();
            frame.removeView(subscriberView);
            frame.addView(subscriberView, params);

		}
		
	};
	
}
